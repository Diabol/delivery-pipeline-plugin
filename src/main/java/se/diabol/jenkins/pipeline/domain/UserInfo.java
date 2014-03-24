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

import hudson.ExtensionList;
import hudson.model.AbstractBuild;
import hudson.model.User;
import hudson.scm.ChangeLogSet;
import hudson.tasks.UserAvatarResolver;
import org.kohsuke.stapler.export.Exported;

import java.util.HashSet;
import java.util.Set;

public class UserInfo extends AbstractItem {

    private static final int AVATAR_SIZE = 16;


    private final String avatarUrl;
    private final String url;

    public UserInfo(String name, String url, String avatarUrl) {
        super(name);
        this.avatarUrl = avatarUrl;
        this.url = url;
    }

    @Exported
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Exported
    public String getUrl() {
        return url;
    }

    public static UserInfo getUser(User user) {
        return new UserInfo(user.getDisplayName(), user.getUrl(), getAvatarUrl(user));
    }

    private static String getAvatarUrl(User user) {
        ExtensionList<UserAvatarResolver> resolvers = UserAvatarResolver.all();
        for (UserAvatarResolver resolver : resolvers) {
            String avatarUrl = resolver.findAvatarFor(user, AVATAR_SIZE, AVATAR_SIZE);
            if (avatarUrl != null) {
                return avatarUrl;
            }
        }
        return null;
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

        return userInfo.getName().equals(userInfo.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
