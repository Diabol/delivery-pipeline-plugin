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
package se.diabol.jenkins.pipeline.token;

import hudson.model.TaskListener;
import hudson.model.AbstractBuild;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

public final class TokenUtils {

    private TokenUtils() {
    }

    private static final Logger LOG = Logger.getLogger(TokenUtils.class.getName());
    private static final String MESSAGE = "Error during decoding template - ";

    /**
     * Expand all tokens for provided template via TokenMacro.expandAll
     * @param build - current build
     * @param template - template to decode
     * @return decoded template or empty when an error occurred or build is empty
     */
    public static String decodedTemplate(AbstractBuild<?, ?> build, String template) {
        try {
            if (build != null) {
                return TokenMacro.expandAll(build, TaskListener.NULL, template);
            } else {
                /* if we don't have build we should hide variable ex. ${VAR} */
                return template.replaceAll("\\$\\{.*?\\}", "...");
            }
        } catch (MacroEvaluationException e) {
            LOG.log(Level.WARNING, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.WARNING, TokenUtils.MESSAGE + e.getMessage());
        }
        return "";
    }

    public static boolean stringIsNotEmpty(String string) {
        if (string == null || "".equals(string)) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }
    }
}
