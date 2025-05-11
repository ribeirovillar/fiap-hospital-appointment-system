# Hospital Appointment System

Sistema de agendamento hospitalar desenvolvido como parte do Tech Challenge 3 da Pós-Graduação em Arquitetura e Desenvolvimento Java da FIAP.

## Arquitetura

O sistema é composto por quatro microserviços independentes, cada um com sua própria base de dados e responsabilidades específicas.

### Auth Service
- **Responsabilidade**: Autenticação e autorização centralizada
- **Funcionalidades**:
  - Cadastro de usuários (médicos, enfermeiros e pacientes)
  - Login com geração de JWT Token
  - Validação de tokens via gRPC para outros serviços
  - Controle de acesso baseado em roles (DOCTOR, NURSE, PATIENT)
- **Tecnologias**:
  - Spring Boot 3.2
  - Spring Security com JWT
  - gRPC
  - PostgreSQL
  - Flyway

### Outros Serviços (Em desenvolvimento)
- **Appointment Service**: Gerenciamento de agendamentos
- **Notification Service**: Sistema de notificações
- **History Service**: Histórico médico

## Tecnologias Principais

- Java 17
- Spring Boot 3.2
- Spring Security
- JWT
- gRPC
- PostgreSQL
- RabbitMQ
- Docker
- Maven

## Configuração do Ambiente

### Pré-requisitos
- Java 17
- Maven 3.8+
- Docker e Docker Compose
- PostgreSQL (ou usar o container Docker)

### Executando o Projeto

1. Clone o repositório:
```bash
git clone [repository-url]
cd fiap-hospital-appointment-system
```

2. Inicie os serviços de infraestrutura:
```bash
docker-compose up -d
```

3. Execute o auth-service:
```bash
cd auth-service
mvn spring-boot:run
```

## Documentação da API

O auth-service expõe a documentação Swagger em:
- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI Spec: http://localhost:8081/api-docs

### Endpoints REST

- `POST /api/auth/register`: Cadastro de novos usuários
- `POST /api/auth/login`: Autenticação e geração de token JWT

### Serviço gRPC

O auth-service expõe um serviço gRPC na porta 9090 para validação de tokens. Este serviço será utilizado pelos outros microserviços para implementar controle de acesso.

Para testar o serviço gRPC, você pode usar o Postman:
1. Crie uma nova requisição gRPC
2. Configure o endpoint: `localhost:9090`
3. Use o método `ValidateTokenAndGetRole`
4. Envie o token JWT no campo `token`

## Estrutura do Projeto

```
fiap-hospital-appointment-system/
├── auth-service/           # Serviço de autenticação
├── appointment-service/    # Serviço de agendamentos (em desenvolvimento)
├── notification-service/   # Serviço de notificações (em desenvolvimento)
├── history-service/        # Serviço de histórico (em desenvolvimento)
├── docker-compose.yml      # Configuração dos containers
└── pom.xml                 # POM pai do projeto
```

## Autor

Demóstenis Villar - Tech Challenge 3 - FIAP Pós-Graduação em Arquitetura e Desenvolvimento Java 