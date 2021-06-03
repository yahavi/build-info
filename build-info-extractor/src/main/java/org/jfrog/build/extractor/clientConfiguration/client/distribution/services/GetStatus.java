package org.jfrog.build.extractor.clientConfiguration.client.distribution.services;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.jfrog.build.api.util.Log;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.request.CreateReleaseBundleRequest;

import java.io.IOException;

import static org.jfrog.build.extractor.clientConfiguration.util.JsonUtils.toJsonString;

public class GetStatus extends VoidDistributionService {
    private static final String GET_STATUS_ENDPOINT = "api/v1/release_bundle";
    private CreateReleaseBundleRequest createReleaseBundleRequest;
    private String gpgPassphrase;
    private String name;
    private String version;
    private String trackerId;

    public GetStatus(String name, String version,String trackerId, Log logger) {
        super(logger);
        this.name = name;
        this.version = version;
        this.trackerId = trackerId;
    }

    @Override
    public HttpRequestBase createRequest() throws IOException {
        HttpPost request = new HttpPost(GET_STATUS_ENDPOINT);
        request.setHeader("Accept"," application/json");
        request.setHeader("X-GPG-PASSPHRASE",gpgPassphrase);
        StringEntity stringEntity = new StringEntity(toJsonString(createReleaseBundleRequest));
        stringEntity.setContentType("application/json");
        request.setEntity(stringEntity);
        return request;
    }

    private String buildUrlForGetStatus() {
        String url = GET_STATUS_ENDPOINT;
        if (StringUtils.isEmpty(name)){
            return url+"/distribution";
        }
        url += "/"+name;
        if (StringUtils.isEmpty(version)){
            return url+"/distribution";
        }
        url += "/"+version+ "/distribution";
        if (StringUtils.isEmpty(trackerId)){

        }
    }
}
