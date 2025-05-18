package com.unipay.service.mfa;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.unipay.exception.BusinessException;
import com.unipay.exception.ExceptionPayloadFactory;
import com.unipay.exception.QrGenerationException;
import com.unipay.models.MFASettings;
import com.unipay.models.User;
import com.unipay.repository.UserRepository;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

import static com.unipay.constants.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MFAServiceImpl implements MFAService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    @Transactional
    public void enableMfa(User user, String code) {
        if (user.getMfaSettings() == null || !validateCode(user, code)) {
            throw new BusinessException(user.getMfaSettings() == null ?
                    ExceptionPayloadFactory.MFA_NOT_SET_UP.get() :
                    ExceptionPayloadFactory.INVALID_MFA_CODE.get());
        }
        user.getMfaSettings().setEnabled(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void disableMfa(User user) {
        user.setMfaSettings(null);
        userRepository.save(user);
    }

    @Override
    public BufferedImage generateQrCodeImage(User user) throws QrGenerationException {
        String secret = getOrGenerateSecret(user);
        QrData data = new QrData.Builder()
                .label(user.getEmail())
                .secret(secret)
                .issuer("UniPay")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        try {
            BitMatrix bitMatrix = new QRCodeWriter().encode(data.getUri(), BarcodeFormat.QR_CODE, QR_IMAGE_SIZE, QR_IMAGE_SIZE);
            return MatrixToImageWriter.toBufferedImage(bitMatrix);
        } catch (WriterException e) {
            throw new QrGenerationException("Failed to generate QR code", e);
        }
    }

    @Override
    public byte[] getImageData(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        }
    }

    @Override
    @Transactional
    public List<String> generateRecoveryCodes(User user) {
        ensureMfaEnabled(user);
        List<String> codes = secureRandom.ints(RECOVERY_CODE_COUNT, 100000, 999999)
                .mapToObj(i -> String.format("%0" + RECOVERY_CODE_LENGTH + "d", i))
                .collect(Collectors.toList());

        List<String> hashedCodes = codes.stream()
                .map(this::hashCode)
                .collect(Collectors.toList());

        user.getMfaSettings().setRecoveryCodes(hashedCodes);
        userRepository.save(user);

        return codes;
    }

    @Override
    @Transactional(readOnly = true)
    public int getRemainingRecoveryCodes(User user) {
        ensureMfaEnabled(user);
        return user.getMfaSettings().getRecoveryCodes().size();
    }

    @Override
    @Transactional
    public boolean validateCode(User user, String code) {
        ensureMfaEnabled(user);
        return verifier.isValidCode(user.getMfaSettings().getSecret(), code);
    }

    @Override
    @Transactional
    public boolean validateRecoveryCode(User user, String code) {
        MFASettings settings = user.getMfaSettings();
        if (settings == null || settings.getRecoveryCodes().isEmpty()) return false;

        List<String> codes = settings.getRecoveryCodes();
        for (int i = 0; i < codes.size(); i++) {
            if (passwordEncoder.matches(code, codes.get(i))) {
                codes.remove(i);
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    private String getOrGenerateSecret(User user) {
        MFASettings settings = user.getMfaSettings();
        if (settings == null || settings.getSecret() == null) {
            String secret = secretGenerator.generate();
            user.setMfaSettings(MFASettings.create(false, secret, user));
            userRepository.save(user);
            return secret;
        }
        return settings.getSecret();
    }

    private void ensureMfaEnabled(User user) {
        MFASettings settings = user.getMfaSettings();
        if (settings == null || !settings.isEnabled()) {
            throw new BusinessException(ExceptionPayloadFactory.MFA_NOT_ENABLED.get());
        }
    }

    private String hashCode(String code) {
        Base32 base32 = new Base32();
        return base32.encodeToString(code.getBytes(StandardCharsets.UTF_8));
    }
}