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

import hudson.model.Api;
import org.acegisecurity.AuthenticationException;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import se.diabol.jenkins.pipeline.trigger.TriggerException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class PipelineApi extends Api {

    private DeliveryPipelineView view;

    public PipelineApi(DeliveryPipelineView view) {
        super(view);
        this.view = view;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void doManualStep(StaplerRequest req, StaplerResponse rsp,
                      @QueryParameter String project,
                      @QueryParameter String upstream,
                      @QueryParameter String buildId) throws IOException, ServletException {
        if (project != null && upstream != null && buildId != null) {
            try {
                view.triggerManual(project, upstream, buildId);
                rsp.setStatus(HttpServletResponse.SC_OK);
            } catch (TriggerException e) {
                rsp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (AuthenticationException e) {
                rsp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            rsp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void doRebuildStep(StaplerRequest req, StaplerResponse rsp,
                      @QueryParameter String project,
                      @QueryParameter String buildId) throws IOException, ServletException {
        if (project != null && buildId != null) {
            try {
                view.triggerRebuild(project, buildId);
                rsp.setStatus(HttpServletResponse.SC_OK);
            } catch (AuthenticationException e) {
                rsp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            rsp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        }
    }


}
