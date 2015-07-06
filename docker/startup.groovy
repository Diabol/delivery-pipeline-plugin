import hudson.cli.BuildCommand
import hudson.model.AbstractProject
import jenkins.model.*;


Jenkins.getInstance().getItem("generate-jobs", Jenkins.getInstance(), AbstractProject.class).scheduleBuild(0, new BuildCommand.CLICause())
