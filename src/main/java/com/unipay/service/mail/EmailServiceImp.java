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
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImp implements EmailService{

    private final ConfirmationTokenRepository confirmationTokenRepository;
    private final EmailContentBuilder emailContentBuilder;
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;


    @Async
    @Override
    @Transactional
    public void sendConfirmationEmail(User user) {
        try {
            ConfirmationToken token = ConfirmationToken.create(user);
            confirmationTokenRepository.save(token);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Confirm Your Account");
            helper.setText(emailContentBuilder.buildEmailContent(user, token), true);

            mailSender.send(message);
            log.info("Confirmation email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send confirmation email", e);
            throw new BusinessException(ExceptionPayloadFactory.FAILED_TO_SEND_EMAIL.get());
        }
    }
    /**
     * Confirms a user's registration using a confirmation token.
     * @param confirmationCode the token to validate and activate the user
     */
    @Override
    @Transactional
    public EmailConfirmationResponse confirmRegistration(String confirmationCode) {
        log.info("Verifying confirmation code...");

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

        log.info("Email {} verified and status set to ACTIVE.", user.getEmail());

        return new EmailConfirmationResponseSuccess(
                "CONFIRMED",
                "Email verified successfully"
        );
    }


    private ConfirmationToken getTokenByCode(String code) {
        ConfirmationToken token = confirmationTokenRepository.findByConfirmationToken(code)
                .orElseThrow(() -> new BusinessException(ExceptionPayloadFactory.CONFIRMATION_TOKEN_NOT_FOUND.get()));

        log.info("Confirmation token fetched");
        return token;
    }
    @Async
    @Override
    @Transactional
    public void sendPasswordResetEmail(User user, String tokenValue) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Reset Your Password");
            helper.setText(
                    emailContentBuilder.buildPasswordResetEmailContent(user, tokenValue),
                    true
            );

            mailSender.send(message);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email", e);
            throw new BusinessException(
                    ExceptionPayloadFactory.FAILED_TO_SEND_EMAIL.get()
            );
        }
    }
}
