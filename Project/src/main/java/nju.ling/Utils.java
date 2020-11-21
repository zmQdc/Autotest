package nju.ling;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    /*
     *   获得dirPath下的所有 .class文件
     * @param dirPath 文件夹路径
     * @return .class文件
     */
    public static List<File> getAllFiles(String dirPath) {
        String suffix = ".class";
        File dir = new File(dirPath);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException(dir.getAbsolutePath() + " is not a directory!");
        }
        List<File> targetFiles = new ArrayList<>();
        File[] allFiles = dir.listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                if (file.isDirectory()) {
                    targetFiles.addAll(getAllFiles(file.getAbsolutePath()));
                } else {
                    if (file.getName().substring(file.getName().lastIndexOf('.')).equals(suffix)) {
                        targetFiles.add(file);
                    }
                }
            }
        }
        return targetFiles;
    }

    /*
     *   输出.txt文件
     * @param outputMethod 需要输出的方法
     * @param Method 方法级为true 类级为false
     */
    public static void outputTxt(List<MyMethod> outputMethods, boolean Method) throws IOException {
        String outputPath;
        if (Method) {
            outputPath = "selection-method.txt";
        } else {
            outputPath = "selection-class.txt";
        }
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath));
            for (MyMethod myMethod : outputMethods) {
                bufferedWriter.write(myMethod.getClassInnerNameStr() + " " + myMethod.getMethodSignatureStr());
                bufferedWriter.write(System.lineSeparator());
            }
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /*
     *   类级输出.dot文件
     * @param outputMethod 需要输出的方法依赖
     * @param fileName 输出的方法名
     */
    public static void outputDot_Class(List<MyMethod> outputMethods, String fileName) {
        List<String> dependents = new ArrayList<>();
        String temp;
        StringBuilder stringBuilder = new StringBuilder();
        for (MyMethod method : outputMethods) {
            for (MyMethod method1 : method.getDependents()) {
                temp = "\"" + method.getClassInnerNameStr() + "\"" + " -> " + "\"" + method1.getClassInnerNameStr() + "\"";
                if (!dependents.contains(temp)) {
                    dependents.add(temp);
                }
            }
        }
        dependents = dependents.stream().distinct().collect(Collectors.toList());
        stringBuilder.append("digraph ").append(fileName).append(" {").append(System.lineSeparator());
        for (String str : dependents) {
            stringBuilder.append("\t").append(str).append(System.lineSeparator());
        }
        stringBuilder.append("}");
        //System.out.println(stringBuilder.toString());
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName + ".dot"));
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     *   方法级输出.dot文件
     * @param outputMethod 需要输出的方法依赖
     * @param fileName 输出的方法名
     */
    public static void outputDot_Method(List<MyMethod> outputMethods, String fileName) {
        List<String> dependents = new ArrayList<>();
        String temp;
        StringBuilder stringBuilder = new StringBuilder();
        for (MyMethod method : outputMethods) {
            for (MyMethod method1 : method.getDependents()) {
                temp = "\"" + method.getMethodSignatureStr() + "\"" + " -> " + "\"" + method1.getMethodSignatureStr() + "\"";
                if (!dependents.contains(temp)) {
                    dependents.add(temp);
                }
            }
        }
        dependents = dependents.stream().distinct().collect(Collectors.toList());
        stringBuilder.append("digraph ").append(fileName).append(" {").append(System.lineSeparator());
        for (String str : dependents) {
            stringBuilder.append("\t").append(str).append(System.lineSeparator());
        }
        stringBuilder.append("}");
        //System.out.println(stringBuilder.toString());
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName + ".dot"));
            bufferedWriter.write(stringBuilder.toString());
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
