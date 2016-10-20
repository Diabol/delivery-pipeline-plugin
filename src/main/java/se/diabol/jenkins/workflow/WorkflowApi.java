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
package se.diabol.jenkins.workflow;

import com.google.api.client.http.EmptyContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;
import jenkins.model.Jenkins;
import se.diabol.jenkins.workflow.api.Json;
import se.diabol.jenkins.workflow.api.Run;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class WorkflowApi {

    private static final Logger LOG = Logger.getLogger(WorkflowApi.class.getName());
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private Jenkins jenkins;

    public WorkflowApi(final Jenkins instance) {
        this.jenkins = instance;
    }

    public List<Run> getRunsFor(String job) {
        try {
            HttpRequest request = requestFor(workflowApiUrl(job) + "runs");
            LOG.fine("Getting workflow runs for " + job + " from Workflow API: " + request.getUrl());
            String responseString = execute(request);
            LOG.fine("Received workflow runs for " + job + ": " + responseString);
            return asListOfRuns(responseString);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public Run lastRunFor(String job) {
        return returnFirstOrNull(getRunsFor(job));
    }

    public Run lastFinishedRunFor(String job) {
        for (Run run : getRunsFor(job)) {
            if (!"IN_PROGRESS".equals(run.status) && !"PAUSED_PENDING_INPUT".equals(run.status)) {
                return run;
            }
        }
        return null;
    }

    protected String execute(HttpRequest request) throws IOException {
        HttpResponse response = request.execute();
        return response.parseAsString();
    }

    private static Run returnFirstOrNull(List<Run> runs) {
        if (!runs.isEmpty()) {
            return runs.get(0);
        } else {
            return null;
        }
    }

    protected static HttpRequest requestFor(String url) throws IOException {
        HttpRequest request = requestFactory().buildGetRequest(new GenericUrl(url));
        request.setConnectTimeout(timeoutThreshold(WorkflowPipelineView.DEFAULT_INTERVAL));
        request.setReadTimeout(timeoutThreshold(WorkflowPipelineView.DEFAULT_INTERVAL));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType("application/json; charset=UTF-8");
        headers.setAcceptEncoding("UTF-8");
        headers.setAccept("*/*");
        request.setContent(new EmptyContent());
        headers.setContentLength(request.getContent().getLength());
        request.setHeaders(headers);
        return request;
    }

    protected static int timeoutThreshold(int updateInterval) {
        return (updateInterval <= 0 ? 1 : updateInterval) * 1000 - 250;
    }

    public static HttpRequestFactory requestFactory() {
        return HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) {
                request.setParser(new JsonObjectParser(JSON_FACTORY));
            }
        });
    }

    public static List<Run> asListOfRuns(String response) {
        Run[] runs = Json.deserialize(response, Run[].class);
        return Arrays.asList(runs);
    }

    private String workflowApiUrl(String jobName) {
        return jenkinsUrl() + "job/" + jobName + "/wfapi/";
    }

    protected String jenkinsUrl() {
        return jenkins.getRootUrl();
    }
}
