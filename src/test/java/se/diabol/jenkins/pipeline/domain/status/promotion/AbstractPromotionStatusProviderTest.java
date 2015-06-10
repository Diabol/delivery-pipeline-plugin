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
