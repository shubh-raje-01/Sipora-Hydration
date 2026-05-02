package in.sipora.backend.modules.payment.infrastructure;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import in.sipora.backend.shared.exception.DomainException;

import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Razorpay adapter — implements the PaymentGateway port.
 *
 * Responsibilities:
 *  - Creates Razorpay orders (amount in paise, INR only)
 *  - Verifies payment signature from frontend checkout callback
 *  - Verifies webhook HMAC-SHA256 signature from Razorpay servers
 *
 * HMAC verification is implemented manually using Java's javax.crypto
 * rather than the Razorpay SDK's Utils class — this avoids version-specific
 * SDK dependencies and is more transparent for security audits.
 *
 * Signature algorithm (per Razorpay docs):
 *  - Payment signature: HMAC-SHA256(orderId + "|" + paymentId, keySecret)
 *  - Webhook signature: HMAC-SHA256(rawPayload, webhookSecret)
 */
@Slf4j
@Component
public class RazorpayGateway implements PaymentGateway {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final RazorpayClient razorpayClient;

    @Value("${sipora.payment.razorpay.key-id}")
    private String keyId;

    @Value("${sipora.payment.razorpay.key-secret}")
    private String keySecret;

    @Value("${sipora.payment.razorpay.webhook-secret}")
    private String webhookSecret;

    public RazorpayGateway(
            @Value("${sipora.payment.razorpay.key-id}")     String keyId,
            @Value("${sipora.payment.razorpay.key-secret}") String keySecret) {
        try {
            this.razorpayClient = new RazorpayClient(keyId, keySecret);
            this.keyId     = keyId;
            this.keySecret = keySecret;
        } catch (RazorpayException e) {
            throw new IllegalStateException("Failed to initialise Razorpay client", e);
        }
    }

    // ── Create order ==>

    @Override
    public GatewayOrderResult createOrder(BigDecimal amount, String receiptId) {
        // Razorpay expects amount in smallest currency unit (paise for INR)
        long amountInPaise = amount
                .setScale(2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",          amountInPaise);
            orderRequest.put("currency",        "INR");
            orderRequest.put("receipt",         receiptId);
            orderRequest.put("payment_capture", 1);   // auto-capture on payment

            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            log.info("Razorpay order created: {} for receipt={} amount={}",
                    razorpayOrderId, receiptId, amount);

            return new GatewayOrderResult(razorpayOrderId, keyId, amount, amountInPaise, "INR");

        } catch (RazorpayException e) {
            log.error("Razorpay createOrder failed for receipt={}: {}", receiptId, e.getMessage());
            throw new DomainException(
                    "Payment gateway error. Please try again.", HttpStatus.BAD_GATEWAY, e);
        }
    }

    // ── Verify payment signature (frontend callback) ==>

    /**
     * Verifies the signature from the Razorpay checkout callback.
     *
     * Per Razorpay docs:
     *   generated_signature = HMAC-SHA256(order_id + "|" + payment_id, key_secret)
     *   Assert generated_signature == razorpay_signature
     */
    @Override
    public boolean verifyPaymentSignature(String gatewayOrderId, String gatewayPaymentId,
                                          String signature) {
        String data = gatewayOrderId + "|" + gatewayPaymentId;
        String computed = computeHmac(data, keySecret);
        boolean valid = MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            log.warn("Payment signature mismatch: orderId={} paymentId={}",
                    gatewayOrderId, gatewayPaymentId);
        }
        return valid;
    }

    // ── Verify webhook signature (Razorpay server → us) ==>

    /**
     * Verifies the HMAC-SHA256 signature of an incoming webhook.
     *
     * Per Razorpay docs:
     *   generated_signature = HMAC-SHA256(raw_body, webhook_secret)
     *   Assert generated_signature == X-Razorpay-Signature header value
     *
     * IMPORTANT: Must be called on the raw, unparsed request body.
     * Do NOT parse the JSON before calling this — parsing may alter whitespace/ordering.
     */
    @Override
    public boolean verifyWebhookSignature(String rawPayload, String signature) {
        String computed = computeHmac(rawPayload, webhookSecret);
        boolean valid = MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8));

        if (!valid) {
            log.warn("Webhook signature verification FAILED — possible spoofed request");
        }
        return valid;
    }

    // ── HMAC helper ==>

    private String computeHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 not available", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}