package com.syuro.wibusystem.mail.provider;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.syuro.wibusystem.mail.api.MailService;
import com.syuro.wibusystem.mail.config.MailProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class ReSendProvider implements MailService {

    private final Resend resend;
    private final String from;

    public ReSendProvider(MailProperties props) {
        this.resend = new Resend(props.apiKey());
        this.from = props.from();
    }

    @Async
    @Override
    public void sendOtpEmail(String to, String recipientName, String otp) {
        CreateEmailOptions request = CreateEmailOptions.builder()
                .from(from)
                .to(List.of(to))
                .subject("Mã xác minh của bạn")
                .html(buildOtpHtml(recipientName, otp))
                .build();
        try {
            resend.emails().send(request);
        } catch (ResendException e) {
            log.error("Failed to send OTP email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }

    private String buildOtpHtml(String name, String otp) {
        return """
                <div style="font-family:sans-serif;max-width:480px;margin:auto;padding:32px">
                  <h2 style="margin-bottom:8px">Xin chào, %s!</h2>
                  <p style="color:#555">Mã xác minh tài khoản của bạn là:</p>
                  <div style="font-size:40px;font-weight:bold;letter-spacing:10px;text-align:center;
                              padding:24px;background:#f4f4f4;border-radius:8px;margin:20px 0">%s</div>
                  <p style="color:#555">Mã có hiệu lực trong <strong>15 phút</strong>.</p>
                  <p style="color:#999;font-size:12px;margin-top:32px">
                    Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này.
                  </p>
                </div>
                """.formatted(name, otp);
    }
}
