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

import org.kohsuke.stapler.StaplerRequest;

public class FullScreen {

    public static boolean isFullScreenRequest(StaplerRequest request) {
        return request != null && hasFullScreenParameter(request)
                && Boolean.parseBoolean(getFullScreenParameter(request));
    }

    static boolean hasFullScreenParameter(StaplerRequest request) {
        return getFullScreenParameter(request) != null;
    }

    static String getFullScreenParameter(StaplerRequest request) {
        return request.getParameter("fullscreen");
    }

}
