package bitdata.code.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DegreeRepository<T> {

    private Map<Integer, MultiValuedMap<String, T>> degreeMap;

    private Map<String, Collection<Integer>> md5Map;

    public List<Integer> listDegrees() {
        return degreeMap.keySet().stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
    }

    public Collection<String> listKeys(Integer degree) {
        return degreeMap.get(degree).keySet();
    }

    public Collection<T> listValues(Integer degree, String key) {
        return degreeMap.get(degree).get(key);
    }

    public Collection<Integer> getSinks(String key) {
        return md5Map.get(key);
    }

    public void build(Collection<T> nodes, MultiValuedMap<T, T> edges, Collection<T> sources, Collection<T> sinks) {
        int n = nodes.size();
        Map<T, Integer> indexMap = buildIndexMap(nodes);
        boolean[][] matrix = buildMatrix(n, edges, indexMap);
        floydWarshall(matrix, n);
        buildDegreeMap(sources, sinks, matrix, indexMap);
    }

    private boolean[][] buildMatrix(int n, MultiValuedMap<T, T> edges, Map<T, Integer> indexMap) {
        boolean[][] matrix = new boolean[n][n];
        for (int i = 0; i < n; i++) {
            matrix[i][i] = true;
        }
        for (Map.Entry<T, T> entry : edges.entries()) {
            int i = indexMap.get(entry.getKey());
            int j = indexMap.get(entry.getValue());
            matrix[i][j] = true;
        }
        return matrix;
    }

    private void buildDegreeMap(Collection<T> sources, Collection<T> sinks, boolean[][] matrix, Map<T, Integer> indexMap) {
        int[] array = new int[sinks.size()];
        int k = 0;
        for (T sink : sinks) {
            int j1 = indexMap.get(sink);
            array[k++] = j1;
        }

        degreeMap = new HashMap<>();
        md5Map = new HashMap<>();
        for (T source : sources) {
            int i = indexMap.get(source);
            List<Integer> list = new ArrayList<>();
            for (int j : array) {
                if (matrix[i][j]) {
                    list.add(j);
                }
            }

            String md5 = getMd5(list);
            MultiValuedMap<String, T> multiValuedMap = degreeMap.get(list.size());
            if (multiValuedMap == null) {
                multiValuedMap = new ArrayListValuedHashMap<>();
                degreeMap.put(list.size(), multiValuedMap);
            }
            multiValuedMap.put(md5, source);
            if (!md5Map.containsKey(md5)) {
                md5Map.put(md5, list);
            }
        }
    }

    private Map<T, Integer> buildIndexMap(Collection<T> nodes) {
        Map<T, Integer> map = new HashMap<>();
        int k = 0;
        for (T node : nodes) {
            map.put(node, k++);
        }
        return map;
    }

    private static String getMd5(List<Integer> sinks) {
        String data = StringUtils.join(sinks, ',');
        return DigestUtils.md5Hex(data);
    }

    private static void floydWarshall(boolean[][] matrix, int n) {
        for (int k = 0; k < n; k++) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (matrix[i][k] && matrix[k][j] && !matrix[i][j]) {
                        matrix[i][j] = true;
                    }
                }
            }
        }
    }
}
