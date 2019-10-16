package de.uni_passau.fim.auermich.branchcoverage;

import com.google.common.collect.Lists;
import de.uni_passau.fim.auermich.branchdistance.utility.Utility;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.*;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static de.uni_passau.fim.auermich.branchdistance.BranchDistance.OPCODE_API;

public class ExceptionHandlerTest {

    @Test
    public void checkExceptionHandlers() throws Exception {

        // describes class names we want to exclude from instrumentation
        Pattern exclusionPattern = Utility.readExcludePatterns();

        // the APK file
        File apkFile = new File("/home/auermich/smali/at.linuxtage.companion_1500151.apk");

        // process directly apk file (support for multi-dex)
        MultiDexContainer<? extends DexBackedDexFile> apk
                = DexFileFactory.loadDexContainer(apkFile, Opcodes.forApi(OPCODE_API));

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
                    System.err.println("Missing implementation for method: " + method.getName());
                    continue;
                }

                for (TryBlock<? extends ExceptionHandler> tryBlock : implementation.getTryBlocks()) {

                    System.out.println("ClassName: " + className + " - MethodName: " + method.getName());
                    // start address is expressed in terms of code units (absolute)
                    System.out.println("TryBlock Starting Address: " + tryBlock.getStartCodeAddress());
                    // the number of code units contained within the try block -> the length of the try block
                    System.out.println("TryBlock Code Unit Count: " + tryBlock.getCodeUnitCount());

                    tryBlock.getExceptionHandlers()
                            .forEach(h -> {
                                // seems to be null for when 'catch_all' is used
                                System.out.println("ExceptionType: " + h.getExceptionTypeReference());
                                // System.out.println(h.getExceptionType());

                                /*
                                * The (absolute) position of the catch block expressed in terms of code units. The catch
                                * block starts after n-th code units. So, we need to map an instruction to its
                                * size (code units) and count them.
                                 */
                                System.out.println(h.getHandlerCodeAddress());
                            });

                    /*
                    System.out.println("Debug Items: ----------------------------------------------");
                    implementation.getDebugItems().forEach(d -> System.out.println(d.getDebugItemType()));
                    implementation.getDebugItems().forEach(d -> System.out.println(d.getCodeAddress()));
                    System.out.println("------------------------------------------------------------");
                    */

                    implementation.getInstructions().forEach(i -> {
                        // code unit represents the 'size' of an instruction (should be expressed as a decimal)
                        System.out.println(i.getOpcode() + " (" + i.getCodeUnits() + ")");
                    });

                }
            }
        }
    }
}
