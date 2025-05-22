package com.unipay.service.mfa;

import com.unipay.exception.QrGenerationException;
import com.unipay.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MFAServiceImpl implements MFAService {

    @Override
    public void enableMfa(User user, String code) {

    }

    @Override
    public void disableMfa(User user) {

    }

    @Override
    public BufferedImage generateQrCodeImage(User user) throws QrGenerationException {
        return null;
    }

    @Override
    public byte[] getImageData(BufferedImage image) throws IOException {
        return new byte[0];
    }

    @Override
    public List<String> generateRecoveryCodes(User user) {
        return List.of();
    }

    @Override
    public int getRemainingRecoveryCodes(User user) {
        return 0;
    }

    @Override
    public boolean validateCode(User user, String code) {
        return false;
    }

    @Override
    public boolean validateRecoveryCode(User user, String code) {
        return false;
    }
}