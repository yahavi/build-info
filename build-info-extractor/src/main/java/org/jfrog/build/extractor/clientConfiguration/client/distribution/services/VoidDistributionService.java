package org.jfrog.build.extractor.clientConfiguration.client.distribution.services;

import org.apache.commons.io.IOUtils;
import org.jfrog.build.api.util.Log;
import org.jfrog.build.extractor.clientConfiguration.client.VoidJFrogService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

abstract class VoidDistributionService extends VoidJFrogService {
    protected VoidDistributionService(Log log) {
        super(log);
    }

    @Override
    protected void setResponse(InputStream stream) throws IOException {
        log.debug("Distribution response: " + getStatusCode());
        String ResponseMessage = IOUtils.toString(stream, StandardCharsets.UTF_8);
        log.debug("Response:  " + ResponseMessage);
    }
}
