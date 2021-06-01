package org.jfrog.build.extractor.distribution;

import org.jfrog.build.api.util.Log;
import org.jfrog.build.extractor.clientConfiguration.DistributionManagerBuilder;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.DistributionManager;

import java.io.Serializable;

public class ReleaseBundleCreate implements Serializable {
    private Log log;
    private String spec;
    private String name;
    private String repo;
    private boolean sign;
    private boolean dryRun;
    private String version;
    private String passphrase;
    private boolean insecureTls;
    private String releaseNotePath;
    private ReleaseNotesSyntax releaseNoteSyntax;
    private DistributionManagerBuilder distributionManagerBuilder;

    public ReleaseBundleCreate() {
    }

    public ReleaseBundleCreate(String name, String version, String spec, String repo, boolean sign, DistributionManagerBuilder distributionManagerBuilder,
                               boolean dryRun, String passphrase, String releaseNotePath, ReleaseNotesSyntax releaseNoteSyntax,
                               boolean insecureTls, Log log) {
        this.log = log;
        this.spec = spec;
        this.name = name;
        this.sign = sign;
        this.repo = repo;
        this.dryRun = dryRun;
        this.version = version;
        this.passphrase = passphrase;
        this.insecureTls = insecureTls;
        this.releaseNotePath = releaseNotePath;
        this.releaseNoteSyntax = releaseNoteSyntax;
        this.distributionManagerBuilder = distributionManagerBuilder;
    }
    /**
     * Main function that create and send the request of Release-Bundle-Create to Distribution.
     */
    public void execute() {
        try (DistributionManager distributionManager = distributionManagerBuilder.build()) {
            // Release-Bundle-Create logic...
        }
    }

    public enum ReleaseNotesSyntax {
        MARKDOWN,
        ASCIIDOC,
        PLAIN_TEXT
    }
}
