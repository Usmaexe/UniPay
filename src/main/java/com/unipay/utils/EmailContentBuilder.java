package com.unipay.utils;


import com.unipay.models.ConfirmationToken;
import com.unipay.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class EmailContentBuilder {

    @Value("${app.name}")
    private String appName;

    @Value("${app.support.email}")
    private String supportEmail;

    public String buildEmailContent(User user, ConfirmationToken token) {
        StringBuilder emailContent = new StringBuilder();

        emailContent.append("<!DOCTYPE html>")
                .append("<html lang=\"en\">")
                .append("<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>Email Verification</title></head>")
                .append("<body style=\"margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;\">")
                .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">")
                .append("<tr><td align=\"center\" style=\"padding: 40px 0;\"><table width=\"600\" style=\"border: 1px solid #eaeaea; border-radius: 8px; padding: 40px;\">")

                .append("<tr><td align=\"center\">")
                .append("<img src=\"https://i.ibb.co/gFvQfqpt/Chat-GPT-Image-Apr-29-2025-02-55-42-PM.png\" alt=\"")
                .append(appName).append(" Logo\" width=\"120\" style=\"margin-bottom: 24px;\">")
                .append("<h1 style=\"color: #1a1a1a; margin: 0 0 24px 0;\">Verify Your Email</h1></td></tr>")

                .append("<tr><td style=\"padding: 24px 0; border-top: 1px solid #eaeaea;\">")
                .append("<p style=\"color: #666666; margin: 0 0 24px 0; line-height: 1.6;\">")
                .append("Hi ").append(user.getUsername()).append(",<br><br>")
                .append("Thank you for creating an account with ").append(appName)
                .append(". Please use the following verification code to confirm your email address:</p>")

                .append("<div style=\"background: #f8f9fa; padding: 16px; border-radius: 6px; text-align: center; margin: 24px 0;\">")
                .append("<code style=\"font-size: 24px; letter-spacing: 2px; color: #2d3748; font-weight: bold;\">")
                .append(token.getConfirmationToken()).append("</code></div>")

                .append("<p style=\"color: #666666; margin: 24px 0; line-height: 1.6;\">")
                .append("This code will expire in 24 hours. If you didn't request this code, you can safely ignore this email.</p></td></tr>")

                .append("<tr><td style=\"padding-top: 24px; border-top: 1px solid #eaeaea;\">")
                .append("<p style=\"color: #999999; font-size: 12px; line-height: 1.6; margin: 0;\">")
                .append("Need help? Contact our support team at <a href=\"mailto:")
                .append(supportEmail).append("\" style=\"color: #4299e1; text-decoration: none;\">")
                .append(supportEmail).append("</a><br>")
                .append("© ").append(LocalDate.now().getYear()).append(" ").append(appName).append(". All rights reserved.</p></td></tr>")

                .append("</table></td></tr></table></body></html>");

        return emailContent.toString();
    }
    public String buildPasswordResetEmailContent(User user, String token) {
        StringBuilder emailContent = new StringBuilder();

        emailContent.append("<!DOCTYPE html>")
                .append("<html lang=\"en\">")
                .append("<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>Password Reset</title></head>")
                .append("<body style=\"margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;\">")
                .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">")
                .append("<tr><td align=\"center\" style=\"padding: 40px 0;\"><table width=\"600\" style=\"border: 1px solid #eaeaea; border-radius: 8px; padding: 40px;\">")

                .append("<tr><td align=\"center\">")
                .append("<img src=\"https://i.ibb.co/gFvQfqpt/Chat-GPT-Image-Apr-29-2025-02-55-42-PM.png\" alt=\"")
                .append(appName).append(" Logo\" width=\"120\" style=\"margin-bottom: 24px;\">")
                .append("<h1 style=\"color: #1a1a1a; margin: 0 0 24px 0;\">Reset Your Password</h1></td></tr>")

                .append("<tr><td style=\"padding: 24px 0; border-top: 1px solid #eaeaea;\">")
                .append("<p style=\"color: #666666; margin: 0 0 24px 0; line-height: 1.6;\">")
                .append("Hi ").append(user.getUsername()).append(",<br><br>")
                .append("We received a request to reset your password for your ").append(appName)
                .append(" account. Please use the following code to reset your password:</p>")

                .append("<div style=\"background: #f8f9fa; padding: 16px; border-radius: 6px; text-align: center; margin: 24px 0;\">")
                .append("<code style=\"font-size: 24px; letter-spacing: 2px; color: #2d3748; font-weight: bold;\">")
                .append(token).append("</code></div>")

                .append("<p style=\"color: #666666; margin: 24px 0; line-height: 1.6;\">")
                .append("This code will expire in 24 hours. If you didn't request a password reset, you can safely ignore this email.</p></td></tr>")

                .append("<tr><td style=\"padding-top: 24px; border-top: 1px solid #eaeaea;\">")
                .append("<p style=\"color: #999999; font-size: 12px; line-height: 1.6; margin: 0;\">")
                .append("Need help? Contact our support team at <a href=\"mailto:")
                .append(supportEmail).append("\" style=\"color: #4299e1; text-decoration: none;\">")
                .append(supportEmail).append("</a><br>")
                .append("© ").append(LocalDate.now().getYear()).append(" ").append(appName).append(". All rights reserved.</p></td></tr>")

                .append("</table></td></tr></table></body></html>");

        return emailContent.toString();
    }
    public String buildNewDeviceLoginEmail(String username, String deviceId, String ipAddress, String userAgent) {
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<!DOCTYPE html>")
                .append("<html lang=\"en\">")
                .append("<head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">")
                .append("<title>New Device Login</title></head>")
                .append("<body style=\"margin: 0; padding: 0; font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;\">")
                .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">")
                .append("<tr><td align=\"center\" style=\"padding: 40px 0;\">")
                .append("<table width=\"600\" style=\"border: 1px solid #eaeaea; border-radius: 8px; padding: 40px;\">")
                .append("<tr><td align=\"center\">")
                .append("<img src=\"https://yourdomain.com/logo.png\" alt=\"YourApp Logo\" width=\"120\" style=\"margin-bottom: 24px;\">")
                .append("<h1 style=\"color: #1a1a1a; margin: 0 0 24px 0;\">New Device Login Detected</h1>")
                .append("</td></tr>")
                .append("<tr><td style=\"padding: 24px 0; border-top: 1px solid #eaeaea;\">")
                .append("<p style=\"color: #666666; margin: 0 0 24px 0; line-height: 1.6;\">")
                .append("Hi ").append(username).append(",<br><br>")
                .append("We noticed a login to your account from a new device. If this was you, no action is needed. Otherwise, please secure your account.")
                .append("</p>")
                .append("<ul style=\"color: #666666; line-height: 1.6;\">")
                .append("<li><strong>Device ID:</strong> ").append(deviceId).append("</li>")
                .append("<li><strong>IP Address:</strong> ").append(ipAddress).append("</li>")
                .append("<li><strong>User-Agent:</strong> ").append(userAgent).append("</li>")
                .append("</ul>")
                .append("</td></tr>")
                .append("<tr><td style=\"padding-top: 24px; border-top: 1px solid #eaeaea;\">")
                .append("<p style=\"color: #999999; font-size: 12px; line-height: 1.6; margin: 0;\">")
                .append("Need help? Contact our support team at <a href=\"mailto:support@yourdomain.com\" style=\"color: #4299e1; text-decoration: none;\">support@yourdomain.com</a><br>")
                .append("© ").append(LocalDate.now().getYear()).append(" YourApp. All rights reserved.")
                .append("</p>")
                .append("</td></tr>")
                .append("</table></td></tr></table></body></html>");
        return emailContent.toString();
    }

}