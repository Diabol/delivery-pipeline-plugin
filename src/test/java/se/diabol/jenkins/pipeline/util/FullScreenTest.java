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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FullScreenTest {

    @Test
    public void shouldRecognizeFullScreenRequest() {
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("fullscreen")).thenReturn("true");
        assertThat(FullScreen.isFullScreenRequest(req), is(true));

        when(req.getParameter("fullscreen")).thenReturn("false");
        assertThat(FullScreen.isFullScreenRequest(req), is(false));
    }
    
    @Test
    public void shouldHaveFullScreenParameter() {
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("fullscreen")).thenReturn("true");
        assertThat(FullScreen.hasFullScreenParameter(req), is(true));

        when(req.getParameter("fullscreen")).thenReturn("false");
        assertThat(FullScreen.hasFullScreenParameter(req), is(true));

        when(req.getParameter("fullscreen")).thenReturn("arbitraryString");
        assertThat(FullScreen.hasFullScreenParameter(req), is(true));
    }

    @Test
    public void shouldNotHaveFullScreenParameter() {
        StaplerRequest req = mock(StaplerRequest.class);
        assertThat(FullScreen.hasFullScreenParameter(req), is(false));
    }

    @Test
    public void shouldGetFullScreenParameter() {
        StaplerRequest req = mock(StaplerRequest.class);
        when(req.getParameter("fullscreen")).thenReturn("true");
        assertThat(FullScreen.getFullScreenParameter(req), is("true"));

        when(req.getParameter("fullscreen")).thenReturn("false");
        assertThat(FullScreen.getFullScreenParameter(req), is("false"));

        when(req.getParameter("fullscreen")).thenReturn("arbitraryString");
        assertThat(FullScreen.getFullScreenParameter(req), is("arbitraryString"));
    }

    @Test
    public void shouldNotGetFullScreenParameter() {
        StaplerRequest req = mock(StaplerRequest.class);
        assertNull(FullScreen.getFullScreenParameter(req));
    }
}
