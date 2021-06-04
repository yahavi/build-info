package org.jfrog.build.extractor.clientConfiguration.client.distribution.response;

import org.jfrog.build.extractor.clientConfiguration.client.distribution.request.DistributeReleaseBundleRequest;

import java.util.List;

public class DistributionStatusResponse {
    int Id;
    int FriendlyId;
    String type;
    String version;
    DistributionSiteStatus distributionSiteStatus;
    DistributeReleaseBundleRequest.DistributionRules distributionRules;
    List<DistributionSiteStatus> sites;

    public DistributionSiteStatus getDistributionSiteStatus() {
        return distributionSiteStatus;
    }

    public void setDistributionSiteStatus(DistributionSiteStatus distributionSiteStatus) {
        this.distributionSiteStatus = distributionSiteStatus;
    }

    public DistributeReleaseBundleRequest.DistributionRules getDistributionRules() {
        return distributionRules;
    }

    public void setDistributionRules(DistributeReleaseBundleRequest.DistributionRules distributionRules) {
        this.distributionRules = distributionRules;
    }

    public List<DistributionSiteStatus> getSites() {
        return sites;
    }

    public void setSites(List<DistributionSiteStatus> sites) {
        this.sites = sites;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getFriendlyId() {
        return FriendlyId;
    }

    public void setFriendlyId(int friendlyId) {
        FriendlyId = friendlyId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }


    public class DistributionSiteStatus {
        String status;
        String error;
        TargetArtifactory targetArtifactory;
        int totalFiles;
        int totalBytes;
        int distributedBytes;
        int distributedFiles;
        List<String> fileErrors;
        List<String> filesInProgress;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public TargetArtifactory getTargetArtifactory() {
            return targetArtifactory;
        }

        public void setTargetArtifactory(TargetArtifactory targetArtifactory) {
            this.targetArtifactory = targetArtifactory;
        }

        public int getTotalFiles() {
            return totalFiles;
        }

        public void setTotalFiles(int totalFiles) {
            this.totalFiles = totalFiles;
        }

        public int getTotalBytes() {
            return totalBytes;
        }

        public void setTotalBytes(int totalBytes) {
            this.totalBytes = totalBytes;
        }

        public int getDistributedBytes() {
            return distributedBytes;
        }

        public void setDistributedBytes(int distributedBytes) {
            this.distributedBytes = distributedBytes;
        }

        public int getDistributedFiles() {
            return distributedFiles;
        }

        public void setDistributedFiles(int distributedFiles) {
            this.distributedFiles = distributedFiles;
        }

        public List<String> getFileErrors() {
            return fileErrors;
        }

        public void setFileErrors(List<String> fileErrors) {
            this.fileErrors = fileErrors;
        }

        public List<String> getFilesInProgress() {
            return filesInProgress;
        }

        public void setFilesInProgress(List<String> filesInProgress) {
            this.filesInProgress = filesInProgress;
        }
    }

    public class TargetArtifactory {
        String serviceId;
        String name;
        String type;

        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(String serviceId) {
            this.serviceId = serviceId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
