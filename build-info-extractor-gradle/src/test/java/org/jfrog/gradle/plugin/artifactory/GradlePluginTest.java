package org.jfrog.gradle.plugin.artifactory;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.util.VersionNumber;
import org.jfrog.build.IntegrationTestsBase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.jfrog.gradle.plugin.artifactory.Utils.*;
import static org.testng.FileAssert.fail;

/**
 * @author yahavi
 */
@Test
public class GradlePluginTest extends IntegrationTestsBase {

    private static final String GRADLE_LOCAL_REPO = "build-info-tests-gradle-local";
    private static final String GRADLE_VIRTUAL_REPO = "build-info-tests-gradle-virtual";

    private static final Path PROJECTS_ROOT = Paths.get(".").normalize().toAbsolutePath().resolve(Paths.get("src", "test", "resources", "org", "jfrog", "gradle", "plugin", "artifactory"));
    private static final File GRADLE_EXAMPLE_DIR = PROJECTS_ROOT.resolve("gradle-example").toFile();
    private static final File GRADLE_EXAMPLE_PUBLISH_DIR = PROJECTS_ROOT.resolve("gradle-example-publish").toFile();
    private static final String ARTIFACTS_GROUP_ID = "/org/jfrog/test/gradle/publish/";

    private static final String[] EXPECTED_ARTIFACTS = {
            "webservice/1.0-SNAPSHOT/webservice-1.0-SNAPSHOT.jar",
            "shared/1.0-SNAPSHOT/shared-1.0-SNAPSHOT.jar",
            "api/1.0-SNAPSHOT/api-1.0-SNAPSHOT.jar",
            "api/1.0-SNAPSHOT/api-1.0-SNAPSHOT.properties",
            "shared/1.0-SNAPSHOT/shared-1.0-SNAPSHOT.properties",
            "webservice/1.0-SNAPSHOT/webservice-1.0-SNAPSHOT.properties",
            "shared/1.0-SNAPSHOT/shared-1.0-SNAPSHOT.pom",
            "webservice/1.0-SNAPSHOT/webservice-1.0-SNAPSHOT.pom",
            "api/ivy-1.0-SNAPSHOT.xml",
            "api/1.0-SNAPSHOT/api-1.0-SNAPSHOT.pom"
    };

    private static final String[] EXPECTED_MODULE_ARTIFACTS = Stream.concat(
            Stream.of(EXPECTED_ARTIFACTS),
            Stream.of("webservice/1.0-SNAPSHOT/webservice-1.0-SNAPSHOT.module", "shared/1.0-SNAPSHOT/shared-1.0-SNAPSHOT.module", "api/1.0-SNAPSHOT/api-1.0-SNAPSHOT.module"))
            .toArray(String[]::new);
    private Map<String, String> envVars;

    public GradlePluginTest() {
        localRepo = GRADLE_LOCAL_REPO;
        virtualRepo = GRADLE_VIRTUAL_REPO;
        envVars = new HashMap<String, String>() {{
            putAll(System.getenv());
            putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "URL", getUrl());
            putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "USERNAME", getUsername());
            putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "PASSWORD", getPassword());
            putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "LOCAL_REPO", localRepo);
        }};
    }

    @BeforeMethod
    @AfterMethod
    protected void cleanup() throws IOException {
        Utils.deleteTestDir();
        deleteContentFromRepo(localRepo);
    }

    @DataProvider
    private Object[][] gradleVersions() {
        return new String[][]{{"4.10.3"}, {"5.6.4"}, {"6.3"}};
    }

    @Test(dataProvider = "gradleVersions")
    public void publicationsTest(String gradleVersion) {
        try {
            createTestDir(GRADLE_EXAMPLE_PUBLISH_DIR);
            BuildResult buildResult = runGradle(gradleVersion, envVars);
            checkBuildResults(buildResult, VersionNumber.parse(gradleVersion).getMajor() >= 6);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    @Test(dataProvider = "gradleVersions")
    public void configurationsTest(String gradleVersion) {
        try {
            createTestDir(GRADLE_EXAMPLE_DIR);
            BuildResult buildResult = runGradle(gradleVersion, envVars);
            checkBuildResults(buildResult, false);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    private void checkBuildResults(BuildResult buildResult, boolean expectModuleArtifacts) throws IOException {
        // Assert all tasks ended with success outcome
        assertSuccess(buildResult, ":api:artifactoryPublish");
        assertSuccess(buildResult, ":shared:artifactoryPublish");
        assertSuccess(buildResult, ":services:webservice:artifactoryPublish");
        assertSuccess(buildResult, ":artifactoryPublish");

        // Check that all expected artifacts uploaded to Artifactory
        String[] expectedArtifacts = expectModuleArtifacts ? EXPECTED_MODULE_ARTIFACTS : EXPECTED_ARTIFACTS;
        for (String expectedArtifact : expectedArtifacts) {
            dependenciesClient.getArtifactMetadata(getUrl() + localRepo + ARTIFACTS_GROUP_ID + expectedArtifact);
        }
    }
}
