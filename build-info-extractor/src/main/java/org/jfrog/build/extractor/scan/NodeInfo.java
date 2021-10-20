package org.jfrog.build.extractor.scan;

import org.apache.commons.lang.StringUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author yahavi
 **/
public class NodeInfo {
    private Set<LicenseKey> licenses = new HashSet<>();
    private Set<IssueKey> issues = new HashSet<>();
    private Severity topSeverity = Severity.Normal;
    private String pkgType;

    public Set<IssueKey> getIssues() {
        return issues;
    }

    public NodeInfo setIssues(Set<IssueKey> issues) {
        this.issues = issues;
        return this;
    }

    public Set<LicenseKey> getLicenses() {
        return licenses;
    }

    public NodeInfo setLicenses(Set<LicenseKey> licenses) {
        this.licenses = licenses;
        return this;
    }

    public Severity getTopSeverity() {
        return topSeverity;
    }

    public NodeInfo setTopSeverity(Severity topSeverity) {
        this.topSeverity = topSeverity;
        return this;
    }

    public String getPkgType() {
        return pkgType;
    }

    public NodeInfo setPkgType(String pkgType) {
        this.pkgType = pkgType;
        return this;
    }

    public void addIssue(Issue issue) {
        IssueKey issueKey = new IssueKey();
        issueKey.setIssueId(issue.getIssueId());
        issueKey.setComponent(issue.getComponent());
        issues.add(issueKey);
    }

    public void addLicense(License license) {
        LicenseKey licenseKey = new LicenseKey();
        licenseKey.setLicenseName(license.getName());
        licenseKey.setViolating(license.isViolate());
        licenses.add(licenseKey);
    }

    public static class IssueKey {
        private Set<String> fixedVersions = new HashSet<>();
        private String component;
        private String issueId;

        public IssueKey() {
        }

        public IssueKey(Issue issue) {
            this.fixedVersions = new HashSet<>(issue.getFixedVersions());
            this.component = issue.getComponent();
            this.issueId = issue.getIssueId();
        }

        public Set<String> getFixedVersions() {
            return fixedVersions;
        }

        public IssueKey setFixedVersions(Set<String> fixedVersions) {
            this.fixedVersions = fixedVersions;
            return this;
        }

        public String getComponent() {
            return component;
        }

        public IssueKey setComponent(String component) {
            this.component = component;
            return this;
        }

        public String getIssueId() {
            return issueId;
        }

        public IssueKey setIssueId(String issueId) {
            this.issueId = issueId;
            return this;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            IssueKey otherIssueKey = (IssueKey) other;
            return StringUtils.equals(issueId, otherIssueKey.issueId) &&
                    StringUtils.equals(component, otherIssueKey.component) &&
                    Objects.equals(fixedVersions, otherIssueKey.fixedVersions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(issueId, component);
        }
    }

    public static class LicenseKey {
        private String licenseName;
        private boolean violating;

        public LicenseKey() {
        }

        public LicenseKey(License license) {
            this.licenseName = license.getName();
            this.violating = license.isViolate();
        }

        public String getLicenseName() {
            return licenseName;
        }

        public LicenseKey setLicenseName(String licenseName) {
            this.licenseName = licenseName;
            return this;
        }

        public boolean isViolating() {
            return violating;
        }

        public LicenseKey setViolating(boolean violating) {
            this.violating = violating;
            return this;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }

            LicenseKey otherIssueKey = (LicenseKey) other;
            return StringUtils.equals(licenseName, otherIssueKey.licenseName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(licenseName);
        }
    }
}