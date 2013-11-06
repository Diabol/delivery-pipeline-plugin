package se.diabol.jenkins.pipeline.model;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AbstractItemTest {

    class AbstractItemTester extends AbstractItem {
        protected AbstractItemTester( String name ) {
            super( name );
        }
    }

    @Test
    public void testHashCode() {
        assertThat(new AbstractItemTester("name").hashCode(), is("name".hashCode()));
    }

    @Test
    public void testEquals() {
        assertThat( new AbstractItemTester( "A" ), is( new AbstractItemTester( "A") ) );
        assertThat( new AbstractItemTester( "A" ).equals( new AbstractItemTester( "B" )), is( false ));
    }
}
