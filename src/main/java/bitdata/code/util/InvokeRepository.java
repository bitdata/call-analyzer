package bitdata.code.util;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.*;

public class InvokeRepository {

    private MultiValuedMap<String, String> superMap = new ArrayListValuedHashMap<>();

    private MultiValuedMap<ClassMethod, ClassMethod> callerMap = new ArrayListValuedHashMap<>();

    public void addClass(String className, Collection<String> interfaceNames) {
        superMap.putAll(className, interfaceNames);
    }

    public void addInvoke(ClassMethod method, ClassMethod invokeMethod) {
        callerMap.put(invokeMethod, method);
    }

    public Collection<ClassMethod> getOuterCallers(Collection<ClassMethod> methods) {
        BooleanTree<ClassMethod> booleanTree = new BooleanTree<>();

        Queue<ClassMethod> queue = new LinkedList<>();
        for (ClassMethod method : methods) {
            booleanTree.setFlag(method, false);
            queue.add(method);
        }
        while (!queue.isEmpty()) {
            ClassMethod e = queue.remove();
            Collection<ClassMethod> callers = callerMap.get(e);
            if (callers != null && !callers.isEmpty()) {
                booleanTree.setFlag(e, true);
                for (ClassMethod caller : callers) {
                    booleanTree.addChild(caller, e);
                    if (!booleanTree.contains(caller)) {
                        booleanTree.setFlag(caller, false);
                        queue.add(caller);
                    }
                }
            }
            Collection<String> interfaceNames = superMap.get(e.getClassName());
            if (interfaceNames != null) {
                for (String interfaceName : interfaceNames) {
                    ClassMethod superMethod = new ClassMethod(interfaceName, e.getMethodName(), e.getSignature());
                    if (!booleanTree.contains(superMethod)) {
                        booleanTree.setFlag(superMethod, false);
                        queue.add(superMethod);
                    }
                }
            }
        }

        booleanTree.setTreeFlag(true);
        return booleanTree.filter(false);
    }


}
