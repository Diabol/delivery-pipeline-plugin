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
package se.diabol.jenkins.pipeline.test;

import hudson.scm.RepositoryBrowser;
import org.jvnet.hudson.test.FakeChangeLogSCM;

import java.io.IOException;
import java.net.URL;

public class FakeRepositoryBrowser extends RepositoryBrowser<FakeChangeLogSCM.EntryImpl> {

    private static final long serialVersionUID = -5144842224743489576L;

    @Override
    public URL getChangeSetLink(FakeChangeLogSCM.EntryImpl changeSet) throws IOException {
        return new URL("http://somewhere.com/" + changeSet.getAuthor().getDisplayName());
    }
}
