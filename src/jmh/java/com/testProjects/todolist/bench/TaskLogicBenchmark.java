package com.testProjects.todolist.bench;

import com.testProjects.todolist.jml.TaskLogic;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1)
@Measurement(iterations = 3, time = 1)
@Fork(1)
@State(Scope.Thread)
public class TaskLogicBenchmark {

    @Param({"0", "1", "2"})
    public int priority;

    @Benchmark
    public int benchNormalizePriority() {
        return TaskLogic.normalizePriority(priority);
    }

    @Benchmark
    public int benchDaysUntilDeadline() {
        return TaskLogic.daysUntilDeadline(10);
    }
}
