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

import static org.jfrog.gradle.plugin.artifactory.Utils.*;
import static org.testng.FileAssert.fail;

/**
 * @author yahavi
 */
@Test
public class GradlePluginTest extends IntegrationTestsBase {

    private static final String GRADLE_LOCAL_REPO = "build-info-tests-gradle-local";
    private static final String GRADLE_REMOTE_REPO = "build-info-tests-gradle-remote";
    private static final String GRADLE_VIRTUAL_REPO = "build-info-tests-gradle-virtual";

    private static final Path PROJECTS_ROOT = Paths.get(".").normalize().toAbsolutePath().resolve(Paths.get("src", "test", "resources", "org", "jfrog", "gradle", "plugin", "artifactory"));
    private static final File GRADLE_EXAMPLE_DIR = PROJECTS_ROOT.resolve("gradle-example").toFile();
    private static final File GRADLE_EXAMPLE_PUBLISH_DIR = PROJECTS_ROOT.resolve("gradle-example-publish").toFile();

    private Map<String, String> envVars;

    public GradlePluginTest() {
        localRepo = GRADLE_LOCAL_REPO;
        remoteRepo = GRADLE_REMOTE_REPO;
        virtualRepo = GRADLE_VIRTUAL_REPO;
        envVars = new HashMap<String, String>() {{
            putAll(System.getenv());
            putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "URL", getUrl());
            putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "USERNAME", getUsername());
            putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "PASSWORD", getPassword());
            putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "LOCAL_REPO", localRepo);
            putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "VIRTUAL_REPO", virtualRepo);
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
            checkBuildResults(buildResult, VersionNumber.parse(gradleVersion).getMajor() >= 6, dependenciesClient, getUrl(), localRepo);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }

    @Test(dataProvider = "gradleVersions")
    public void configurationsTest(String gradleVersion) {
        try {
            createTestDir(GRADLE_EXAMPLE_DIR);
            BuildResult buildResult = runGradle(gradleVersion, envVars);
            checkBuildResults(buildResult, false, dependenciesClient, getUrl(), localRepo);
        } catch (IOException e) {
            fail(e.getMessage(), e);
        }
    }
}
