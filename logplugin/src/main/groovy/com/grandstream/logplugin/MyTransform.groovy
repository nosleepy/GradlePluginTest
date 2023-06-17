package com.grandstream.logplugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter

class MyTransform extends Transform {
    @Override
    String getName() {
        //名字,输出的文件会默认生成在这个名字的目录下,比如：MyPlugin\app\build\intermediates\transforms\MyTransform
        return "MyTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
        //可以从中获取jar包和class文件夹路径,需要输出给下一个任务
        Collection<TransformInput> inputs = transformInvocation.getInputs()
        //OutputProvider管理输出路径
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
        //遍历所有的输入,有两种类型,分别是文件夹类型(也就是我们自己写的代码)和jar类型(引入的jar包),这里我们只处理自己写的代码
        for (TransformInput input : inputs) {
            for (JarInput jarInput : input.jarInputs) {
                File jarFile = jarInput.file
                File destJar = outputProvider.getContentLocation(jarInput.name,
                        jarInput.contentTypes,
                        jarInput.scopes, Format.JAR)
                FileUtils.copyFile(jarFile, destJar)
            }
            //遍历所有文件夹
            for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
                //获取transform的输出目录,等我们插桩后就将修改过的class文件替换掉transform输出目录中的文件,就达到修改的效果了
                File dest = outputProvider.getContentLocation(directoryInput.getName(),
                        directoryInput.getContentTypes(), directoryInput.getScopes(),
                        Format.DIRECTORY)
                transformDir(directoryInput.getFile(), dest)
            }
        }
    }

    /**
     * 遍历文件夹,对文件进行插桩
     * @param input 源文件
     * @param dest  源文件修改后的输出地址
     */
    private static void transformDir(File input, File dest) throws IOException {
        if (dest.exists()) {
            FileUtils.forceDelete(dest)
        }
        FileUtils.forceMkdir(dest)
        String srcDirPath = input.getAbsolutePath()
        String destDirPath = dest.getAbsolutePath()
        File[] fileList = input.listFiles();
        if (fileList == null) {
            return
        }
        for (File file : fileList) {
            String destFilePath = file.getAbsolutePath().replace(srcDirPath, destDirPath)
            File destFile = new File(destFilePath)
            if (file.isDirectory()) {
                //如果是文件夹,继续遍历
                transformDir(file, destFile);
            } else if (file.isFile()) {
                //创造了大小为0的新文件,或者,如果该文件已存在,则将打开并删除该文件关闭而不修改,但更新文件日期和时间
                FileUtils.touch(destFile)
                asmHandleFile(file.getAbsolutePath(), destFile.getAbsolutePath())
            }
        }
    }

    /**
     * 通过ASM进行插桩
     * @param inputPath 源文件路径
     * @param destPath  输出路径
     */
    private static void asmHandleFile(String inputPath, String destPath) {
        try {
            //获取源文件的输入流
            FileInputStream is = new FileInputStream(inputPath);
            //将原文件的输入流交给ASM的ClassReader
            ClassReader cr = new ClassReader(is);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            //构建一个ClassVisitor,ClassVisitor可以理解为一组回调接口,类似于ClickListener
            MyClassVisitor visitor = new MyClassVisitor(cw);
            //这里是重点,asm通过ClassReader的accept方法去解析class文件,去读取每一个节点。
            // 每读到一个节点,就会通过传入的visitor相应的方法回调,这样我们就能在每一个节点的回调中去做操作
            cr.accept(visitor, 0);
            //将文件保存到输出目录下
            FileOutputStream fos = new FileOutputStream(destPath)
            fos.write(cw.toByteArray())
            fos.close()
        } catch (IOException e) {
            e.printStackTrace()
        }
    }
}