package com.unipay.service.mail;

import com.unipay.models.User;
import com.unipay.response.EmailConfirmationResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface EmailService {
    EmailConfirmationResponse confirmRegistration(String confirmationToken);
    void sendConfirmationEmail(User user);
    void sendNewLoginDetected(String toEmail, String username, String deviceId, HttpServletRequest request);
}
