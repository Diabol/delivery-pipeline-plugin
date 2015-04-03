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
package se.diabol.jenkins.pipeline;

import org.acegisecurity.BadCredentialsException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import se.diabol.jenkins.pipeline.trigger.TriggerException;

import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PipelineApiTest {

    @Test
    public void testDoManualStep() throws Exception {
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        StaplerResponse response = Mockito.mock(StaplerResponse.class);

        DeliveryPipelineView view = Mockito.mock(DeliveryPipelineView.class);
        doThrow(new TriggerException("Ops")).when(view).triggerManual("upstream", "downstream", "12");
        doThrow(new BadCredentialsException("Ops")).when(view).triggerManual("upstream", "downstream", "13");

        PipelineApi api = new PipelineApi(view);

        api.doManualStep(request, response, null, null, null);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);

        response = Mockito.mock(StaplerResponse.class);
        api.doManualStep(request, response, null, "hej", null);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);

        response = Mockito.mock(StaplerResponse.class);
        api.doManualStep(request, response, "hej", "hej", null);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);

        response = Mockito.mock(StaplerResponse.class);
        api.doManualStep(request, response, null, "hej", "hej");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);

        response = Mockito.mock(StaplerResponse.class);
        api.doManualStep(request, response, null, null, "hej");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);

        response = Mockito.mock(StaplerResponse.class);
        api.doManualStep(request, response,"upstream", "downstream", "12");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        response = Mockito.mock(StaplerResponse.class);
        api.doManualStep(request, response,"upstream", "downstream", "13");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);

        view = Mockito.mock(DeliveryPipelineView.class);
        api = new PipelineApi(view);
        response = Mockito.mock(StaplerResponse.class);
        api.doManualStep(request, response,"upstream", "downstream", "14");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);
        verify(view).triggerManual("upstream", "downstream", "14");
    }


    @Test
    public void testDoRebuild() throws Exception {
        StaplerRequest request = Mockito.mock(StaplerRequest.class);
        StaplerResponse response = Mockito.mock(StaplerResponse.class);
        DeliveryPipelineView view = Mockito.mock(DeliveryPipelineView.class);
        doThrow(new BadCredentialsException("Ops")).when(view).triggerRebuild("secretproject", "1");
        PipelineApi api = new PipelineApi(view);

        api.doRebuildStep(request, response, null, null);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);

        api.doRebuildStep(request, response, "project", "1");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_OK);

        api.doRebuildStep(request, response, "secretproject", "1");
        verify(response, times(1)).setStatus(HttpServletResponse.SC_FORBIDDEN);

    }


}