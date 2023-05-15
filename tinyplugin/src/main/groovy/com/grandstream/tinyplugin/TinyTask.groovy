package com.grandstream.tinyplugin

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.util.function.Consumer

class TinyTask extends DefaultTask {
    private String apiKey
    private ArrayList<String> fileList

    @Inject
    TinyTask(String apiKey, ArrayList<String> fileList) {
        this.apiKey = apiKey
        this.fileList = fileList
    }

    @TaskAction
    def tiny() {
        println "fileList size = " + fileList.size()
        fileList.forEach(new Consumer<String>() {
            @Override
            void accept(String s) {
                executeCmd(s)
            }
        })
    }

    def executeCmd(String path) {
        String compressCmd = "curl --user api:" + apiKey + " --data-binary @" + path + " -s https://api.tinify.com/shrink"
        println compressCmd
        def p = compressCmd.execute()
        def json = new JsonSlurper().parseText(p.text)
        println json
        String downloadUrl = json['output']['url']
        String downloadCmd = "curl " + downloadUrl + " -o " + path
        downloadCmd.execute()
    }
}