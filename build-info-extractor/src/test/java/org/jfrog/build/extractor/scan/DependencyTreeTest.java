package org.jfrog.build.extractor.scan;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author yahavi
 */
public class DependencyTreeTest {

    private DependencyTree root, one, two, three;

    /**
     * Build an empty tree with 5 nodes
     */
    @BeforeMethod
    public void init() {
        root = new DependencyTree("0");
        one = new DependencyTree("1");
        two = new DependencyTree("2");
        three = new DependencyTree("3");
        root.add(one); // 0 -> 1
        root.add(two); // 0 -> 2
        two.add(three); // 2 -> 3
    }

    @Test
    public void testNoIssues() {
        // Sanity test - Check tree with no issues
        Set<NodeInfo.IssueKey> rootIssues = root.processTreeIssues();
        assertTrue(rootIssues.isEmpty());
        assertEquals(Severity.Normal, root.getTopSeverity());
    }

    @Test
    public void testOneIssue() {
        // Populate "1" with one low issue and one unknown license.
        addIssue(one, Severity.Low);

        // Assert the tree has 1 low issue
        Set<NodeInfo.IssueKey> rootIssues = root.processTreeIssues();
        assertEquals(1, rootIssues.size());
        assertEquals(Severity.Low, root.getTopSeverity());
    }

    @Test
    public void testTwoIssues() {
        // Populate node two with one empty issue and one empty license.
        addIssue(one, Severity.Low);
        addIssue(two, Severity.Medium);

        // Assert the tree has 2 issues
        Set<NodeInfo.IssueKey> rootIssues = root.processTreeIssues();
        assertEquals(2, rootIssues.size());
        assertEquals(Severity.Medium, root.getTopSeverity());
        assertEquals(Severity.Low, one.getTopSeverity());
        assertEquals(Severity.Medium, two.getTopSeverity());
    }

    @Test
    public void testIsLicenseViolating() {
        assertFalse(root.isLicenseViolating());

        addLicense(one, false);
        addLicense(one, true);
        addLicense(two, false);

        // Assert that all issues are in the tree
        Set<NodeInfo.LicenseKey> rootLicense = new HashSet<>();
        root.collectAllScopesAndLicenses(new HashSet<>(), rootLicense);
        assertEquals(3, rootLicense.size());
        assertTrue(root.isLicenseViolating());
        assertTrue(one.isLicenseViolating());
        assertFalse(two.isLicenseViolating());
        assertFalse(three.isLicenseViolating());
    }

    @Test
    public void testNoFixedVersions() {
        // Check no fixed versions
        addIssue(one, Severity.Normal);
        Set<NodeInfo.IssueKey> rootIssues = root.processTreeIssues();
        assertTrue(((NodeInfo.IssueKey) rootIssues.toArray()[0]).getFixedVersions().isEmpty());
    }

    @Test
    public void testFixedVersions() {
        Set<String> fixedVersions = new HashSet<>();
        fixedVersions.add("1.2.3");
        NodeInfo.IssueKey issue = new NodeInfo.IssueKey();
        issue.setFixedVersions(fixedVersions);
        one.getIssues().add(issue);
        Set<NodeInfo.IssueKey> rootIssues = root.processTreeIssues();
        assertEquals(fixedVersions, ((NodeInfo.IssueKey) rootIssues.toArray()[0]).getFixedVersions());
    }

    /**
     * Create a random issue
     *
     * @param node     - The dependency tree node
     * @param severity - The issue severity
     */
    private void addIssue(DependencyTree node, Severity severity) {
        NodeInfo.IssueKey issueKey = new NodeInfo.IssueKey();
        issueKey.setIssueId(generateUID());
        issueKey.setComponent(generateUID());
        node.getIssues().add(issueKey);
        if (severity.isHigherThan(node.getTopSeverity())) {
            node.setTopSeverity(severity);
        }
    }

    /**
     * Create a random license
     *
     * @param node      - The dependency tree node
     * @param violating - A boolean indicates if the licenses is violating a policy.
     */
    private void addLicense(DependencyTree node, boolean violating) {
        NodeInfo.LicenseKey licenseKey = new NodeInfo.LicenseKey();
        licenseKey.setLicenseName(generateUID());
        licenseKey.setViolating(violating);
        node.getLicenses().add(licenseKey);
    }

    private String generateUID() {
        return UUID.randomUUID().toString();
    }
}