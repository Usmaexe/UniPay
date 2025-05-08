package com.unipay.payload;


import com.unipay.enums.UserStatus;
import com.unipay.models.Role;
import com.unipay.models.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


@Getter
@Setter
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private final User user;
    private boolean mfaVerified = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        user.getUserRoles().forEach(userRole -> {
            Role role = userRole.getRole();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            role.getPermissions().forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission.getName().name()))
            );
        });

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE &&
                (!isMfaRequired() || mfaVerified);
    }
    public String getId() {
        return user.getId();
    }
    public boolean isMfaVerified() {
        return mfaVerified;
    }
    public boolean isMfaRequired() {
        return user.getMfaSettings() != null && user.getMfaSettings().isEnabled();
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(user.getId(), that.user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(user.getId());
    }

    // Add session awareness
    public boolean isSessionValid(String sessionId) {
        return user.getSessions().stream()
                .anyMatch(s -> s.getId().equals(sessionId) && s.isValid());
    }
}

