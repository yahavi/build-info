package org.jfrog.gradle.plugin.artifactory;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.util.VersionNumber;
import org.jfrog.build.IntegrationTestsBase;
import org.jfrog.build.extractor.clientConfiguration.ArtifactoryDependenciesClientBuilder;
import org.jfrog.build.extractor.clientConfiguration.util.DependenciesDownloaderHelper;
import org.testng.annotations.*;
import org.testng.collections.Maps;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author yahavi
 */
@Test
public class GradlePluginTest extends IntegrationTestsBase {

    private static final VersionNumber GRADLE_6 = VersionNumber.parse("6.0");
    private static final String GRADLE_LOCAL_REPO = "build-info-tests-gradle-local";
    private static final String GRADLE_REMOTE_REPO = "build-info-tests-gradle-remote";
    private static final String GRADLE_VIRTUAL_REPO = "build-info-tests-gradle-virtual";

    private static final File TEMP_WORKSPACE = new File(System.getProperty("java.io.tmpdir"), "gradle_tests_space");
    private static final Path PROJECTS_ROOT = Paths.get(".").toAbsolutePath().normalize().resolve(Paths.get("src", "test", "resources", "org", "jfrog", "gradle", "plugin", "artifactory"));
    private static final File GRADLE_EXAMPLE_DIR = PROJECTS_ROOT.resolve("gradle-example-publish").toFile();

    private ArtifactoryDependenciesClientBuilder dependenciesClientBuilder;
    private DependenciesDownloaderHelper downloaderHelper;
    private Map<String, String> envVars;

    public GradlePluginTest() {
        localRepo = GRADLE_LOCAL_REPO;
        remoteRepo = GRADLE_REMOTE_REPO;
        virtualRepo = GRADLE_VIRTUAL_REPO;
    }

    @BeforeClass
    protected void setUp() {
        envVars = Maps.newHashMap();
        envVars.putAll(System.getenv());
        envVars.putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "URL", getUrl());
        envVars.putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "USERNAME", getUsername());
        envVars.putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "PASSWORD", getPassword());
        envVars.putIfAbsent(BITESTS_ARTIFACTORY_ENV_VAR_PREFIX + "LOCAL_REPO", localRepo);
    }

    @BeforeMethod
    @AfterMethod
    protected void cleanup() throws IOException {
        FileUtils.deleteDirectory(TEMP_WORKSPACE);
        deleteContentFromRepo(localRepo);
    }

    @DataProvider
    private Object[][] gradleVersions() {
        return new Object[][]{{"5.6.4"}};
    }

    @Test(dataProvider = "gradleVersions")
    public void publicationsTest(String gradleVersion) {
        BuildResult buildResult = runGradle(gradleVersion, GRADLE_EXAMPLE_DIR);
        checkTaskOutcome(buildResult, ":api:artifactoryPublish");
        checkTaskOutcome(buildResult, ":shared:artifactoryPublish");
        checkTaskOutcome(buildResult, ":services:webservice:artifactoryPublish");
        checkTaskOutcome(buildResult, ":artifactoryPublish");
    }

    BuildResult runGradle(String gradleVersion, File projectDir) {
        List<String> arguments = Arrays.asList("--stacktrace", "clean", "artifactoryPublish");
        return GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(projectDir)
                .withPluginClasspath()
                .withArguments(arguments)
                .withEnvironment(envVars)
                .build();
    }

    private void checkTaskOutcome(BuildResult buildResult, String taskName) {
        BuildTask buildTask = buildResult.task(taskName);
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), SUCCESS);
    }
}
