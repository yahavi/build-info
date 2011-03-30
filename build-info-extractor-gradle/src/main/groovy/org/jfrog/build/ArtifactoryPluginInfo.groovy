package org.jfrog.build

import org.gradle.util.ConfigureUtil

/**
 * @author Yoav Landman
 */
class ArtifactoryPluginInfo {

    Publish publish
    Resolve resolve

    def artifactory(Closure closure) {
        closure.delegate = this
        closure()
        System.out.println("Artifactory plugin configured.");
    }

    def publish(Closure closure) {
        publish = new Publish()
        ConfigureUtil.configure(closure, publish)
    }

    def resolve(Closure closure) {
        resolve = new Resolve()
        ConfigureUtil.configure(closure, resolve)
    }
}

class Publish {
    boolean buildInfo
    Repository repository

    def repository(Closure closure) {
        repository = new Repository()
        ConfigureUtil.configure(closure, repository)
    }
}

class Resolve {
    Repository repository

    def repository(Closure closure) {
        repository = new Repository()
        ConfigureUtil.configure(closure, repository)
    }
}

class Repository {
    String url
}
