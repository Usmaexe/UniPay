package com.unipay.models;

import com.unipay.command.CreateAddressCommand;
import com.unipay.command.ProfileCommand;
import com.unipay.enums.UserGender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the extended profile information of a user.
 * Includes personal and contact details, and associations with addresses and payment methods.
 *
 * <p>Key Fields:
 * <ul>
 *   <li><strong>user</strong>: The user to whom this profile belongs (one-to-one relationship).</li>
 *   <li><strong>fullName</strong>: The user's full legal name.</li>
 *   <li><strong>dateOfBirth</strong>: The user's date of birth.</li>
 *   <li><strong>phoneNumber</strong>: Contact phone number of the user.</li>
 *   <li><strong>gender</strong>: Gender identity of the user.</li>
 *   <li><strong>nationality</strong>: The user's nationality or country of citizenship.</li>
 *   <li><strong>addresses</strong>: A set of addresses linked to the profile.</li>
 *   <li><strong>paymentMethods</strong>: A set of payment methods associated with the user.</li>
 * </ul>
 *
 * <p>This class contains the user's personal and contact details, extending the user entity's basic
 * information. It supports adding addresses and payment methods, as well as tracking demographic details like
 * gender and nationality.
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile extends BaseEntity {

    /**
     * The user to whom this profile belongs.
     * This is a one-to-one relationship with the {@link User} entity.
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * The user's full legal name.
     * This field is used to store the full name of the user for profile purposes.
     */
    private String fullName;

    /**
     * The user's date of birth.
     * This field captures the date of birth of the user, useful for age verification and profile personalization.
     */
    private LocalDate dateOfBirth;

    /**
     * The user's contact phone number.
     * This is stored as a string to accommodate various formats (e.g., with or without country code).
     */
    private String phoneNumber;

    /**
     * The user's gender identity.
     * This field stores the gender of the user, typically as a string (e.g., "Male", "Female", etc.).
     */
    @Enumerated(EnumType.STRING)
    private UserGender gender;

    /**
     * The user's nationality or country of citizenship.
     * This field represents the user's national identity or country of origin.
     */
    private String nationality;

    /**
     * A set of addresses linked to the user's profile.
     * This allows the user to have multiple addresses stored in their profile.
     * This is a one-to-many relationship with the {@link Address} entity.
     */
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL)
    private Set<Address> addresses = new HashSet<>();

    /**
     * A set of payment methods associated with the user's profile.
     * This allows the user to have multiple payment methods linked to their profile.
     * This is a one-to-many relationship with the {@link PaymentMethod} entity.
     */
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL)
    private Set<PaymentMethod> paymentMethods = new HashSet<>();

    /**
     * Creates a new {@link UserProfile} instance from the provided {@link ProfileCommand}.
     *
     * @param command The command containing the user's profile data.
     * @return A new {@link UserProfile} object initialized with the command's values.
     */
    public static UserProfile create(final ProfileCommand command){
        final UserProfile userProfile = new UserProfile();

        userProfile.fullName = command.getFullName();
        userProfile.dateOfBirth = command.getDateOfBirth();
        userProfile.phoneNumber = command.getPhoneNumber();
        userProfile.gender = UserGender.valueOf(command.getGender());
        userProfile.nationality = command.getNationality();

        return userProfile;
    }
    public Address addAddress(final CreateAddressCommand command){
        final Address address = Address.create(command);

        address.linkToProfile(this);
        this.addresses.add(address);
        return address;
    }
    public static Set<Address> createAddress(final Set<CreateAddressCommand> addressCommands){
        return addressCommands.stream().map(Address::create).collect(Collectors.toSet());
    }
}
