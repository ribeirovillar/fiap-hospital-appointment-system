# Auth Service

Serviço de autenticação e autorização do sistema de agendamento hospitalar.

## Funcionalidades

- Cadastro de usuários (médicos, enfermeiros e pacientes)
- Login com geração de JWT Token
- Validação de tokens via gRPC para outros serviços
- Controle de acesso baseado em roles (DOCTOR, NURSE, PATIENT)

## Tecnologias

- Java 17
- Spring Boot 3.2
- Spring Security com JWT
- gRPC
- PostgreSQL
- Flyway
- MapStruct
- Lombok

## Configuração

### Pré-requisitos
- Java 17
- Maven 3.8+
- PostgreSQL (ou usar o container Docker)
- Postman (para testar os endpoints)

### Variáveis de Ambiente
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hospital_auth
    username: postgres
    password: postgres

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 horas

grpc:
  server:
    port: 9090
```

### Executando o Projeto
```bash
# Compilar
mvn clean install

# Executar
mvn spring-boot:run
```

## Documentação da API

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI Spec: http://localhost:8081/api-docs

### Testando a API

#### Usando Postman

1. Importe a coleção do Postman:
   - Abra o Postman
   - Clique em "Import"
   - Selecione o arquivo `postman/hospital-auth-service.postman_collection.json`

2. Endpoints REST:
   - `POST /api/auth/register`: Cadastro de novos usuários
   - `POST /api/auth/login`: Autenticação e geração de token JWT

3. Endpoint gRPC:
   - Configure o ambiente gRPC no Postman:
     1. Clique em "New" > "gRPC Request"
     2. Configure o endpoint: `localhost:9090`
     3. Importe o arquivo `auth.proto` do diretório `src/main/proto`
     4. Selecione o método `ValidateTokenAndGetRole`
     5. Use a variável `{{jwt_token}}` no campo `token` (será preenchida automaticamente após o login)

#### Exemplo de Fluxo de Teste

1. Registre um novo usuário usando o endpoint `/register`
2. Faça login usando o endpoint `/login` para obter o token JWT
3. Use o token obtido para testar o endpoint gRPC de validação

## Estrutura do Projeto

```
auth-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/fiap/hospital/auth/
│   │   │       ├── domain/          # Entidades e regras de negócio
│   │   │       ├── application/     # Casos de uso e serviços
│   │   │       └── infrastructure/  # Adaptadores e configurações
│   │   └── resources/
│   └── test/
├── postman/                         # Coleção do Postman
└── pom.xml
```

## Arquitetura Hexagonal (Ports and Adapters)

O serviço utiliza a Arquitetura Hexagonal, que divide a aplicação em três camadas principais:

### 1. Domínio (Core)
- Contém as regras de negócio centrais
- Independente de frameworks e tecnologias
- Localização: `domain/`
  - `entities/`: Entidades de domínio (ex: User)
  - `enums/`: Enumerações (ex: UserRole)
  - `ports/`: Interfaces do domínio
    - `auth/`: Ports de autenticação
      - `AuthPort`: Interface para autenticação
    - `persistence/`: Ports de persistência
      - `UserRepositoryPort`: Interface para persistência de usuários

### 2. Aplicação
- Orquestra o fluxo entre infraestrutura e domínio
- Contém casos de uso e adaptadores de entrada
- Localização: `application/`
  - `adapters/`: Adaptadores de entrada
    - `rest/`: Adaptadores REST
      - `AuthController`: Controller REST para autenticação
    - `service/`: Adaptadores de serviço
      - `AuthServiceAdapter`: Implementação do AuthPort
  - `dto/`: Objetos de transferência de dados
  - `exception/`: Tratamento de exceções

### 3. Infraestrutura (Adapters)
- Implementa as interfaces definidas no domínio
- Contém adaptadores de saída
- Localização: `infrastructure/`
  - `adapters/`: Adaptadores de saída
    - `grpc/`: Adaptadores gRPC
      - `AuthGrpcAdapter`: Implementação do serviço gRPC
    - `persistence/`: Adaptadores de persistência
      - `UserRepositoryAdapter`: Implementação do UserRepositoryPort
    - `security/`: Adaptadores de segurança
      - `JwtTokenAdapter`: Serviço de geração e validação de tokens
      - `UserDetailsAdapter`: Adaptador para Spring Security
  - `config/`: Configurações da aplicação
  - `filters/`: Filtros (ex: JwtAuthenticationFilter)

## Testes

```bash
# Executar todos os testes
mvn test

# Executar testes com cobertura
mvn verify
```

## Integração com Outros Serviços

O Auth Service fornece endpoints gRPC para outros serviços validarem tokens e verificarem permissões:

```protobuf
service AuthService {
  rpc validateTokenAndGetRole(TokenValidationRequest) returns (TokenValidationResponse);
}
```

## Segurança

- Autenticação via JWT
- Senhas criptografadas com BCrypt
- CORS configurado
- Validação de tokens
- Diferentes níveis de acesso por role

## Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes. 