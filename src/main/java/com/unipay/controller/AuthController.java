package com.unipay.controller;

import com.unipay.command.LoginCommand;
import com.unipay.command.MfaVerificationRequest;
import com.unipay.command.TokenRefreshRequest;
import com.unipay.command.UserRegisterCommand;
import com.unipay.dto.CurrentUser;
import com.unipay.mapper.UserMapper;
import com.unipay.models.User;
import com.unipay.response.EmailConfirmationResponse;
import com.unipay.response.EmailConfirmationResponseFailed;
import com.unipay.response.LoginResponse;
import com.unipay.response.UserRegistrationResponse;
import com.unipay.service.authentication.AuthenticationService;
import com.unipay.service.mail.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.unipay.constants.ResourcePaths.*;

@RestController
@RequestMapping(V1 + AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final EmailService emailService;
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a user along with profile and settings",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "User registered successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UserRegistrationResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid input"
                    )
            }
    )
    @PostMapping(REGISTER)
    public ResponseEntity<UserRegistrationResponse> registerUser(
            @RequestBody UserRegisterCommand command, HttpServletRequest request
    ) {
        authenticationService.register(command, request);
        return ResponseEntity.ok(new UserRegistrationResponse("User registered successfully"));
    }

    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns JWT tokens",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login successful",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid credentials"
                    )
            }
    )
    @PostMapping(LOGIN)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginCommand command, HttpServletRequest request) {
        LoginResponse response = authenticationService.login(command, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Verify MFA code",
            description = "Validates MFA code after initial authentication",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "MFA verified successfully",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid MFA code or expired challenge"
                    )
            }
    )
    @PostMapping(MFA_VERIFY)
    public ResponseEntity<LoginResponse> verifyMfa(
            @RequestBody MfaVerificationRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginResponse response = authenticationService.verifyMfa(
                request.challengeToken(),
                request.code(),
                httpRequest
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Refresh access token",
            description = "Generates new access token using valid refresh token",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Token refreshed successfully",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Invalid or expired refresh token"
                    )
            }
    )
    @PostMapping(REFRESH)
    public ResponseEntity<LoginResponse> refreshToken(
            @RequestBody TokenRefreshRequest request,
            HttpServletRequest httpRequest
    ) {
        LoginResponse response = authenticationService.refreshToken(
                request.refreshToken(),
                httpRequest
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Confirm user registration",
            description = "Verifies a user's email using the confirmation token sent via email"
    )
    @PostMapping(CONFIRM)
    public ResponseEntity<EmailConfirmationResponse> confirmRegistration(@RequestParam("code") String confirmationCode) {
        final EmailConfirmationResponse confirmationResponse = emailService.confirmRegistration(confirmationCode);
        return ResponseEntity.ok(confirmationResponse);
    }

    @Operation(
            summary = "Get current user",
            description = "Returns details for the currently authenticated user"
    )
    @GetMapping(CURRENT)
    public ResponseEntity<CurrentUser> getCurrentUser() {
        final User user = authenticationService.getCurrentUser();
        return ResponseEntity.ok(userMapper.toUser(user));
    }
    @Operation(
            summary = "Forgot Password",
            description = "Initiates password reset process and sends reset email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Password reset email sent"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PostMapping(FORGOT_PASSWORD)
    public ResponseEntity<Void> forgotPassword(@RequestParam("email") String email, HttpServletRequest request) {
        authenticationService.forgotPassword(email, request);
        return ResponseEntity.ok().build();
    }
}