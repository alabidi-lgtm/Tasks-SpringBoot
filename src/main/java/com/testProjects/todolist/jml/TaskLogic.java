package com.testProjects.todolist.jml;

public class TaskLogic {

    /*@
      @ requires priority >= 0 && priority <= 2;
      @ ensures \result == priority;
      @*/
    public static int normalizePriority(int priority) {
        return priority;
    }

    /*@
      @ requires deadlineDays >= 0;
      @ ensures \result >= 0;
      @*/
    public static int daysUntilDeadline(int deadlineDays) {
        return deadlineDays;
    }
}
