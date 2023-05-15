package com.grandstream.tinyplugin

import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

class TinyPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.getExtensions().create("tinyExt", TinyExtension.class)
        project.afterEvaluate(new Action<Project>() {
            @Override
            void execute(Project p) {
                TinyExtension tinyExtension = project.getExtensions().getByType(TinyExtension.class)
                println tinyExtension.apiKey
                String path = project.rootDir.absolutePath + File.separator + "app" + File.separator + "src" +
                    File.separator + "main" + File.separator + "res"
                File dir = new File(path)
                ArrayList<String> fileList = getPngFileList(dir)
                project.getTasks().create("tinyPng", TinyTask, tinyExtension.apiKey, fileList)
            }
        })
    }

    def getPngFileList(File file) {
        println "path = " + file.absolutePath
        ArrayList<String> fileList = new ArrayList<>()
        if (file.isDirectory()) {
            File[] fileArr = file.listFiles()
            for (i in 0..<fileArr.length) {
                if (fileArr[i].isDirectory()) {
                    fileList.addAll(getPngFileList(fileArr[i]))
                } else {
                    if (fileArr[i].absolutePath.toLowerCase().endsWith("png")) {
                        fileList.add(fileArr[i].absolutePath)
                    }
                }
            }
        } else {
            if (file.absolutePath.toLowerCase().endsWith("png")) {
                fileList.add(file.absolutePath)
            }
        }
        return fileList
    }
}