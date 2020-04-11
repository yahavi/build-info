package org.jfrog.gradle.plugin.artifactory;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;
import org.jfrog.build.extractor.clientConfiguration.client.ArtifactoryDependenciesClient;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

    // The temporary test dir. We run the tests here.
    private static final File TEST_DIR = new File(System.getProperty("java.io.tmpdir"), "gradle_tests_space");

    // CI example paths
    static final Path LIBS_DIR = GradlePluginTest.GRADLE_EXTRACTOR.resolve(Paths.get("build", "libs"));
    static final Path INIT_SCRIPT = GradlePluginTest.GRADLE_EXTRACTOR_SRC.resolve(Paths.get("main", "resources", "initscripttemplate.gradle"));
    static final Path BUILD_INFO_PROPERTIES = GradlePluginTest.PROJECTS_ROOT.resolve("buildinfo.properties");

    // Expected artifacts
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

    static BuildResult runGradle(String gradleVersion, Map<String, String> envVars, boolean applyInitScript) {
        List<String> arguments = new ArrayList<>(Arrays.asList("clean", "artifactoryPublish", "--stacktrace"));
        if (applyInitScript) {
            arguments.add("--init-script=gradle.init");
        }
        //noinspection UnstableApiUsage
        return GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(TEST_DIR)
                .withPluginClasspath()
                .withArguments(arguments)
                .withEnvironment(envVars)
                .build();
    }

    static void createTestDir(Path sourceDir) throws IOException {
        FileUtils.copyDirectory(sourceDir.toFile(), TEST_DIR);
    }

    static void deleteTestDir() throws IOException {
        FileUtils.deleteDirectory(TEST_DIR);
    }

    static void generateInitScript() throws IOException {
        String content = new String(Files.readAllBytes(INIT_SCRIPT), StandardCharsets.UTF_8);
        content = content.replace("${pluginLibDir}", LIBS_DIR.toString());
        Path target = TEST_DIR.toPath().resolve("gradle.init");
        Files.write(target, content.getBytes(StandardCharsets.UTF_8));
    }

    @SuppressWarnings("RegExpRedundantEscape")
    static String generateBuildInfoProperties(String contextUrl, String username, String password, String localRepo, String virtualRepo) throws IOException {
        String content = new String(Files.readAllBytes(BUILD_INFO_PROPERTIES), StandardCharsets.UTF_8);
        content = content.replaceAll("\\$\\{contextUrl\\}", contextUrl);
        content = content.replaceAll("\\$\\{username\\}", username);
        content = content.replaceAll("\\$\\{password\\}", password);
        content = content.replaceAll("\\$\\{localRepo\\}", localRepo);
        content = content.replaceAll("\\$\\{virtualRepo\\}", virtualRepo);
        Path target = TEST_DIR.toPath().resolve("buildinfo.properties");
        Files.write(target, content.getBytes(StandardCharsets.UTF_8));
        return target.toString();
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
