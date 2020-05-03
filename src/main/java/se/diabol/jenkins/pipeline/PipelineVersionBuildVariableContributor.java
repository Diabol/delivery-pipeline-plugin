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
package se.diabol.jenkins.pipeline;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildVariableContributor;
import se.diabol.jenkins.pipeline.PipelineVersionContributor.PipelineVersionAction;

import java.util.Map;

@Extension
public class PipelineVersionBuildVariableContributor extends BuildVariableContributor {
    @Override
    public void buildVariablesFor(AbstractBuild build, Map<String, String> variablesOut) {
        PipelineVersionAction pipelineVersionAction = build.getAction(PipelineVersionAction.class);

        if (pipelineVersionAction != null) {
            variablesOut.put(PipelineVersionContributor.VERSION_PARAMETER, pipelineVersionAction.getVersion());
        }
    }
}
