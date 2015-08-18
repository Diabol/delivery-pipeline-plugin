/*
This file is part of Delivery Pipeline Plugin.

Delivery Pipeline Plugin is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Delivery Pipeline Plugin is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Delivery Pipeline Plugin.
If not, see <http://www.gnu.org/licenses/>.
*/
package se.diabol.jenkins.pipeline.domain;

import java.util.ArrayList;
import java.util.List;

import se.diabol.jenkins.pipeline.domain.task.Task;

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
