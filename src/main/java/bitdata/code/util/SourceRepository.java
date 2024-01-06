package bitdata.code.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SourceRepository {

    private Map<String, String> pathMap = new HashMap<>();

    private Map<String, TreeMap<Integer, ClassMethod>> lineMap = new HashMap<>();

    public void addClass(String className, String sourceFilePath) {
        pathMap.put(sourceFilePath, className);
        lineMap.put(className, new TreeMap<>());
    }

    public void addMethod(ClassMethod method, Integer line) {
        TreeMap<Integer, ClassMethod> treeMap = lineMap.get(method.getClassName());
        treeMap.put(line, method);
    }

    public boolean contains(String className) {
        return lineMap.containsKey(className);
    }

    public String getClassName(String sourceFilePath) {
        return pathMap.get(sourceFilePath);
    }

    public ClassMethod getMethod(String className, Integer line) {
        TreeMap<Integer, ClassMethod> treeMap = lineMap.get(className);
        if (treeMap == null) {
            return null;
        }
        Map.Entry<Integer, ClassMethod> entry = treeMap.floorEntry(line);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }
}
