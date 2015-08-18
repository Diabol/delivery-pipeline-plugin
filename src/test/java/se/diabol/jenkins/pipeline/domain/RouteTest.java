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

import org.junit.Test;

import se.diabol.jenkins.pipeline.domain.status.Status;
import se.diabol.jenkins.pipeline.domain.task.Task;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class RouteTest {

    @Test
    public void testSetTasks() {
        Route route = new Route();
        assertEquals(0, route.getTasks().size());

        List<Task> tasks = new ArrayList<Task>();
        tasks.add(mock(Task.class));
        tasks.add(mock(Task.class));
        tasks.add(mock(Task.class));

        route.setTasks(tasks);
        assertEquals(3, route.getTasks().size());
    }

    @Test
    public void testGetTotalBuildTime() {
        Route route = new Route();
        assertEquals(0, route.getTotalBuildTime());

        List<Task> tasks = new ArrayList<Task>();
        Task task1 = mock(Task.class);
        Status status1 = mock(Status.class);
        when(status1.getDuration()).thenReturn(100L);
        when(task1.getStatus()).thenReturn(status1);

        Task task2 = mock(Task.class);
        Status status2 = mock(Status.class);
        when(status2.getDuration()).thenReturn(200L);
        when(task2.getStatus()).thenReturn(status2);

        Task task3 = mock(Task.class);
        Status status3 = mock(Status.class);
        when(status3.getDuration()).thenReturn(300L);
        when(task3.getStatus()).thenReturn(status3);

        tasks.add(task1);
        tasks.add(task2);
        tasks.add(task3);
        route.setTasks(tasks);

        assertEquals(600L, route.getTotalBuildTime());
    }

    @Test
    public void testAddTask() {
        Route route = new Route();
        assertEquals(0, route.getTasks().size());

        route.addTask(mock(Task.class));
        assertEquals(1, route.getTasks().size());

        route.addTask(mock(Task.class));
        route.addTask(mock(Task.class));
        assertEquals(3, route.getTasks().size());
    }

}
