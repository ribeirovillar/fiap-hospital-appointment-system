# Auth Service

## Descrição
O Auth Service é um microserviço responsável pela autenticação e autorização do sistema de agendamento hospitalar. Ele gerencia usuários, autenticação via JWT e fornece endpoints gRPC para validação de tokens e verificação de permissões.

## Tecnologias Utilizadas

### Backend
- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway (Migrações de banco de dados)
- JWT (JSON Web Tokens)
- gRPC
- MapStruct
- Lombok
- OpenAPI/Swagger

### Testes
- JUnit 5
- Spring Test
- H2 Database (para testes)

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

## Funcionalidades Principais

1. **Autenticação**
   - Login com username/password
   - Geração de JWT
   - Validação de tokens

2. **Gerenciamento de Usuários**
   - Registro de novos usuários
   - Diferentes níveis de acesso (DOCTOR, NURSE, PATIENT)

3. **API gRPC**
   - Validação de tokens
   - Verificação de permissões
   - Integração com outros microserviços

## Configuração

### Pré-requisitos
- Java 17
- Maven
- PostgreSQL

### Variáveis de Ambiente
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hospital_auth
    username: postgres
    password: postgres

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24 horas em milissegundos

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

A documentação da API está disponível via Swagger UI:
- URL: `http://localhost:8081/swagger-ui.html`
- API Docs: `http://localhost:8081/api-docs`

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