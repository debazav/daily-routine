import { Database } from '../database/database';
import { Routine } from '../types';

describe('Database Tests', () => {
  let db: Database;

  beforeAll(async () => {
    db = new Database();
    // Aguardar inicialização do banco
    await new Promise(resolve => setTimeout(resolve, 100));
  });

  afterAll(async () => {
    await db.clearAllRoutines();
    db.close();
  });

  beforeEach(async () => {
    await db.clearAllRoutines();
  });

  describe('Routine CRUD Operations', () => {
    const testRoutine: Routine = {
      dayOfWeek: 'monday',
      title: 'Test Routine',
      startTime: '09:00',
      endTime: '10:00',
      description: 'Test description'
    };

    it('should create and retrieve a routine', async () => {
      const id = await db.createRoutine(testRoutine);
      expect(id).toBeGreaterThan(0);

      const retrievedRoutine = await db.getRoutineById(id);
      expect(retrievedRoutine).toMatchObject({
        id,
        dayOfWeek: 'monday',
        title: 'Test Routine',
        startTime: '09:00',
        endTime: '10:00',
        description: 'Test description'
      });
    });

    it('should update a routine', async () => {
      const id = await db.createRoutine(testRoutine);
      
      const updatedRoutine: Routine = {
        ...testRoutine,
        title: 'Updated Routine',
        description: 'Updated description'
      };

      await db.updateRoutine(id, updatedRoutine);
      
      const retrievedRoutine = await db.getRoutineById(id);
      expect(retrievedRoutine?.title).toBe('Updated Routine');
      expect(retrievedRoutine?.description).toBe('Updated description');
    });

    it('should delete a routine', async () => {
      const id = await db.createRoutine(testRoutine);
      
      // Verificar que existe
      const routine = await db.getRoutineById(id);
      expect(routine).toBeTruthy();
      
      // Deletar
      await db.deleteRoutine(id);
      
      // Verificar que foi deletada
      const deletedRoutine = await db.getRoutineById(id);
      expect(deletedRoutine).toBeNull();
    });

    it('should get routines by day', async () => {
      const mondayRoutine = { ...testRoutine, dayOfWeek: 'monday' };
      const tuesdayRoutine = { ...testRoutine, dayOfWeek: 'tuesday', title: 'Tuesday Routine' };
      
      await db.createRoutine(mondayRoutine);
      await db.createRoutine(tuesdayRoutine);
      
      const mondayRoutines = await db.getRoutinesByDay('monday');
      const tuesdayRoutines = await db.getRoutinesByDay('tuesday');
      
      expect(mondayRoutines).toHaveLength(1);
      expect(mondayRoutines[0].dayOfWeek).toBe('monday');
      
      expect(tuesdayRoutines).toHaveLength(1);
      expect(tuesdayRoutines[0].dayOfWeek).toBe('tuesday');
    });
  });

  describe('Time Conflict Detection', () => {
    const baseRoutine: Routine = {
      dayOfWeek: 'monday',
      title: 'Base Routine',
      startTime: '09:00',
      endTime: '10:00',
      description: 'Base routine'
    };

    it('should detect overlapping start times', async () => {
      await db.createRoutine(baseRoutine);
      
      const overlappingRoutine: Routine = {
        ...baseRoutine,
        title: 'Overlapping Routine',
        startTime: '09:30',
        endTime: '11:00'
      };

      const conflicts = await db.checkTimeConflict('monday', '09:30', '11:00');
      expect(conflicts).toHaveLength(1);
      expect(conflicts[0].title).toBe('Base Routine');
    });

    it('should detect overlapping end times', async () => {
      await db.createRoutine(baseRoutine);
      
      const overlappingRoutine: Routine = {
        ...baseRoutine,
        title: 'Overlapping Routine',
        startTime: '08:00',
        endTime: '09:30'
      };

      const conflicts = await db.checkTimeConflict('monday', '08:00', '09:30');
      expect(conflicts).toHaveLength(1);
      expect(conflicts[0].title).toBe('Base Routine');
    });

    it('should detect completely contained routines', async () => {
      await db.createRoutine(baseRoutine);
      
      const containedRoutine: Routine = {
        ...baseRoutine,
        title: 'Contained Routine',
        startTime: '09:15',
        endTime: '09:45'
      };

      const conflicts = await db.checkTimeConflict('monday', '09:15', '09:45');
      expect(conflicts).toHaveLength(1);
      expect(conflicts[0].title).toBe('Base Routine');
    });

    it('should allow adjacent routines', async () => {
      await db.createRoutine(baseRoutine);
      
      const adjacentRoutine: Routine = {
        ...baseRoutine,
        title: 'Adjacent Routine',
        startTime: '10:00',
        endTime: '11:00'
      };

      const conflicts = await db.checkTimeConflict('monday', '10:00', '11:00');
      expect(conflicts).toHaveLength(0);
    });

    it('should exclude routine from conflict check when updating', async () => {
      const id = await db.createRoutine(baseRoutine);
      
      // Tentar atualizar a mesma rotina (não deve conflitar consigo mesma)
      const conflicts = await db.checkTimeConflict('monday', '09:00', '10:00', id);
      expect(conflicts).toHaveLength(0);
    });

    it('should not detect conflicts for different days', async () => {
      await db.createRoutine(baseRoutine);
      
      const tuesdayRoutine: Routine = {
        ...baseRoutine,
        dayOfWeek: 'tuesday',
        title: 'Tuesday Routine',
        startTime: '09:00',
        endTime: '10:00'
      };

      const conflicts = await db.checkTimeConflict('tuesday', '09:00', '10:00');
      expect(conflicts).toHaveLength(0);
    });
  });

  describe('Edge Cases', () => {
    it('should handle routine without description', async () => {
      const routineWithoutDescription: Routine = {
        dayOfWeek: 'monday',
        title: 'No Description',
        startTime: '09:00',
        endTime: '10:00'
      };

      const id = await db.createRoutine(routineWithoutDescription);
      const retrievedRoutine = await db.getRoutineById(id);
      
      expect(retrievedRoutine?.description).toBeNull();
    });

    it('should handle special characters in text fields', async () => {
      const specialRoutine: Routine = {
        dayOfWeek: 'monday',
        title: 'Routine with émojis 🎉 and special chars: áéíóú',
        startTime: '09:00',
        endTime: '10:00',
        description: 'Description with émojis 🚀 and special chars: áéíóú'
      };

      const id = await db.createRoutine(specialRoutine);
      const retrievedRoutine = await db.getRoutineById(id);
      
      expect(retrievedRoutine?.title).toBe(specialRoutine.title);
      expect(retrievedRoutine?.description).toBe(specialRoutine.description);
    });

    it('should handle all days of the week', async () => {
      const days = ['monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday', 'sunday'];
      
      for (const day of days) {
        const routine: Routine = {
          dayOfWeek: day,
          title: `${day} routine`,
          startTime: '09:00',
          endTime: '10:00'
        };

        const id = await db.createRoutine(routine);
        const retrievedRoutine = await db.getRoutineById(id);
        
        expect(retrievedRoutine?.dayOfWeek).toBe(day);
      }
    });

    it('should return empty array for non-existent day', async () => {
      const routines = await db.getRoutinesByDay('nonexistent');
      expect(routines).toEqual([]);
    });

    it('should return null for non-existent routine id', async () => {
      const routine = await db.getRoutineById(999);
      expect(routine).toBeNull();
    });
  });

  describe('Database Operations', () => {
    it('should clear all routines', async () => {
      const routine1: Routine = {
        dayOfWeek: 'monday',
        title: 'Routine 1',
        startTime: '09:00',
        endTime: '10:00'
      };

      const routine2: Routine = {
        dayOfWeek: 'tuesday',
        title: 'Routine 2',
        startTime: '11:00',
        endTime: '12:00'
      };

      await db.createRoutine(routine1);
      await db.createRoutine(routine2);

      // Verificar que existem
      const mondayRoutines = await db.getRoutinesByDay('monday');
      const tuesdayRoutines = await db.getRoutinesByDay('tuesday');
      expect(mondayRoutines).toHaveLength(1);
      expect(tuesdayRoutines).toHaveLength(1);

      // Limpar tudo
      await db.clearAllRoutines();

      // Verificar que foram removidas
      const mondayRoutinesAfter = await db.getRoutinesByDay('monday');
      const tuesdayRoutinesAfter = await db.getRoutinesByDay('tuesday');
      expect(mondayRoutinesAfter).toHaveLength(0);
      expect(tuesdayRoutinesAfter).toHaveLength(0);
    });
  });
});
