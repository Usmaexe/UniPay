package com.unipay.models;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;



@Setter
@Getter
@Entity
public class MFASettings extends BaseEntity{
    private boolean enabled;
    private String secret; // Base32 encoded TOTP secret

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ElementCollection
    private List<String> recoveryCodes;


    private List<String> generateRecoveryCodes() {
        return IntStream.range(0, 10)
                .mapToObj(i -> UUID.randomUUID().toString())
                .collect(Collectors.toList());
    }
    public static MFASettings create(boolean enabled, String secret, User user){
        final MFASettings mfaSettings = new MFASettings();

        mfaSettings.enabled = enabled;
        mfaSettings.secret = secret;
        mfaSettings.user = user;
        mfaSettings.recoveryCodes = mfaSettings.generateRecoveryCodes();

        return mfaSettings;
    }
    public boolean validateCode(String code) {
        // Get the stored secret from MFA settings
        String secret = this.getSecret();

        // TOTP parameters
        long timeStep = 30L; // 30-second time steps
        long time = Instant.now().getEpochSecond() / timeStep;
        int codeDigits = 6; // Length of verification code

        // Allow a 1-step window for time synchronization
        for (int i = -1; i <= 1; i++) {
            String calculatedCode = generateTotp(secret, time + i, codeDigits);
            if (calculatedCode.equals(code)) {
                return true;
            }
        }
        return false;
    }

    private String generateTotp(String secret, long time, int codeDigits) {
        byte[] key = Base64.getDecoder().decode(secret);
        byte[] data = new byte[8];
        long value = time;

        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xF;
            long truncatedHash = 0;

            for (int i = 0; i < 4; ++i) {
                truncatedHash <<= 8;
                truncatedHash |= (hash[offset + i] & 0xFF);
            }

            truncatedHash &= 0x7FFFFFFF;
            truncatedHash %= Math.pow(10, codeDigits);

            return String.format("%0" + codeDigits + "d", truncatedHash);
        } catch (GeneralSecurityException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
