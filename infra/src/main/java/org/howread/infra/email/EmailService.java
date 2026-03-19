package org.howread.infra.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.howread.user.application.port.EmailPort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * [Adapter] EmailPort를 JavaMailSender로 구현.
 *
 * 이메일 전송 기술(SMTP, SES 등) 세부사항을 캡슐화한다.
 * 발송 실패는 상위로 예외를 전파하여 Service 계층이 처리하도록 한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements EmailPort {

    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationCode(String to, String code) {
        String subject = "[HowRead] 이메일 인증번호";
        String body = buildVerificationHtml(code);
        send(to, subject, body);
    }

    @Override
    public void sendPasswordResetCode(String to, String code) {
        String subject = "[HowRead] 비밀번호 재설정 인증번호";
        String body = buildPasswordResetHtml(code);
        send(to, subject, body);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("이메일 발송 완료: {}", to);
        } catch (Exception e) {
            log.error("이메일 발송 실패: to={}", to, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    private String buildVerificationHtml(String code) {
        return """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
                  <h2 style="color: #333;">이메일 인증</h2>
                  <p>아래 인증번호를 입력해 주세요. 인증번호는 10분간 유효합니다.</p>
                  <div style="font-size: 32px; font-weight: bold; letter-spacing: 8px;
                              text-align: center; padding: 20px; background: #f5f5f5;
                              border-radius: 8px; margin: 20px 0;">
                    %s
                  </div>
                  <p style="color: #888; font-size: 12px;">본인이 요청하지 않은 경우 이 메일을 무시하세요.</p>
                </div>
                """.formatted(code);
    }

    private String buildPasswordResetHtml(String code) {
        return """
                <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto;">
                  <h2 style="color: #333;">비밀번호 재설정</h2>
                  <p>아래 인증번호를 입력해 비밀번호를 재설정해 주세요. 인증번호는 10분간 유효합니다.</p>
                  <div style="font-size: 32px; font-weight: bold; letter-spacing: 8px;
                              text-align: center; padding: 20px; background: #f5f5f5;
                              border-radius: 8px; margin: 20px 0;">
                    %s
                  </div>
                  <p style="color: #888; font-size: 12px;">본인이 요청하지 않은 경우 이 메일을 무시하세요.</p>
                </div>
                """.formatted(code);
    }
}
