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
package se.diabol.jenkins.workflow.util;

import com.cloudbees.hudson.plugins.folder.Folder;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class NameTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void shouldReturnNameOfWorkflowRun() throws Exception {
        String expectedName = "expectedJobName";
        WorkflowJob workflowJob = jenkins.jenkins.createProject(WorkflowJob.class, expectedName);
        WorkflowRun workflowRun = new WorkflowRun(workflowJob);
        assertThat(Name.of(workflowRun), is(expectedName));
    }

    @Test
    public void shouldIncludeFolderNameOfWorkflowRunLocatedInFolder() throws IOException {
        String jobName = "expectedJobName";
        String folderName = "myfolder";
        Folder folder = jenkins.jenkins.createProject(Folder.class, folderName);
        WorkflowJob workflowJob = folder.createProject(WorkflowJob.class, jobName);
        WorkflowRun workflowRun = new WorkflowRun(workflowJob);
        assertThat(Name.of(workflowRun), is(folderName + "/" + jobName));
    }

    @Test
    public void shouldIncludeParentNameOfWorkflowRunWhenParentIsMultiBranch() throws Exception {
        String folderName = "folder";
        Folder folder = jenkins.jenkins.createProject(Folder.class, folderName);
        MultiBranchProject multiBranch = new WorkflowMultiBranchProject(folder, "mb");
        WorkflowJob workflowJob = new WorkflowJob(multiBranch, "wf");
        WorkflowRun workflowRun = new WorkflowRun(workflowJob);

        assertThat(Name.of(workflowRun), is("folder/mb/wf"));
    }
    
    @Test
    public void shouldIncludeParentNameOfWorkflowRunLocatedInFolderLocatedInFolder()
            throws IOException {
        String jobName = "expectedJobName";
        String rootFolderName = "myroot";
        String leafFolderName = "myleaf";
        Folder rootFolder = jenkins.jenkins.createProject(Folder.class, rootFolderName);
        Folder leafFolder = jenkins.jenkins.createProject(Folder.class, leafFolderName);
        rootFolder.createProject(Folder.class, leafFolderName);
        WorkflowJob workflowJob = leafFolder.createProject(WorkflowJob.class, jobName);
        WorkflowRun workflowRun = new WorkflowRun(workflowJob);
        assertThat(Name.of(workflowRun), is(leafFolderName + "/" + jobName));
    }

    @Test
    @SuppressWarnings("AccessStaticViaInstance")
    public void nullNameShouldReturnNull() {
        assertNull(new Name().of(null));
    }

}
