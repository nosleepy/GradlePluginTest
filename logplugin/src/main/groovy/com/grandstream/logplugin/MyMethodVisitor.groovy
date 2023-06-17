package com.grandstream.logplugin

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class MyMethodVisitor extends AdviceAdapter {
    //当前的类名
    private String className

    //当前的方法名
    private String methodName

    MyMethodVisitor(MethodVisitor methodVisitor, int access, String methodName, String className, String descriptor) {
        super(ASM7, methodVisitor, access, methodName, descriptor)
        this.methodName = methodName
        this.className = className
    }

    @Override
    void visitCode() {
        super.visitCode()
        //在这里插入自己的字节码指令
        //可以插入页面打开关闭的打点上报代码,这里用log打印代替了
        mv.visitLdcInsn("wlzhou")//可以用来过滤log日志的tag
        mv.visitLdcInsn(className + "--->" + methodName)//插入要打印的内容
        mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false)
        mv.visitInsn(POP)
    }
}