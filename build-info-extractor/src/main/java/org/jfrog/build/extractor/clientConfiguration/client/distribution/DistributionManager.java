package org.jfrog.build.extractor.clientConfiguration.client.distribution;

import org.apache.commons.lang.StringUtils;
import org.jfrog.build.api.util.Log;
import org.jfrog.build.extractor.clientConfiguration.client.ManagerBase;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.request.CreateReleaseBundleRequest;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.services.CreateReleaseBundle;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.services.Version;

import java.io.IOException;


public class DistributionManager extends ManagerBase {
    public DistributionManager(String url, String username, String password, String accessToken, Log logger) {
        super(url, username, password, accessToken, logger);
    }

    public DistributionManager(String url, Log log) {
        super(url, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, log);
    }

    public org.jfrog.build.client.Version getVersion() throws IOException {
        Version versionService = new Version(log);
        return versionService.execute(jfrogHttpClient);
    }

    public void CreateReleaseBundle(CreateReleaseBundleRequest createReleaseBundleRequest,String gpgPassphrase) throws IOException {
        CreateReleaseBundle createReleaseBundleService = new CreateReleaseBundle(createReleaseBundleRequest,gpgPassphrase, log);
        createReleaseBundleService.execute(jfrogHttpClient);
    }
}
