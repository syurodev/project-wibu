package com.syuro.wibusystem.shared.id;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.generator.GeneratorCreationContext;

import java.lang.reflect.Member;
import java.util.EnumSet;

/**
 * Hibernate ID generator implementing the Snowflake algorithm.
 *
 * Bit layout (64-bit Long):
 *   1  bit  — sign (always 0 → positive)
 *   41 bits — timestamp delta from EPOCH in milliseconds (~69 years range)
 *   5  bits — datacenter ID (0–31)
 *   5  bits — worker ID (0–31)
 *   12 bits — per-millisecond sequence (0–4095)
 *
 * Defaults: datacenter=0, worker=0.
 * For multi-node deployments, configure via environment variables:
 *   SNOWFLAKE_DATACENTER_ID and SNOWFLAKE_WORKER_ID (0–31 each).
 */
public class SnowflakeGenerator implements BeforeExecutionGenerator {

    /** 2024-01-01T00:00:00Z — reduces ID size for IDs generated after this date. */
    private static final long EPOCH = 1_704_067_200_000L;

    private static final int DATACENTER_ID_BITS = 5;
    private static final int WORKER_ID_BITS = 5;
    private static final int SEQUENCE_BITS = 12;

    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS); // 31
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);         // 31
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);          // 4095

    private static final int WORKER_ID_SHIFT = SEQUENCE_BITS;                                // 12
    private static final int DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;           // 17
    private static final int TIMESTAMP_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS; // 22

    // Static state — shared across all entity types; synchronized on class lock
    private static long sequence = 0L;
    private static long lastTimestamp = -1L;

    private static final long WORKER_ID;
    private static final long DATACENTER_ID;

    static {
        DATACENTER_ID = parseLongEnv("SNOWFLAKE_DATACENTER_ID", MAX_DATACENTER_ID);
        WORKER_ID = parseLongEnv("SNOWFLAKE_WORKER_ID", MAX_WORKER_ID);
    }

    /** Constructor required by {@code @IdGeneratorType}. */
    public SnowflakeGenerator(SnowflakeGenerated annotation, Member member, GeneratorCreationContext context) {
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EventTypeSets.INSERT_ONLY;
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner,
                           Object currentValue, EventType eventType) {
        return nextId();
    }

    public static synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp < lastTimestamp) {
            throw new IllegalStateException(
                    "Clock moved backwards %d ms — refusing Snowflake ID generation"
                            .formatted(lastTimestamp - timestamp));
        }

        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                // Sequence exhausted in this millisecond — busy-wait for the next one
                do {
                    timestamp = System.currentTimeMillis();
                } while (timestamp <= lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (DATACENTER_ID << DATACENTER_ID_SHIFT)
                | (WORKER_ID << WORKER_ID_SHIFT)
                | sequence;
    }

    private static long parseLongEnv(String envKey, long max) {
        String val = System.getenv(envKey);
        if (val == null || val.isBlank()) return 0L;
        long parsed = Long.parseLong(val.trim());
        if (parsed < 0 || parsed > max) {
            throw new IllegalArgumentException(
                    "%s must be between 0 and %d, got %d".formatted(envKey, max, parsed));
        }
        return parsed;
    }
}
