package bitdata.code.util;

import org.apache.bcel.classfile.*;
import org.apache.commons.lang3.StringUtils;

public class MappingUtil {

    public static String parseClassMapping(JavaClass javaClass) {
        if (!isRestController(javaClass)) {
            return null;
        }

        for (AnnotationEntry entry : javaClass.getAnnotationEntries()) {
            String mapping = parseMapping(entry);
            if (!StringUtils.isEmpty(mapping)) {
                return mapping;
            }
        }
        return "";
    }

    public static String parseMethodMapping(Method method, String classMapping) {
        if (classMapping == null) {
            return null;
        }
        for (AnnotationEntry entry : method.getAnnotationEntries()) {
            String mapping = parseMapping(entry);
            if (!StringUtils.isEmpty(mapping)) {
                return mapping;
            }
        }
        return null;
    }

    private static String parseMapping(AnnotationEntry entry) {
        String[] fields = entry.getAnnotationType().split("/");
        String annotation = fields[fields.length - 1].replace(";", "");
        if (StringUtils.equals("RequestMapping", annotation) ||
                StringUtils.equals("GetMapping", annotation) ||
                StringUtils.equals("PostMapping", annotation)) {
            for (ElementValuePair pair : entry.getElementValuePairs()) {
                if (StringUtils.equals(pair.getNameString(), "value")) {
                    ElementValue value = pair.getValue();
                    if (value instanceof ArrayElementValue) {
                        return ((ArrayElementValue) value).getElementValuesArray()[0].toString();
                    }
                }
            }
        }
        return null;
    }

    private static boolean isRestController(JavaClass javaClass) {
        for (AnnotationEntry entry : javaClass.getAnnotationEntries()) {
            String[] fields = entry.getAnnotationType().split("/");
            String annotation = fields[fields.length - 1].replace(";", "");
            if (StringUtils.equals("RestController", annotation)) {
                return true;
            }
        }
        return false;
    }

}
