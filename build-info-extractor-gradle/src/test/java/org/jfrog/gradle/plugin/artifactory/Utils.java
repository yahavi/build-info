package org.jfrog.gradle.plugin.artifactory;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.BuildTask;
import org.gradle.testkit.runner.GradleRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author yahavi
 */
public class Utils {

    private static final File TEST_DIR = new File(System.getProperty("java.io.tmpdir"), "gradle_tests_space");

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

    static void assertSuccess(BuildResult buildResult, String taskName) {
        BuildTask buildTask = buildResult.task(taskName);
        assertNotNull(buildTask);
        assertEquals(buildTask.getOutcome(), SUCCESS);
    }

}
