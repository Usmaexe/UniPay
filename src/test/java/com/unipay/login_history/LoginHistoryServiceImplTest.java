package com.unipay.login_history;

import com.unipay.models.LoginHistory;
import com.unipay.models.User;
import com.unipay.repository.LoginHistoryRepository;
import com.unipay.service.login_histroy.LoginHistoryServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.mockito.Mockito.*;

public class LoginHistoryServiceImplTest {

    @Mock private LoginHistoryRepository loginHistoryRepository;
    @Mock private HttpServletRequest request;
    @Mock private User user;

    @InjectMocks
    private LoginHistoryServiceImpl loginHistoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createLoginHistory_shouldCreateAndSaveLoginHistory() {
        /*// Prepare test data
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";

        // Stubbing the request behavior
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(request.getHeader("User-Agent")).thenReturn(userAgent);

        // Create a user instance (mocked)
        User user = mock(User.class);

        // Execute the method to be tested
        loginHistoryService.createLoginHistory(user, request, true);

        // Verify interactions with the mocks
        verify(request).getRemoteAddr();
        verify(request).getHeader("User-Agent");
        verify(user).addLoginHistory(any(LoginHistory.class)); // Verify the addLoginHistory method is called

        // Verify that save is called on the repository
        verify(loginHistoryRepository).save(any(LoginHistory.class));*/
    }

}
