# AutomatedTesting2020

选题方向：经典自动化测试

使用wala版本为version 1.5.4


## 程序结构
Project/src/main/java 下是代码部分

程序入口为TestSelection.java中的main方法

MyMethod.java是存放method所需信息的一个类，如classInnerName、methodSignature

Utils.java是方法类，提供文件的读取和输出

Project/src/main/resource 下是静态资源，包含exclusion.txt、rt.jar、scope.txt、wala.properties

## 程序流程

1. 构建分析域、调用图
    
    读取exclusion.txt文件，通过AnalysisScopeReader.readJavaScope()方法构建分析域
    
    通过ClassHierarchyFactory.makeWithRoot()方法构建调用图
    
2. 生成CallGraph 获取Application method 转为MyMethod并构建依赖关系

    使用CHA算法构建调用图，CallGraph中的节点的前后继表示依赖关系，对MyMethod进行依赖的构建
    
3. 获取变更信息

    读取change_info.txt文件得到变更信息，通过递归对MyMethod中的方法进行变更信息的更新
    
4. 输出

    从变更信息中得到需要输出的method


