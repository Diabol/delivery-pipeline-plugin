package se.diabol.jenkins.workflow;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

import java.io.IOException;
import java.util.logging.Logger;

public class WorkflowApi {

    private static final Logger LOG = Logger.getLogger(WorkflowApi.class.getName());

    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();
    private static final String URL = "http://localhost:8080/jenkins/job/";

    static void lastRunFor(String job) {
        try {
            String url = URL + job + "/wfapi/runs";
            HttpRequest request = requestFor(url);
            LOG.info("Getting workflow runs for " + job + ": " + url);
            HttpResponse response = request.execute();
            LOG.info("Received workflow runs for " + job + ": " + response.parseAsString());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static HttpRequest requestFor(String url) throws IOException {
        HttpRequest request = requestFactory().buildGetRequest(new GenericUrl(url));
        request.setConnectTimeout(1750);
        request.setReadTimeout(1750);
        return request;
    }

    public static HttpRequestFactory requestFactory() {
        return HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) {
                request.setParser(new JsonObjectParser(JSON_FACTORY));
            }
        });
    }
}
