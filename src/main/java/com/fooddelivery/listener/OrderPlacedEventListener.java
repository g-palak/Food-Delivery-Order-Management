package com.fooddelivery.listener;

import com.fooddelivery.entity.Order;
import com.fooddelivery.service.AssignmentService;
import com.fooddelivery.service.NotificationService;
import com.fooddelivery.entity.Notification;
import com.fooddelivery.enums.NotificationType;
import com.fooddelivery.enums.NotificationStatus;
import com.fooddelivery.repository.NotificationRepository;
import com.fooddelivery.event.OrderPlacedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

/**
 * Async side effects of order placement.
 *
 * <p>AFTER_COMMIT guarantees listeners run only if the order transaction committed,
 * preventing false notifications on failed payments.</p>
 */
@Component
public class OrderPlacedEventListener {

    private final AssignmentService assignmentService;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;

    public OrderPlacedEventListener(AssignmentService assignmentService,
                                    NotificationService notificationService,
                                    NotificationRepository notificationRepository) {
        this.assignmentService = assignmentService;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlaced(OrderPlacedEvent event) {
        Order order = event.getOrder();

        // Trigger auto-assignment to available delivery partners
        assignmentService.assignDeliveryPartnerIfAvailable(order.getId());

        // Create a notification for the restaurant owner
        Notification notification = new Notification();
        notification.setRecipient(order.getRestaurant().getOwner());
        notification.setOrder(order);
        notification.setType(NotificationType.NEW_ORDER);
        notification.setStatus(NotificationStatus.UNREAD);
        notification.setMessage("New order placed: " + order.getId());
        notificationRepository.save(notification);
    }
}
