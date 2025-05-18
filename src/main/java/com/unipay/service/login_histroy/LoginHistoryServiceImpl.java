package com.unipay.service.login_histroy;

import com.unipay.models.LoginHistory;
import com.unipay.models.User;
import com.unipay.repository.LoginHistoryRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class LoginHistoryServiceImpl implements LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    @Override
    @Transactional
    public void createLoginHistory(User user, HttpServletRequest request, boolean successful) {
        String ipAddress = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        LoginHistory loginHistory = LoginHistory.create(user, successful);
        loginHistory.setIpAddress(ipAddress);
        loginHistory.setUserAgent(userAgent);
        loginHistory.setSuccessful(successful);

        user.addLoginHistory(loginHistory);

        loginHistoryRepository.save(loginHistory);

        log.info("LoginHistory created for userId={} from IP={} [{}]", user.getId(), ipAddress, successful ? "SUCCESS" : "FAILURE");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LoginHistory> getLoginHistoryByUserId(Pageable pageable, String userId) {
        log.debug("Fetching LoginHistory for userId={}", userId);

        Page<LoginHistory> loginHistories = loginHistoryRepository.getLoginHistoriesByUser_Id(pageable, userId);

        log.info("Fetched {} LoginHistory records for userId={}", loginHistories.getTotalElements(), userId);
        return loginHistories;
    }
}
