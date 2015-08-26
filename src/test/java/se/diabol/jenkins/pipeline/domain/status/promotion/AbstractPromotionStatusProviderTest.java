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
package se.diabol.jenkins.pipeline.domain.status.promotion;

import hudson.ExtensionList;
import jenkins.model.Jenkins;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AbstractPromotionStatusProviderTest {

    @Test
    public void testGetAll() {
        final Jenkins mockJenkins = mock(Jenkins.class);

        @SuppressWarnings("unchecked")
        final ExtensionList<AbstractPromotionStatusProvider> mockExtensionList = mock(ExtensionList.class);
        when(mockJenkins.getExtensionList(AbstractPromotionStatusProvider.class)).thenReturn(mockExtensionList);

        final AbstractPromotionStatusProvider.JenkinsInstanceProvider mockJenkinsInstanceProvider = mock(AbstractPromotionStatusProvider.JenkinsInstanceProvider.class);
        when(mockJenkinsInstanceProvider.getJenkinsInstance()).thenReturn(mockJenkins);

        AbstractPromotionStatusProvider.setJenkinsInstanceProvider(mockJenkinsInstanceProvider);

        final ExtensionList<AbstractPromotionStatusProvider> extensionList = AbstractPromotionStatusProvider.all();
        assertNotNull(extensionList);
        assertSame(mockExtensionList, extensionList);
    }
}
