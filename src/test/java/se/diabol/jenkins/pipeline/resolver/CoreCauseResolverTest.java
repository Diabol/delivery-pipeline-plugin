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
package se.diabol.jenkins.pipeline.resolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import hudson.cli.BuildCommand;
import hudson.model.Cause;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import se.diabol.jenkins.pipeline.domain.TriggerCause;

public class CoreCauseResolverTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    private CoreCauseResolver resolver = new CoreCauseResolver();

    @Test
    @WithoutJenkins
    public void remote() {
        TriggerCause triggerCause = resolver.resolveCause(new Cause.RemoteCause("hostname", "note"));
        assertNotNull(triggerCause);
        assertEquals(TriggerCause.TYPE_REMOTE, triggerCause.getType());
    }

    @Test
    public void cliCause() {
        TriggerCause triggerCause = resolver.resolveCause(new BuildCommand.CLICause("username"));
        assertNotNull(triggerCause);
        assertEquals(TriggerCause.TYPE_MANUAL, triggerCause.getType());
    }

    @Test
    public void testNullUser() {
        assertEquals("anonymous", CoreCauseResolver.getDisplayName(null));
    }

}
