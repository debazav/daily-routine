# 📅 Controle de Rotina Diária

Uma aplicação web completa para gerenciar rotinas diárias por dia da semana, desenvolvida com Node.js, TypeScript e SQLite.

## ✨ Funcionalidades

- **Página inicial** com os sete dias da semana
- **Adicionar rotinas** com título, horário de início e fim
- **Editar rotinas** existentes
- **Excluir rotinas** com confirmação
- **Validação de conflitos** de horário no mesmo dia
- **Interface responsiva** e moderna
- **Persistência de dados** com SQLite

## 🛠️ Tecnologias Utilizadas

- **Backend**: Node.js + Express + TypeScript
- **Frontend**: HTML + CSS + JavaScript vanilla
- **Banco de Dados**: SQLite
- **Estilização**: CSS moderno com gradientes e animações

## 📋 Pré-requisitos

- Node.js (versão 14 ou superior)
- npm ou yarn

## 🚀 Instalação e Execução

1. **Clone o repositório** (se aplicável) ou navegue até a pasta do projeto:
   ```bash
   cd daily-routine
   ```

2. **Instale as dependências**:
   ```bash
   npm install
   ```

3. **Compile o TypeScript**:
   ```bash
   npm run build
   ```

4. **Inicie o servidor**:
   ```bash
   npm start
   ```

5. **Acesse a aplicação**:
   Abra seu navegador e acesse: `http://localhost:3000`

## 🔧 Scripts Disponíveis

- `npm run build` - Compila o TypeScript para JavaScript
- `npm start` - Inicia o servidor em modo produção
- `npm run dev` - Inicia o servidor em modo desenvolvimento com ts-node
- `npm run watch` - Inicia o servidor com hot-reload usando nodemon

## 📁 Estrutura do Projeto

```
daily-routine/
├── src/
│   ├── types/
│   │   └── index.ts          # Definições de tipos TypeScript
│   ├── database/
│   │   └── database.ts       # Classe de gerenciamento do SQLite
│   └── server.ts             # Servidor Express principal
├── public/
│   ├── index.html            # Página inicial
│   ├── day.html              # Página de rotinas do dia
│   ├── styles.css            # Estilos CSS
│   └── js/
│       ├── index.js          # JavaScript da página inicial
│       └── day.js            # JavaScript da página de rotinas
├── package.json              # Dependências e scripts
├── tsconfig.json             # Configuração do TypeScript
└── README.md                 # Este arquivo
```

## 🎯 Como Usar

### 1. Página Inicial
- Acesse `http://localhost:3000`
- Visualize os sete dias da semana com contadores de rotinas
- Clique em qualquer dia para gerenciar suas rotinas

### 2. Gerenciando Rotinas
- **Adicionar**: Clique em "Adicionar Rotina" e preencha os campos
- **Editar**: Clique em "Editar" em qualquer rotina existente
- **Excluir**: Clique em "Excluir" e confirme a ação

### 3. Validações
- Todos os campos obrigatórios devem ser preenchidos
- Horário de início deve ser anterior ao horário de fim
- Não é possível criar rotinas com horários sobrepostos no mesmo dia

## 🔌 API Endpoints

### Rotinas
- `GET /api/routines/:dayOfWeek` - Lista rotinas de um dia
- `GET /api/routines/:dayOfWeek/:id` - Busca uma rotina específica
- `POST /api/routines` - Cria uma nova rotina
- `PUT /api/routines/:id` - Atualiza uma rotina existente
- `DELETE /api/routines/:id` - Exclui uma rotina

### Páginas
- `GET /` - Página inicial
- `GET /day/:dayOfWeek` - Página de rotinas de um dia específico

## 🎨 Características da Interface

- **Design responsivo** que funciona em desktop e mobile
- **Animações suaves** para melhor experiência do usuário
- **Feedback visual** para ações (sucesso, erro, loading)
- **Modais intuitivos** para adicionar/editar rotinas
- **Confirmação** antes de excluir rotinas
- **Validação em tempo real** de conflitos de horário

## 🗄️ Banco de Dados

O SQLite é usado para persistência local. O arquivo `database.sqlite` será criado automaticamente na primeira execução.

### Estrutura da Tabela
```sql
CREATE TABLE routines (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dayOfWeek TEXT NOT NULL,
    title TEXT NOT NULL,
    startTime TEXT NOT NULL,
    endTime TEXT NOT NULL,
    description TEXT
);
```

## 🔒 Validações Implementadas

1. **Campos obrigatórios**: Título, horário de início e fim
2. **Ordem dos horários**: Início deve ser anterior ao fim
3. **Conflitos de horário**: Não permite sobreposição no mesmo dia
4. **Formato de horário**: Validação de formato HH:MM
5. **Existência de rotina**: Verifica se a rotina existe antes de editar/excluir

## 🚀 Deploy

Para fazer deploy em produção:

1. Compile o projeto: `npm run build`
2. Configure variáveis de ambiente se necessário
3. Execute: `npm start`
4. Configure um proxy reverso (nginx, Apache) se necessário

## 🤝 Contribuindo

1. Faça um fork do projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo LICENSE para mais detalhes.

## 🐛 Problemas Conhecidos

- Os erros de linter do TypeScript são esperados devido à configuração relaxada para facilitar o desenvolvimento
- O banco SQLite é local, então os dados não são compartilhados entre diferentes instâncias

## 📞 Suporte

Se você encontrar algum problema ou tiver dúvidas, abra uma issue no repositório ou entre em contato. 