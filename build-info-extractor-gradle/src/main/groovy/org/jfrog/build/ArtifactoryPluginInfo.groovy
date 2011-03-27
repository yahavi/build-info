package org.jfrog.build

import org.gradle.util.ConfigureUtil

/**
 * @author Yoav Landman
 */
class ArtifactoryPluginInfo {

  Publish publish

  def artifactory(Closure closure) {
    closure.delegate = this
    closure()
  }

  def publish(Closure closure) {
    publish = new Publish()
    ConfigureUtil.configure(closure, publish)
    System.out.println("Artifactory plugin configured.");
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

class Repository {
  String url
}
