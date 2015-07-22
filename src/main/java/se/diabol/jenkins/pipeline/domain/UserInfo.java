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
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import org.kohsuke.stapler.export.Exported;

import java.util.HashSet;
import java.util.Set;

public class UserInfo extends AbstractItem {

    private final String url;

    public UserInfo(String name, String url) {
        super(name);
        this.url = url;
    }

    @Exported
    public String getUrl() {
        return url;
    }

    public static UserInfo getUser(User user) {
        return new UserInfo(user.getDisplayName(), user.getUrl());
    }

    public static Set<UserInfo> getContributors(AbstractBuild<?, ?> build) {
        Set<UserInfo> contributors = new HashSet<UserInfo>();
        for (ChangeLogSet.Entry entry : build.getChangeSet()) {
            contributors.add(UserInfo.getUser(entry.getAuthor()));
        }
        return contributors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserInfo userInfo = (UserInfo) o;

        return userInfo.getName().equals(getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
