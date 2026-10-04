import sqlite3 from 'sqlite3';
import { Routine } from '../types';

export class Database {
  private db: sqlite3.Database;

  constructor() {
    this.db = new sqlite3.Database('./database.sqlite', (err) => {
      if (err) {
        console.error('Erro ao conectar ao banco de dados:', err);
      } else {
        console.log('Conectado ao banco de dados SQLite');
        this.initDatabase();
      }
    });
  }

  private initDatabase(): void {
    const createTableQuery = `
      CREATE TABLE IF NOT EXISTS routines (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        dayOfWeek TEXT NOT NULL,
        title TEXT NOT NULL,
        startTime TEXT NOT NULL,
        endTime TEXT NOT NULL,
        description TEXT
      )
    `;

    this.db.run(createTableQuery, (err) => {
      if (err) {
        console.error('Erro ao criar tabela:', err);
      } else {
        console.log('Tabela routines criada ou já existe');
      }
    });
  }

  // Buscar todas as rotinas de um dia específico
  getRoutinesByDay(dayOfWeek: string): Promise<Routine[]> {
    return new Promise((resolve, reject) => {
      const query = 'SELECT * FROM routines WHERE dayOfWeek = ? ORDER BY startTime';
      this.db.all(query, [dayOfWeek], (err, rows) => {
        if (err) {
          reject(err);
        } else {
          resolve(rows as Routine[]);
        }
      });
    });
  }

  // Buscar uma rotina específica
  getRoutineById(id: number): Promise<Routine | null> {
    return new Promise((resolve, reject) => {
      const query = 'SELECT * FROM routines WHERE id = ?';
      this.db.get(query, [id], (err, row) => {
        if (err) {
          reject(err);
        } else {
          resolve(row as Routine || null);
        }
      });
    });
  }

  // Criar nova rotina
  createRoutine(routine: Routine): Promise<number> {
    return new Promise((resolve, reject) => {
      const query = `
        INSERT INTO routines (dayOfWeek, title, startTime, endTime, description)
        VALUES (?, ?, ?, ?, ?)
      `;
      this.db.run(query, [routine.dayOfWeek, routine.title, routine.startTime, routine.endTime, routine.description], function(err) {
        if (err) {
          reject(err);
        } else {
          resolve(this.lastID);
        }
      });
    });
  }

  // Atualizar rotina
  updateRoutine(id: number, routine: Routine): Promise<void> {
    return new Promise((resolve, reject) => {
      const query = `
        UPDATE routines 
        SET dayOfWeek = ?, title = ?, startTime = ?, endTime = ?, description = ?
        WHERE id = ?
      `;
      this.db.run(query, [routine.dayOfWeek, routine.title, routine.startTime, routine.endTime, routine.description, id], (err) => {
        if (err) {
          reject(err);
        } else {
          resolve();
        }
      });
    });
  }

  // Excluir rotina
  deleteRoutine(id: number): Promise<void> {
    return new Promise((resolve, reject) => {
      const query = 'DELETE FROM routines WHERE id = ?';
      this.db.run(query, [id], (err) => {
        if (err) {
          reject(err);
        } else {
          resolve();
        }
      });
    });
  }

  // Verificar conflitos de horário
  checkTimeConflict(dayOfWeek: string, startTime: string, endTime: string, excludeId?: number): Promise<Routine[]> {
    return new Promise((resolve, reject) => {
      let query = `
        SELECT * FROM routines 
        WHERE dayOfWeek = ? 
        AND (
          (startTime <= ? AND endTime > ?) OR
          (startTime < ? AND endTime >= ?) OR
          (startTime >= ? AND endTime <= ?)
        )
      `;
      
      const params = [dayOfWeek, startTime, startTime, endTime, endTime, startTime, endTime];
      
      if (excludeId) {
        query += ' AND id != ?';
        params.push(excludeId.toString());
      }

      this.db.all(query, params, (err, rows) => {
        if (err) {
          reject(err);
        } else {
          resolve(rows as Routine[]);
        }
      });
    });
  }

  // Limpar todas as rotinas (para testes)
  clearAllRoutines(): Promise<void> {
    return new Promise((resolve, reject) => {
      const query = 'DELETE FROM routines';
      this.db.run(query, (err) => {
        if (err) {
          reject(err);
        } else {
          resolve();
        }
      });
    });
  }

  // Fechar conexão
  close(): void {
    this.db.close();
  }
} 