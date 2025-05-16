package com.unipay.models;


import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;



@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_session_expiry", columnList = "expiresAt"),
        @Index(name = "idx_session_revoked", columnList = "revoked")
})
public class UserSession extends BaseEntity{
    @Column(nullable = false)
    private boolean revoked = false;
    private String deviceId;
    private String ipAddress;
    private String userAgent;
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public boolean isValid() {
        return !revoked && expiresAt.isAfter(Instant.now());
    }
}
