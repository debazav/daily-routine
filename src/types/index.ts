export interface Routine {
  id?: number;
  dayOfWeek: string;
  title: string;
  startTime: string;
  endTime: string;
  description?: string;
}

export interface DayOfWeek {
  id: string;
  name: string;
  namePt: string;
}

export interface TimeConflict {
  hasConflict: boolean;
  conflictingRoutine?: Routine;
} 