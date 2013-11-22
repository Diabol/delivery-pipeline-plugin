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
package se.diabol.jenkins.pipeline.model.status;

import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public interface Status {
    @SuppressWarnings("unused")
    public boolean isIdle();

    @SuppressWarnings("unused")
    public boolean isQueued();

    @SuppressWarnings("unused")
    public boolean isRunning();

    @SuppressWarnings("unused")
    public boolean isSuccess();

    @SuppressWarnings("unused")
    public boolean isFailed();

    @SuppressWarnings("unused")
    public boolean isUnstable();

    @SuppressWarnings("unused")
    public boolean isCancelled();

    @SuppressWarnings("unused")
    public boolean isDisabled();


    @SuppressWarnings("unused")
    long getLastActivity();

    @SuppressWarnings("unused")
    String getTimestamp();

    @SuppressWarnings("unused")
    long getDuration();
}
