package com.unipay.response;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class LoginResponse {
    private String token;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;

    public LoginResponse(String token, String username, Collection<? extends GrantedAuthority> authorities) {
        this.token = token;
        this.username = username;
        this.authorities = authorities;
    }
}