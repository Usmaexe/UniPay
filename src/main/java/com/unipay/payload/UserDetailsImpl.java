package com.unipay.payload;


import com.unipay.enums.UserStatus;
import com.unipay.models.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Setter
public class UserDetailsImpl implements UserDetails {

    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    public static UserDetailsImpl create(User user) {
        Set<GrantedAuthority> authorities = user.getUserRoles().stream()
                .flatMap(role -> role.getRole().getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getName().name()))
                .collect(Collectors.toSet());

        user.getUserRoles().forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRole().getName().name()))
        );

        return new UserDetailsImpl(user, authorities);
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
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }
}

