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
