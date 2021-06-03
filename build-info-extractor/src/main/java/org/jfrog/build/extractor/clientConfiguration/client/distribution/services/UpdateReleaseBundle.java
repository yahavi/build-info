package org.jfrog.build.extractor.clientConfiguration.client.distribution.services;

import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.jfrog.build.api.util.Log;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.request.UpdateReleaseBundleRequest;

import java.io.IOException;

import static org.jfrog.build.extractor.clientConfiguration.util.JsonUtils.toJsonString;

public class UpdateReleaseBundle extends VoidDistributionService {
    private static final String UPDATE_RELEASE_BUNDLE_ENDPOINT = "api/v1/release_bundle";
    private final UpdateReleaseBundleRequest updateReleaseBundleRequest;
    private final String name;
    private final String version;
    private final String gpgPassphrase;

    public UpdateReleaseBundle(UpdateReleaseBundleRequest updateReleaseBundleRequest, String name, String version, String gpgPassphrase, Log logger) {
        super(logger);
        this.updateReleaseBundleRequest = updateReleaseBundleRequest;
        this.name = name;
        this.version = version;
        this.gpgPassphrase = gpgPassphrase;
    }

    @Override
    public HttpRequestBase createRequest() throws IOException {
        HttpPut request = new HttpPut(String.format("%s/%s/%s", UPDATE_RELEASE_BUNDLE_ENDPOINT, name, version));
        request.setHeader("Accept", " application/json");
        request.setHeader("X-GPG-PASSPHRASE", gpgPassphrase);
        StringEntity stringEntity = new StringEntity(toJsonString(updateReleaseBundleRequest));
        stringEntity.setContentType("application/json");
        request.setEntity(stringEntity);
        return request;
    }
}
