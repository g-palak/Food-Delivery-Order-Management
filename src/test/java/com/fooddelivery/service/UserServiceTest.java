package com.fooddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fooddelivery.dto.request.UpdateUserRequest;
import com.fooddelivery.dto.request.UserRequest;
import com.fooddelivery.dto.response.UserResponse;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.CustomerNotFoundException;
import com.fooddelivery.exception.DuplicateResourceException;
import com.fooddelivery.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    SecurityContext securityContext;
    @Mock
    org.springframework.security.core.Authentication authentication;
    @Mock
    UserDetails userDetails;

    @InjectMocks
    UserServiceImpl userService;

    private void mockPrincipal(Long userId) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn(String.valueOf(userId));
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createUser_success_createsUser() {
        UserRequest request = new UserRequest("Alice", "123", Role.CUSTOMER);
        when(userRepository.existsByContact("123")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("Alice", response.name());
    }

    @Test
    void createUser_failsWhenContactDuplicate() {
        UserRequest request = new UserRequest("Alice", "123", Role.CUSTOMER);
        when(userRepository.existsByContact("123")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUser(request));
    }

    @Test
    void getUser_returnsUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Alice");
        user.setContact("123");
        user.setRole(Role.CUSTOMER);
        user.setDeleted(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(1L);

        assertNotNull(response);
        assertEquals("Alice", response.name());
    }

    @Test
    void deleteUser_success_softDeletes() {
        User user = new User();
        user.setId(1L);
        user.setDeleted(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.deleteUser(1L);

        assertEquals(true, user.isDeleted());
        verify(userRepository).save(user);
    }
}
