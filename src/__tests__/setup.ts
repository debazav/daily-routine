// Configuração global para os testes
import { Database } from '../database/database';

// Mock do console para reduzir output durante os testes
const originalConsoleLog = console.log;
const originalConsoleError = console.error;

beforeAll(() => {
  // Silenciar logs durante os testes
  console.log = jest.fn();
  console.error = jest.fn();
});

afterAll(() => {
  // Restaurar console após os testes
  console.log = originalConsoleLog;
  console.error = originalConsoleError;
});

// Função helper para limpar banco de dados de teste
export const clearTestDatabase = async (db: Database) => {
  try {
    await db.clearAllRoutines();
  } catch (error) {
    // Ignorar erros de limpeza
  }
};

// Timeout global para testes
jest.setTimeout(10000);
