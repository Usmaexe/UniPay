package com.unipay.models;

import com.unipay.command.CreateAddressCommand;
import com.unipay.enums.AddressType;
import com.unipay.enums.Country;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a physical address associated with a user's profile.
 * This entity includes details such as street, city, state, zip code, country,
 * and the type of address (e.g., billing, shipping).
 * The address is linked to a specific user's profile.
 *
 * @see UserProfile
 * @see AddressType
 */
@Entity
@Getter
@Setter
@Table(name = "addresses")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Address extends BaseEntity {

    /**
     * The user profile associated with this address.
     * This field is mandatory, as an address must be linked to a specific user profile.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    /**
     * The street address.
     * Represents the street name and house/building number.
     */
    private String street;

    /**
     * The city in which the address is located.
     */
    private String city;

    /**
     * The state or province in which the address is located.
     */
    private String state;

    /**
     * The postal code or zip code for the address.
     */
    private String zipCode;

    /**
     * The country of the address.
     */
    @Enumerated(EnumType.STRING)
    private Country country;

    /**
     * The type of address (e.g., billing, shipping).
     * The type is represented by an enum to enforce predefined values.
     */
    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    public static Address create(final CreateAddressCommand command){
        final Address address = new Address();

        address.street = command.getStreet();
        address.city = command.getCity();
        address.state = command.getState();
        address.zipCode = command.getZipCode();
        address.country = Country.valueOf(command.getCountry());

        return address;
    }
    public void linkToProfile(UserProfile profile) {
        this.userProfile = profile;
        profile.getAddresses().add(this);
    }
}
