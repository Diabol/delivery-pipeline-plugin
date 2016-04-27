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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpResponse;

import java.io.IOException;

public class Json {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static <T> T deserialize(HttpResponse jsonResponse, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(jsonResponse.parseAsString().getBytes(), type);
        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe);
        }
    }

    public static <T> T deserialize(byte[] json, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(json, type);
        } catch (IOException ioe) {
            throw new IllegalArgumentException(ioe);
        }
    }

    public String serialize(Object serializable) {
        try {
            return OBJECT_MAPPER.writeValueAsString(serializable);
        } catch (JsonProcessingException jpe) {
            throw new IllegalArgumentException(jpe);
        }
    }

}
