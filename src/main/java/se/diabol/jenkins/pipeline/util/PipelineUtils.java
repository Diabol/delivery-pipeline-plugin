package se.diabol.jenkins.pipeline.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class PipelineUtils {

    public static String formatTimestamp(long timestamp) {
        DateFormat f = new SimpleDateFormat("YYYY-MM-dd'T'HH:mm:ss.SSS'Z'");
        return f.format(new Date(timestamp));
    }

}
