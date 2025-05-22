package com.unipay.controller;

import com.unipay.criteria.UserCriteria;
import com.unipay.dto.UserDto;
import com.unipay.mapper.UserMapper;
import com.unipay.models.User;
import com.unipay.response.UserRegistrationResponse;
import com.unipay.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.unipay.constants.ResourcePaths.USERS;
import static com.unipay.constants.ResourcePaths.V1;

/**
 * UserController is responsible for handling HTTP requests related to user management.
 * It provides an endpoint for user registration and interacts with the UserService
 * to create a new user, profile, and settings in the system.
 *
 * This class uses OpenAPI annotations for API documentation and specifies the
 * expected behavior of the registration endpoint.
 *
 * Endpoints:
 * - POST /v1/users/register: Registers a new user with their profile and settings.
 *
 * @see UserService
 * @see UserMapper
 * @see UserRegistrationResponse
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(V1 + USERS)
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;


    @Operation(
            summary = "Get users by criteria",
            description = "Retrieves a paginated list of users based on filtering criteria",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    @GetMapping
    public ResponseEntity<Page<UserDto>> getUsersByCriteria(UserCriteria criteria, Pageable pageable) {
        Page<User> users = userService.getAllByCriteria(pageable, criteria);
        Page<UserDto> userDtos = users.map(userMapper::toDto);
        return ResponseEntity.ok(userDtos);
    }
}
