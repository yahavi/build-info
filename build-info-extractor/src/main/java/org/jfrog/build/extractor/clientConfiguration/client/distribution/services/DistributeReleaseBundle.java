package org.jfrog.build.extractor.clientConfiguration.client.distribution.services;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.jfrog.build.api.util.Log;
import org.jfrog.build.client.JFrogHttpClient;
import org.jfrog.build.extractor.clientConfiguration.client.JFrogService;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.request.DistributeReleaseBundleRequest;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.response.DistributeReleaseBundleResponse;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.response.DistributionStatusResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.jfrog.build.extractor.clientConfiguration.util.JsonUtils.toJsonString;

public class DistributeReleaseBundle extends JFrogService<DistributeReleaseBundleResponse> {
    private static final String DISTRIBUTE_RELEASE_BUNDLE_ENDPOINT = "api/v1/distribution";
    private static final int DEFAULT_MAX_WAIT_MINUTES = 60;
    private static final int DEFAULT_SYNC_SLEEP_INTERVAL = 60;

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
    public DistributeReleaseBundleResponse execute(JFrogHttpClient client) throws IOException {
        log.info("Distributing: " + name + "/" + version);
        super.execute(client);
        log.info(String.format("Sync: Distributing %s/%s...", name, version));
        super.execute(client);
        // Sync distribution
        waitForDistribution(client);
        return result;
    }

    @Override
    protected void setResponse(InputStream stream) throws IOException {
        result = getMapper(false).readValue(stream, DistributeReleaseBundleResponse.class);
        log.debug("Distribution response: " + getStatusCode());
        log.debug("Response:  " + toJsonString(result));
    }

    private void waitForDistribution(JFrogHttpClient client) throws IOException {
        String trackerId = result.getId();
        GetStatus getStatusService = new GetStatus(name, version, trackerId, log);
        for (int timeElapsed = 0; timeElapsed < DEFAULT_MAX_WAIT_MINUTES * 60; timeElapsed += DEFAULT_SYNC_SLEEP_INTERVAL) {
            if (timeElapsed % 60 == 0) {
                log.info(String.format("Sync: Distributing %s/%s...", name, version));
            }
            List<DistributionStatusResponse> statusResponse = getStatusService.execute(client);
            if (statusResponse.get(0).getDistributionSiteStatus().getStatus().equals("Failed")) {
                throw new IOException("JFrog service failed. Received " + statusCode + ": " + toJsonString(statusResponse));
            }
            if (statusResponse.get(0).getDistributionSiteStatus().getStatus().equals("Completed")) {
                log.info("Distribution Completed!");
                return;
            }
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                throw new IOException("Fail to wait for Distribution sync", e);
            }
        }
        throw new IOException("Timeout for sync distribution");
    }
}
