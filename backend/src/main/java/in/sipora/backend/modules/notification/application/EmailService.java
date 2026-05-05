package in.sipora.backend.modules.notification.application;

import in.sipora.backend.modules.identity.api.IdentityModuleApi;
import in.sipora.backend.modules.identity.api.UserSummary;
import in.sipora.backend.modules.identity.domain.UserRegisteredEvent;
import in.sipora.backend.modules.notification.infrastructure.MailSenderService;
import in.sipora.backend.modules.ordering.api.OrderingModuleApi;
import in.sipora.backend.modules.ordering.api.OrderSummary;
import in.sipora.backend.modules.ordering.domain.OrderEvents.OrderCancelledEvent;
import in.sipora.backend.modules.ordering.domain.OrderEvents.OrderConfirmedEvent;
import in.sipora.backend.modules.ordering.domain.OrderEvents.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Notification module's sole service — listens to domain events and sends emails.
 *
 * DESIGN:
 *
 * @TransactionalEventListener(phase = AFTER_COMMIT)
 *   Fires ONLY after the publishing transaction successfully commits to DB.
 *   This is critical: if an order transaction rolls back, no email is sent.
 *   If the annotation were plain @EventListener, emails would fire even on rollback.
 *
 * @Async("emailExecutor")
 *   Runs on the dedicated email thread pool (see AsyncConfig).
 *   The Tomcat request thread is freed immediately after the business method.
 *   A slow SMTP server cannot delay the HTTP response to the customer.
 *
 * NO DIRECT MODULE IMPORTS except:
 *   - identity.api.IdentityModuleApi    (public contract — allowed)
 *   - identity.domain.UserRegisteredEvent  (event record — allowed)
 *   - ordering.api.OrderingModuleApi    (public contract — allowed)
 *   - ordering.domain.OrderEvents       (event records — allowed)
 *
 * This module NEVER imports from catalog, cart, payment, or review.
 *
 * FAULT TOLERANCE:
 *   MailSenderService catches all SMTP exceptions internally.
 *   If a user lookup fails (user deleted), we log and skip — no crash.
 *
 * SUBJECTS:
 *   All subject lines are defined here as constants for easy A/B testing later.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    // ── Email subject constants ==>
    private static final String SUBJECT_WELCOME = "Welcome to Sipora — Stay Hydrated! 💧";
    private static final String SUBJECT_ORDER_PLACED = "We've received your order — %s";
    private static final String SUBJECT_ORDER_CONFIRMED = "Payment confirmed! Your order %s is on its way";
    private static final String SUBJECT_ORDER_CANCELLED = "Your order %s has been cancelled";

    // ── Template names (match src/main/resources/templates/email/*.html) ──
    private static final String TEMPLATE_WELCOME = "email/welcome";
    private static final String TEMPLATE_ORDER_PLACED = "email/order-placed";
    private static final String TEMPLATE_ORDER_CONFIRMED = "email/order-confirmed";
    private static final String TEMPLATE_ORDER_CANCELLED = "email/order-cancelled";

    private final MailSenderService mailSenderService;
    private final TemplateEngine templateEngine;
    private final EmailContextBuilder contextBuilder;
    private final IdentityModuleApi identityModuleApi;
    private final OrderingModuleApi orderingModuleApi;

    // Welcome email — on user registration ==>

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Sending welcome email to userId={}", event.userId());

        try {
            Context ctx = contextBuilder.welcomeContext(event.fullName(), event.email());
            String html = templateEngine.process(TEMPLATE_WELCOME, ctx);
            String text = "Welcome to Sipora, %s! Visit us at https://sipora.in"
                    .formatted(event.fullName());

            mailSenderService.sendHtml(event.email(), SUBJECT_WELCOME, html, text);
        } catch (Exception e) {
            log.error("Failed to send welcome email to userId={}: {}", event.userId(), e.getMessage());
        }
    }

    // Order placed — acknowledgement (payment not yet confirmed) ==>

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Sending order-placed email for orderId={}", event.orderId());

        try {
            UserInfo user = resolveUser(event.userId(), event.userEmail(), event.userName());
            OrderSummary order = resolveOrder(event.orderId());
            if (order == null) return;

            Context ctx = contextBuilder.orderPlacedContext(user.name(), user.email(), order);
            String html = templateEngine.process(TEMPLATE_ORDER_PLACED, ctx);
            String text = "Hi %s, we've received your order %s. Total: %s %s. Complete payment to confirm your order."
                    .formatted(user.name(), event.orderNumber(),
                            event.currencyCode(), event.totalAmount());

            mailSenderService.sendHtml(
                    user.email(),
                    SUBJECT_ORDER_PLACED.formatted(event.orderNumber()),
                    html, text);

        } catch (Exception e) {
            log.error("Failed to send order-placed email for orderId={}: {}",
                    event.orderId(), e.getMessage());
        }
    }

    // Order confirmed — payment captured ==>

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Sending order-confirmed email for orderId={}", event.orderId());

        try {
            UserInfo user = resolveUser(event.userId(), event.userEmail(), event.userName());
            OrderSummary order = resolveOrder(event.orderId());
            if (order == null) return;

            Context ctx = contextBuilder.orderConfirmedContext(user.name(), user.email(), order);
            String html = templateEngine.process(TEMPLATE_ORDER_CONFIRMED, ctx);
            String text = "Great news, %s! Payment received for order %s. Amount: %s %s. We'll ship it soon!"
                    .formatted(user.name(), event.orderNumber(),
                            event.currencyCode(), event.totalAmount());

            mailSenderService.sendHtml(
                    user.email(),
                    SUBJECT_ORDER_CONFIRMED.formatted(event.orderNumber()),
                    html, text);

        } catch (Exception e) {
            log.error("Failed to send order-confirmed email for orderId={}: {}",
                    event.orderId(), e.getMessage());
        }
    }

    // Order cancelled ==>

    @Async("emailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderCancelled(OrderCancelledEvent event) {
        log.info("Sending order-cancelled email for orderId={}", event.orderId());

        try {
            UserInfo user = resolveUser(event.userId(), event.userEmail(), event.userName());
            OrderSummary order = resolveOrder(event.orderId());
            if (order == null) return;

            Context ctx = contextBuilder.orderCancelledContext(
                    user.name(), user.email(), order, event.reason());
            String html = templateEngine.process(TEMPLATE_ORDER_CANCELLED, ctx);
            String text = "Hi %s, your order %s has been cancelled. Reason: %s. If you were charged, a refund will be processed within 5-7 days."
                    .formatted(user.name(), event.orderNumber(), event.reason());

            mailSenderService.sendHtml(
                    user.email(),
                    SUBJECT_ORDER_CANCELLED.formatted(event.orderNumber()),
                    html, text);

        } catch (Exception e) {
            log.error("Failed to send order-cancelled email for orderId={}: {}",
                    event.orderId(), e.getMessage());
        }
    }

    // ── Internal helpers ==>

    /**
     * Resolves user name + email from the event payload or IdentityModuleApi.
     *
     * Events published from OrderController carry real values;
     * events from OrderingModuleApiImpl (via payment webhook path) may
     * carry "email-placeholder". In both cases we verify via IdentityModuleApi.
     */
    private UserInfo resolveUser(java.util.UUID userId, String eventEmail, String eventName) {
        // If the event has real values, use them directly (avoids a DB call)
        if (isReal(eventEmail) && isReal(eventName)) {
            return new UserInfo(eventName, eventEmail);
        }

        // Fall back to IdentityModuleApi lookup
        return identityModuleApi.getUserById(userId)
                .map(u -> new UserInfo(u.fullName(), u.email()))
                .orElseGet(() -> {
                    log.warn("User not found for userId={} — skipping email", userId);
                    return null;
                });
    }

    private OrderSummary resolveOrder(java.util.UUID orderId) {
        return orderingModuleApi.getOrderById(orderId)
                .orElseGet(() -> {
                    log.warn("Order not found for orderId={} — skipping email", orderId);
                    return null;
                });
    }

    private boolean isReal(String value) {
        return value != null && !value.isBlank() && !value.contains("placeholder");
    }

    /** Lightweight value type for resolved user display info. */
    private record UserInfo(String name, String email) {
        boolean isValid() { return name != null && email != null; }
    }
}