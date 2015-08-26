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
package se.diabol.jenkins.pipeline.domain.status.promotion;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.AbstractBuild;
import jenkins.model.Jenkins;

import java.util.List;

public abstract class AbstractPromotionStatusProvider implements ExtensionPoint {

    static private JenkinsInstanceProvider jenkinsInstanceProvider = new JenkinsInstanceProvider();

    abstract public boolean isBuildPromoted(AbstractBuild<?, ?> build);
    abstract public List<PromotionStatus> getPromotionStatusList(AbstractBuild<?, ?> build);

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
