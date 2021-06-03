package org.jfrog.build.extractor.clientConfiguration.client.distribution.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class DistributeReleaseBundleRequest {
    @JsonProperty("dry_run")
    boolean dryRun;
    @JsonProperty("distribution_rules")
    DistributionRules distributionRules;

    public DistributionRules getDistributionRules() {
        return distributionRules;
    }

    public void setDistributionRules(DistributionRules distributionRules) {
        this.distributionRules = distributionRules;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public class DistributionRules{
        @JsonProperty("site_name")
        String siteName;
        @JsonProperty("city_name")
        String cityName;
        @JsonProperty("country_codes")
        List<String> countryCodes;

        public String getSiteName() {
            return siteName;
        }

        public void setSiteName(String siteName) {
            this.siteName = siteName;
        }

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public List<String> getCountryCodes() {
            return countryCodes;
        }

        public void setCountryCodes(List<String> countryCodes) {
            this.countryCodes = countryCodes;
        }
    }
}
