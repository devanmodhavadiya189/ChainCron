package com.chaincron.service.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class SlotCounter {

    private final AtomicLong slot = new AtomicLong(1);

    public long currentSlot() {
        return slot.get();
    }

    public long advanceSlot() {
        long next = slot.incrementAndGet();
        log.debug("Slot advanced to {}", next);
        return next;
    }

    public long slotForScheduledAt(OffsetDateTime scheduledAt, int pollIntervalSeconds) {
        long now = Instant.now().toEpochMilli();
        long target = scheduledAt.toInstant().toEpochMilli();
        long delayMs = Math.max(0, target - now);
        long pollIntervalMs = (long) pollIntervalSeconds * 1000;
        long futureSlots = (long) Math.ceil((double) delayMs / pollIntervalMs);
        return slot.get() + futureSlots;
    }
}
