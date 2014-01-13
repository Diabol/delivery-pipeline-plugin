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
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
@SuppressWarnings("UnusedDeclaration")
public class PipelineEnvironmentContributor extends RunListener<Run> {

    private static final Logger LOG = Logger.getLogger(PipelineEnvironmentContributor.class.getName());

    @Override
    public void onStarted(Run run, TaskListener listener) {
        try {
            if (run instanceof AbstractBuild) {
                AbstractBuild build = (AbstractBuild) run;
                AbstractBuild upstreamBuild = PipelineFactory.getUpstreamBuild(build);
                if (upstreamBuild != null) {
                    String version = PipelineVersionContributor.getVersion(upstreamBuild);
                    if (version != null) {
                        PipelineVersionContributor.setVersion(build, version);
                        listener.getLogger().println("Setting version to: " + version + " from upstream version");
                    }
                }
            }
        } catch (IOException e) {
            listener.getLogger().println("Could not set pipeline version! " + e.getMessage());
            LOG.log(Level.WARNING, "Could not set pipeline version!", e);
        }

    }
}
