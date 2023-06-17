package com.grandstream.logplugin

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MyClassVisitor extends ClassVisitor implements Opcodes {
    //当前类的类名称
    //本例：com/grandstream/gradletest/MainActivity
    private String className;

    //className类的父类名称
    //本例：androidx/appcompat/app/AppCompatActivity
    private String superName;

    MyClassVisitor(ClassVisitor classVisitor) {
        super(ASM5, classVisitor)
    }

    @Override
    void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
        super.visit(version, access, className, signature, superName, interfaces)
        this.className = className
        this.superName = superName
    }

    @Override
    MethodVisitor visitMethod(int access, String methodName, String descriptor, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = cv.visitMethod(access, methodName, descriptor, signature, exceptions)
        //判断是否自己要匹配的方法(这里匹配onCreate和onDestroy方法)
        if ("android/support/v7/app/AppCompatActivity".equals(superName)//support包
                || "androidx/appcompat/app/AppCompatActivity".equals(superName)) {//androidx包
            if (methodName.startsWith("onCreate")) {
                return new MyMethodVisitor(methodVisitor, access, methodName, className, descriptor)
            }
            if (methodName.startsWith("onDestroy")) {
                return new MyMethodVisitor(methodVisitor, access, methodName, className, descriptor)
            }
        }
        return methodVisitor
    }
}