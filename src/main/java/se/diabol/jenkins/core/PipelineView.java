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

import org.acegisecurity.AuthenticationException;
import se.diabol.jenkins.pipeline.trigger.TriggerException;

import java.util.List;

/**
 * Interface defining common methods for delivery pipeline views.
 * Note that Jenkins does <b>NOT</b> support the following:
 * <ul>
 *   <li>Java 8 interface default methods</li>
 *   <li><tt>@Exported</tt> annotations on interface methods - these needs to be set on the method in the
 *   implementing class</li>
 * </ul>
 */
public interface PipelineView {

    void triggerManual(String projectName, String upstreamName, String buildId)
            throws TriggerException, AuthenticationException;

    void triggerRebuild(String projectName, String buildId);

    void abortBuild(String projectName, String buildId) throws TriggerException, AuthenticationException;

    /**
     * Whether to allow a new pipeline to be started from the pipeline view.
     * Expected to be annotated with <tt>@Exported</tt> for consumption from the view page.
     *
     * @return whether to allow a new pipeline to be started from the pipeline view.
     */
    boolean isAllowPipelineStart();

    /**
     * Whether to allow running pipelines to be aborted from the pipeline view.
     * Expected to be annotated with <tt>@Exported</tt> for consumption from the view page.
     *
     * @return whether to allow pipelines to be aborted from the pipeline view.
     */
    boolean isAllowAbort();

    /**
     * A date time string representing when the pipeline view was last updated.
     * Expected to be annotated with <tt>@Exported</tt> for consumption from the view page.
     *
     * @return a string representation on when the pipeline view was last updated.
     */
    String getLastUpdated();

    /**
     * Exposes any error that is currently associated with the pipeline view.
     *
     * @return the error currently associated with this pipeline view.
     */
    String getError();

    /**
     * Resolve the pipelines associated with this view.
     * Expected to be annotated with <tt>@Exported</tt> for consumption from the view page.
     *
     * @return a list of components associated with this pipeline view.
     */
    List<? extends GenericComponent> getPipelines();
}
