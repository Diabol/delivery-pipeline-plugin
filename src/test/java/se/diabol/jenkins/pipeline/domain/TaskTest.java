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
import se.diabol.jenkins.pipeline.domain.status.StatusFactory;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class TaskTest {

    @Test
    public void testEquals() {
        Task a1 = new Task( "A", "unimportant_name", null, StatusFactory.idle(), "unimportant_link", false, null, null );
        Task b = new Task( "B", "unimportant_name", null, StatusFactory.idle(), "unimportant_link", false, null, null );
        Task a2 = new Task( "A", "unimportant_name", null, StatusFactory.idle(), "unimportant_link", false, null, null );

        assertThat( a1, is( a1 ) );
        assertThat( a1, is( a2 ) );
        assertThat( a1.equals( b ), is( false ));
    }
}
