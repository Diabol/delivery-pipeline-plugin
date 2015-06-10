package se.diabol.jenkins.pipeline.domain.status.promotion;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import jenkins.model.Jenkins;

import java.util.List;

public abstract class AbstractPromotionStatusProvider implements ExtensionPoint {

    static private JenkinsInstanceProvider jenkinsInstanceProvider = new JenkinsInstanceProvider();

    abstract public boolean isBuildPromoted(AbstractBuild build);
    abstract public List<PromotionStatus> getPromotionStatusList(AbstractBuild build);

    public static ExtensionList<AbstractPromotionStatusProvider> all() {
        return AbstractPromotionStatusProvider.jenkinsInstanceProvider.getJenkinsInstance().getExtensionList(AbstractPromotionStatusProvider.class);
    }

    // package scope setters for unit testing

    static void setJenkinsInstanceProvider(JenkinsInstanceProvider jenkinsInstanceProvider) {
        AbstractPromotionStatusProvider.jenkinsInstanceProvider = jenkinsInstanceProvider;
    }

    // Decorators to make code unit-testable

    static class JenkinsInstanceProvider {
        public Jenkins getJenkinsInstance() {
            return Jenkins.getInstance();
        }
    }
}
