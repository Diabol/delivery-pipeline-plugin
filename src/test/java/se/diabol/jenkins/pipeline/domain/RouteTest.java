package se.diabol.jenkins.pipeline.domain;

import org.junit.Test;
import se.diabol.jenkins.pipeline.domain.status.Status;

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
