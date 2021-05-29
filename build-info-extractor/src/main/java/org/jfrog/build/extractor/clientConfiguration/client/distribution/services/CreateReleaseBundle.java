package org.jfrog.build.extractor.clientConfiguration.client.distribution.services;

import org.apache.http.client.methods.HttpRequestBase;
import org.jfrog.build.api.util.Log;
import org.jfrog.build.extractor.clientConfiguration.client.VoidJFrogService;

import java.io.IOException;

public class CreateReleaseBundle extends VoidJFrogService {
    private static final String CREAT_RELEASE_BUNDLE_ENDPOINT = "api/v1/release_bundle";

    public CreateReleaseBundle(Log logger) {
        super(logger);
    }

    @Override
    public HttpRequestBase createRequest() throws IOException {
        return null;
    }
}
