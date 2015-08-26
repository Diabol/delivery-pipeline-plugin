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
package se.diabol.jenkins.pipeline.util;

import jenkins.model.Jenkins;

public final class JenkinsUtil {

    private JenkinsUtil() {
    }

    /**
     * Gets the Jenkins singleton.
     * @return the instance, or null if Jenkins has not been started, or was already shut down
     * @throws IllegalStateException {@link Jenkins} has not been started, or was already shut down
     */
    public static Jenkins getInstance() {
        Jenkins instance = Jenkins.getInstance();
        if (instance == null) {
            throw new IllegalStateException("Jenkins has not been started, or was already shut down");
        }
        return instance;
    }

    /**
     * Returns whether the specified plug-in is installed.
     *
     * @param shortName
     *            the plug-in to check
     * @return <code>true</code> if the specified plug-in is installed,
     *         <code>false</code> if not.
     */
    public static boolean isPluginInstalled(final String shortName) {
        return getInstance().getPlugin(shortName) != null;
    }



}
