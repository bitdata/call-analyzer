package bitdata.code.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassMethod {

    private String className;

    private String signature;

    private static Pattern pattern = Pattern.compile("^[^\\\\(]");

    public ClassMethod(String className, String signature) {
        this.className = className;
        this.signature = signature;
    }

    public String getClassName() {
        return className;
    }

    public String getSignature() {
        return signature;
    }

    public String getMethodName() {
        Matcher matcher = pattern.matcher(signature);
        return matcher.group();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassMethod)) {
            return false;
        }
        ClassMethod m = (ClassMethod) o;
        return StringUtils.equals(this.className, m.className) &&
                StringUtils.equals(this.signature, m.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getSignature());
    }
}
