package se.diabol.jenkins.pipeline.model;

import org.junit.Test;
import se.diabol.jenkins.pipeline.model.status.StatusFactory;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaskTest {

    @Test
    public void testEquals() {
        Task a1 = new Task( "A", "unimportant_name", null, StatusFactory.idle(), "unimportant_link", false, null );
        Task b = new Task( "B", "unimportant_name", null, StatusFactory.idle(), "unimportant_link", false, null );
        Task a2 = new Task( "A", "unimportant_name", null, StatusFactory.idle(), "unimportant_link", false, null );

        assertThat( a1, is( a1 ) );
        assertThat( a1, is( a2 ) );
        assertThat( a1.equals( b ), is( false ));
    }
}
