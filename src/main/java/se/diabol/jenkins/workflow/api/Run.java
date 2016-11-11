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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.util.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Run {

    public final Map<String, ?> _links;
    public final String id;
    public final String name;
    public final String status;
    public final DateTime startTimeMillis;
    public final DateTime endTimeMillis;
    public final Long durationMillis;
    public final List<Stage> stages;

    public Run(@JsonProperty("_links") Map<String, ?> _links,
               @JsonProperty("id") String id,
               @JsonProperty("name") String name,
               @JsonProperty("status") String status,
               @JsonProperty("startTimeMillis") DateTime startTimeMillis,
               @JsonProperty("endTimeMillis") DateTime endTimeMillis,
               @JsonProperty("durationMillis") Long durationMillis,
               @JsonProperty("stages") List<Stage> stages) {
        this._links = _links;
        this.id = id;
        this.name = name;
        this.status = status;
        this.startTimeMillis = startTimeMillis;
        this.endTimeMillis = endTimeMillis;
        this.durationMillis = durationMillis;
        this.stages = stages;
    }

    public boolean hasStage(final String name) {
        for (Stage stage : stages) {
            String stageName = stage.name;
            if (stageName != null && stageName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public Stage getStageByName(final String name) {
        for (Stage stage : stages) {
            String stageName = stage.name;
            if (stageName != null && stageName.equals(name)) {
                return stage;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Run{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
