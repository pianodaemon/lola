package com.immortalcrab.as400.parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.javatuples.Pair;

public class PairExtractor {

    enum Transitions {
        WRAP_UP, SEEKOUT_KEY, SEEKOUT_VALUE, ERR_MISSING_TOKEN, ERR_TOO_MANY_TOKENS
    }

    public static List<Pair<String, String>> go4it(final String filePath) throws PairExtractorError {

        FileReader fr = null;
        try {
            fr = new FileReader(filePath);
            return go4it(fr);
        } catch (FileNotFoundException ex) {
            throw new PairExtractorError("Not found input file of tokens", ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                throw new PairExtractorError("Issues were detected when closing a file reader", ex);
            }
        }
    }

    public static List<Pair<String, String>> go4it(InputStreamReader inReader) throws PairExtractorError {

        PairExtractor ic = new PairExtractor();
        return ic.traverseBuffer(inReader);
    }

    private List<Pair<String, String>> traverseBuffer(InputStreamReader inReader) throws PairExtractorError {
        BufferedReader br = new BufferedReader(inReader);

        LinkedList<Pair<String, String>> rset = new LinkedList<>();
        int idx = 0;
        try {
            String st;
            while ((st = br.readLine()) != null) {
                ++idx;
                if (st.isBlank()) {
                    continue;
                }
                rset.add(this.parseLine(idx, st.trim()));
            }
        } catch (IOException ex) {
            throw new PairExtractorError("Issue found when traversing buffer of input tokens", ex);
        }

        return rset;
    }

    private Pair<String, String> parseLine(final int idx, final String line) throws PairExtractorError {

        final String delimiters = "<>";
        StringTokenizer st = new StringTokenizer(line, delimiters);

        Pair<String, String> rpair = null;
        {
            Transitions stage = Transitions.SEEKOUT_KEY;
            String k = null;
            boolean done = false;
            do {

                switch (stage) {

                    case SEEKOUT_KEY: {
                        if (st.hasMoreTokens()) {
                            k = st.nextToken();
                            stage = Transitions.SEEKOUT_VALUE;
                        } else {
                            stage = Transitions.ERR_MISSING_TOKEN;
                        }
                        break;
                    }

                    case SEEKOUT_VALUE: {
                        if (st.hasMoreTokens() && !k.isBlank()) {
                            rpair = new Pair<>(k, st.nextToken());
                            stage = Transitions.WRAP_UP;
                        } else {
                            stage = Transitions.ERR_MISSING_TOKEN;
                        }
                        break;
                    }

                    case WRAP_UP: {
                        if (!st.hasMoreTokens()) {
                            done = true;
                        } else {
                            stage = Transitions.ERR_TOO_MANY_TOKENS;
                        }
                        break;
                    }

                    case ERR_TOO_MANY_TOKENS:
                    case ERR_MISSING_TOKEN:
                        throw new PairExtractorError("Line " + idx + " is malformed (" + stage.toString() + ")");
                }
            } while (!done);
        }

        return rpair;
    }
}
