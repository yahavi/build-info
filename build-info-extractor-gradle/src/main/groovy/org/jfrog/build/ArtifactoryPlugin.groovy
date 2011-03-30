/*
 * Copyright (C) 2010 JFrog Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfrog.build

import org.apache.commons.lang.StringUtils
import org.apache.ivy.plugins.resolver.DependencyResolver
import org.apache.ivy.plugins.resolver.IBiblioResolver
import org.apache.ivy.plugins.resolver.IvyRepResolver
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.Upload
import org.jfrog.build.api.ArtifactoryResolutionProperties
import org.jfrog.build.client.ClientGradleProperties
import org.jfrog.build.client.ClientIvyProperties
import org.jfrog.build.client.ClientProperties
import org.jfrog.build.extractor.BuildInfoExtractorUtils
import org.jfrog.build.extractor.gradle.BuildInfoRecorderTask
import org.slf4j.Logger
import static org.jfrog.build.ArtifactoryPluginUtils.BUILD_INFO_TASK_NAME

/**
 * Performs the following steps per project:
 * after load (before eval) per project:
 * 1. Adds a resolver according to the peroperties supplied
 * 2. Adds the buildInfoTask
 * 3. Register ourselves as build listener (ProjectEvaluatedBuildListener)
 *
 * after eval (all buildScripts executed):
 * 1. Configure the buildInfoTask via
 *  1.1 Get all projects for the gradle object
 *  1.2 Configure the buildInfoTask per project - on top of what may have been configured by the projects buildScript
 *
 */
class ArtifactoryPlugin implements Plugin<Project> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ArtifactoryPlugin.class);
    public static final String ENCODING = "UTF-8"

    //Assumes execution from projectsEvaluated() - via init-script (e.g. from CI plugin) or via direct project config

    def void apply(Project project) {
        if ("buildSrc".equals(project.name)) {
            log.debug("Artifactory Plugin disabled for ${project.path}")
            return
        }
        Map startParamProps = project.gradle.getStartParameter().projectProperties
        String buildStart = startParamProps['build.start'];
        if (!buildStart) {
            startParamProps['build.start'] = "" + System.currentTimeMillis()
        }
        log.debug("Using Artifactory Plugin for ${project.path}")
        Properties props = getMergedEnvAndSystemProps()
        props.putAll(startParamProps)
        Map<String, ?> projectProps = project.properties
        for (Map.Entry<String, ?> entry: projectProps.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                props.put(entry.getKey(), entry.getValue())
            }
        }
        defineResolvers(project, props)
        // add the build info task
        createBuildInfoTask(project, props);
    }

    private void defineResolvers(Project project, Properties props) {
        String artifactoryUrl = props.getProperty(ClientProperties.PROP_CONTEXT_URL) ?: 'http://repo.gradle.org/gradle'
        while (artifactoryUrl.endsWith("/")) {
            artifactoryUrl = StringUtils.removeEnd(artifactoryUrl, "/")
        }
        String buildRoot = props.getProperty(ArtifactoryResolutionProperties.ARTIFACTORY_BUILD_ROOT_MATRIX_PARAM_KEY);
        String downloadId = props.getProperty(ClientProperties.PROP_RESOLVE_REPOKEY) ?:
            project.rootProject.convention.plugins.artifactory?.resolve?.repository?.url
        if (StringUtils.isNotBlank(downloadId)) {
            def artifactoryDownloadUrl = props.getProperty('artifactory.downloadUrl') ?: "${artifactoryUrl}/${downloadId}"
            log.debug("Artifactory URL: $artifactoryUrl")
            log.debug("Artifactory Download ID: $downloadId")
            log.debug("Artifactory Download URL: $artifactoryDownloadUrl")
            if (StringUtils.isNotBlank(buildRoot)) {
                artifactoryDownloadUrl += ";" + buildRoot + ";"
                injectPropertyIntoExistingResolvers(project.repositories.getAll(), buildRoot)
            }
            //TODO: cleanup any existing decalred repos - need to be called after eval to do that! (why isn't that the case?)

            //Add artifactory url to the list of repositories
            project.repositories {
                mavenRepo urls: [artifactoryDownloadUrl]
            }
        } else {
            log.debug("No repository resolution defined for ${project.name}")
        }
    }

    private def injectPropertyIntoExistingResolvers(Set<DependencyResolver> allResolvers, String buildRoot) {
        for (DependencyResolver resolver: allResolvers) {
            if (resolver instanceof IvyRepResolver) {
                resolver.artroot = StringUtils.removeEnd(resolver.artroot, '/') + ';' + buildRoot + ';'
                resolver.ivyroot = StringUtils.removeEnd(resolver.ivyroot, '/') + ';' + buildRoot + ';'
            } else if (resolver instanceof IBiblioResolver) {
                resolver.root = StringUtils.removeEnd(resolver.root, '/') + ';' + buildRoot + ';'
            }
        }
    }

    private Properties getMergedEnvAndSystemProps() {
        Properties props = new Properties();
        props.putAll(System.getenv());
        return BuildInfoExtractorUtils.mergePropertiesWithSystemAndPropertyFile(props);
    }

    void createBuildInfoTask(Project project, Properties props) {
        if (project.tasks.findByName(BUILD_INFO_TASK_NAME)) {
            return
        }
        def isRoot = project.equals(project.getRootProject())
        log.debug("Configuring buildInfo task for project ${project.name}: is root? ${isRoot}")
        BuildInfoRecorderTask buildInfo = project.getTasks().add(BUILD_INFO_TASK_NAME, BuildInfoRecorderTask.class)
        buildInfo.setDescription("Generates build info from build artifacts");

        //Add and cofigure the build info task for each project
        project.allprojects.each {
            configureBuildInfoTask(it, buildInfo, props)
        }
    }

    void configureBuildInfoTask(Project project, BuildInfoRecorderTask buildInfoTask, Properties props) {
        TaskContainer tasks = project.getTasks()
        if (buildInfoTask.getConfiguration() == null) {
            buildInfoTask.configuration = project.configurations.findByName(Dependency.ARCHIVES_CONFIGURATION)
        }
        //Make sure each project's buildInfoTask depends on the excution of its children's buildInfoTask
        project.subprojects.each { if (it.tasks.findByName(BUILD_INFO_TASK_NAME)) buildInfoTask.dependsOn(it.buildInfo) }

        // If no configuration no descriptor
        if (buildInfoTask.configuration == null) {
            return
        }

        // Set up the ivy publish (upload) task and the ivy descriptor parameters. buildInfoTask.ivyDescriptor can be set on the build script.
        if (Boolean.parseBoolean(props.getProperty(ClientIvyProperties.PROP_PUBLISH_IVY)) &&
                buildInfoTask.ivyDescriptor == null) {
            // Flag to publish the Ivy XML file, but no ivy descriptor file inputted, activate default upload${configuration}.
            Upload uploadTask = tasks.getByName(buildInfoTask.configuration.getUploadTaskName())
            if (!uploadTask.isUploadDescriptor()) {
                throw new GradleException("""Cannot publish Ivy descriptor if ivyDescriptor not set in task: ${buildInfoTask.path}
                    And flag uploadDescriptor not set in default task: ${uploadTask.path}""")
            }
            buildInfoTask.ivyDescriptor = uploadTask.descriptorDestination
            buildInfoTask.dependsOn(uploadTask)
        } else {
            buildInfoTask.ivyDescriptor = null
        }

        // Set maven pom parameters
        if (Boolean.parseBoolean(props.getProperty(ClientGradleProperties.PROP_PUBLISH_MAVEN)) &&
                buildInfoTask.mavenDescriptor == null) {
            // Flag to publish the Maven POM, but no pom file inputted, activate default Maven install.
            // if the project doesn't have the maven install task, throw an exception
            Upload installTask = tasks.withType(Upload.class).findByName('install')
            if (installTask == null) {
                throw new GradleException("""Cannot publish Maven descriptor if mavenDescriptor not set in task: ${buildInfoTask.path}
                    And default install task for project ${project.path} is not an Upload task""")
            }
            buildInfoTask.mavenDescriptor = new File(project.getRepositories().getMavenPomDir(), "pom-default.xml")
            buildInfoTask.dependsOn(installTask)
        } else {
            buildInfoTask.mavenDescriptor = null
        }
    }
}

