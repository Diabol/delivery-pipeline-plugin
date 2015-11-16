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
package se.diabol.jenkins.pipeline.domain;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet;
import hudson.scm.RepositoryBrowser;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Change {
    private UserInfo author;
    private String message;
    private String commitId;
    private String changeLink;

    private static final Logger LOG = Logger.getLogger(Change.class.getName());


    public Change(UserInfo author, String message, String commitId, String changeLink) {
        this.author = author;
        this.message = message;
        this.commitId = commitId;
        this.changeLink = changeLink;
    }

    @Exported
    public UserInfo getAuthor() {
        return author;
    }

    @Exported
    public String getMessage() {
        return message;
    }

    @Exported
    public String getCommitId() {
        return commitId;
    }

    @Exported
    public String getChangeLink() {
        return changeLink;
    }

    public static List<Change> getChanges(AbstractBuild<?, ?> build) {
        RepositoryBrowser repositoryBrowser = build.getProject().getScm().getBrowser();
        List<Change> result = new ArrayList<Change>();
        for (ChangeLogSet.Entry entry : build.getChangeSet()) {
            UserInfo user = UserInfo.getUser(entry.getAuthor());
            String changeLink = null;
            if (repositoryBrowser != null) {
                try {
                    @SuppressWarnings("unchecked")
                    URL link = repositoryBrowser.getChangeSetLink(entry);
                    if (link != null) {
                        changeLink = link.toExternalForm();
                    }
                } catch (IOException e) {
                   LOG.log(Level.WARNING, "Could not get changeset link for: " + build.getProject().getFullDisplayName() + " " + build.getDisplayName(), e);
                }
            }
            result.add(new Change(user, entry.getMsgAnnotated(), entry.getCommitId(), changeLink));
        }
        return result;
    }



}
