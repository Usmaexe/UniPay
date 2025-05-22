package com.unipay.command;

import com.unipay.utils.AssertValidation;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command class for user registration. This class represents the data needed to create a new user
 * in the system, including the username, email, password, profile details, and user settings.
 *
 * The class includes a `validate()` method to ensure that the data provided in these fields
 * adheres to the validation rules defined in the AssertValidation utility, and that nested profile
 * and settings data are also validated.
 *
 * The following fields are included:
 * - username: The username of the user (will be validated for format).
 * - email: The user's email (will be validated for format).
 * - password: The user's password (will be validated for strength).
 * - profile: A `ProfileCommand` object containing user's personal information like full name, phone number, etc.
 * - settings: A `UserSettingsCommand` object containing user's preferences such as language, timezone, etc.
 *
 * Example usage:
 * <pre>
 * UserRegisterCommand userRegisterCommand = new UserRegisterCommand();
 * userRegisterCommand.setUsername("john_doe");
 * userRegisterCommand.setEmail("john.doe@unipay.com");
 * userRegisterCommand.setPassword("SecurePass123!");
 * userRegisterCommand.setProfile(new ProfileCommand());
 * userRegisterCommand.setSettings(new UserSettingsCommand());
 * userRegisterCommand.validate(); // Validates the registration data.
 * </pre>
 *
 * @see AssertValidation
 * @see ProfileCommand
 * @see UserSettingsCommand
 */
@Getter
@AllArgsConstructor
public class UserRegisterCommand {

    /**
     * The username of the user.
     * This field will be validated to ensure it follows a specific pattern,
     * such as being alphanumeric and having a length between 3 to 20 characters.
     */
    private String username;

    /**
     * The email of the user.
     * This field will be validated to ensure it matches a valid email format.
     */
    private String email;

    /**
     * The password for the user's account.
     * This field will be validated to ensure it meets security criteria such as having a minimum length
     * and containing a mix of letters, numbers, and special characters.
     */
    private String password;

    /**
     * The user's profile information.
     * This field contains a `ProfileCommand` object, which will be validated to ensure that
     * the profile data (e.g., full name, phone number, date of birth) is correct.
     */
    private ProfileCommand profile;

    /**
     * The user's settings information.
     * This field contains a `UserSettingsCommand` object, which will be validated to ensure that
     * user preferences like language and timezone are set correctly.
     */
    private UserSettingsCommand settings;

    /**
     * Validates the user's registration data using the AssertValidation utility class.
     * This method checks if the username, email, password, profile, and settings data adhere
     * to the appropriate format and constraints.
     *
     * Throws an IllegalArgumentException if any validation fails.
     */
    public void validate() {
        AssertValidation.assertValidUsername(username);
        AssertValidation.assertValidPassword(password);
        AssertValidation.assertValidEmail(email);
        profile.validate();
        settings.validate();
    }
}
