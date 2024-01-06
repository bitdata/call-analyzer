package bitdata.code.service;

import bitdata.code.util.ClassMethod;
import bitdata.code.util.InvokeRepository;
import bitdata.code.util.SourceRepository;
import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.MethodGen;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

@Service
public class CallParseService {

    private SourceRepository sourceRepository;

    private InvokeRepository invokeRepository;

    public void parseJars(Collection<String> jarFileNames) throws IOException {
        parseSource(jarFileNames);
        parseInvoke(jarFileNames);
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
                    sourceRepository.addMethod(javaClass.getClassName(), method.getSignature(), line);
                }
            }
        });
    }

    private void parseInvoke(Collection<String> jarFileNames) throws IOException {
        invokeRepository = new InvokeRepository();
        parse(jarFileNames, (javaClass) -> {
            invokeRepository.addClass(javaClass.getClassName(), javaClass.getSuperclassName(), javaClass.getInterfaceNames());
            ConstantPoolGen constantPoolGen = new ConstantPoolGen(javaClass.getConstantPool());
            for (Method method : javaClass.getMethods()) {
                ClassMethod classMethod = new ClassMethod(javaClass.getClassName(), method.getSignature());
                MethodGen methodGen = new MethodGen(method, javaClass.getClassName(), constantPoolGen);
                if (methodGen.getInstructionList() == null) {
                    continue;
                }
                InstructionHandle instructionHandle = methodGen.getInstructionList().getStart();
                while (instructionHandle != null && instructionHandle.getInstruction() != null) {
                    short opCode = instructionHandle.getInstruction().getOpcode();
                    if (opCode >= Const.INVOKEVIRTUAL && opCode <= Const.INVOKEINTERFACE) {
                        InvokeInstruction invokeInstruction = (InvokeInstruction) instructionHandle.getInstruction();
                        String className = invokeInstruction.getReferenceType(constantPoolGen).toString();
                        if (sourceRepository.contains(className)) {
                            String signature = invokeInstruction.getSignature(constantPoolGen);
                            invokeRepository.addInvoke(classMethod, new ClassMethod(className, signature));
                        }
                    }
                    instructionHandle = instructionHandle.getNext();
                }
            }
        });
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
