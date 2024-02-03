package bitdata.code.util;

import org.apache.bcel.classfile.*;
import org.apache.commons.lang3.StringUtils;

public class AnnotationUtil {

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

    public static String parseClassApiAnnotation(JavaClass javaClass) {
        for (AnnotationEntry entry : javaClass.getAnnotationEntries()) {
            String mapping = parseApiAnnotation(entry);
            if (!StringUtils.isEmpty(mapping)) {
                return mapping;
            }
        }
        return "";
    }

    public static String parseMethodMapping(Method method) {
        for (AnnotationEntry entry : method.getAnnotationEntries()) {
            String mapping = parseMapping(entry);
            if (!StringUtils.isEmpty(mapping)) {
                return mapping;
            }
        }
        return null;
    }

    public static String parseMethodApiAnnotation(Method method) {
        for (AnnotationEntry entry : method.getAnnotationEntries()) {
            String mapping = parseApiAnnotation(entry);
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
            return getAnnotationValue(entry, "value");
        }
        return null;
    }

    private static String parseApiAnnotation(AnnotationEntry entry) {
        String[] fields = entry.getAnnotationType().split("/");
        String annotation = fields[fields.length - 1].replace(";", "");
        if (StringUtils.equals("Api", annotation)) {
            return getAnnotationValue(entry, "tags");
        }
        if (StringUtils.equals("ApiOperation", annotation)) {
            return getAnnotationValue(entry, "value");
        }
        return null;
    }

    private static String getAnnotationValue(AnnotationEntry entry, String name) {
        for (ElementValuePair pair : entry.getElementValuePairs()) {
            if (StringUtils.equals(pair.getNameString(), name)) {
                ElementValue value = pair.getValue();
                if (value instanceof SimpleElementValue) {
                    return value.toString();
                }
                if (value instanceof ArrayElementValue) {
                    return ((ArrayElementValue) value).getElementValuesArray()[0].toString();
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
