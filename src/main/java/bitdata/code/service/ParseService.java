package bitdata.code.service;

import bitdata.code.entity.SourceLine;
import bitdata.code.util.*;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

@Service
public class ParseService {

    private SourceRepository sourceRepository;

    private InvokeRepository invokeRepository;

    private Map<ClassMethod, String> annotationRepository;

    public void parseJars(Collection<String> jarFileNames) throws IOException {
        parseSource(jarFileNames);
        parseInvoke(jarFileNames);
    }

    public Collection<String> analyze(Collection<SourceLine> sourceLines) {
        if (sourceRepository == null) {
            throw new RuntimeException("Please parse jars first");
        }

        Set<ClassMethod> methods = new HashSet<>();
        for (SourceLine sourceLine : sourceLines) {
            String className = sourceRepository.getClassName(sourceLine.getSourceFilePath());
            if (!StringUtils.isEmpty(className)) {
                ClassMethod method = sourceRepository.getMethod(className, sourceLine.getLine());
                if (method != null) {
                    if (!method.getMethodName().startsWith("<")) {
                        methods.add(method);
                    }
                }
            }
        }

        List<String> list = new ArrayList<>();
        Set<Integer> set = new HashSet<>();
        DegreeRepository<ClassMethod> degreeRepository = invokeRepository.getSourceCallers(methods);
        for (Integer degree : degreeRepository.listDegrees()) {
            for (String key : degreeRepository.listKeys(degree)) {
                set.addAll(degreeRepository.getSinks(key));
                list.add(degree.toString() + "/" + set.size() + "/" + methods.size());
                for (ClassMethod method : degreeRepository.listValues(degree, key)) {
                    if (!method.getMethodName().startsWith("<")) {
                        String sourceFileName = sourceRepository.getSourceFileName(method.getClassName());
                        Integer line = sourceRepository.getLine(method);
                        if (line != null) {
                            String itemText = method.getClassName() + "." + method.getMethodName() + "(" + sourceFileName + ":" + line + ")";
                            String mapping = annotationRepository.get(method);
                            if (!StringUtils.isEmpty(mapping)) {
                                itemText += " " + mapping;
                            }
                            list.add(itemText);
                        }
                    }
                }
            }
        }
        return list;
    }

    private void parseSource(Collection<String> jarFileNames) throws IOException {
        sourceRepository = new SourceRepository();
        parse(jarFileNames, (javaClass) -> {
            sourceRepository.addClass(javaClass.getClassName(), javaClass.getSourceFilePath());
            for (Method method : javaClass.getMethods()) {
                LineNumberTable lineNumberTable = method.getLineNumberTable();
                if (lineNumberTable != null) {
                    int line = Integer.MAX_VALUE;
                    for (LineNumber lineNumber : method.getLineNumberTable().getLineNumberTable()) {
                        if (lineNumber.getLineNumber() < line) {
                            line = lineNumber.getLineNumber();
                        }
                    }
                    sourceRepository.addMethod(new ClassMethod(javaClass.getClassName(), method.getName(), method.getSignature()), line);
                }
            }
        });
    }

    private void parseInvoke(Collection<String> jarFileNames) throws IOException {
        invokeRepository = new InvokeRepository();
        annotationRepository = new HashMap<>();
        parse(jarFileNames, (javaClass) -> {
            List<String> interfaceNames = new ArrayList<>();
            if (sourceRepository.contains(javaClass.getSuperclassName())) {
                interfaceNames.add(javaClass.getSuperclassName());
            }
            for (String interfaceName : javaClass.getInterfaceNames()) {
                if (sourceRepository.contains(interfaceName)) {
                    interfaceNames.add(interfaceName);
                }
            }
            invokeRepository.addClass(javaClass.getClassName(), interfaceNames);
            String classMapping = AnnotationUtil.parseClassMapping(javaClass);
            String classApiAnnotation = AnnotationUtil.parseClassApiAnnotation(javaClass);
            ConstantPoolGen constantPoolGen = new ConstantPoolGen(javaClass.getConstantPool());
            for (Method method : javaClass.getMethods()) {
                ClassMethod classMethod = new ClassMethod(javaClass.getClassName(), method.getName(), method.getSignature());
                if (classMapping != null) {
                    String methodMapping = AnnotationUtil.parseMethodMapping(method);
                    String methodApiAnnotation = AnnotationUtil.parseMethodApiAnnotation(method);
                    annotationRepository.put(classMethod,
                            classMapping + methodMapping + " | " + classApiAnnotation + "->" + methodApiAnnotation);
                }
                invokeRepository.addMethod(classMethod);
                parseInvoke(javaClass, constantPoolGen, classMethod, method);
            }
        });
    }

    private void parseInvoke(JavaClass javaClass, ConstantPoolGen constantPoolGen, ClassMethod classMethod, Method method) {
        MethodGen methodGen = new MethodGen(method, javaClass.getClassName(), constantPoolGen);
        if (methodGen.getInstructionList() == null) {
            return;
        }
        InstructionHandle instructionHandle = methodGen.getInstructionList().getStart();
        while (instructionHandle != null && instructionHandle.getInstruction() != null) {
            short opCode = instructionHandle.getInstruction().getOpcode();
            if (opCode >= Const.INVOKEVIRTUAL && opCode <= Const.INVOKEINTERFACE) {
                parseInvokeVirtual(constantPoolGen, classMethod, instructionHandle);
            } else if (opCode == Const.INVOKEDYNAMIC) {
                parseInvokeDynamic(javaClass, constantPoolGen, classMethod, instructionHandle);
            }
            instructionHandle = instructionHandle.getNext();
        }
    }

    private void parseInvokeVirtual(ConstantPoolGen constantPoolGen, ClassMethod classMethod, InstructionHandle instructionHandle) {
        InvokeInstruction invokeInstruction = (InvokeInstruction) instructionHandle.getInstruction();
        String className = invokeInstruction.getReferenceType(constantPoolGen).toString();
        if (sourceRepository.contains(className)) {
            String methodName = invokeInstruction.getMethodName(constantPoolGen);
            String signature = invokeInstruction.getSignature(constantPoolGen);
            invokeRepository.addInvoke(classMethod, new ClassMethod(className, methodName, signature));
        }
    }

    private void parseInvokeDynamic(JavaClass javaClass, ConstantPoolGen constantPoolGen, ClassMethod classMethod, InstructionHandle instructionHandle) {
        INVOKEDYNAMIC invokeDynamic = (INVOKEDYNAMIC) instructionHandle.getInstruction();
        Constant constant = constantPoolGen.getConstant(invokeDynamic.getIndex());
        if (!(constant instanceof ConstantInvokeDynamic)) {
            String className = invokeDynamic.getType(constantPoolGen).toString();
            String methodName = invokeDynamic.getMethodName(constantPoolGen);
            String signature = invokeDynamic.getSignature(constantPoolGen);
            invokeRepository.addInvoke(classMethod, new ClassMethod(className, methodName, signature));
            return;
        }
        ConstantInvokeDynamic cid = (ConstantInvokeDynamic) constant;
        BootstrapMethod bootstrapMethod = BootstrapMethodUtil.getBootstrapMethod(javaClass, cid.getBootstrapMethodAttrIndex());
        if (bootstrapMethod != null) {
            ClassMethod dynamicMethod = BootstrapMethodUtil.getMethodFromBootstrapMethod(javaClass, bootstrapMethod);
            if (dynamicMethod != null) {
                invokeRepository.addInvoke(classMethod, dynamicMethod);
            }
        }
    }

    private void parse(Collection<String> jarFileNames, Consumer<JavaClass> parseClass) throws IOException {
        for (String jarFileName : jarFileNames) {
            try (JarInputStream jarInputStream = new JarInputStream(new BufferedInputStream(new FileInputStream(jarFileName)))) {
                JarEntry jarEntry;
                while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
                    if (jarEntry.isDirectory()) {
                        continue;
                    }
                    if (jarEntry.getName().endsWith(".class")) {
                        JavaClass javaClass = new ClassParser(jarInputStream, jarEntry.getName()).parse();
                        parseClass.accept(javaClass);
                    }
                }
            }
        }
    }

}
