package nju.ling;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/*
 *   方法类
 *   用于所需要信息的存储
 */
public class MyMethod {
    private String classInnerNameStr;
    private String methodSignatureStr;

    private boolean isTest; //判断该method是否为测试类
    private boolean isVisited = false;
    private boolean isChanged = false;  //是否更新过

    List<MyMethod> dependents = new ArrayList<>();

    public String getClassInnerNameStr() {
        return classInnerNameStr;
    }

    public void setClassInnerNameStr(String classInnerNameStr) {
        this.classInnerNameStr = classInnerNameStr;
    }

    public String getMethodSignatureStr() {
        return methodSignatureStr;
    }

    public void setMethodSignatureStr(String methodSignatureStr) {
        this.methodSignatureStr = methodSignatureStr;
    }

    public boolean isTest() {
        return isTest;
    }

    public void setTest(boolean test) {
        isTest = test;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public void setVisited(boolean visited) {
        isVisited = visited;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setChanged(boolean changed) {
        isChanged = changed;
    }

    public List<MyMethod> getDependents() {
        return dependents;
    }

    public void setDependents(List<MyMethod> dependents) {
        this.dependents = dependents;
    }

    public MyMethod(ShrikeBTMethod method) {
        classInnerNameStr = method.getDeclaringClass().getName().toString();
        methodSignatureStr = method.getSignature();
        isTest = isTestMethodNode(method);
    }

    //通过注解对判断method是否为junit test
    public static boolean isTestMethodNode(IMethod method) {
        Collection<Annotation> annotations = method.getAnnotations();
        if (annotations == null) {
            return false;
        }
        TypeReference testAnnotation = TypeReference.findOrCreate(ClassLoaderReference.Application, "Lorg/junit/Test");
        for (Annotation annotation : annotations) {
            if (annotation.getType().equals(testAnnotation)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return classInnerNameStr + " " + methodSignatureStr + " " + isTest + " " + isChanged;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MyMethod method = (MyMethod) o;
        return Objects.equals(classInnerNameStr, method.classInnerNameStr) &&
                Objects.equals(methodSignatureStr, method.methodSignatureStr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classInnerNameStr, methodSignatureStr, isTest, isVisited, isChanged, dependents);
    }
}
