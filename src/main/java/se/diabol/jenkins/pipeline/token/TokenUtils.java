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

import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;



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
            return decode(build, template);
        } catch (MacroEvaluationException e) {
            LOG.log(Level.FINE, "Failed to evaluate token using token-macro plugin", e);
            return template;
        } catch (Exception e) {
            LOG.log(Level.WARNING, TokenUtils.MESSAGE + e.getMessage());
            return "";
        }
    }

    private static String decode(AbstractBuild<?, ?> build, String template)
            throws MacroEvaluationException, IOException, InterruptedException {
        if (build == null) {
            return hideVariable(template);
        } else {
            // If workspace is not available, build a phony one and expand anyway.
            // This avoids broken variable expansion on builds with transient workspaces
            FilePath workspace = build.getWorkspace();
            if (workspace == null) {
                workspace = new FilePath((hudson.remoting.VirtualChannel) null, "") ;
                return TokenMacro
                        .expandAll(build, workspace, TaskListener.NULL, template, true, (List<TokenMacro>)null);
            }
            return TokenMacro.expandAll(build, TaskListener.NULL, template);
        }
    }

    private static String hideVariable(String template) {
        return template.replaceAll("\\$\\{.*?\\}", "...");
    }

    public static boolean stringIsNotEmpty(String string) {
        return string != null && !"".equals(string);
    }
}
