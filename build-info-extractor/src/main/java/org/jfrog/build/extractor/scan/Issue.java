package org.jfrog.build.extractor.scan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * @author yahavi
 */
@SuppressWarnings("unused")
public class Issue implements Comparable<Issue> {
    private Severity severity = Severity.Normal;
    private List<String> fixedVersions;
    private String component = "";
    private String description;
    private String issueId;
    private String summary;

    public Severity getSeverity() {
        return this.severity;
    }

    public String getComponent() {
        return this.component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getDescription() {
        return description;
    }

    public String getIssueId() {
        return issueId;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getFixedVersions() {
        return fixedVersions;
    }

    void setFixedVersions(List<String> fixedVersions) {
        this.fixedVersions = fixedVersions;
    }

    @JsonIgnore
    public boolean isTopSeverity() {
        return getSeverity() == Severity.Critical;
    }

    @JsonIgnore
    public boolean isHigherSeverityThan(Issue o) {
        return getSeverity().isHigherThan(o.getSeverity());
    }

    @Override
    public int compareTo(@Nonnull Issue otherIssue) {
        return Integer.compare(hashCode(), Objects.hashCode(otherIssue));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Issue otherIssue = (Issue) other;
        return StringUtils.equals(issueId, otherIssue.issueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issueId);
    }

    public static class Builder {
        private Severity severity = Severity.Normal;
        private List<String> fixedVersions;
        private String component = "";
        private String description;
        private String issueId;
        private String summary;

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder fixedVersions(List<String> fixedVersions) {
            this.fixedVersions = fixedVersions;
            return this;
        }

        public Builder component(String component) {
            this.component = component;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder issueId(String issueId) {
            this.issueId = issueId;
            return this;
        }

        public Builder summary(String summary) {
            this.summary = summary;
            return this;
        }

        public Issue build() {
            Issue issue = new Issue();
            issue.severity = this.severity;
            issue.fixedVersions = this.fixedVersions;
            issue.component = this.component;
            issue.description = this.description;
            issue.issueId = this.issueId;
            issue.summary = this.summary;
            return issue;
        }
    }
}
