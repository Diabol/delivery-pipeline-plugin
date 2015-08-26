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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import se.diabol.jenkins.pipeline.test.TestUtil;

import static org.junit.Assert.fail;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Jenkins.class)
public class JenkinsUtilTest {

    @Test
    public void testValidUtilClass() throws Exception {
        TestUtil.assertUtilityClassWellDefined(JenkinsUtil.class);
    }

    @Test(expected = IllegalStateException.class)
    public void getInstanceJenkinsReturnsNull() throws Exception {
        PowerMockito.mockStatic(Jenkins.class);
        PowerMockito.when(Jenkins.getInstance()).thenReturn(null);
        JenkinsUtil.getInstance();
    }

    @Test(expected=IllegalStateException.class)
    public void testIsPluginInstalledNoJenkins() {
        JenkinsUtil.isPluginInstalled("analysis-core");
        fail("Should throw exception");
    }


}
