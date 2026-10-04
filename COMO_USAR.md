# 🚀 Como Usar a Aplicação

## Passo a Passo para Rodar

### 1. **Navegar para o diretório correto**
```bash
cd daily-routine
```

### 2. **Instalar dependências** (se ainda não fez)
```bash
npm install
```

### 3. **Compilar o TypeScript**
```bash
npm run build
```

### 4. **Iniciar o servidor**
```bash
npm run dev
```

### 5. **Acessar a aplicação**
Abra seu navegador e acesse: **http://localhost:3000**

## ✅ O que você deve ver:

1. **Página inicial** com 7 cards dos dias da semana
2. **Cards clicáveis** - clique em qualquer dia
3. **Navegação** para a página de rotinas do dia
4. **Botão "Adicionar Rotina"** para criar novas rotinas
5. **Formulário** com título, horário início/fim e descrição

## 🔧 Se algo não funcionar:

### Problema: "Página fica carregando"
- Verifique se o servidor está rodando (deve mostrar "Servidor rodando na porta 3000")
- Abra o console do navegador (F12) para ver erros
- Tente recarregar a página

### Problema: "Erro de conexão"
- Verifique se a porta 3000 não está sendo usada por outro programa
- Tente usar `npm start` em vez de `npm run dev`

### Problema: "Erro de módulo"
- Execute `npm install` novamente
- Verifique se o Node.js está atualizado

## 📱 Funcionalidades:

- ✅ **Adicionar rotinas** com horários
- ✅ **Editar rotinas** existentes  
- ✅ **Excluir rotinas** com confirmação
- ✅ **Validação** de conflitos de horário
- ✅ **Interface responsiva** (funciona no celular)

## 🎯 Teste Rápido:

1. Clique em "Segunda-feira"
2. Clique em "Adicionar Rotina"
3. Preencha: "Acordar" - 07:00 - 08:00
4. Clique "Salvar"
5. Veja a rotina criada!

A aplicação está pronta para uso! 🎉
