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
import hudson.model.TaskListener;
import org.jenkinsci.plugins.tokenmacro.DataBoundTokenMacro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;

import java.io.IOException;

@Extension
@SuppressWarnings("UnusedDeclaration")
public class PipelineVersionTokenMacro extends DataBoundTokenMacro {

    @Override
    public String evaluate(AbstractBuild<?, ?> context, TaskListener listener, String macroName) throws
            MacroEvaluationException, IOException, InterruptedException {
        String version = PipelineVersionContributor.getVersion(context);
        if (version == null) {
            throw new MacroEvaluationException("Could not find " + PipelineVersionContributor.VERSION_PARAMETER
                    + " parameter on this build!");
        }
        return version;
    }

    @Override
    public boolean acceptsMacroName(String macroName) {
        return PipelineVersionContributor.VERSION_PARAMETER.equals(macroName);
    }
}
