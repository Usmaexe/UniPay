package com.unipay.service.profile;

import com.unipay.command.CreateAddressCommand;
import com.unipay.command.ProfileCommand;
import com.unipay.exception.BusinessException;
import com.unipay.exception.ExceptionPayloadFactory;
import com.unipay.models.Address;
import com.unipay.models.User;
import com.unipay.models.UserProfile;
import com.unipay.repository.AddressRepository;
import com.unipay.repository.UserProfileRepository;
import com.unipay.service.authentication.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the {@link UserProfileService} interface.
 *
 * <p>This service handles the creation of {@link UserProfile} instances based on user-submitted profile data.
 * It ensures proper association of the profile with the {@link User} entity and persists the data using
 * the {@link UserProfileRepository}.</p>
 *
 * <p>Key Responsibilities:
 * <ul>
 *   <li>Validating input data from {@link ProfileCommand}.</li>
 *   <li>Constructing a {@link UserProfile} object and linking it to a {@link User}.</li>
 *   <li>Saving the profile entity to the database.</li>
 *   <li>Logging each step of the process for traceability and debugging.</li>
 * </ul>
 * </p>
 *
 * <p>This service uses Spring's {@code @Transactional} annotation to ensure atomic operations during profile creation.</p>
 *
 * @author
 * @see com.unipay.service.profile.UserProfileService
 * @see com.unipay.models.UserProfile
 * @see com.unipay.command.ProfileCommand
 */
@Slf4j
@Service
public class UserProfileServiceImpl implements UserProfileService {

    /**
     * Repository for accessing and persisting {@link UserProfile} entities.
     */
    private final AuthenticationService authenticationService;
    private final AddressRepository addressRepository;
    private final UserProfileRepository userProfileRepository;


    @Autowired
    public UserProfileServiceImpl(
            @Lazy AuthenticationService authenticationService,
            AddressRepository addressRepository,
            UserProfileRepository userProfileRepository
    ) {
        this.authenticationService = authenticationService;
        this.addressRepository = addressRepository;
        this.userProfileRepository = userProfileRepository;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Creates and saves a new user profile for the given user. Validates the command,
     * builds a new {@link UserProfile} entity, and persists it using the repository.</p>
     *
     * @param profileCommand The command containing profile details submitted by the user.
     * @param user The user for whom the profile is being created.
     * @return The saved {@link UserProfile} entity.
     * @throws RuntimeException if an error occurs during persistence.
     */
    @Override
    @Transactional
    public UserProfile create(ProfileCommand profileCommand, User user) {
        profileCommand.validate();
        log.debug("Start creating User profile for user {}", user.getId());

        UserProfile userProfile = UserProfile.create(profileCommand);
        userProfile.setUser(user);

        userProfile = userProfileRepository.save(userProfile);
        log.info("User profile created successfully with ID {}", userProfile.getId());
        return userProfile;
    }

    /**
     * Adds a new address using the provided command data.
     *
     * @param addressCommand The command object containing the details required to create the address.
     * @throws BusinessException If address creation fails due to validation or processing errors.
     */
    @Override
    public void addAddress(CreateAddressCommand addressCommand) {
        User currentUser = authenticationService.getCurrentUser();
        UserProfile profile = currentUser.getProfile();

        if (profile == null) {
            throw new BusinessException(ExceptionPayloadFactory.USER_PROFILE_NOT_FOUND.get());
        }

        Address newAddress = profile.addAddress(addressCommand);
        addressRepository.save(newAddress);
    }

}
