import request from 'supertest';
import express from 'express';
import { Database } from '../database/database';
import { clearTestDatabase } from './setup';

// Importar o app do servidor
let app: express.Application;
let db: Database;

// Dados de teste
const testRoutine = {
  dayOfWeek: 'monday',
  title: 'Test Routine',
  startTime: '09:00',
  endTime: '10:00',
  description: 'Test description'
};

const testRoutine2 = {
  dayOfWeek: 'monday',
  title: 'Test Routine 2',
  startTime: '11:00',
  endTime: '12:00',
  description: 'Test description 2'
};

const conflictingRoutine = {
  dayOfWeek: 'monday',
  title: 'Conflicting Routine',
  startTime: '09:30',
  endTime: '10:30',
  description: 'This should conflict'
};

describe('API Tests', () => {
  beforeAll(async () => {
    // Criar instância do banco de dados de teste
    db = new Database();
    
    // Aguardar inicialização do banco
    await new Promise(resolve => setTimeout(resolve, 100));
    
    // Limpar banco antes dos testes
    await clearTestDatabase(db);
    
    // Importar e configurar o app
    const { createApp } = await import('../server');
    app = createApp(db);
  });

  afterAll(async () => {
    // Limpar banco após os testes
    await clearTestDatabase(db);
    db.close();
  });

  beforeEach(async () => {
    // Limpar banco antes de cada teste
    await clearTestDatabase(db);
  });

  describe('GET /api/routines/:dayOfWeek', () => {
    it('should return empty array for day with no routines', async () => {
      const response = await request(app)
        .get('/api/routines/monday')
        .expect(200);

      expect(response.body).toEqual([]);
    });

    it('should return routines for specific day', async () => {
      // Criar rotina primeiro
      await db.createRoutine(testRoutine);
      
      const response = await request(app)
        .get('/api/routines/monday')
        .expect(200);

      expect(response.body).toHaveLength(1);
      expect(response.body[0]).toMatchObject({
        dayOfWeek: 'monday',
        title: 'Test Routine',
        startTime: '09:00',
        endTime: '10:00'
      });
    });

    it('should return only routines for specified day', async () => {
      // Criar rotinas em dias diferentes
      await db.createRoutine(testRoutine);
      await db.createRoutine({ ...testRoutine2, dayOfWeek: 'tuesday' });
      
      const response = await request(app)
        .get('/api/routines/monday')
        .expect(200);

      expect(response.body).toHaveLength(1);
      expect(response.body[0].dayOfWeek).toBe('monday');
    });
  });

  describe('GET /api/routines/:dayOfWeek/:id', () => {
    it('should return 404 for non-existent routine', async () => {
      await request(app)
        .get('/api/routines/monday/999')
        .expect(404);
    });

    it('should return specific routine by id', async () => {
      // Criar rotina
      const id = await db.createRoutine(testRoutine);
      
      const response = await request(app)
        .get(`/api/routines/monday/${id}`)
        .expect(200);

      expect(response.body).toMatchObject({
        id,
        dayOfWeek: 'monday',
        title: 'Test Routine',
        startTime: '09:00',
        endTime: '10:00'
      });
    });
  });

  describe('POST /api/routines', () => {
    it('should create new routine successfully', async () => {
      const response = await request(app)
        .post('/api/routines')
        .send(testRoutine)
        .expect(201);

      expect(response.body).toMatchObject({
        dayOfWeek: 'monday',
        title: 'Test Routine',
        startTime: '09:00',
        endTime: '10:00',
        description: 'Test description'
      });
      expect(response.body.id).toBeDefined();
    });

    it('should return 400 for missing required fields', async () => {
      const invalidRoutine = {
        dayOfWeek: 'monday',
        title: 'Test'
        // Missing startTime and endTime
      };

      const response = await request(app)
        .post('/api/routines')
        .send(invalidRoutine)
        .expect(400);

      expect(response.body.error).toBe('Todos os campos obrigatórios devem ser preenchidos');
    });

    it('should return 409 for time conflict', async () => {
      // Criar primeira rotina
      await db.createRoutine(testRoutine);
      
      // Tentar criar rotina conflitante
      const response = await request(app)
        .post('/api/routines')
        .send(conflictingRoutine)
        .expect(409);

      expect(response.body.error).toBe('Conflito de horário detectado');
      expect(response.body.conflictingRoutine).toBeDefined();
    });

    it('should validate that start time is before end time', async () => {
      const invalidTimeRoutine = {
        ...testRoutine,
        startTime: '10:00',
        endTime: '09:00'
      };

      const response = await request(app)
        .post('/api/routines')
        .send(invalidTimeRoutine)
        .expect(400);

      expect(response.body.error).toBe('O horário de início deve ser anterior ao horário de fim');
    });
  });

  describe('PUT /api/routines/:id', () => {
    it('should update existing routine successfully', async () => {
      // Criar rotina
      const id = await db.createRoutine(testRoutine);
      
      const updatedRoutine = {
        ...testRoutine,
        title: 'Updated Routine',
        description: 'Updated description'
      };

      const response = await request(app)
        .put(`/api/routines/${id}`)
        .send(updatedRoutine)
        .expect(200);

      expect(response.body).toMatchObject({
        id,
        title: 'Updated Routine',
        description: 'Updated description'
      });
    });

    it('should return 404 for non-existent routine', async () => {
      await request(app)
        .put('/api/routines/999')
        .send(testRoutine)
        .expect(404);
    });

    it('should return 409 for time conflict when updating', async () => {
      // Criar duas rotinas
      const id1 = await db.createRoutine(testRoutine);
      const id2 = await db.createRoutine(testRoutine2);
      
      // Tentar atualizar segunda rotina para conflitar com a primeira
      const conflictingUpdate = {
        ...testRoutine2,
        startTime: '09:30',
        endTime: '10:30'
      };

      const response = await request(app)
        .put(`/api/routines/${id2}`)
        .send(conflictingUpdate)
        .expect(409);

      expect(response.body.error).toBe('Conflito de horário detectado');
    });

    it('should allow updating routine without conflicts with itself', async () => {
      // Criar rotina
      const id = await db.createRoutine(testRoutine);
      
      // Atualizar a mesma rotina (deve funcionar)
      const updatedRoutine = {
        ...testRoutine,
        title: 'Updated Title'
      };

      const response = await request(app)
        .put(`/api/routines/${id}`)
        .send(updatedRoutine)
        .expect(200);

      expect(response.body.title).toBe('Updated Title');
    });
  });

  describe('DELETE /api/routines/:id', () => {
    it('should delete existing routine successfully', async () => {
      // Criar rotina
      const id = await db.createRoutine(testRoutine);
      
      // Verificar que existe
      const routines = await db.getRoutinesByDay('monday');
      expect(routines).toHaveLength(1);
      
      // Deletar
      await request(app)
        .delete(`/api/routines/${id}`)
        .expect(204);
      
      // Verificar que foi deletada
      const routinesAfter = await db.getRoutinesByDay('monday');
      expect(routinesAfter).toHaveLength(0);
    });

    it('should return 404 for non-existent routine', async () => {
      await request(app)
        .delete('/api/routines/999')
        .expect(404);
    });
  });

  describe('Time Conflict Validation', () => {
    it('should detect overlapping start times', async () => {
      await db.createRoutine(testRoutine);
      
      const overlappingRoutine = {
        ...testRoutine,
        startTime: '09:30',
        endTime: '11:00'
      };

      await request(app)
        .post('/api/routines')
        .send(overlappingRoutine)
        .expect(409);
    });

    it('should detect overlapping end times', async () => {
      await db.createRoutine(testRoutine);
      
      const overlappingRoutine = {
        ...testRoutine,
        startTime: '08:00',
        endTime: '09:30'
      };

      await request(app)
        .post('/api/routines')
        .send(overlappingRoutine)
        .expect(409);
    });

    it('should detect completely contained routines', async () => {
      await db.createRoutine(testRoutine);
      
      const containedRoutine = {
        ...testRoutine,
        startTime: '09:15',
        endTime: '09:45'
      };

      await request(app)
        .post('/api/routines')
        .send(containedRoutine)
        .expect(409);
    });

    it('should allow adjacent routines', async () => {
      await db.createRoutine(testRoutine);
      
      const adjacentRoutine = {
        ...testRoutine,
        startTime: '10:00',
        endTime: '11:00'
      };

      await request(app)
        .post('/api/routines')
        .send(adjacentRoutine)
        .expect(201);
    });
  });

  describe('Edge Cases', () => {
    it('should handle empty description', async () => {
      const routineWithoutDescription = {
        dayOfWeek: 'monday',
        title: 'Test Routine',
        startTime: '09:00',
        endTime: '10:00'
      };

      const response = await request(app)
        .post('/api/routines')
        .send(routineWithoutDescription)
        .expect(201);

      expect(response.body.description).toBeNull();
    });

    it('should handle different days of week', async () => {
      const sundayRoutine = {
        ...testRoutine,
        dayOfWeek: 'sunday'
      };

      const response = await request(app)
        .post('/api/routines')
        .send(sundayRoutine)
        .expect(201);

      expect(response.body.dayOfWeek).toBe('sunday');
    });

    it('should handle special characters in title and description', async () => {
      const specialRoutine = {
        dayOfWeek: 'monday',
        title: 'Routine with émojis 🎉 and special chars: áéíóú',
        startTime: '09:00',
        endTime: '10:00',
        description: 'Description with émojis 🚀 and special chars: áéíóú'
      };

      const response = await request(app)
        .post('/api/routines')
        .send(specialRoutine)
        .expect(201);

      expect(response.body.title).toBe(specialRoutine.title);
      expect(response.body.description).toBe(specialRoutine.description);
    });
  });
});
