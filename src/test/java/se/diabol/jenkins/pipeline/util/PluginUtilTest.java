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

import static org.junit.Assert.fail;

import org.junit.Test;

public class PluginUtilTest {

    @Test(expected=IllegalStateException.class)
    public void testIsPluginInstalledNoJenkins() {
        PluginUtil.isPluginInstalled("analysis-core");
        fail("Should throw exception");
    }

    @Test(expected=IllegalAccessException.class)
    public void testConstructorPrivate() throws Exception {
        PluginUtil.class.newInstance();
        fail("Utility class constructor should be private");
    }
}
