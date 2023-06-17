package com.grandstream.logplugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class LogPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.getExtensions().create("log", LogExtension.class)
        project.afterEvaluate {
            LogExtension logExtension = project.getExtensions().getByType(LogExtension.class)
            println "name = " + logExtension.name
        }
        project.getTasks().create("logTask") {
            println '--------------------logTask--------------------------'
        }
        AppExtension appExtension = project.getExtensions().findByType(AppExtension.class)
        appExtension.registerTransform(new MyTransform()) //注册Transform
    }
}