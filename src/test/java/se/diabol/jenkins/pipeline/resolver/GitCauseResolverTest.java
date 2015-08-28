package se.diabol.jenkins.pipeline.resolver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import static org.junit.Assert.assertNull;

import hudson.model.Cause;
import hudson.plugins.git.GitStatus;

import org.junit.Test;

import se.diabol.jenkins.pipeline.domain.TriggerCause;

public class GitCauseResolverTest {

    @Test
    public void withCorrectCause() {
        TriggerCause cause = new GitCauseResolver().resolveCause(new GitStatus.CommitHookCause("sha1"));
        assertNotNull(cause);
        assertEquals(TriggerCause.TYPE_SCM, cause.getType());
        assertEquals("SCM", cause.getDescription());
    }

    @Test
    public void withUnknownCause() {
        assertNull(new GitCauseResolver().resolveCause(new Cause.RemoteCause("hostname", "note")));
    }


}
