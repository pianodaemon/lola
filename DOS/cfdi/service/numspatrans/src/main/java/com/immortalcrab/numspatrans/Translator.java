package com.immortalcrab.numspatrans;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import org.python.core.PyString;
import org.python.util.PythonInterpreter;

public class Translator {

    public static void main(String[] args) {
        try {
            System.out.println(translateIntegerToSpanish(123451));
        } catch (Exception e) {
            System.out.println("CRITICAL ERROR: " + e);
        }
    }

    public static String translateIntegerToSpanish(long number) throws Exception {

        var preProps = System.getProperties();
        var postProps = new Properties();
        var argv = new String[] { String.valueOf(number) };
        PyString result;
        var pyScript = new File("/resources/numspatrans.py");

        byte[] bytes = Files.readAllBytes(pyScript.toPath());
        var bais = new ByteArrayInputStream(bytes);

        PythonInterpreter.initialize(preProps, postProps, argv);

        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.execfile(bais);
            result = (PyString) pyInterp.get("res");
        }
        return result.asString();
    }
}
