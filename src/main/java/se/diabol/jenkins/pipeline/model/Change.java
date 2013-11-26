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
package se.diabol.jenkins.pipeline.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Change {
    private UserInfo author;
    private String message;
    private String commitId;
    private String changeLink;

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
}
