package bitdata.code.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SourceRepository {

    private Map<String, String> pathMap = new HashMap<>();

    private Map<String, TreeMap<Integer, String>> lineMap = new HashMap<>();

    public void addClass(String className, String sourceFilePath) {
        pathMap.put(sourceFilePath, className);
        lineMap.put(className, new TreeMap<>());
    }

    public void addMethod(String className, String signature, Integer line) {
        TreeMap<Integer, String> treeMap = lineMap.get(className);
        treeMap.put(line, signature);
    }

    public boolean contains(String className){
        return lineMap.containsKey(className);
    }

    public String getClassName(String sourceFilePath) {
        return pathMap.get(sourceFilePath);
    }

    public String getMethod(String className, Integer line) {
        TreeMap<Integer, String> treeMap = lineMap.get(className);
        if (treeMap == null) {
            return null;
        }
        Map.Entry<Integer, String> entry = treeMap.floorEntry(line);
        if (entry == null) {
            return null;
        }
        return entry.getValue();
    }
}
