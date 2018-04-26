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
package se.diabol.jenkins.core;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class GenericComponentTest {

    @Test
    public void shouldGetLastActivity() {
        final long latestActivity = System.currentTimeMillis();

        GenericPipeline first = new GenericPipeline("") {
            @Override
            public long getLastActivity() {
                return 1000;
            }
        };

        GenericPipeline second = new GenericPipeline("") {
            @Override
            public long getLastActivity() {
                return 2000;
            }
        };

        GenericPipeline third = new GenericPipeline("") {
            @Override
            public long getLastActivity() {
                return latestActivity;
            }
        };

        GenericComponent component = new GenericComponent("component") {
            @Override
            public List<? extends GenericPipeline> getPipelines() {
                return Arrays.asList(first, second, third);
            }
        };

        assertThat(component.getLastActivity(), is(latestActivity));
    }

}
