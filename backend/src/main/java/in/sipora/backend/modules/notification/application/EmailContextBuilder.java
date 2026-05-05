package in.sipora.backend.modules.notification.application;

import in.sipora.backend.modules.ordering.api.OrderSummary;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Builds Thymeleaf Context objects for each email template.
 *
 * Each method returns a populated Context ready to be passed to
 * TemplateEngine.process("email/template-name", context).
 *
 * Common variables are available in all templates via addCommonVars():
 *  - brandName    "Sipora"
 *  - brandUrl     "https://sipora.in"
 *  - supportEmail "support@sipora.in"
 *  - year         current year
 *  - generatedAt  formatted IST timestamp
 */
@Component
public class EmailContextBuilder {

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a z", Locale.ENGLISH);

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    // ── Welcome ==>

    public Context welcomeContext(String fullName, String email) {
        Context ctx = new Context(Locale.ENGLISH);
        addCommonVars(ctx);
        ctx.setVariable("fullName", fullName);
        ctx.setVariable("email", email);
        ctx.setVariable("shopUrl", "https://sipora.in/products");
        return ctx;
    }

    // ── Order placed (acknowledgement) ==>

    public Context orderPlacedContext(String fullName, String email, OrderSummary order) {
        Context ctx = new Context(Locale.ENGLISH);
        addCommonVars(ctx);
        ctx.setVariable("fullName", fullName);
        ctx.setVariable("email", email);
        ctx.setVariable("orderNumber", order.orderNumber());
        ctx.setVariable("totalAmount", formatAmount(order.totalAmount()));
        ctx.setVariable("currencyCode", order.currencyCode());
        ctx.setVariable("orderUrl", orderUrl(order.orderId().toString()));
        ctx.setVariable("placedAt", formatNowIst());
        return ctx;
    }

    // ── Order confirmed (payment success) ==>

    public Context orderConfirmedContext(String fullName, String email, OrderSummary order) {
        Context ctx = new Context(Locale.ENGLISH);
        addCommonVars(ctx);
        ctx.setVariable("fullName", fullName);
        ctx.setVariable("email", email);
        ctx.setVariable("orderNumber", order.orderNumber());
        ctx.setVariable("totalAmount", formatAmount(order.totalAmount()));
        ctx.setVariable("currencyCode", order.currencyCode());
        ctx.setVariable("orderUrl", orderUrl(order.orderId().toString()));
        ctx.setVariable("confirmedAt", formatNowIst());
        return ctx;
    }

    // ── Order cancelled ==>

    public Context orderCancelledContext(String fullName, String email,
                                         OrderSummary order, String reason) {
        Context ctx = new Context(Locale.ENGLISH);
        addCommonVars(ctx);
        ctx.setVariable("fullName", fullName);
        ctx.setVariable("email", email);
        ctx.setVariable("orderNumber", order.orderNumber());
        ctx.setVariable("totalAmount", formatAmount(order.totalAmount()));
        ctx.setVariable("currencyCode", order.currencyCode());
        ctx.setVariable("reason", reason != null ? reason : "Order cancelled");
        ctx.setVariable("shopUrl", "https://sipora.in/products");
        ctx.setVariable("cancelledAt", formatNowIst());
        return ctx;
    }

    // ── Helpers ==>

    private void addCommonVars(Context ctx) {
        ctx.setVariable("brandName", "Sipora");
        ctx.setVariable("brandUrl",  "https://sipora.in");
        ctx.setVariable("supportEmail", "support@sipora.in");
        ctx.setVariable("year", ZonedDateTime.now(IST).getYear());
        ctx.setVariable("generatedAt", formatNowIst());
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%.2f", amount);
    }

    private String formatNowIst() {
        return ZonedDateTime.now(IST).format(DISPLAY_FORMATTER);
    }

    private String orderUrl(String orderId) {
        return "https://sipora.in/orders/" + orderId;
    }
}