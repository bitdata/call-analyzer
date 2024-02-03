package bitdata.code.util;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.*;

public class InvokeRepository {

    private MultiValuedMap<String, String> superMap = new ArrayListValuedHashMap<>();

    private Set<ClassMethod> methodSet = new HashSet<>();

    private MultiValuedMap<ClassMethod, ClassMethod> callerMap = new ArrayListValuedHashMap<>();

    public void addClass(String className, Collection<String> interfaceNames) {
        superMap.putAll(className, interfaceNames);
    }

    public void addMethod(ClassMethod method) {
        methodSet.add(method);
    }

    public void addInvoke(ClassMethod method, ClassMethod invokeMethod) {
        callerMap.put(invokeMethod, method);
    }

    public DegreeRepository<ClassMethod> getSourceCallers(Collection<ClassMethod> methods) {
        BooleanGraph<ClassMethod> booleanGraph = new BooleanGraph<>();
        Queue<ClassMethod> queue = new LinkedList<>();
        for (ClassMethod method : methods) {
            booleanGraph.setFlag(method, false);
            queue.add(method);
        }
        while (!queue.isEmpty()) {
            ClassMethod e = queue.remove();
            Collection<ClassMethod> callers = callerMap.get(e);
            if (callers != null && !callers.isEmpty()) {
                booleanGraph.setFlag(e, true);
                for (ClassMethod caller : callers) {
                    booleanGraph.addChild(caller, e);
                    if (!booleanGraph.contains(caller)) {
                        booleanGraph.setFlag(caller, false);
                        queue.add(caller);
                    }
                }
            }
            Collection<String> interfaceNames = superMap.get(e.getClassName());
            if (interfaceNames != null) {
                for (String interfaceName : interfaceNames) {
                    ClassMethod superMethod = new ClassMethod(interfaceName, e.getMethodName(), e.getSignature());
                    if (methodSet.contains(superMethod)) {
                        if (!booleanGraph.contains(superMethod)) {
                            booleanGraph.setFlag(superMethod, false);
                            queue.add(superMethod);
                        }
                        booleanGraph.addChild(superMethod, e);
                    }
                }
            }
        }

        booleanGraph.setTreeFlag(true);
        Collection<ClassMethod> sources = booleanGraph.filter(false);

        DegreeRepository<ClassMethod> degreeRepository = new DegreeRepository<>();
        degreeRepository.build(booleanGraph.getNodes(), booleanGraph.getEdges(), sources, methods);
        return degreeRepository;
    }


}
