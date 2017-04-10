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
package se.diabol.jenkins.workflow.api;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import com.google.api.client.util.DateTime;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JsonTest {

    @Test
    public void shouldDeserializeJsonResponse() {
        Run[] arrayOfRuns = Json.deserialize(load("workflow/wfapi-last_run.json"), Run[].class);
        List<Run> runs = Arrays.asList(arrayOfRuns);
        assertFalse(runs.isEmpty());
        assertThat(runs.size(), is(2));

        assertThat(firstOf(runs).id, is("2014-10-16_13-07-52"));
        assertThat(firstOf(runs).name, is("#16"));
        assertThat(firstOf(runs).status, is("PAUSED_PENDING_INPUT"));
        assertThat(firstOf(runs).startTimeMillis, is(new DateTime(1413461275770L)));
        assertThat(firstOf(runs).endTimeMillis, is(new DateTime(1413461285999L)));
        assertThat(firstOf(runs).durationMillis, is(10229L));

        assertThat(lastOf(runs).id, is("2014-10-16_12-45-06"));
        assertThat(lastOf(runs).name, is("#15"));
        assertThat(lastOf(runs).status, is("SUCCESS"));
        assertThat(lastOf(runs).startTimeMillis, is(new DateTime(1413459910289L)));
        assertThat(lastOf(runs).endTimeMillis, is(new DateTime(1413459937070L)));
        assertThat(lastOf(runs).durationMillis, is(26781L));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deserializeShouldThrowIllegalArgumentForIOException() {
        Json.deserialize("{ \"invalid json", Run[].class);
    }

    @Test
    public void shouldSerializeJson() {
        Run run = new Run(Collections.<String, String>emptyMap(), "id", "name", "SUCCESS", null, null, 0L,
                Collections.<Stage>emptyList());
        String json = Json.serialize(run);
        assertThat(json, is("{\"_links\":{},\"id\":\"id\",\"name\":\"name\",\"status\":\"SUCCESS\","
                + "\"startTimeMillis\":null,\"endTimeMillis\":null,\"durationMillis\":0,\"stages\":[]}"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void serializeShouldThrowIllegalArgumentForIOException() {
        class NonSerializable {}

        NonSerializable clazz = new NonSerializable();
        Json.serialize(clazz);
    }

    private static <T> T firstOf(List<T> list) {
        assertThat(list.size(), greaterThan(0));
        return list.get(0);
    }

    private static <T> T lastOf(List<T> list) {
        assertThat(list.size(), greaterThan(0));
        return list.get(list.size() - 1);
    }

    private static String load(String name) {
        try {
            return CharStreams.toString(new InputStreamReader(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(name), Charsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
