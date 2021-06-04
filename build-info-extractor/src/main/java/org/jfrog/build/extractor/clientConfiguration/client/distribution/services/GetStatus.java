package org.jfrog.build.extractor.clientConfiguration.client.distribution.services;

import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.jfrog.build.api.dependency.BuildPatternArtifacts;
import org.jfrog.build.api.util.Log;
import org.jfrog.build.extractor.clientConfiguration.client.JFrogService;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.request.CreateReleaseBundleRequest;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.response.DistributionStatusResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.jfrog.build.extractor.clientConfiguration.util.JsonUtils.toJsonString;

public class GetStatus extends JFrogService<List<DistributionStatusResponse>> {
    private static final String GET_STATUS_ENDPOINT = "api/v1/release_bundle";
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
        HttpGet request = new HttpGet(buildUrlForGetStatus() );
        return request;
    }

    @Override
    protected void setResponse(InputStream stream) throws IOException {
        result = getMapper(true).readValue(stream, TypeFactory.defaultInstance().constructCollectionLikeType(List.class, DistributionStatusResponse.class));
    }

    private String buildUrlForGetStatus() {
        String url = GET_STATUS_ENDPOINT;
        if (StringUtils.isEmpty(name)) {
            return url + "/distribution";
        }
        url += "/" + name;
        if (StringUtils.isEmpty(version)) {
            return url + "/distribution";
        }
        url += "/" + version + "/distribution";
        if (StringUtils.isNotEmpty(trackerId)) {
            return url + "/" + trackerId;
        }
        return url;
    }
}
