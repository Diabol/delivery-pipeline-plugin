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
package se.diabol.jenkins.pipeline.domain.status;

import se.diabol.jenkins.pipeline.domain.status.promotion.PromotionStatus;

import java.util.List;

public final class StatusFactory {

    private StatusFactory() {
    }

    public static Status idle() {
        return new SimpleStatus(StatusType.IDLE, -1, -1);
    }

    public static Status running(int percentage, long lastActivity, long duration) {
        return new Running(percentage, lastActivity, duration);
    }

    public static Status queued(long lastActivity) {
        return new SimpleStatus(StatusType.QUEUED, lastActivity, -1);
    }

    public static Status success(long lastActivity, long duration, boolean promoted, List<PromotionStatus> promotions) {
        return new SimpleStatus(StatusType.SUCCESS, lastActivity, duration, promoted, promotions);
    }

    public static Status failed(long lastActivity, long duration, boolean promoted, List<PromotionStatus> promotions) {
        return new SimpleStatus(StatusType.FAILED, lastActivity, duration, promoted, promotions);
    }

    public static Status unstable(long lastActivity, long duration) {
        return new SimpleStatus(StatusType.UNSTABLE, lastActivity, duration);
    }

    public static Status cancelled(long lastActivity, long duration) {
        return new SimpleStatus(StatusType.CANCELLED, lastActivity, duration);
    }
    public static Status notBuilt(long lastActivity, long duration) {
        return new SimpleStatus(StatusType.NOT_BUILT, lastActivity, duration);
    }

    public static Status disabled() {
        return new SimpleStatus(StatusType.DISABLED, -1, -1);
    }
}
