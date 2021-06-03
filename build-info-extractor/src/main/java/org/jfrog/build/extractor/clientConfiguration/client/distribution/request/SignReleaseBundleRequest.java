package org.jfrog.build.extractor.clientConfiguration.client.distribution.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignReleaseBundleRequest {
    @JsonProperty("storing_repository")
    String storingRepository;

    public SignReleaseBundleRequest() {
    }

    public SignReleaseBundleRequest(String storingRepository) {
        this.storingRepository = storingRepository;
    }

    public String getStoringRepository() {
        return storingRepository;
    }

    public void setStoringRepository(String storingRepository) {
        this.storingRepository = storingRepository;
    }
}
