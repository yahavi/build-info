package org.jfrog.build.extractor.clientConfiguration.client.distribution.response;

import java.util.List;

public class DistributeReleaseBundleResponse {
    String id;
    List<DistributionStatusResponse.TargetArtifactory> sites;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<DistributionStatusResponse.TargetArtifactory> getSites() {
        return sites;
    }

    public void setSites(List<DistributionStatusResponse.TargetArtifactory> sites) {
        this.sites = sites;
    }
}
