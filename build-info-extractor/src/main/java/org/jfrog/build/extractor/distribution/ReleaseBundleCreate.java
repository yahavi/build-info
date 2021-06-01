package org.jfrog.build.extractor.distribution;

import org.jfrog.build.api.util.Log;
import org.jfrog.build.extractor.clientConfiguration.DistributionManagerBuilder;
import org.jfrog.build.extractor.clientConfiguration.client.distribution.DistributionManager;

import java.io.Serializable;

public class ReleaseBundleCreate implements Serializable {
    private Log log;
    private String spec;
    private String name;
    private boolean dryRun;
    private String version;
    private String description;
    private boolean insecureTls;
    private String gpgPassphrase;
    private String releaseNotePath;
    private boolean signImmediately;
    private String storingRepository;
    private ReleaseNotesSyntax releaseNoteSyntax;
    private DistributionManagerBuilder distributionManagerBuilder;

    public ReleaseBundleCreate() {
    }

    public ReleaseBundleCreate(String name, String version, String spec, String StoringRepository, boolean signImmediately, DistributionManagerBuilder distributionManagerBuilder,
                               boolean dryRun, String gpgPassphrase, String releaseNotePath, ReleaseNotesSyntax releaseNoteSyntax, String description,
                               boolean insecureTls, Log log) {
        this.log = log;
        this.spec = spec;
        this.name = name;
        this.dryRun = dryRun;
        this.version = version;
        this.description = description;
        this.insecureTls = insecureTls;
        this.gpgPassphrase = gpgPassphrase;
        this.signImmediately = signImmediately;
        this.releaseNotePath = releaseNotePath;
        this.storingRepository = StoringRepository;
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
