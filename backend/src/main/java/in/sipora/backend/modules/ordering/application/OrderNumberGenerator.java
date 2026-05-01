package in.sipora.backend.modules.ordering.application;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates human-readable order numbers in the format:
 *   SIP-YYYYMMDD-NNNN
 *
 * Example: SIP-20250423-0047
 *
 * The counter resets each day. For a production system with multiple
 * instances, replace the AtomicLong with a DB sequence:
 *   SELECT nextval('order_number_seq')
 *
 * For Sipora's current scale (single instance) this in-memory counter is fine.
 * The UUID primary key is the true identifier — the order number is display-only.
 */
@Component
public class OrderNumberGenerator {

    private final AtomicLong counter = new AtomicLong(0);
    private volatile String  currentDate = todayString();

    public synchronized String next() {
        String today = todayString();
        if (!today.equals(currentDate)) {
            currentDate = today;
            counter.set(0);
        }
        return "SIP-%s-%04d".formatted(today, counter.incrementAndGet());
    }

    private static String todayString() {
        return LocalDate.now(ZoneId.of("Asia/Kolkata"))
                .toString()
                .replace("-", "");
    }
}