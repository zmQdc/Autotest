package nju.ling;

import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestSelection {
    static final ClassLoader classLoader = TestSelection.class.getClassLoader(); //类加载器，用于加载类
    static final String exclusionPath = "exclusion.txt";    //排除配置文件路径
    static final String scopePath = "scope.txt";    //域配置文件

    public static void testSelection(String project_target, String change_info, boolean select_method) throws InvalidClassFileException, WalaException, IOException, CancelException {
        List<File> classFiles = Utils.getAllFiles(project_target);    //得到project_target路径下的.class文件
        File exclusionFile = new File(exclusionPath);

        //构建分析域
        AnalysisScope scope = AnalysisScopeReader.readJavaScope(
                scopePath,
                exclusionFile,
                classLoader
        );
        for (File file : classFiles) {
            scope.addClassFileToScope(ClassLoaderReference.Application, file);
        }
        /*
         *   利用CHA算法构建调用图
         *   1. 生成类层次关系对象
         *   2. 生成进入点
         *   3. 遍历callGraph中的所有节点得到 类型描述符 和 类型签名 并储存
         */
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
        CHACallGraph callGraph = new CHACallGraph(cha);
        callGraph.init(eps);

        //存放 Application method
        List<CGNode> applicationNodes = new ArrayList<>();
        //构建 分析需要使用的 method 这一步不构建该 method 的依赖
        List<MyMethod> allNodes = new ArrayList<>();

        //遍历CallGraph中的节点 得到applicationNodes 和 method
        for (CGNode node : callGraph) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if (method.getDeclaringClass().getClassLoader().toString().equals("Application")) {
                    applicationNodes.add(node);
                    allNodes.add(new MyMethod(method));
                }
            }
        }

        //构建每个method的依赖关系dependents
        for (int i = 0; i < allNodes.size(); i++) {
            MyMethod myNode = allNodes.get(i);
            CGNode appNode = applicationNodes.get(i);

            //wala 的 CallGraph 中前后继表示依赖关系
            Iterator<CGNode> predIter = callGraph.getPredNodes(appNode);
            List<MyMethod> dependents = new ArrayList<>();
            while (predIter.hasNext()) {
                CGNode node = predIter.next();
                if (node.getMethod() instanceof ShrikeBTMethod) {
                    ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                    if (method.getDeclaringClass().getClassLoader().toString().equals("Application")) {
                        MyMethod temp = new MyMethod(method);
                        for (MyMethod method1 : allNodes) {
                            if (
                                    method1.getMethodSignatureStr().equals(temp.getMethodSignatureStr()) &&
                                            method1.getClassInnerNameStr().equals(temp.getClassInnerNameStr())
                            ) {
                                dependents.add(method1);
                            }
                        }
                    }
                }
            }
            myNode.setDependents(dependents);
        }


        //通过 change_info 获得变更信息
        List<MyMethod> changedMethod;
        List<MyMethod> selectedMethod;
        if (select_method) {
            changedMethod = getChangedMethod_Method(change_info, allNodes);
        } else {
            changedMethod = getChangedMethod_Class(change_info, allNodes);
        }
        selectedMethod = selectMethod(allNodes, changedMethod);

        //输出 txt文件 文件路径为 ./
        Utils.outputTxt(selectedMethod, select_method);
    }

    public static void main(String[] args) throws CancelException, WalaException, InvalidClassFileException, IOException {
        //testSelection("F:\\大三上\\自动化测试\\autotest_demo\\Data\\ClassicAutomatedTesting\\0-CMD\\target","F:\\大三上\\自动化测试\\autotest_demo\\Data\\ClassicAutomatedTesting\\0-CMD\\data\\change_info.txt",true);
        if (args.length != 3) {
            System.out.println("Error!\nPlease input right parameters!");
            System.exit(-1);
        }
        boolean select_method;
        select_method = args[0].equals("-m");
        String project_target = args[1];
        String change_info = args[2];
        testSelection(project_target, change_info, select_method);
        //testSelection("ClassicAutomatedTesting\\5-MoreTriangle\\target", "ClassicAutomatedTesting\\5-MoreTriangle\\data\\change_info.txt", true);
    }

    /*
     *   方法级方法变更的获取
     *   判断 ClassInnerName和 MethodSignature 是否相同
     */
    public static List<MyMethod> getChangedMethod_Method(String change_info, List<MyMethod> allMethods) throws IOException {
        List<MyMethod> changedMethod = new ArrayList<>();
        BufferedReader br = null;
        String str;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(change_info)));
            while ((str = br.readLine()) != null) {
                String[] strs = str.split(" ");
                for (MyMethod method : allMethods) {
                    if (method.getClassInnerNameStr().equals(strs[0])
                            &&
                            method.getMethodSignatureStr().equals(strs[1])) {
                        method.setChanged(true);
                        changedMethod.add(method);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert br != null;
        br.close();
        return changedMethod;
    }

    /*
     *   类级方法变更的获得
     *   判定标准是ClassInnerName是否变更
     */
    public static List<MyMethod> getChangedMethod_Class(String change_info, List<MyMethod> allMethods) throws IOException {
        List<MyMethod> changedMethod = new ArrayList<>();
        List<String> changedClassName = new ArrayList<>();
        BufferedReader br = null;
        String str;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(change_info)));
            while ((str = br.readLine()) != null) {
                String[] strs = str.split(" ");
                if (!changedClassName.contains(strs[0])) changedClassName.add(strs[0]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        assert br != null;
        br.close();
        for (MyMethod method : allMethods) {
            if (changedClassName.contains(method.getClassInnerNameStr())) {
                method.setChanged(true);
                changedMethod.add(method);
            }
        }
        return changedMethod;
    }

    /*
     *   选择变更过的method
     */
    public static List<MyMethod> selectMethod(List<MyMethod> allMethods, List<MyMethod> changedMethod) {
        for (MyMethod method : changedMethod) {
            updateDependsInfo(method);
        }
        List<MyMethod> selectedMethods = new ArrayList<>();
        for (MyMethod method : allMethods) {
            if (method.isChanged() && method.isTest()) {
                selectedMethods.add(method);
            }
        }
        return selectedMethods;
    }

    /*
     *   通过递归更新变更的信息
     */
    public static void updateDependsInfo(MyMethod myMethod) {
        if (myMethod.isVisited()) return;
        myMethod.setChanged(true);
        myMethod.setVisited(true);
        for (MyMethod method : myMethod.getDependents()) {
            updateDependsInfo(method);
        }
    }

}
