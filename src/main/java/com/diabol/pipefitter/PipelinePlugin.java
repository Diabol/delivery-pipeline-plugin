package com.diabol.pipefitter;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.RootAction;

import java.util.logging.Logger;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelinePlugin extends Plugin {

    private static final Logger LOGGER = Logger.getLogger(PipelinePlugin.class.getName());

    public static final String PLUGIN_NAME = "pipefitter";

    @Override
    public void start() throws Exception {
       LOGGER.info("Started");
    }

    @Override
    public void stop() throws Exception {
        LOGGER.info("Stopped");
    }


    @Extension
    public static class PipelineRootLink implements RootAction {

        public String getIconFileName() {
            return "/plugin/" + PLUGIN_NAME + "/images/pipeline-icon.png";
        }

        public String getDisplayName() {
            return "Pipeline configuration";
        }

        public String getUrlName() {
            return "/plugin/" + PLUGIN_NAME + "/";
        }
    }
}
