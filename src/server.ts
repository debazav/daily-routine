import express from 'express';
import cors from 'cors';
import path from 'path';
import { Database } from './database/database';
import { Routine } from './types';

// Função para criar o app (para testes)
export function createApp(database?: Database) {
  const app = express();
  const db = database || new Database();

  // Middleware
  app.use(cors());
  app.use(express.json());

  // Configurar MIME types para arquivos estáticos
  app.use(express.static(path.join(__dirname, '../public'), {
    setHeaders: (res, path) => {
      if (path.endsWith('.js')) {
        res.setHeader('Content-Type', 'application/javascript');
      } else if (path.endsWith('.css')) {
        res.setHeader('Content-Type', 'text/css');
      }
    }
  }));

  // Rotas da API

  // Buscar rotinas de um dia específico
  app.get('/api/routines/:dayOfWeek', async (req, res) => {
    try {
      const { dayOfWeek } = req.params;
      const routines = await db.getRoutinesByDay(dayOfWeek);
      res.json(routines);
    } catch (error) {
      console.error('Erro ao buscar rotinas:', error);
      res.status(500).json({ error: 'Erro interno do servidor' });
    }
  });

  // Buscar uma rotina específica
  app.get('/api/routines/:dayOfWeek/:id', async (req, res) => {
    try {
      const { id } = req.params;
      const routine = await db.getRoutineById(parseInt(id));
      if (!routine) {
        return res.status(404).json({ error: 'Rotina não encontrada' });
      }
      res.json(routine);
    } catch (error) {
      console.error('Erro ao buscar rotina:', error);
      res.status(500).json({ error: 'Erro interno do servidor' });
    }
  });

  // Criar nova rotina
  app.post('/api/routines', async (req, res) => {
    try {
      const routine: Routine = req.body;
      
      // Validações básicas
      if (!routine.dayOfWeek || !routine.title || !routine.startTime || !routine.endTime) {
        return res.status(400).json({ error: 'Todos os campos obrigatórios devem ser preenchidos' });
      }

      // Validar que horário de início é anterior ao de fim
      if (routine.startTime >= routine.endTime) {
        return res.status(400).json({ error: 'O horário de início deve ser anterior ao horário de fim' });
      }

      // Verificar conflitos de horário
      const conflicts = await db.checkTimeConflict(routine.dayOfWeek, routine.startTime, routine.endTime);
      if (conflicts.length > 0) {
        return res.status(409).json({ 
          error: 'Conflito de horário detectado',
          conflictingRoutine: conflicts[0]
        });
      }

      const id = await db.createRoutine(routine);
      const newRoutine = await db.getRoutineById(id);
      res.status(201).json(newRoutine);
    } catch (error) {
      console.error('Erro ao criar rotina:', error);
      res.status(500).json({ error: 'Erro interno do servidor' });
    }
  });

  // Atualizar rotina
  app.put('/api/routines/:id', async (req, res) => {
    try {
      const { id } = req.params;
      const routine: Routine = req.body;
      
      // Verificar se a rotina existe
      const existingRoutine = await db.getRoutineById(parseInt(id));
      if (!existingRoutine) {
        return res.status(404).json({ error: 'Rotina não encontrada' });
      }

      // Validações básicas
      if (!routine.dayOfWeek || !routine.title || !routine.startTime || !routine.endTime) {
        return res.status(400).json({ error: 'Todos os campos obrigatórios devem ser preenchidos' });
      }

      // Validar que horário de início é anterior ao de fim
      if (routine.startTime >= routine.endTime) {
        return res.status(400).json({ error: 'O horário de início deve ser anterior ao horário de fim' });
      }

      // Verificar conflitos de horário (excluindo a própria rotina)
      const conflicts = await db.checkTimeConflict(routine.dayOfWeek, routine.startTime, routine.endTime, parseInt(id));
      if (conflicts.length > 0) {
        return res.status(409).json({ 
          error: 'Conflito de horário detectado',
          conflictingRoutine: conflicts[0]
        });
      }

      await db.updateRoutine(parseInt(id), routine);
      const updatedRoutine = await db.getRoutineById(parseInt(id));
      res.json(updatedRoutine);
    } catch (error) {
      console.error('Erro ao atualizar rotina:', error);
      res.status(500).json({ error: 'Erro interno do servidor' });
    }
  });

  // Excluir rotina
  app.delete('/api/routines/:id', async (req, res) => {
    try {
      const { id } = req.params;
      
      // Verificar se a rotina existe
      const existingRoutine = await db.getRoutineById(parseInt(id));
      if (!existingRoutine) {
        return res.status(404).json({ error: 'Rotina não encontrada' });
      }

      await db.deleteRoutine(parseInt(id));
      res.status(204).send();
    } catch (error) {
      console.error('Erro ao excluir rotina:', error);
      res.status(500).json({ error: 'Erro interno do servidor' });
    }
  });

  // Rota para a página inicial
  app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, '../public/index.html'));
  });

  // Rota para a página de rotinas de um dia específico
  app.get('/day/:dayOfWeek', (req, res) => {
    res.sendFile(path.join(__dirname, '../public/day.html'));
  });

  return app;
}

// Inicialização do servidor (apenas quando executado diretamente)
if (require.main === module) {
  const PORT = process.env.PORT || 3000;
  const app = createApp();
  
  app.listen(PORT, () => {
    console.log(`Servidor rodando na porta ${PORT}`);
    console.log(`Acesse: http://localhost:${PORT}`);
  });

  // Graceful shutdown
  process.on('SIGINT', () => {
    console.log('\nEncerrando servidor...');
    process.exit(0);
  });
} 