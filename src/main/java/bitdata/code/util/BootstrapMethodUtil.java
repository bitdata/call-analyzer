package bitdata.code.util;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;
import org.apache.commons.lang3.StringUtils;

public class BootstrapMethodUtil {

    public static BootstrapMethod getBootstrapMethod(JavaClass javaClass, int index) {
        for (Attribute attribute : javaClass.getAttributes()) {
            if (attribute instanceof BootstrapMethods) {
                BootstrapMethods bootstrapMethods = (BootstrapMethods) attribute;
                BootstrapMethod[] bootstrapMethodArray = bootstrapMethods.getBootstrapMethods();
                if (bootstrapMethodArray != null && bootstrapMethodArray.length > index) {
                    return bootstrapMethodArray[index];
                }
            }
        }
        return null;
    }

    public static ClassMethod getMethodFromBootstrapMethod(JavaClass javaClass, BootstrapMethod bootstrapMethod) {
        for (int argIndex : bootstrapMethod.getBootstrapArguments()) {
            Constant constantArg = javaClass.getConstantPool().getConstant(argIndex);
            if (!(constantArg instanceof ConstantMethodHandle)) {
                continue;
            }
            ClassMethod method = getMethodFromConstantMethodHandle(javaClass, (ConstantMethodHandle) constantArg);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    public static ClassMethod getMethodFromConstantMethodHandle(JavaClass javaClass, ConstantMethodHandle constantMethodHandle) {
        ConstantPool constantPool = javaClass.getConstantPool();
        Constant constantCP = constantPool.getConstant(constantMethodHandle.getReferenceIndex());
        if (!(constantCP instanceof ConstantCP)) {
            return null;
        }
        ConstantCP constantClassAndMethod = (ConstantCP) constantCP;
        String className = constantPool.getConstantString(constantClassAndMethod.getClassIndex(), Const.CONSTANT_Class);
        className = Utility.compactClassName(className, false);
        Constant constantNAT = constantPool.getConstant(constantClassAndMethod.getNameAndTypeIndex());
        if (!(constantNAT instanceof ConstantNameAndType)) {
            return null;
        }
        ConstantNameAndType constantNameAndType = (ConstantNameAndType) constantNAT;
        String methodName = constantPool.constantToString(constantNameAndType.getNameIndex(), Const.CONSTANT_Utf8);
        if (StringUtils.isEmpty(methodName)) {
            return null;
        }
        String signature = constantNameAndType.getSignature(constantPool);
        return new ClassMethod(className, methodName, signature);
    }

}
