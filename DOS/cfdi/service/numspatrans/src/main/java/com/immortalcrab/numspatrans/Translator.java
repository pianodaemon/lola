package com.immortalcrab.numspatrans;

import java.io.InputStream;
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
        final String pyFilename = "/numspatrans.py";

        InputStream is = Translator.class.getResourceAsStream(pyFilename);
        if (is == null) {
            throw new Exception("There is a problem with python script");
        }

        PythonInterpreter.initialize(preProps, postProps, argv);

        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.execfile(is);
            result = (PyString) pyInterp.get("res");
        }
        return result.asString();
    }
}
