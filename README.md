# Hospital Appointment System

A microservices-based hospital appointment system built with Spring Boot.

## Project Structure

The project is organized as a monorepo with the following services:

1. **auth-service**: Handles user authentication and authorization
   - User registration and login
   - Role-based access control (DOCTOR, NURSE, PATIENT)
   - JWT token generation and validation

2. **appointment-service**: Manages appointment scheduling
   - Create, update, and cancel appointments
   - Integration with notification service via RabbitMQ
   - Role-based access to appointment operations

3. **notification-service**: Handles appointment notifications
   - Email notifications for appointments
   - RabbitMQ message processing
   - Notification templates

4. **history-service**: Manages patient medical history
   - GraphQL API for flexible data queries
   - Patient history management
   - Role-based access to medical records

## Prerequisites

- Java 17
- Maven 3.8+
- PostgreSQL
- RabbitMQ
- Docker (optional)

## Setup Instructions

1. Clone the repository:
```bash
git clone [repository-url]
cd hospital-appointment
```

2. Build the project:
```bash
mvn clean install
```

3. Start the services:
```bash
# Start auth-service
cd auth-service
mvn spring-boot:run

# Start appointment-service
cd ../appointment-service
mvn spring-boot:run

# Start notification-service
cd ../notification-service
mvn spring-boot:run

# Start history-service
cd ../history-service
mvn spring-boot:run
```

## Configuration

Each service has its own `application.yml` file where you can configure:
- Database connection
- RabbitMQ settings
- Service ports
- JWT settings
- Email configuration

## API Documentation

Each service exposes its own API endpoints:

- auth-service: `/api/auth/**`
- appointment-service: `/api/appointments/**`
- history-service: `/graphql`

## Security

The system implements:
- JWT-based authentication
- Role-based access control
- Secure password storage
- HTTPS support

## Development Guidelines

1. Follow the established package structure
2. Write unit tests for all new features
3. Use meaningful commit messages
4. Follow the coding standards
5. Document new features and changes

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request 