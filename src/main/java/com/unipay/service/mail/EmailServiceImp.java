package com.unipay.service.mail;


import com.unipay.enums.UserStatus;
import com.unipay.exception.BusinessException;
import com.unipay.exception.ExceptionPayloadFactory;
import com.unipay.models.ConfirmationToken;
import com.unipay.models.User;
import com.unipay.repository.ConfirmationTokenRepository;
import com.unipay.repository.UserRepository;
import com.unipay.response.EmailConfirmationResponse;
import com.unipay.response.EmailConfirmationResponseFailed;
import com.unipay.response.EmailConfirmationResponseSuccess;
import com.unipay.utils.EmailContentBuilder;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImp implements EmailService {

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final EmailContentBuilder emailContentBuilder;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void sendConfirmationEmail(User user) {
        ConfirmationToken token = ConfirmationToken.create(user);
        confirmationTokenRepository.save(token);
        sendConfirmationEmailAsync(user, token);
    }

    @Async
    public void sendConfirmationEmailAsync(User user, ConfirmationToken token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Confirm Your Account");
            helper.setText(emailContentBuilder.buildEmailContent(user, token), true);
            mailSender.send(message);
            log.info("Confirmation email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send confirmation email", e);
        }
    }

    @Override
    @Transactional
    public EmailConfirmationResponse confirmRegistration(String confirmationCode) {
        ConfirmationToken token = getTokenByCode(confirmationCode);
        if (token == null || token.isExpired()) {
            return new EmailConfirmationResponseFailed(
                    "INVALID_TOKEN",
                    "Verification token is invalid or expired"
            );
        }
        User user = userRepository.findByEmail(token.getUser().getEmail())
                .orElseThrow(() -> new BusinessException(ExceptionPayloadFactory.USER_NOT_FOUND.get()));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return new EmailConfirmationResponseSuccess(
                "CONFIRMED",
                "Email verified successfully"
        );
    }

    private ConfirmationToken getTokenByCode(String code) {
        return confirmationTokenRepository.findByConfirmationToken(code)
                .orElseThrow(() -> new BusinessException(ExceptionPayloadFactory.CONFIRMATION_TOKEN_NOT_FOUND.get()));
    }

    @Override
    public void sendNewLoginDetected(String toEmail, String username, String deviceId, HttpServletRequest request) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String subject = "New login from a new device";
        String body = emailContentBuilder.buildNewDeviceLoginEmail(username, deviceId, ipAddress, userAgent);
        sendEmailAsync(toEmail, subject, body);
    }

    @Async
    public void sendEmailAsync(String toEmail, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", toEmail, e);
        }
    }
}

