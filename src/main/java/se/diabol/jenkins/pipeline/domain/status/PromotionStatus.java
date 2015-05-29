package se.diabol.jenkins.pipeline.domain.status;


import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.domain.AbstractItem;

import java.util.List;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class PromotionStatus {
    final private String name;
    final private long duration;
    final private long startTime;
    final private String user;
    final private String icon;
    final private List<String> params;

    public PromotionStatus(String name, long startTime, long duration, String user, String icon, List<String> params) {
        this.name = name;
        this.startTime = startTime;
        this.duration = duration;
        this.user = user;
        this.icon = icon;
        this.params = params;
    }

    @Exported
    public String getName() {
        return name;
    }

    @Exported
    public long getStartTime() {
        return startTime;
    }

    @Exported
    public long getDuration() {
        return duration;
    }

    @Exported
    public String getUser() {
        return user;
    }

    @Exported
    public String getIcon() {
        return icon;
    }

    @Exported
    public List<String> getParams() {
        return params;
    }
}
