package com.syuro.wibusystem.security.session.anonymous.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Thông tin thiết bị gửi từ client khi khởi tạo anonymous session.
 * Tất cả field đều nullable — client có thể không gửi đủ (browser restrictions, bot, ...).
 * Fingerprint được tính từ tổ hợp các field này, thiếu field → fingerprint kém unique hơn.
 */
public record DeviceInfo(
        @JsonProperty("client_ip")
        String clientIp,

        @JsonProperty("user_agent")
        String userAgent,

        String language,
        String timezone,

        @JsonProperty("screen_resolution")
        String screenResolution,

        String platform
) {
}
