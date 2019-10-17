package de.uni_passau.fim.auermich.branchcoverage;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.branchdistance.utility.Utility;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.builder.BuilderInstruction;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MutableMethodImplementation;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10t;
import org.jf.dexlib2.builder.instruction.BuilderInstruction10x;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.*;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static de.uni_passau.fim.auermich.branchdistance.BranchDistance.OPCODE_API;

public class ExceptionHandlerTest {

    private MultiDexContainer<? extends DexBackedDexFile> apk;
    private File apkFile;
    private Pattern exclusionPattern;

    @Before
    public void readAPK() throws Exception {

        String os = System.getProperty("os.name");

        // describes class names we want to exclude from instrumentation
        exclusionPattern = Utility.readExcludePatterns();

        if (os.startsWith("Windows")) {
            apkFile = new File("C:\\Users\\Michael\\Documents\\Work\\Android\\apks\\ws.xsoh.etar_15.apk");
        } else {
            apkFile = new File("/home/auermich/smali/at.linuxtage.companion_1500151.apk");
        }

        apk = DexFileFactory.loadDexContainer(apkFile, Opcodes.forApi(OPCODE_API));;
    }

    @Test
    public void insertLabels() throws Exception {

        apk.getDexEntryNames().forEach(dexFile -> {
            try {
                insertLabels(apk.getEntry(dexFile), dexFile, exclusionPattern);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void insertLabels(DexFile dexFile, String dexFileName, Pattern exclusionPattern) throws Exception {

        List<ClassDef> classes = Lists.newArrayList();

        for (ClassDef classDef : dexFile.getClasses()) {

            // the class name is part of the method id
            String className = Utility.dottedClassName(classDef.getType());

            // exclude certain packages/classes from instrumentation, e.g. android.widget.*
            if (exclusionPattern != null && exclusionPattern.matcher(className).matches()) {
                // System.out.println("Excluding class: " + className + " from instrumentation!");
                classes.add(classDef);
                continue;
            }

            List<Method> methods = Lists.newArrayList();

            for (Method method : classDef.getMethods()) {
                MethodImplementation implementation = method.getImplementation();

                if (implementation == null) {
                    System.err.println("Missing implementation for method: " + method.getName());
                    methods.add(method);
                    continue;
                }

                MutableMethodImplementation mutableMethodImplementation
                        = new MutableMethodImplementation(implementation);

                // mutableMethodImplementation.newLabelForAddress(0);
                // mutableMethodImplementation.newLabelForIndex(0);

                // NOTE that the label is only inserted if an instruction, e.g. goto, refers to it
                /*
                BuilderInstruction nop = new BuilderInstruction10x(Opcode.NOP);
                mutableMethodImplementation.addInstruction(0, nop);
                Label label = nop.getLocation().addNewLabel();

                BuilderInstruction jump = new BuilderInstruction10t(Opcode.GOTO, label);
                mutableMethodImplementation.addInstruction(0, jump);
                */

                int lastIndex = mutableMethodImplementation.getInstructions().size() - 1;

                Label firstLabel = mutableMethodImplementation.newLabelForIndex(lastIndex);
                BuilderInstruction jump = new BuilderInstruction10t(Opcode.GOTO, firstLabel);
                mutableMethodImplementation.addInstruction(0, jump);

                // mutableMethodImplementation.addInstruction(0, new BuilderInstruction10x(Opcode.NOP));

                for (BuilderInstruction instruction : mutableMethodImplementation.getInstructions()) {
                    // instruction.getLocation().addNewLabel();
                }

                methods.add(new ImmutableMethod(
                        method.getDefiningClass(),
                        method.getName(),
                        method.getParameters(),
                        method.getReturnType(),
                        method.getAccessFlags(),
                        method.getAnnotations(),
                        mutableMethodImplementation));
            }

            classes.add(new ImmutableClassDef(
                    classDef.getType(),
                    classDef.getAccessFlags(),
                    classDef.getSuperclass(),
                    classDef.getInterfaces(),
                    classDef.getSourceFile(),
                    classDef.getAnnotations(),
                    classDef.getFields(),
                    methods));
        }

        Utility.writeToDexFile(apkFile.getParent() + File.separator + dexFileName, classes, OPCODE_API);
    }

    @Test
    public void checkExceptionHandlers() throws Exception {

        apk.getDexEntryNames().forEach(dexFile -> {
            try {
                checkExceptionHandlers(apk.getEntry(dexFile), exclusionPattern);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void checkExceptionHandlers(DexFile dexFile, Pattern exclusionPattern) {

        for (ClassDef classDef : dexFile.getClasses()) {

            // the class name is part of the method id
            String className = Utility.dottedClassName(classDef.getType());

            // exclude certain packages/classes from instrumentation, e.g. android.widget.*
            if (exclusionPattern != null && exclusionPattern.matcher(className).matches()) {
                // System.out.println("Excluding class: " + className + " from instrumentation!");
                continue;
            }

            for (Method method : classDef.getMethods()) {
                MethodImplementation implementation = method.getImplementation();

                if (implementation == null) {
                    System.err.println("Missing implementation for method: " + method.toString());
                    continue;
                }

                int consumedCodeUnits = 0;

                for (TryBlock<? extends ExceptionHandler> tryBlock : implementation.getTryBlocks()) {

                    System.out.println("MethodName: " + method.toString());
                    // start address is expressed in terms of code units (absolute)
                    System.out.println("TryBlock Starting Address: " + tryBlock.getStartCodeAddress());
                    // the number of code units contained within the try block -> the length of the try block
                    System.out.println("TryBlock Code Unit Count: " + tryBlock.getCodeUnitCount());

                    Instruction startInstructionTryBlock = null;
                    Instruction endInstructionTryBlock = null;
                    List<Instruction> instructionsInTryBlock = new ArrayList<>();

                    for (Instruction instruction : implementation.getInstructions()) {

                        /*
                        * The relation between a code unit and an instruction is as follows:
                        *
                        * code unit | instruction
                        *      0
                        *               instr1
                        *      k
                        *               instr2
                        *      n
                        *
                        * This means to check whether we reached a starting point, e.g., the first instruction
                        * of a try block, we need to compare the code unit counter before consuming the next instruction.
                        *
                        * However, if we want to check some end point, e.g., the end of a try block, we need to compare
                        * the code unit counter after the consumption of the next instruction.
                         */

                        // the starting point is before the actual instruction
                        if (consumedCodeUnits == tryBlock.getStartCodeAddress()) {
                            startInstructionTryBlock = instruction;
                            instructionsInTryBlock.add(startInstructionTryBlock);
                            System.out.println("First Instruction within try block:" + startInstructionTryBlock.getOpcode());
                            // the end point is after the actual instruction
                        } else if (consumedCodeUnits + instruction.getCodeUnits()
                                == tryBlock.getStartCodeAddress() + tryBlock.getCodeUnitCount()) {
                            endInstructionTryBlock = instruction;
                            instructionsInTryBlock.add(endInstructionTryBlock);
                            System.out.println("Last Instruction within try block:" + endInstructionTryBlock.getOpcode());
                            break;
                        }

                        // avoid inserting start instruction twice if using !isEmpty()
                        if (!instructionsInTryBlock.isEmpty() && !instruction.equals(startInstructionTryBlock)) {
                            instructionsInTryBlock.add(instruction);
                        }

                        consumedCodeUnits += instruction.getCodeUnits();
                    }

                    System.out.println("Detected first Instruction within try block:" + startInstructionTryBlock != null);
                    System.out.println("Detected last Instruction within try block:" + endInstructionTryBlock != null);

                    instructionsInTryBlock.forEach(instruction -> {
                        System.out.println(instruction.getOpcode() + "(" + instruction.getCodeUnits() + ")");
                    });


                    tryBlock.getExceptionHandlers()
                            .forEach(h -> {
                                // seems to be null for when 'catch_all' is used
                                System.out.println("ExceptionType: " + h.getExceptionTypeReference());
                                // System.out.println(h.getExceptionType());

                                // TODO: can we determine the end of a cath block, there is at least no explicit end marker
                                // may search until next jump or abortion instruction (throw, return,...)

                                /*
                                * The (absolute) position of the catch block expressed in terms of code units. The catch
                                * block starts after n-th code units. So, we need to map an instruction to its
                                * size (code units) and count them.
                                 */
                                System.out.println(h.getHandlerCodeAddress());

                                AtomicInteger ctrCodeUnits = new AtomicInteger(0);

                                for (Instruction instruction : implementation.getInstructions()) {
                                    if (ctrCodeUnits.get() == h.getHandlerCodeAddress()) {
                                        System.out.println("First Instruction within catch block: " + instruction.getOpcode());
                                        break;
                                    }
                                    ctrCodeUnits.set(ctrCodeUnits.get() + instruction.getCodeUnits());
                                }
                            });

                    /*
                    System.out.println("Debug Items: ----------------------------------------------");
                    implementation.getDebugItems().forEach(d -> System.out.println(d.getDebugItemType()));
                    implementation.getDebugItems().forEach(d -> System.out.println(d.getCodeAddress()));
                    System.out.println("------------------------------------------------------------");
                    */

                    /*
                    implementation.getInstructions().forEach(i -> {
                        // code unit represents the 'size' of an instruction (should be expressed as a decimal)
                        System.out.println(i.getOpcode() + " (" + i.getCodeUnits() + ")");
                    });
                    */
                }
            }
        }
    }
}
