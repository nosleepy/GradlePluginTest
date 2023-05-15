package com.grandstream.myplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class TestPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.getExtensions().create("user", UserExtension.class)
        project.afterEvaluate {
            UserExtension userExtension = project.getExtensions().getByType(UserExtension.class)
            println "username = " + userExtension.username + ",password = " + userExtension.password
        }
        project.getTasks().create("testTask") {
            println '--------------------testTask--------------------------'
        }
    }
}