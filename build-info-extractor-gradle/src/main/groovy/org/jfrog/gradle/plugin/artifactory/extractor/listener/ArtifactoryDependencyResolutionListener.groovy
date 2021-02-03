package org.jfrog.gradle.plugin.artifactory.extractor.listener

import org.gradle.BuildAdapter
import org.gradle.BuildResult
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ModuleVersionIdentifier
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedComponentResult
import org.apache.commons.lang.ArrayUtils
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.artifacts.result.DefaultResolvedDependencyResult
import org.gradle.api.internal.project.taskfactory.TaskIdentity
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState
import org.gradle.execution.taskgraph.TaskListenerInternal
import org.gradle.profile.ProjectProfile
import org.gradle.profile.TaskExecution
import org.jfrog.build.extractor.BuildInfoExtractorUtils

import static org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask.EXTRACT_MODULE_TASK_NAME



class ArtifactoryDependencyResolutionListener extends BuildAdapter implements DependencyResolutionListener, TaskListenerInternal {
    final Map<String, String[][]> hierarchyMap = new HashMap<>()

    @Override
    void beforeResolve(ResolvableDependencies dependencies) {
    }

    @Override
    void afterResolve(ResolvableDependencies dependencies) {
        buildDependencyMap(dependencies.getResolutionResult().getAllDependencies() as Set<DefaultResolvedDependencyResult>)
        hierarchyMap.entrySet()
    }

    @Override
    void buildFinished(BuildResult result) {
        result.getGradle()
    }

    void buildDependencyMap(Set<DependencyResult> dependencies) {
        for (DependencyResult dependency : dependencies) {
            if (dependency instanceof DefaultResolvedDependencyResult) {
                updateDependencyPathsToRoot(dependency, hierarchyMap)
            }
        }
    }

    void updateDependencyPathsToRoot(DefaultResolvedDependencyResult dependency, Map<String, String[][]> hierarchyMap) {
        String[] newDependent = getPathToRoot(dependency)

        String compId = getCompId(dependency.getSelected().getModuleVersion())
        String[][] curDependants = hierarchyMap[compId]
        curDependants = ArrayUtils.add(curDependants, newDependent)
        hierarchyMap[compId] = curDependants
    }

    String[] getPathToRoot(DefaultResolvedDependencyResult dependency) {
        ResolvedComponentResult from = dependency.getFrom()
        if (from.getDependents().isEmpty()) {
            // todo try specific root  == ComponentSelectionReasons.ROOT
            if (from.getSelectionReason().isExpected()) {
                return [getCompId(from.getModuleVersion())]
            }
            // Something went wrong
            return []
        }
        return getPathToRootWithCurModule(from.getDependents().iterator().next() as DefaultResolvedDependencyResult)
    }

    String[] getPathToRootWithCurModule(DefaultResolvedDependencyResult dependency) {
        List<String> dependants = getPathToRoot(dependency)
        return dependants << getCompId(dependency.getSelected().getModuleVersion())
    }

    static String getCompId(ModuleVersionIdentifier module) {
        return BuildInfoExtractorUtils.getModuleIdString(module.getGroup(), module.getName(), module.getVersion())
    }
/*
    // TaskListenerInternal
    @Override
    public void beforeExecute(TaskIdentity<?> taskIdentity) {
        long now = clock.getCurrentTime();
        ProjectProfile projectProfile = buildProfile.getProjectProfile(taskIdentity.getProjectPath());
        projectProfile.getTaskProfile(taskIdentity.getTaskPath()).setStart(now);
    }

    @Override
    public void afterExecute(TaskIdentity<?> taskIdentity, TaskState state) {
        long now = clock.getCurrentTime();
        ProjectProfile projectProfile = buildProfile.getProjectProfile(taskIdentity.getProjectPath());
        TaskExecution taskExecution = projectProfile.getTaskProfile(taskIdentity.getTaskPath());
        taskExecution.setFinish(now);
        taskExecution.completed(state);
    }


 */
    @Override
    void beforeExecute(TaskIdentity taskIdentity) {
        if (taskIdentity.name == EXTRACT_MODULE_TASK_NAME) {
            println(taskIdentity.getProjectPath())
        }
    }

    @Override
    void afterExecute(TaskIdentity taskIdentity, TaskState taskState) {
        if (taskIdentity.name == EXTRACT_MODULE_TASK_NAME) {
            println(taskIdentity.getProjectPath())
        }
    }
}
