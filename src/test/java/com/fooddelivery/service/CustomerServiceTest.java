package com.fooddelivery.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fooddelivery.dto.request.UpdateUserRequest;
import com.fooddelivery.dto.request.UserRequest;
import com.fooddelivery.dto.response.OrderResponse;
import com.fooddelivery.dto.response.UserResponse;
import com.fooddelivery.entity.Order;
import com.fooddelivery.entity.User;
import com.fooddelivery.enums.Role;
import com.fooddelivery.exception.CustomerNotFoundException;
import com.fooddelivery.exception.DuplicateResourceException;
import com.fooddelivery.repository.OrderRepository;
import com.fooddelivery.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    CustomerServiceImpl customerService;

    @Test
    void createCustomer_success_forcesCustomerRole() {
        UserRequest request = new UserRequest("Bob", "555", Role.CUSTOMER);
        when(userRepository.existsByContact("555")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse response = customerService.createCustomer(request);

        assertNotNull(response);
        assertEquals(Role.CUSTOMER, response.role());
    }

    @Test
    void createCustomer_failsWhenContactDuplicate() {
        UserRequest request = new UserRequest("Bob", "555", Role.CUSTOMER);
        when(userRepository.existsByContact("555")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> customerService.createCustomer(request));
    }

    @Test
    void updateCustomer_success_updatesMutableFields() {
        User user = new User();
        user.setId(1L);
        user.setName("Bob");
        user.setDeleted(false);
        user.setRole(Role.CUSTOMER);

        UpdateUserRequest request = new UpdateUserRequest("Bobby", "999");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = customerService.updateCustomer(1L, request);

        assertNotNull(response);
        assertEquals("Bobby", response.name());
        assertEquals("999", response.contact());
    }
}
