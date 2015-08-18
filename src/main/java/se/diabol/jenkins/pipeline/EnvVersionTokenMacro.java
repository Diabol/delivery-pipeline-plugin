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
import java.util.Map;

@Extension(optional = true)
public class EnvVersionTokenMacro extends DataBoundTokenMacro {

    private static final String NAME = "ENV_VERSION";
    public static final Class<DataBoundTokenMacro> clazz = DataBoundTokenMacro.class;

    @DataBoundTokenMacro.Parameter(required = false)
    public boolean stripSnapshot = false;

    @Override
    public String evaluate(AbstractBuild<?, ?> context, TaskListener listener, String macroName)
            throws MacroEvaluationException, IOException, InterruptedException {
        Map<String, String> env = context.getEnvironment(listener);
        if (env.containsKey(NAME)) {
            if (stripSnapshot) {
                String version = env.get(NAME);
                return version.replace("-SNAPSHOT", "");
            } else {
                return env.get(NAME);
            }
        }
        return "";
    }

    @Override
    public boolean acceptsMacroName(String macroName) {
        return NAME.equals(macroName);
    }
}
