# ClientDome - KMKM Identity Verification System

## Overview

ClientDome is the main web application for the **KMKM (Know-Me-Know-Me)** identity verification system. It provides a user-friendly interface for KYC (Know Your Customer) document processing with AI-powered chat assistance.

## Features

- **🔐 Secure Authentication**: OAuth2 integration with Descope for user authentication
- **🤖 AI Chat Assistant**: Google Gemini-powered chat bot to guide users through the KYC process
- **📄 Document Upload**: Support for uploading identity documents (Aadhaar, PAN, 10th grade marksheet)
- **🔄 Document Processing**: Orchestrates multiple microservices for document extraction and validation
- **💾 File Management**: Secure temporary file storage for uploaded documents
- **📊 Results Dashboard**: Display processed KYC verification results

## Architecture

This application follows a microservices architecture where ClientDome acts as the main orchestrator:

```
ClientDome (Port 8011) - Main UI & Orchestration
├── AadhaarDome (Port 8012) - Aadhaar card data extraction
├── PanDome (Port 8013) - PAN card data extraction  
├── MarksheetDome (Port 8014) - Marksheet data extraction
└── ValidatorDome (Port 8016) - Cross-document validation
```

## Prerequisites

- **Java 21** or higher
- **Maven 3.6+**
- **Google Gemini API Key**
- **Descope Account** (for OAuth2 authentication)
- Access to specialist "dome" microservices

## Configuration

### Environment Variables

Set the following environment variable:
```bash
export GOOGLE_API_KEY=your_gemini_api_key_here
```

### Application Properties

Update `src/main/resources/application.properties` with your credentials:

```properties
# Descope OAuth2 Configuration
descope.project-id=<YOUR-PROJECT-ID>
spring.security.oauth2.client.registration.descope-m2m.client-id=<YOUR-CLIENT-ID>
spring.security.oauth2.client.registration.descope-m2m.client-secret=<YOUR-CLIENT-SECRET>
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://api.descope.com/v1/apps/<YOUR-PROJECT-ID>

# Specialist Service URLs (update if deployed elsewhere)
service.url.aadhardome=http://localhost:8012/extract
service.url.pandome=http://localhost:8013/extract
service.url.sheetdome=http://localhost:8014/extract
service.url.validatordome=http://localhost:8016/validate
```

## Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Siddhu2502/clientDome.git
   cd clientDome
   ```

2. **Install dependencies**
   ```bash
   mvn clean install
   ```

3. **Configure environment**
   - Set up your Google Gemini API key
   - Configure Descope OAuth2 credentials
   - Ensure specialist dome services are running

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8011`

## API Endpoints

### Authentication
- `GET /` - Home page (requires authentication)
- `GET /result` - Results page

### Chat Interface
- `POST /api/chat` - Send message to AI chat assistant
  - Request: `{"message": "user message"}`
  - Response: `{"response": "ai response"}`

### Document Upload
- `POST /api/upload` - Upload identity documents
  - Parameters: `file` (MultipartFile), `docType` (string)
  - Supported document types: `aadhar`, `pan`, `marksheet`

### KYC Processing
- `POST /api/process-kyc` - Process uploaded documents through KYC pipeline
  - Orchestrates parallel document extraction and validation

## Key Components

### Controllers
- **HomeController**: Handles main pages and user session management
- **ChatController**: Manages AI chat interactions and document processing
- **LoginSignupController**: Handles authentication flows

### Services
- **ChatService**: Google Gemini integration for AI chat functionality
- **KycOrchestrationService**: Coordinates multiple microservices for document processing
- **FileStorageService**: Manages temporary file storage for uploaded documents
- **DescopeTokenService**: Handles OAuth2 token management

### Configuration
- **OAuth2ClientConfig**: OAuth2 and JWT authentication setup
- **GeminiConfig**: Google Gemini AI client configuration
- **WebClientConfig**: HTTP client configuration for microservice communication
- **SecurityConfig**: Spring Security configuration

## Dependencies

Key dependencies include:
- **Spring Boot 3.5.5**: Web framework and auto-configuration
- **Spring Security**: Authentication and authorization
- **Spring OAuth2 Client**: OAuth2 integration
- **Thymeleaf**: Server-side templating engine
- **Spring WebFlux**: Reactive web client for microservice communication
- **Descope Java SDK**: Authentication service integration
- **Google Gemini AI**: AI chat functionality

## Development

### Running in Development Mode
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Building for Production
```bash
mvn clean package
java -jar target/clientdome-0.0.1-SNAPSHOT.jar
```

## File Structure

```
src/
├── main/
│   ├── java/com/kmkm/clientdome/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST and web controllers
│   │   ├── dto/            # Data transfer objects
│   │   ├── service/        # Business logic services
│   │   └── ClientDomeApplication.java
│   └── resources/
│       ├── static/         # CSS, JavaScript files
│       ├── templates/      # Thymeleaf HTML templates
│       └── application.properties
└── test/
    └── java/               # Unit tests
```

## Troubleshooting

### Common Issues

1. **Java Version Error**: Ensure you're using Java 21 or higher
2. **Authentication Failed**: Verify Descope credentials in application.properties
3. **Gemini API Error**: Check your Google API key environment variable
4. **Service Unavailable**: Ensure all specialist dome services are running

### Logs
Application logs are available in the console output. For debugging, enable debug logging:
```properties
logging.level.com.kmkm.clientdome=DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

[Add your license information here]

## Support

For issues and questions, please create an issue in the GitHub repository.