package com.syuro.wibusystem.shared.id;

import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an entity ID field for Snowflake ID generation.
 *
 * Generated IDs are 64-bit {@code Long} values composed of:
 *   41 bits — milliseconds since custom epoch (2024-01-01)
 *   10 bits — datacenter (5) + worker (5) IDs
 *   12 bits — per-millisecond sequence number
 *
 * Apply on the {@code id} field in place of {@code @UuidGenerator}.
 */
@IdGeneratorType(SnowflakeGenerator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SnowflakeGenerated {
}
