# Plano de testes — Cafetech API

## Estratégia

Dois níveis, priorizando o que costuma quebrar em APIs de cadastro:

1. **Unitários (JUnit 5 + Mockito)** — todas as regras de negócio dos services, sem banco e
   sem contexto do Spring. Rápidos, rodam a cada build. Repositórios são mocks (`@Mock`);
   o service é injetado com `@InjectMocks`. Suíte-alvo: `./mvnw test`.
2. **Integrados (manuais, profil dev com H2)** — smoke test de cada endpoint via `curl`,
   conferindo código HTTP, corpo JSON e persistência (revisitar recursos criados).

## Escopo dos testes unitários

### ClienteService (8 testes)

| ID | Cenário | Esperado |
|---|---|---|
| CLI-01 | Criar cliente com dados válidos | Salva e retorna cliente com nome/e-mail informados |
| CLI-02 | Criar com e-mail já existente (case-insensitive) | `RegraDeNegocioException` (409), sem chamar `save` |
| CLI-03 | Buscar por id existente | Retorna o cliente |
| CLI-04 | Buscar por id inexistente | `RecursoNaoEncontradoException` (404) |
| CLI-05 | Atualizar cliente | Campos alterados |
| CLI-06 | Atualizar com e-mail de outro cliente | `RegraDeNegocioException` (409) |
| CLI-07 | Excluir cliente sem máquinas | `delete` chamado |
| CLI-08 | Excluir cliente com máquinas | `RegraDeNegocioException` (409), sem `delete` |

### MaquinaService (6 testes)

| ID | Cenário | Esperado |
|---|---|---|
| MAQ-01 | Criar máquina sem status informado | Status padrão `ATIVA`, cliente vinculado |
| MAQ-02 | Criar máquina com status `INATIVA` | Status persistido |
| MAQ-03 | Criar para cliente inexistente | `RecursoNaoEncontradoException` (404) |
| MAQ-04 | Criar com número de série duplicado | `RegraDeNegocioException` (409) |
| MAQ-05 | Excluir máquina sem manutenção aberta | `delete` chamado |
| MAQ-06 | Excluir máquina com manutenção aberta | `RegraDeNegocioException` (409) |

### ManutencaoService (9 testes)

| ID | Cenário | Esperado |
|---|---|---|
| MAN-01 | Abrir manutenção em máquina `ATIVA` | Status `ABERTA`, data de hoje, máquina → `EM_MANUTENCAO` |
| MAN-02 | Abrir para máquina inexistente | `RecursoNaoEncontradoException` (404) |
| MAN-03 | Abrir para máquina `INATIVA` | `RegraDeNegocioException` (409) |
| MAN-04 | Abrir quando já existe manutenção em aberto | `RegraDeNegocioException` (409) |
| MAN-05 | Iniciar manutenção `ABERTA` | Status → `EM_ANDAMENTO` |
| MAN-06 | Iniciar manutenção que não está `ABERTA` | `RegraDeNegocioException` (409) |
| MAN-07 | Concluir manutenção | `CONCLUIDA` + data de conclusão; máquina sem outra manutenção → `ATIVA` |
| MAN-08 | Concluir com data anterior à abertura | `RegraDeNegocioException` (409) |
| MAN-09 | Concluir manutenção já `CONCLUIDA` | `RegraDeNegocioException` (409) |

## Plano de testes integrados (smoke, perfil dev)

| ID | Requisição | Esperado |
|---|---|---|
| INT-01 | POST `/clientes` válido | 201 + Location + corpo |
| INT-02 | POST `/clientes` sem nome | 400 com lista `campo/mensagem` |
| INT-03 | GET `/clientes/1` após criação | 200 com dados persistidos |
| INT-04 | POST `/maquinas` com clienteId inexistente | 404 |
| INT-05 | POST `/manutencoes` em máquina ATIVA | 201; GET máquina mostra `EM_MANUTENCAO` |
| INT-06 | PUT `/manutencoes/conclusao` | 200; GET máquina mostra `ATIVA` |
| INT-07 | DELETE cliente com máquinas | 400 com mensagem de bloqueio |
| INT-08 | GET rota inexistente | 404 |

## Critérios de aceite

- `mvnw test` verde, 23/23 unitários.
- Suíte roda sem banco externo (H2 em memória via perfil dev, só para o teste de contexto).
- Toda mensagem de erro é verificada por `contains` na mensagem — sem acoplamento a texto exato.
