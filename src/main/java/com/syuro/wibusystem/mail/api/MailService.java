package com.syuro.wibusystem.mail.api;

public interface MailService {
    void sendOtpEmail(String to, String recipientName, String otp);
}
