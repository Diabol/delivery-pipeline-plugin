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

import hudson.Extension;
import hudson.model.Cause;
import hudson.plugins.git.GitStatus;

import se.diabol.jenkins.pipeline.CauseResolver;
import se.diabol.jenkins.pipeline.domain.TriggerCause;

@Extension(optional = true)
public class GitCauseResolver extends CauseResolver {

    // Force a classloading error plugin isn't available
    @SuppressWarnings("UnusedDeclaration")
    public static final Class CLASS = GitStatus.CommitHookCause.class;

    @Override
    public TriggerCause resolveCause(Cause cause) {
        if (cause instanceof GitStatus.CommitHookCause) {
            return new TriggerCause(TriggerCause.TYPE_SCM, "SCM");
        }
        return null;
    }
}
