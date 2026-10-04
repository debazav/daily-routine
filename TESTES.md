# 🧪 Testes da API - Daily Routine

Este documento descreve a estrutura de testes implementada para a aplicação Daily Routine.

## 📊 Cobertura de Testes

- **Total de Testes**: 38 testes
- **Cobertura de Código**: 76.19%
- **Cobertura de Funções**: 86.48%
- **Cobertura de Branches**: 60.86%

## 🏗️ Estrutura dos Testes

```
src/__tests__/
├── setup.ts           # Configuração global dos testes
├── api.test.ts        # Testes da API REST
└── database.test.ts   # Testes do banco de dados
```

## 🚀 Como Executar os Testes

### Executar todos os testes
```bash
npm test
```

### Executar testes em modo watch
```bash
npm run test:watch
```

### Executar testes com cobertura
```bash
npm run test:coverage
```

## 📋 Testes da API (`api.test.ts`)

### Endpoints Testados

#### GET `/api/routines/:dayOfWeek`
- ✅ Retorna array vazio para dia sem rotinas
- ✅ Retorna rotinas de um dia específico
- ✅ Retorna apenas rotinas do dia solicitado

#### GET `/api/routines/:dayOfWeek/:id`
- ✅ Retorna 404 para rotina inexistente
- ✅ Retorna rotina específica por ID

#### POST `/api/routines`
- ✅ Cria nova rotina com sucesso
- ✅ Retorna 400 para campos obrigatórios faltando
- ✅ Retorna 409 para conflito de horário
- ✅ Valida que horário de início é anterior ao de fim

#### PUT `/api/routines/:id`
- ✅ Atualiza rotina existente com sucesso
- ✅ Retorna 404 para rotina inexistente
- ✅ Retorna 409 para conflito de horário ao atualizar
- ✅ Permite atualizar rotina sem conflitar consigo mesma

#### DELETE `/api/routines/:id`
- ✅ Deleta rotina existente com sucesso
- ✅ Retorna 404 para rotina inexistente

### Validações de Conflito de Horário
- ✅ Detecta sobreposição de horários de início
- ✅ Detecta sobreposição de horários de fim
- ✅ Detecta rotinas completamente contidas
- ✅ Permite rotinas adjacentes

### Casos Extremos
- ✅ Lida com descrição vazia
- ✅ Lida com diferentes dias da semana
- ✅ Lida com caracteres especiais e emojis

## 🗄️ Testes do Banco de Dados (`database.test.ts`)

### Operações CRUD
- ✅ Criar e recuperar rotina
- ✅ Atualizar rotina
- ✅ Deletar rotina
- ✅ Buscar rotinas por dia

### Detecção de Conflitos de Horário
- ✅ Detecta sobreposição de horários de início
- ✅ Detecta sobreposição de horários de fim
- ✅ Detecta rotinas completamente contidas
- ✅ Permite rotinas adjacentes
- ✅ Exclui rotina do conflito ao atualizar
- ✅ Não detecta conflitos para dias diferentes

### Casos Extremos
- ✅ Lida com rotina sem descrição
- ✅ Lida com caracteres especiais e emojis
- ✅ Lida com todos os dias da semana
- ✅ Retorna array vazio para dia inexistente
- ✅ Retorna null para ID inexistente

### Operações do Banco
- ✅ Limpa todas as rotinas

## 🔧 Configuração dos Testes

### Jest Configuration (`package.json`)
```json
{
  "jest": {
    "preset": "ts-jest",
    "testEnvironment": "node",
    "testMatch": ["**/__tests__/**/*.test.ts"],
    "collectCoverageFrom": [
      "src/**/*.ts",
      "!src/**/*.d.ts"
    ],
    "setupFilesAfterEnv": ["<rootDir>/src/__tests__/setup.ts"]
  }
}
```

### Setup Global (`setup.ts`)
- Configuração de console para reduzir output
- Função helper para limpar banco de dados
- Timeout global de 10 segundos

## 📈 Métricas de Qualidade

### Cobertura por Arquivo
- **server.ts**: 70.45% (rotas da API)
- **database.ts**: 84.74% (operações do banco)

### Linhas Não Cobertas
- **server.ts**: Linhas de erro e shutdown
- **database.ts**: Linhas de erro de conexão

## 🎯 Cenários de Teste

### Cenários Positivos
1. **CRUD Completo**: Criar, ler, atualizar e deletar rotinas
2. **Filtros por Dia**: Buscar rotinas específicas por dia da semana
3. **Validações**: Campos obrigatórios e formatos corretos

### Cenários Negativos
1. **Dados Inválidos**: Campos faltando ou formatos incorretos
2. **Recursos Inexistentes**: IDs ou dias que não existem
3. **Conflitos de Horário**: Tentativas de criar rotinas sobrepostas

### Cenários Extremos
1. **Caracteres Especiais**: Emojis e acentos em títulos/descrições
2. **Limites de Dados**: Rotinas sem descrição, horários extremos
3. **Operações em Massa**: Múltiplas rotinas, limpeza do banco

## 🚨 Tratamento de Erros

### Erros HTTP Testados
- **400 Bad Request**: Dados inválidos ou campos faltando
- **404 Not Found**: Rotina ou recurso inexistente
- **409 Conflict**: Conflito de horário detectado
- **500 Internal Server Error**: Erros internos do servidor

### Validações Implementadas
- Campos obrigatórios (título, horários)
- Ordem dos horários (início < fim)
- Conflitos de horário no mesmo dia
- Existência de rotina antes de editar/deletar

## 🔄 Integração Contínua

### Scripts NPM
```bash
npm test          # Executa todos os testes
npm run test:watch    # Modo watch para desenvolvimento
npm run test:coverage # Testes com relatório de cobertura
```

### Workflow Recomendado
1. Desenvolver funcionalidade
2. Escrever testes para a funcionalidade
3. Executar `npm test` para verificar
4. Executar `npm run test:coverage` para análise
5. Commit após todos os testes passarem

## 📝 Boas Práticas Implementadas

### Organização
- Testes separados por funcionalidade
- Setup e teardown adequados
- Dados de teste reutilizáveis

### Isolamento
- Banco de dados limpo antes de cada teste
- Instâncias independentes para cada suite
- Mocks para dependências externas

### Legibilidade
- Nomes descritivos para testes
- Estrutura clara com describe/it
- Comentários explicativos quando necessário

### Manutenibilidade
- Dados de teste centralizados
- Funções helper reutilizáveis
- Configuração global consistente

## 🎉 Resultados

✅ **38 testes passando**
✅ **Cobertura de 76.19%**
✅ **Todas as rotas da API testadas**
✅ **Validações de negócio validadas**
✅ **Casos extremos cobertos**

A aplicação está bem testada e pronta para produção! 🚀
