package bitdata.code.util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@ApiModel
public class ClassMethod {

    @ApiModelProperty("class name")
    private String className;

    @ApiModelProperty("method name")
    private String methodName;

    @ApiModelProperty("method signature")
    private String signature;

    public ClassMethod(String className, String methodName, String signature) {
        this.className = className;
        this.methodName = methodName;
        this.signature = signature;
    }

    public String getClassName() {
        return className;
    }

    public String getSignature() {
        return signature;
    }

    public String getMethodName() {
        return methodName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassMethod)) {
            return false;
        }
        ClassMethod m = (ClassMethod) o;
        return StringUtils.equals(this.className, m.className) &&
                StringUtils.equals(this.methodName, m.methodName) &&
                StringUtils.equals(this.signature, m.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClassName(), getMethodName(), getSignature());
    }
}
