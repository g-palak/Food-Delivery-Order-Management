# Hermes — Project Notes for Food Delivery Order Management

This file records the project context, architectural decisions, and verification status
from the current session. It is a project knowledge doc, not a raw chat export.

## Session date
2026-07-14

## Project
Spring Boot 3 food delivery order management backend with:
- Java 21
- Spring Data JPA
- MySQL primary database (`application.properties`)
- H2 in-memory database under the `test` profile
- Maven build
- Spring Security with role-based access
- Async application-event notifications

## Architecture
Layered monolith with three core layers:
- `controller` — HTTP contracts, request validation, security checks
- `service` + `service/impl` — business logic and transaction boundaries
- `repository` — Spring Data JPA access

DTOs separate HTTP contracts from persistence:
- request records use Bean Validation
- response records are returned from controllers; entities are never exposed

## Domain
User polymorphisms: ADMIN, RESTAURANT_OWNER, CUSTOMER, DELIVERY_PARTNER
Core flows:
- City management
- Restaurant and menu item management
- Order placement and lifecycle transitions
- Assignment and delivery workflow
- Payment simulation
- Review and rating
- Notifications via application events

## Security
RBAC is enforced with `@PreAuthorize` on most controllers.
`GlobalExceptionHandler` maps access, validation, and domain errors to HTTP responses.

## Persistence
Production persistence is MySQL with Hibernate.
Tests use H2 under Spring's `test` profile with `create-drop` DDL.

## Key design decisions captured in this session
- No out-of-scope systems were introduced: no OAuth/JWT, no Kafka/Redis, no Docker/containers.
- Pessimistic locking is used for `MenuItem` stock and `Assignment` acceptance.
- Order state machine transitions are validated centrally in service layer helpers.
- Notification listener uses after-commit semantics so side effects only run on committed order placement.
- Explicit `ResourceNotFoundException` usage instead of generic `RuntimeException` for missing entities.

## Outstanding tasks
- Domain-backed `UserDetailsService` to replace in-memory principals
- Controller auth wiring should pass real authenticated user IDs into service methods
- Core-flow integration tests under Spring Boot/H2 context for:
  - successful order placement
  - order placement with insufficient stock
  - restaurant accepting an order
  - delivery partner accepting an assignment
  - review creation after delivery
- Mapper extraction to reduce entity->DTO duplication
- Address the seven issue areas from the architecture review:
  1. Transaction coverage gaps
  2. REST API contract cleanup
  3. Concurrency validation ordering
  4. Validation depth
  5. Identity/authorization wiring
  6. Duplication in responses/mapping
  7. State-machine centralization

## Verification status as of session write-up
- Maven tooling in this workspace does not start (`mvn -version` fails with Plexus launcher class error), so canonical compile/test execution is not possible here.
- Static content verification artifacts exist in temp for key edits; label those as ad-hoc only.
- Service layer unit tests are present but not executed in this workspace.
