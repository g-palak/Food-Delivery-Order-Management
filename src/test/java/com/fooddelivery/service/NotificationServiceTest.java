package com.fooddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.fooddelivery.dto.response.NotificationResponse;
import com.fooddelivery.entity.Notification;
import com.fooddelivery.entity.User;
import com.fooddelivery.repository.NotificationRepository;
import com.fooddelivery.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    NotificationRepository notificationRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    SecurityContext securityContext;
    @Mock
    org.springframework.security.core.Authentication authentication;
    @Mock
    UserDetails userDetails;

    @InjectMocks
    NotificationServiceImpl notificationService;

    private void mockPrincipal(Long userId) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(String.valueOf(userId));
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getMyNotifications_returnsInbox() {
        mockPrincipal(1L);

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setRecipient(new User() {{ setId(1L); }});
        notification.setMessage("Order assigned");

        when(notificationRepository.findByRecipientId(1L)).thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getMyNotifications();

        assertEquals(1, responses.size());
        assertEquals("Order assigned", responses.get(0).message());
    }
}
