package org.jfrog.gradle.plugin.artifactory;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.jfrog.build.extractor.clientConfiguration.client.ArtifactoryDependenciesClient;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author yahavi
 */
public class Utils {

    private static final File TEST_DIR = new File(System.getProperty("java.io.tmpdir"), "gradle_tests_space");
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
            Stream.of(
                    "webservice/1.0-SNAPSHOT/webservice-1.0-SNAPSHOT.module",
                    "shared/1.0-SNAPSHOT/shared-1.0-SNAPSHOT.module",
                    "api/1.0-SNAPSHOT/api-1.0-SNAPSHOT.module")).
            toArray(String[]::new);

    static BuildResult runGradle(String gradleVersion, Map<String, String> envVars) {
        List<String> arguments = Arrays.asList("clean", "artifactoryPublish", "--stacktrace");
        //noinspection UnstableApiUsage
        return GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(TEST_DIR)
                .withPluginClasspath()
                .withArguments(arguments)
                .withEnvironment(envVars)
                .build();
    }

    static void createTestDir(File sourceDir) throws IOException {
        FileUtils.copyDirectory(sourceDir, TEST_DIR);
    }

    static void deleteTestDir() throws IOException {
        FileUtils.deleteDirectory(TEST_DIR);
    }

    static void checkBuildResults(BuildResult buildResult, boolean expectModuleArtifacts, ArtifactoryDependenciesClient dependenciesClient, String url, String repo) throws IOException {
        // Assert all tasks ended with success outcome
        assertSuccess(buildResult, ":api:artifactoryPublish");
        assertSuccess(buildResult, ":shared:artifactoryPublish");
        assertSuccess(buildResult, ":services:webservice:artifactoryPublish");
        assertSuccess(buildResult, ":artifactoryPublish");

        // Check that all expected artifacts uploaded to Artifactory
        String[] expectedArtifacts = expectModuleArtifacts ? EXPECTED_MODULE_ARTIFACTS : EXPECTED_ARTIFACTS;
        for (String expectedArtifact : expectedArtifacts) {
            dependenciesClient.getArtifactMetadata(url + repo + ARTIFACTS_GROUP_ID + expectedArtifact);
        }
    }

    static void assertSuccess(BuildResult buildResult, String taskName) {
        BuildTask buildTask = buildResult.task(taskName);
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), SUCCESS);
    }

}
