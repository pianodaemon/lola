package com.immortalcrab.numspatrans;

import java.util.Properties;

import org.python.core.PyString;
import org.python.util.PythonInterpreter;

public class Cli {

    public static String translateIntegerToSpanish(long number) {
        var preProps = System.getProperties();
        var postProps = new Properties();
        var argv = new String[] { String.valueOf(number) };
        PyString result;

        PythonInterpreter.initialize(preProps, postProps, argv);

        try (PythonInterpreter pyInterp = new PythonInterpreter()) {
            pyInterp.execfile("numspatrans.py");
            result = (PyString) pyInterp.get("res");
        }
        return result.asString();
    }
}