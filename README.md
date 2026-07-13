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
- paymentDoneAt

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
- createdAt (Assumption: createdAt = sentAt)
  
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
General user Access:
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

DeliveryPartner Access:
- PATCH /orders/{id}/pickup
- PATCH /orders/{id}/deliver

### AssignmentController:
DeliveryPartner Access:
- GET /assignments
- PATCH /assignments/{id}/accept

### Extra services
- AssignmentService: will also contain automatic assignment and handle concurrent partner acceptance
- NotificationService: for asynchronous update notifications
- InventoryService:  to manage item inventory
- PaymentService: to handle payment processing

## Repositories:
- CityRepository
- RestaurantRepository
- MenuItemRepository
- OrderRepository
- OrderItemRepository
- AssignmentRepository
- ReviewRepository
- UserRepository

## Architecture overview
This project follows a layered Spring Boot 3 monolith architecture:

- Controller layer: HTTP entrypoints, request validation, and security enforcement.
- Service layer: business rules, transaction boundaries, and coordination between repositories.
- Repository layer: Spring Data JPA access to the database.
- DTO layer: separate request/response records; JPA entities are never exposed directly.
- Event layer: Spring application events for asynchronous notification dispatch.

Transaction boundaries are declared at the service layer, not the controller layer. Controllers are kept thin and delegate immediately to services.

## Assumptions
- Payment is simulated; no external payment gateway integration.
- Authentication/authorization uses Spring Security role-based access; JWT/OAuth and login flows are out of scope.
- Docker, Kafka, and Redis are out of scope.
- The system is deployed as a single monolith.
- Database is MySQL in production; H2 in-memory is used for tests.
- Soft delete is implemented via a `deleted` flag on `User`, `Restaurant`, and `MenuItem`.
- Contact is currently modeled as a simple String/phone number for assignment.
- `createdAt` for `Assignment` is treated as equivalent to the related order creation time.
- `createdAt` for `Notification` is treated as equivalent to `sentAt`.

## Entity relationships
- `User`: supports `ADMIN`, `RESTAURANT_OWNER`, `CUSTOMER`, and `DELIVERY_PARTNER` roles.
  - 1:N with `Order` as customer
  - 1:N with `Restaurant` as owner
  - 1:N with `Assignment` as delivery partner
  - 1:N with `Notification` as recipient
  - 1:N with `Review` as reviewer/reviewee
- `City`: 1:N with `Restaurant`
- `Restaurant`: 1:N with `MenuItem`; belongs to `City` and `User` as owner
- `MenuItem`: belongs to `Restaurant`; tracked by `OrderItem`
- `Order`: belongs to `Customer` and `Restaurant`; 1:N with `OrderItem`
- `OrderItem`: links `Order` and `MenuItem` with snapshot fields for name/price at order time
- `Assignment`: links `Order` and `DeliveryPartner`
- `Notification`: linked to `Order` and `User` recipient; supports async fan-out by notification type
- `Review`: linked to `Order`, `User` reviewer, and `User` reviewee; supports restaurant, delivery partner, and customer reviews

## API summary
Base paths are grouped by bounded context:

- `/cities` — city CRUD
- `/restaurants/{restaurantId}/menu-items` — menu item CRUD
- `/customers` — customer profile management and order history
- `/delivery-partners` — delivery partner management
- `/orders` — order lifecycle, placement, and tracking
- `/assignments` — partner assignment listing and acceptance/rejection
- `/reviews` — review creation and lookup
- `/notifications/me` — authenticated inbox for notifications

Access is enforced with Spring Security role annotations at the controller level, and service-layer ownership checks are applied where ownership depends on authenticated identity.

## Sequence diagrams

### Order placement flow
```text
Customer -> OrderController : POST /orders (PlaceOrderRequest)
OrderController -> OrderService : placeOrder(customerId, request)
OrderService -> UserRepository : findById(customerId)
OrderService -> RestaurantRepository : findById(restaurantId)
OrderService -> MenuItemRepository : findAllById(itemIds) [PESSIMISTIC_WRITE]
OrderService -> MenuItemRepository : save(decremented stock)
OrderService -> OrderRepository : save(Order)
OrderService -> OrderItemRepository : saveAll(OrderItems)
OrderService -> PaymentService : processPayment(orderId)
PaymentService -> OrderRepository : update payment status
OrderService -> ApplicationEventPublisher : publishEvent(OrderPlacedEvent) [AFTER_COMMIT]
OrderPlacedEventListener -> NotificationService : create notifications
OrderPlacedEventListener -> AssignmentService : auto-assign partner if needed
OrderService -> OrderController : OrderResponse
OrderController -> Customer : 201 Created
```

### Assignment acceptance flow
```text
DeliveryPartner -> AssignmentController : PATCH /assignments/{id}/accept
AssignmentController -> AssignmentService : acceptAssignment(id)
AssignmentService -> AssignmentRepository : findById(id) [PESSIMISTIC_WRITE]
AssignmentService -> AssignmentRepository : save(ACCEPTED)
AssignmentService -> NotificationService : async notification
AssignmentService -> AssignmentController : AssignmentResponse
AssignmentController -> DeliveryPartner : 200 OK
```

### Notification fan-out flow
```text
@Service
public class OrderPlacedEventListener {
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void onOrderPlaced(OrderPlacedEvent event) {
        // dispatch without blocking transaction
        notificationService.notifyCustomer(event.order());
        notificationService.notifyRestaurant(event.order());
        if (autoAssign) {
            assignmentService.autoAssign(event.order());
        }
    }
}
```

## Concurrency handling
Concurrency is controlled with pessimistic locking at the repository layer:

- Menu item inventory validation and reservation: `MenuItemRepository` query methods use `PESSIMISTIC_WRITE` locking so concurrent orders serialize on the same row during stock checks and updates. This prevents overselling when multiple customers attempt to buy the last unit.
- Order status transitions: assignment acceptance uses `PESSIMISTIC_WRITE` on the assignment row. Two partners racing to accept the same assignment will serialize at the database layer; the second transaction will see the updated status and fail fast.
- Event publishing: `OrderPlacedEvent` is published with `@TransactionalEventListener(phase = AFTER_COMMIT)` so listeners only run after the transaction commits. This avoids orphaned notifications when the transaction rolls back.
- Inventory validation happens under lock in `OrderServiceImpl.placeOrder`: menu items are loaded with pessimistic locks before stock checks and before creating `OrderItem` records. The stock deduction and order creation happen in the same transaction.

## Transaction management
- All state-changing service methods use `@Transactional` explicitly.
- Default propagation is `REQUIRED`; nested operations reuse the existing transaction boundary.
- Failure in payment, inventory, or event phases rolls back the outer transaction when configured appropriately; on rollback, inventory is restored to its pre-order value because the same transaction performs both stock read and deduct.
- Read-only operations are not wrapped in a transaction unless they cross multiple repositories and need repeatable-read guarantees.

## Asynchronous notifications
- Domain events are published after successful transaction commit.
- Notification creation is delegated to `NotificationService` and is invoked from event listeners.
- Controllers return immediately; notification dispatch proceeds asynchronously.
- This separates core business success/failure from downstream notification side effects.

## Testing strategy
- Unit tests use JUnit 5 and Mockito at the service interface layer. Success and failure paths are covered for each service contract.
- Integration tests use `@SpringBootTest` with the `test` profile, H2 in-memory database, and schema creation set to `create-drop`.
- Concurrency scenarios use `CyclicBarrier` in integration tests to simulate simultaneous requests and verify pessimistic-lock behavior.
- Ad-hoc static verification artifacts are generated when runtime build tooling is unavailable. Canonical Maven/JDK-based test execution is the source of truth for runtime validation.

## How to run the project

### Prerequisites
- JDK 21
- Maven 3.9+
- MySQL 8.x for production/testcontainers usage if desired

### Run application
```bash
./mvnw spring-boot:run
```
or
```bash
mvn spring-boot:run
```

### Run tests
```bash
mvn test
```

### Run only integration tests
```bash
mvn test -Dtest="*IT,*IntegrationTest,*IntegrationTests"
```

### Run a single integration test class
```bash
mvn test -Dtest=com.fooddelivery.integration.ConcurrencyIntegrationTest
```

### Build
```bash
mvn clean package
```

### Profiles
- Default: loads `application.properties` with MySQL datasource configuration.
- `test`: loads `application-test.properties` with H2 in-memory datasource, `create-drop` schema mode, and testing-oriented settings.
