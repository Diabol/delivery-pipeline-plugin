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

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_ACCEPTABLE;
import static javax.servlet.http.HttpServletResponse.SC_OK;

import hudson.model.Api;
import org.acegisecurity.AuthenticationException;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import se.diabol.jenkins.pipeline.trigger.TriggerException;

import java.io.IOException;
import javax.servlet.ServletException;

public class PipelineApi extends Api {

    private final PipelineView view;

    public PipelineApi(PipelineView view) {
        super(view);
        this.view = view;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void doManualStep(StaplerRequest request,
                             StaplerResponse response,
                             @QueryParameter String project,
                             @QueryParameter String upstream,
                             @QueryParameter String buildId) throws IOException, ServletException {
        if (project != null && upstream != null && buildId != null) {
            try {
                view.triggerManual(project, upstream, buildId);
                response.setStatus(SC_OK);
            } catch (TriggerException e) {
                response.setStatus(SC_INTERNAL_SERVER_ERROR);
            } catch (AuthenticationException e) {
                response.setStatus(SC_FORBIDDEN);
            }
        } else {
            response.setStatus(SC_NOT_ACCEPTABLE);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void doRebuildStep(StaplerRequest request,
                              StaplerResponse response,
                              @QueryParameter String project,
                              @QueryParameter String buildId) throws IOException, ServletException {
        if (project != null && buildId != null) {
            try {
                view.triggerRebuild(project, buildId);
                response.setStatus(SC_OK);
            } catch (AuthenticationException e) {
                response.setStatus(SC_FORBIDDEN);
            }
        } else {
            response.setStatus(SC_NOT_ACCEPTABLE);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void doInputStep(StaplerRequest request,
                             StaplerResponse response,
                             @QueryParameter String project,
                             @QueryParameter String upstream,
                             @QueryParameter String buildId) throws IOException, ServletException {
        doManualStep(request, response, project, upstream, buildId);
    }

}
