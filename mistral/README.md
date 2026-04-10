# Certificate Alert Service

A Spring Boot application for monitoring and alerting on TLS certificate expirations.

## Features

- **User Management**: Role-based access control with groups (ADMIN, MANAGER, USER)
- **Certificate Management**: Upload certificates from files or fetch from URLs
- **Expiration Monitoring**: Configurable thresholds for expiration alerts
- **Email Notifications**: Automatic email alerts when certificates are about to expire
- **REST API**: Full RESTful API for integration
- **Web Interface**: Simple web UI for managing certificates and alerts

## Technical Stack

- **Backend**: Spring Boot 3.2.0
- **Security**: Spring Security with OAuth2
- **Database**: H2 (in-memory) with JPA/Hibernate
- **Frontend**: Bootstrap 5 with vanilla JavaScript
- **Build**: Maven
- **Java**: 17

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+

### Running the Application

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Accessing the Application

- **Web UI**: `http://localhost:8080/`
- **H2 Console**: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:certalertdb`)
- **API Documentation**: Available via SpringDoc (if added)

### Default Credentials

The application uses OAuth2 for authentication. In a development environment, you can use:

- Username: `admin`
- Password: `password`
- Role: `ADMIN`

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/refresh` - Refresh JWT token

### Users (Admin only)
- `GET /api/users` - List all users
- `POST /api/users` - Create new user
- `GET /api/users/{id}` - Get user details
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Groups
- `GET /api/groups` - List all groups
- `POST /api/groups` - Create new group (Admin only)
- `GET /api/groups/{id}` - Get group details
- `PUT /api/groups/{id}` - Update group (Admin only)
- `DELETE /api/groups/{id}` - Delete group (Admin only)

### Certificates
- `POST /api/certificates/upload` - Upload certificate file
- `POST /api/certificates/fetch` - Fetch certificate from URL
- `GET /api/certificates` - List certificates by group
- `GET /api/certificates/expiring` - List expiring certificates

### Alerts
- `GET /api/alerts/config` - Get alert configuration
- `PUT /api/alerts/config` - Update alert configuration

## Configuration

Application configuration is in `src/main/resources/application.properties`:

```properties
# Server port
server.port=8080

# Database (H2 in-memory by default)
spring.datasource.url=jdbc:h2:mem:certalertdb

# Security
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080

# Email settings
spring.mail.host=smtp.example.com
spring.mail.port=587
spring.mail.username=alerts@example.com
spring.mail.password=password

# Alert threshold (days)
certalert.expiration.threshold=30
```

## Security

The application uses role-based access control:

- **ADMIN**: Full access to all features
- **MANAGER**: Can upload certificates and manage alerts for their group
- **USER**: Can view certificates and alerts for their group

## Scheduling

The application includes a scheduled job that runs daily at 9 AM to check for expiring certificates and send email alerts.

## Frontend

The frontend is a simple SPA using Bootstrap 5 and vanilla JavaScript:

- `index.html` - Main certificate management page
- `alerts.html` - Alert dashboard
- `admin.html` - Admin interface (user/group management)
- `login.html` - Login page

## Deployment

For production deployment:

1. Configure a proper database (PostgreSQL, MySQL, etc.)
2. Set up proper SMTP settings for email alerts
3. Configure OAuth2 with a proper authorization server
4. Set up proper logging and monitoring
5. Configure HTTPS

## Building for Production

```bash
mvn clean package
java -jar target/certificate-alert-service-1.0-SNAPSHOT.jar
```

## Future Enhancements

- Add proper OAuth2 authorization server
- Implement certificate renewal workflow
- Add more detailed certificate information display
- Implement certificate search and filtering
- Add export functionality (CSV, PDF)
- Implement webhook notifications
- Add audit logging
- Implement certificate versioning

## License

This project is licensed under the MIT License.