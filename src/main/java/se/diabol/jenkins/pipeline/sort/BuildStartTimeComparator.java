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
package se.diabol.jenkins.pipeline.sort;

import hudson.model.AbstractBuild;

import java.io.Serializable;
import java.util.Comparator;

public final class BuildStartTimeComparator implements Comparator<AbstractBuild>, Serializable {

    private static final long serialVersionUID = -4594547197445847342L;

    @Override
    public int compare(AbstractBuild build1, AbstractBuild build2) {
        return compare(build2.getStartTimeInMillis(), build1.getStartTimeInMillis());
    }

    protected int compare(long first, long second) {
        return Long.compare(first, second);
    }
}
