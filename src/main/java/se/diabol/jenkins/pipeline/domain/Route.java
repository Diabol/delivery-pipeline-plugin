package se.diabol.jenkins.pipeline.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A possible route in the pipeline, comprised of tasks.
 */
public class Route {

    private List<Task> tasks = new ArrayList<Task>();

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public long getTotalBuildTime() {
        long totalBuildTime = 0;
        for (Task task: tasks) {
            totalBuildTime += task.getStatus().getDuration();
        }
        return totalBuildTime;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }
}
