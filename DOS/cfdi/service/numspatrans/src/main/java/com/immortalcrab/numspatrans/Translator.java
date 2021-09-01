package com.immortalcrab.numspatrans;

import java.io.FileNotFoundException;
import java.util.Properties;
import org.springframework.util.ResourceUtils;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;

public class Translator {

    public static void main(String[] args) {
        System.out.println(translateIntegerToSpanish(12344));
    }

    public static String translateIntegerToSpanish(long number) {

        var preProps = System.getProperties();
        var postProps = new Properties();
        var argv = new String[] { String.valueOf(number) };
        PyString result;
        final String pyFilename = "numspatrans.py";

        String pyPath;
        try {
            pyPath = ResourceUtils.getFile("classpath:" + pyFilename).getAbsolutePath();
        } catch (FileNotFoundException e) {
            pyPath = pyFilename;
        }

        PythonInterpreter.initialize(preProps, postProps, argv);

        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.execfile(pyPath);
            result = (PyString) pyInterp.get("res");
        }
        return result.asString();
    }
}