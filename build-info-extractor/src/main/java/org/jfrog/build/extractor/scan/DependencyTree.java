package org.jfrog.build.extractor.scan;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * Dependency tree for Xray scan. Used in 'Eclipse' and 'Idea' Xray plugins.
 *
 * @author yahavi
 */
@JsonFilter("xray-graph-filter")
public class DependencyTree extends DefaultMutableTreeNode {

    private Set<NodeInfo.LicenseKey> licenses = new HashSet<>();
    private Set<NodeInfo.IssueKey> issues = new HashSet<>();
    private Severity topSeverity = Severity.Normal;
    private Set<Scope> scopes = new HashSet<>();
    private GeneralInfo generalInfo;
    private String packagePrefix = "";

    /**
     * metadata should be true if one of the following statement is true:
     * Node represents the root of the dependency tree.
     * Node represents a project.
     * Node represents a module in project.
     */
    private boolean metadata;

    public DependencyTree() {
        super();
    }

    public DependencyTree(Object userObject) {
        super(userObject);
    }

    public void setLicenses(Set<NodeInfo.LicenseKey> licenses) {
        this.licenses = licenses;
    }

    public void setIssues(Set<NodeInfo.IssueKey> issues) {
        this.issues = issues;
    }

    @JsonProperty("component_id")
    @SuppressWarnings("unused")
    public String getComponentId() {
        return packagePrefix + this;
    }

    public void setScopes(Set<Scope> scopes) {
        this.scopes = scopes;
    }

    @SuppressWarnings("unused")
    public void setGeneralInfo(GeneralInfo generalInfo) {
        this.generalInfo = generalInfo;
    }

    public void setTopSeverity(Severity topSeverity) {
        this.topSeverity = topSeverity;
    }

    public Set<NodeInfo.LicenseKey> getLicenses() {
        return licenses;
    }

    public Set<NodeInfo.IssueKey> getIssues() {
        return issues;
    }

    public Set<Scope> getScopes() {
        return scopes;
    }

    @SuppressWarnings("unused")
    public GeneralInfo getGeneralInfo() {
        return generalInfo;
    }

    /**
     * @return top severity issue of the current node and its ancestors
     */
    public Severity getTopSeverity() {
        return topSeverity;
    }

    /**
     * @return if one or more of the licenses is violating define policy
     */
    @SuppressWarnings("unused")
    public boolean isLicenseViolating() {
        if (licenses.stream().anyMatch(NodeInfo.LicenseKey::isViolating)) {
            return true;
        }
        return getChildren().stream().anyMatch(DependencyTree::isLicenseViolating);
    }

    @SuppressWarnings("unused")
    public boolean isMetadata() {
        return metadata;
    }

    @SuppressWarnings("unused")
    public void setMetadata(boolean metadata) {
        this.metadata = metadata;
    }

    public void setPrefix(String prefix) {
        packagePrefix = prefix.toLowerCase() + "://";
        getChildren().forEach(node -> node.setPrefix(prefix));
    }

    /**
     * @return total number of issues of the current node and its ancestors
     */
    @SuppressWarnings("WeakerAccess")
    public int getIssueCount() {
        return issues.size();
    }

    /**
     * @return Node's children
     */
    @SuppressWarnings({"WeakerAccess", "unchecked"})
    public Vector<DependencyTree> getChildren() {
        return children != null ? children : new Vector<>();
    }

    @JsonProperty(value = "nodes")
    @SuppressWarnings({"unchecked", "unused"})
    public List<DependencyTree> getNodes() {
        return children;
    }

    /**
     * 1. Populate current node's issues components
     * 2. Populate current node and subtree's issues
     * 3. Populate current node and subtree's top issue
     * 4. Sort the tree
     *
     * @return all issues of the current node and its ancestors
     */
    @SuppressWarnings({"WeakerAccess", "unused"})
    public Set<NodeInfo.IssueKey> processTreeIssues() {
        setIssuesComponent();
        addChildIssues();
        sortChildren();
        return issues;
    }

    private void setIssuesComponent() {
        Object userObject = getUserObject();
        if (userObject != null) {
            issues.forEach(issue -> issue.setComponent(userObject.toString()));
        }
    }

    private void addChildIssues() {
        getChildren().forEach(child -> {
            issues.addAll(child.processTreeIssues());
            if (child.topSeverity.isHigherThan(topSeverity)) {
                topSeverity = child.topSeverity;
            }
        });
    }

    private void sortChildren() {
        getChildren().sort(Comparator
                .comparing(DependencyTree::getTopSeverity)
                .thenComparing(DependencyTree::getIssueCount)
                .thenComparing(DependencyTree::getChildCount)
                .reversed()
                .thenComparing(DependencyTree::toString));
    }

    /**
     * Recursively, collect all scopes and licenses.
     *
     * @param allScopes   - Out - All dependency tree scopes
     * @param allLicenses - Out - All dependency tree licenses
     */
    @SuppressWarnings("unused")
    public void collectAllScopesAndLicenses(Set<Scope> allScopes, Set<NodeInfo.LicenseKey> allLicenses) {
        Enumeration<?> enumeration = breadthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            DependencyTree child = (DependencyTree) enumeration.nextElement();
            allScopes.addAll(child.getScopes());
            allLicenses.addAll(child.getLicenses());
        }
    }
}
