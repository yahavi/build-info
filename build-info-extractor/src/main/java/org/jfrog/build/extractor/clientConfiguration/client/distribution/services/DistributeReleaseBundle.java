package org.jfrog.build.extractor.clientConfiguration.client.distribution.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.jfrog.build.api.util.Log;
import org.jfrog.build.client.JFrogHttpClient;
import org.jfrog.build.extractor.clientConfiguration.client.JFrogService;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.request.DistributeReleaseBundleRequest;

import java.io.IOException;
import java.io.InputStream;

import static org.jfrog.build.extractor.clientConfiguration.util.JsonUtils.toJsonString;

public class DistributeReleaseBundle extends JFrogService<String> {
    private static final String DISTRIBUTE_RELEASE_BUNDLE_ENDPOINT = "api/v1/distribution";
    private final String name;
    private final String version;
    private final DistributeReleaseBundleRequest distributeReleaseBundleRequest;

    protected DistributeReleaseBundle(String name, String version, DistributeReleaseBundleRequest distributeReleaseBundleRequest, Log log) {
        super(log);
        this.name = name;
        this.version = version;
        this.distributeReleaseBundleRequest = distributeReleaseBundleRequest;
    }

    @Override
    public HttpRequestBase createRequest() throws IOException {
        HttpPost request = new HttpPost(String.format("%s/%s/%s", DISTRIBUTE_RELEASE_BUNDLE_ENDPOINT, name, version));
        request.setHeader("Accept", " application/json");
        StringEntity stringEntity = new StringEntity(toJsonString(distributeReleaseBundleRequest));
        stringEntity.setContentType("application/json");
        request.setEntity(stringEntity);
        return request;
    }

    @Override
    public String execute(JFrogHttpClient client) throws IOException {
        log.info("Distributing: " + name + "/" + version);
        String trackerId = super.execute(client);
        log.info(String.format("Sync: Distributing %s/%s...", name, version));
        return super.execute(client);
    }

    @Override
    protected void setResponse(InputStream stream) throws IOException {
        JsonNode response = getMapper(false).readTree(stream);
        log.debug("Distribution response: " + getStatusCode());
        log.debug("Response:  " + response);
        result = response.get("id").asText();
    }
}
