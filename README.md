# Food-Delivery-Order-Management
Food Delivery Order Management System

## Requirements:
A food delivery order management system at scale with multiple restaurants across cities, menumanagement per restaurant, customer order placement, the order lifecycle (placed → accepted→ preparing → out-for-delivery → delivered), and delivery-partner assignment. Concurrentorders for the same menu item should not oversell limited stock, and partner assignment shouldhandle multiple partners contending for the same order. Order placement must atomically reflectitem stock, order state, and payment. Status updates should fan out asynchronously tocustomer, restaurant, and delivery partner without blocking the calling flow. Ratings and reviewsafter delivery should be supported.

Roles:
admin (manage cities, restaurants, and delivery partners), restaurant owner (managemenu and accept or reject orders), customer (browse, order, track, rate), and delivery partner(accept assignments and update status).

## Entities:
Users (Contains different roles like Admin, RestaurantOwner, Customer, DeliveryPartner)
- id
- name
- contact (Assumption: currently keeping it just to phone number, but to expand for a larger system with more pssibilities, we can have an enum for this that can have email, phone number etc)
- role

Role 
- (Enum values: Admin, RestaurantOwner, Customer, DeliveryPartner)

Cities
- id
- name
- country
- state
- longitude
- latitude
  
Restaurants
- id
- name
- address
- cityId
- ownerId
- status (Enum: Active or inactive)
  
MenuItems
- id
- name
- price
- stockQuantity
- availability (Enum: available, unavailable)
- restaurantId (Assumption: one menu item is specific to one restaurant)

Orders
- id
- customerId
- restaurantId
- orderStatus
- totalPrice
- paymentStatus
- createdAt

OrderItem
- id
- orderId
- menuItemId
- quantity
- price
  
OrderStatus
- (Enum: values: placed, accepted, preparing, out_of_delivery, delivered, rejected, cancelled)
  
PaymentStatus
- (Enum: values: pending, success, failed, refunded) (Assumption: considering the case where the bill hasn't been shared but the order has been made as pending only)
(Assumption: Not diving deep in the payment architecture, where we can have multiple types of payment methods)

AssignmentStatus
- (Enum: value: pending, accepted, rejected, cancelled, expired)
  
Assignments
- id
- orderId
- deliveryPartnerId
- assignmentStatus
- updatedAt (Assumption: createdAt would be same as for the order.)

Notifications
- id
- orderId
- recipientId
- notificationType
- notificationStatus
- message
- sentAt (Assumption: createdAt = sentAt)
  
NotificationType
- (Enum: value: order_placed, order_accepted, order_preparing, order_picked_up, order_delivered)
  
NotificationStatus
- (Enum: values: pending, sent, failed)
  
Reviews
- id
- orderId
- reviewerId
- revieweeId
- rating
- comment
- createdAt
- revieweeType
  
ReviewType
- (Enum: values: restaurant, delivery_partner, customer)


## APIs required:

### CityController
Admin Access:
- GET /cities/{id or uuid}
- GET /cities
- POST /cities

### DeliveryPartnerController
Admin Access:
- GET /delivery-partners/{id or uuid}
- GET /delivery-partners
- POST /delivery-partners
- PATCH /delivery-partners/{id or uuid}
- DELETE /delivery-partners/{id or uuid} (soft delete)

### RestaurantController
Admin Access:
- GET /restaurants/{id or uuid}
- GET /restaurants
- POST /restaurants
- PATCH /restaurants/{id}
- DELETE /restaurants/{id or uuid} (soft delete)
  
Restaurant Access:
- GET /restaurants/{restaurantId}/menu-items
- GET /restaurants/{restaurantId}/menu-items/{itemId}
- POST /restaurants/{restaurantId}/menu-items           (Assumption: the owners can just update items of their own restaurant)
- PATCH /restaurants/{restaurantId}/menu-items/{itemId}
- DELETE /restaurants/{restaurantId}/menu-items/{itemId} (soft delete)

### CustomerController
Customer Access:
- GET /customers/{id}
- POST /customers
- PATCH /customers/{id}
- GET /customers/{id}/orders
- GET /customers/me
- PATCH /customers/me
- GET /customers/me/orders

### ReviewController
Customer Access:
- GET /reviews  (filtering can be done based on reviewee id)
- POST /reviews

### OrderController
Customer Access:
- GET /orders/{id}
- POST /orders

Restaurant Access:
- PATCH /orders/{id}/accept
- PATCH /orders/{id}/reject
- PATCH /orders/{id}/preparing
- PATCH /orders/{id}/ready

### AssignmentController:
DeliveryPartner Access:
- PATCH /assignments/{id}/accept  
- PATCH /assignments/{id}/pickup
- PATCH /assignments/{id}/deliver

### Extra services
- AssignmentService: will also contain automatic/asynchronous assignment
- NotificationService
- InventoryService
- PaymentService

## Repositories:
- CityRepository
- RestaurantRepository
- MenuItemRepository
- OrderRepository
- OrderItemRepository
- AssignmentRepository
- ReviewRepository
- UserRepository
