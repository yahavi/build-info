package org.jfrog.build.extractor.clientConfiguration.client.distribution.request;

public class CreateReleaseBundleRequest extends UpdateReleaseBundleRequest {
    String name;
    String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
