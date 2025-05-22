package com.unipay.controller;

import com.unipay.command.LoginCommand;
import com.unipay.command.UserRegisterCommand;
import com.unipay.dto.CurrentUser;
import com.unipay.mapper.UserMapper;
import com.unipay.models.User;
import com.unipay.response.EmailConfirmationResponse;
import com.unipay.response.LoginResponse;
import com.unipay.response.UserRegistrationResponse;
import com.unipay.service.authentication.AuthenticationService;
import com.unipay.service.mail.EmailService;
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


    @PostMapping(REGISTER)
    public ResponseEntity<UserRegistrationResponse> register(@RequestBody UserRegisterCommand command) {
        authenticationService.register(command);
        return ResponseEntity.ok(new UserRegistrationResponse("User registered successfully"));
    }

    @PostMapping(LOGIN)
    public ResponseEntity<LoginResponse> login(@RequestBody LoginCommand command, HttpServletRequest request) {
        LoginResponse response = authenticationService.login(command, request);
        return ResponseEntity.ok(response);
    }
    @PostMapping(CONFIRM)
    public ResponseEntity<EmailConfirmationResponse> confirmRegistration(@RequestParam("code") String confirmationCode) {
        final EmailConfirmationResponse confirmationResponse = emailService.confirmRegistration(confirmationCode);
        return ResponseEntity.ok(confirmationResponse);
    }
    @GetMapping(CURRENT)
    public ResponseEntity<CurrentUser> getCurrentUser() {
        User user = authenticationService.getCurrentUser();
        return ResponseEntity.ok(userMapper.toUser(user));
    }
}