# Diário de Viagens

Aplicativo Android para cadastro e gerenciamento de viagens, desenvolvido em Kotlin utilizando SQLite para armazenamento local.

## Funcionalidades

### ✅ Tela de Login
- Autenticação local com validação de campos
- Cadastro de novos usuários
- Usuário padrão: `admin` / `123456`

### ✅ Tela Principal
- Listagem de viagens usando RecyclerView
- Filtro por país (com busca e seleção, sem erros de digitação)
- Interface com Material Design

### ✅ Funcionalidades CRUD
- **Adicionar viagens**: Formulário completo com seleção de país, comentário e nota (0 a 10)
- **Editar viagens**: Botão de edição em cada item
- **Excluir viagens**: Botão de exclusão com confirmação
- **Visualizar viagens**: Clique no item para ver detalhes completos

### ✅ Locais Visitados
- Cada viagem pode conter múltiplos locais visitados
- Adicionar, editar e remover locais visitados (nome, data, comentário)
- Interface dedicada para cadastro/edição de locais
- Visualização dos locais na tela de detalhes da viagem

### ✅ Banco de Dados Local
- SQLite com 3 tabelas relacionadas:
  - `users`: Usuários do sistema
  - `trips`: Viagens cadastradas
  - `places`: Locais visitados (relacionados à viagem)

### ✅ Validações
- Campos obrigatórios
- Validação de formato de datas
- Verificação de usuário existente
- Busca de país por lista (evita erro de digitação)

## Estrutura do Projeto

```
app/src/main/java/com/trabalhofinal/diariodeviagens/
├── data/
│   ├── TravelDbHelper.kt          # Helper do banco SQLite
│   └── entity/
│       ├── User.kt                # Entidade de usuário
│       ├── Trip.kt                # Entidade de viagem
│       └── Place.kt               # Entidade de local
├── ui/
│   ├── LoginActivity.kt           # Tela de login
│   ├── MainActivity.kt            # Tela principal
│   └── adapter/
│       └── TripAdapter.kt         # Adapter do RecyclerView
└── res/
    └── layout/
        ├── activity_login.xml           # Layout da tela de login
        ├── activity_main.xml            # Layout da tela principal
        ├── item_trip.xml                # Layout do item da lista
        ├── dialog_add_edit_trip.xml     # Layout do diálogo de viagem
        ├── dialog_add_edit_place.xml    # Layout do diálogo de local visitado
        ├── dialog_trip_details.xml      # Layout de detalhes da viagem
```

## Tecnologias Utilizadas

- **Kotlin**: Linguagem principal
- **SQLite**: Banco de dados local
- **RecyclerView**: Lista de viagens
- **Material Design**: Interface
- **ViewBinding**: Binding de views
- **DatePickerDialog**: Seleção de datas

## Como Executar

1. Clone o repositório:
   ```sh
   https://github.com/MarceloAssun/Diario-de-Viagens.git
   ```
2. Abra o projeto no Android Studio
3. Sincronize o Gradle
4. Execute em um dispositivo ou emulador

## Usuário Padrão

Para facilitar os testes, o aplicativo cria automaticamente um usuário padrão:
- **Usuário**: `admin`
- **Senha**: `123456`

## Funcionalidades Implementadas

### ✅ Requisitos Mínimos Atendidos

1. **Tela de Login** ✅
   - Autenticação local
   - Validação de campos
   - Cadastro de usuários

2. **Tela Principal** ✅
   - Listagem com RecyclerView
   - Dados salvos no banco

3. **Adição de Itens** ✅
   - Formulário completo
   - Validação de campos

4. **Edição e Exclusão** ✅
   - Botões nos itens
   - Confirmação de exclusão

5. **Banco de Dados Local** ✅
   - SQLite com 3 tabelas
   - Relacionamentos configurados

6. **Código Comentado** ✅
   - Documentação completa
   - Explicações das operações

### 🎯 Funcionalidades Extras

- **Filtro por país**: Visualização organizada e sem erros de digitação
- **Interface**: Material Design 3
- **Validações robustas**: Campos obrigatórios
- **Locais visitados**: Múltiplos locais por viagem, com edição e remoção
- **UX aprimorada**: Feedback visual e mensagens

**Desenvolvido para a disciplina de Dispositivos Móveis** 
