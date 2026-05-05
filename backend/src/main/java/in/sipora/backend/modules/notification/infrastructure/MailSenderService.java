package in.sipora.backend.modules.notification.infrastructure;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Low-level email sender wrapping JavaMailSender.
 *
 * Sends HTML emails with a plain-text fallback.
 * All exceptions are caught and logged — a failed email must never
 * propagate and cause the calling business transaction to roll back.
 *
 * The @Async("emailExecutor") annotation is applied in EmailService
 * (not here) so this component remains unit-testable without async.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailSenderService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${sipora.mail.from-name:Sipora Hydration}")
    private String fromName;

    /**
     * Sends an HTML email.
     *
     * @param to          recipient address
     * @param subject     email subject line
     * @param htmlBody    fully rendered HTML (from Thymeleaf)
     * @param textBody    plain-text fallback (for email clients that block HTML)
     */
    public void sendHtml(String to, String subject, String htmlBody, String textBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(textBody, htmlBody);   // (text, html) — multipart/alternative

            mailSender.send(message);
            log.info("Email sent: to={} subject={}", to, subject);

        } catch (MessagingException | MailException | java.io.UnsupportedEncodingException e) {
            // Log and continue — never let an email failure bubble up
            log.error("Failed to send email to={} subject={}: {}", to, subject, e.getMessage());
        }
    }
}