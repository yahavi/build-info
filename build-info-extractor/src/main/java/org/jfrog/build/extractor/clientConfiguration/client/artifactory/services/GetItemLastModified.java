package org.jfrog.build.extractor.clientConfiguration.client.artifactory.services;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.jfrog.build.api.util.Log;
import org.jfrog.build.client.ItemLastModified;
import org.jfrog.build.extractor.clientConfiguration.client.JFrogService;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

public class GetItemLastModified extends JFrogService<ItemLastModified> {
    public static final String ITEM_LAST_MODIFIED = "api/storage/";

    private final String path;

    public GetItemLastModified(String path, Log logger) {
        super(logger);
        this.path = path;
    }

    @Override
    public HttpRequestBase createRequest() {
        return new HttpGet(ITEM_LAST_MODIFIED + path + "?lastModified");
    }

    @Override
    protected void handleEmptyEntity() throws IOException {
        throw new IOException("The path " + path + " returned empty entity");
    }

    @Override
    protected void setResponse(InputStream stream) throws IOException {
        result = getMapper().readValue(stream, ItemLastModified.class);
        try {
            if (result.getLastModified() == 0 || result.getUri() == null) {
                throw new IOException("JSON response is missing URI or LastModified fields when requesting info for path " + path);
            }
        } catch (ParseException e) {
            throw new IOException("JSON response is missing URI or LastModified fields when requesting info for path " + path);
        }
    }

    @Override
    protected void handleUnsuccessfulResponse(HttpEntity entity) throws IOException {
        log.error("Failed while requesting item info for path: " + path);
        throwException(entity, getStatusCode());
    }
}
