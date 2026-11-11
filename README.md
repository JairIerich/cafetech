# Cafetech — API REST de clientes, máquinas e controle de manutenção

API REST em **Spring Boot 3** para uma empresa que instala e faz manutenção de máquinas de
café em cafeterias, restaurantes e escritórios. Inclui front-end próprio (HTML/CSS/JS
servido pelo Spring Boot) e banco relacional (MySQL/PostgreSQL via Docker ou H2 em memória
para desenvolvimento).

## O problema que ela resolve

Numa empresa de serviços assim, a informação vive espalhada: quem é o cliente, que máquina
está instalada nele, o que precisa ser consertado e o que já foi consertado. A Cafetech
centraliza esse cadastro e aplica regras de negócio no backend:

- cada cliente tem **e-mail único** (case-insensitive);
- cada máquina tem **número de série único** e pertence a um cliente cadastrado;
- uma máquina só tem **uma manutenção em aberto por vez**;
- abrir manutenção marca a máquina como `EM_MANUTENCAO`; concluir devolve para `ATIVA`;
- máquina `INATIVA` não recebe manutenção;
- não é possível excluir cliente que tem máquinas, nem máquina com manutenção em aberto —
  excluir máquina apaga junto o histórico de manutenções concluídas.

## Stack

- Java 17 + Spring Boot 3.5 (Web, Data JPA, Validation, Lombok)
- JPA/Hibernate — MySQL 8 e PostgreSQL 16 (Docker), H2 em memória (dev)
- Testes: JUnit 5 + Mockito (padrão BDDMockito)
- Front-end: HTML/CSS/JS vanilla servido em `http://localhost:8080/index.html`

## Como rodar

### 1. Rápido — sem banco instalado (H2 em memória)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### 2. Com banco de verdade (MySQL ou PostgreSQL)

```bash
docker compose up -d mysql      # MySQL na porta 3307
# ou
docker compose up -d postgres   # PostgreSQL na porta 5433

./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
# ou
./mvnw spring-boot:run -Dspring-boot.run.profiles=postgres
```

As credenciais podem ser sobrescritas com variáveis de ambiente:
`CAFETECH_DB_URL`, `CAFETECH_DB_USERNAME`, `CAFETECH_DB_PASSWORD`.

Front-end: abra `http://localhost:8080/index.html` no navegador.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/clientes` | Lista clientes paginado |
| GET | `/clientes/{id}` | Detalha um cliente |
| POST | `/clientes` | Cadastra cliente (201 + Location) |
| PUT | `/clientes` | Atualiza cliente (id no corpo) |
| DELETE | `/clientes/{id}` | Exclui cliente (bloqueado se tiver máquinas) |
| GET | `/maquinas?clienteId=&status=` | Lista máquinas com filtros |
| GET | `/maquinas/{id}` | Detalha uma máquina |
| POST | `/maquinas` | Cadastra máquina para um cliente |
| PUT | `/maquinas` | Atualiza máquina (id no corpo) |
| DELETE | `/maquinas/{id}` | Exclui máquina (bloqueada com manutenção em aberto) |
| GET | `/manutencoes?maquinaId=&status=` | Lista manutenções com filtros |
| GET | `/manutencoes/{id}` | Detalha uma manutenção |
| POST | `/manutencoes` | Abre manutenção (201 + Location) |
| PUT | `/manutencoes/andamento` | Move para `EM_ANDAMENTO` |
| PUT | `/manutencoes/conclusao` | Conclui manutenção e reativa a máquina |

Listagens retornam no formato de página do Spring Data:
`{"content": [...], "totalElements": N, "pageable": {...}}`.

## Exemplos de requisições

```bash
# Cadastrar cliente
curl -X POST http://localhost:8080/clientes \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Silva","email":"maria@cafe.com","telefone":"11999990000","endereco":"Rua das Flores, 100"}'

# Cadastrar máquina para o cliente 1
curl -X POST http://localhost:8080/maquinas \
  -H "Content-Type: application/json" \
  -d '{"modelo":"Coffee Master 3000","numeroSerie":"CMX-0001","dataAquisicao":"2025-03-10","clienteId":1}'

# Abrir manutenção corretiva na máquina 1
curl -X POST http://localhost:8080/manutencoes \
  -H "Content-Type: application/json" \
  -d '{"maquinaId":1,"descricao":"Bombas com vazamento","tipo":"CORRETIVA","tecnico":"Carlos"}'

# Iniciar e concluir a manutenção 1
curl -X PUT http://localhost:8080/manutencoes/andamento \
  -H "Content-Type: application/json" \
  -d '{"id":1}'
curl -X PUT http://localhost:8080/manutencoes/conclusao \
  -H "Content-Type: application/json" \
  -d '{"id":1}'

# Listar máquinas ativas
curl http://localhost:8080/maquinas?status=ATIVA
```

## Tratamento de erros

| Situação | Resposta |
|---|---|
| Recurso inexistente | `404` vazio |
| Regra de negócio (duplicado, estado inválido) | `400` com a mensagem: `"E-mail ja cadastrado para outro cliente!"` |
| Validação de campos | `400` com lista de erros por campo |
| Erro interno | `500` com JSON padrão do Spring |

```json
// POST /clientes com e-mail duplicado -> 400
"E-mail ja cadastrado para outro cliente!"

// POST /clientes sem nome -> 400
[{"campo":"nome","mensagem":"não deve estar em branco"}]
```

## Estrutura do projeto

```
src/main/java/com/cafetech/api
├── controller/        # ClienteController, MaquinaController, ManutencaoController
├── service/           # regras de negócio (ClienteService, MaquinaService, ManutencaoService)
├── domain/
│   ├── cliente/       # Cliente, DTOs (DadosCadastro/Atualizacao/Listagem/Detalhamento), repository
│   ├── maquina/       # Maquina, StatusMaquina, DTOs, repository
│   └── manutencao/    # Manutencao, TipoManutencao, StatusManutencao, DTOs, repository
├── exception/         # ValidacaoException (regras de negócio)
└── infra/exception/   # TratadorDeErros (@RestControllerAdvice)

src/main/resources
├── static/index.html  # front-end (HTML/CSS/JS vanilla)
│   └── css/styles.css
└── application-{dev,mysql,postgres}.properties

docker-compose.yml    # MySQL (porta 3307) e PostgreSQL (porta 5433)
```

## Testes

```bash
./mvnw test
```

23 testes unitários (JUnit 5 + Mockito) cobrindo os três services: duplicidade de e-mail e
número de série, dependências bloqueantes de exclusão, ciclo completo de manutenção
(ABERTA → EM_ANDAMENTO → CONCLUIDA com reativação da máquina) e validação de datas.
O plano de testes está em `docs/plano-testes.md`.
