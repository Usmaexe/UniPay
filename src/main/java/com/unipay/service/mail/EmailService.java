package com.unipay.service.mail;

import com.unipay.models.User;
import com.unipay.response.EmailConfirmationResponse;
import com.unipay.response.EmailConfirmationResponseFailed;

public interface EmailService {
    void sendPasswordResetEmail(User user, String tokenValue);
    EmailConfirmationResponse confirmRegistration(String confirmationToken);
    void sendConfirmationEmail(User user);
}
