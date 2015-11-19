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

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.ChangeLogSet;
import hudson.scm.NullSCM;
import org.jvnet.hudson.test.FakeChangeLogSCM;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * We have to mimic FakeChangeLogSCM because by default it doesn't call "setParent" on Entries.
 *
 */
public class ParentAwareSCM extends NullSCM {

    private List<FakeChangeLogSCM.EntryImpl> entries = new ArrayList();

    public FakeChangeLogSCM.EntryImpl addChange() {
        FakeChangeLogSCM.EntryImpl e = new Entry();
        this.entries.add(e);
        return e;
    }

    @Override
    public ChangeLogParser createChangeLogParser() {
        return new FakeChangeLogSCM.FakeChangeLogParser() {
            @Override
            public FakeChangeLogSCM.FakeChangeLogSet parse(AbstractBuild build, File changelogFile) throws IOException, SAXException {
                FakeChangeLogSCM.FakeChangeLogSet changeLogSet = super.parse(build, changelogFile);

                // Call "setParent" on each entry
                for (FakeChangeLogSCM.EntryImpl entry : changeLogSet) {
                    ((Entry) entry).setParent(changeLogSet);
                }

                return changeLogSet;
            }
        };
    }

    @Override
    public boolean checkout(AbstractBuild<?, ?> build, Launcher launcher, FilePath remoteDir, BuildListener listener, File changeLogFile) throws IOException, InterruptedException {
        (new FilePath(changeLogFile)).touch(0L);
        build.addAction(new FakeChangeLogSCM.ChangelogAction(this.entries));
        this.entries = new ArrayList();
        return true;
    }

    static class Entry extends FakeChangeLogSCM.EntryImpl {
        public void setParent(ChangeLogSet changeLogSet) {
            super.setParent(changeLogSet);
        }
    }
}
