package org.jfrog.build.extractor.clientConfiguration.client.artifactory.services;

import org.apache.commons.io.IOUtils;
import org.jfrog.build.api.util.Log;
import org.jfrog.build.client.DownloadResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class Download extends DownloadBase<DownloadResponse> {
    public Download(String downloadFrom, Map<String, String> headers, Log log) {
        super(downloadFrom, false, headers, log);
    }

    @Override
    protected void setResponse(InputStream stream) throws IOException {
        result = new DownloadResponse(IOUtils.toString(stream, StandardCharsets.UTF_8), getHeaders());
    }
}
