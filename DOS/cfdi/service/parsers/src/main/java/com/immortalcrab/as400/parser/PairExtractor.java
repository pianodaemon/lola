package com.immortalcrab.as400.parser;

import com.immortalcrab.as400.error.DecodeError;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import org.javatuples.Pair;

public class PairExtractor {

    enum Transitions {
        WRAP_UP, SEEKOUT_KEY, SEEKOUT_VALUE, ERR_MISSING_TOKEN, ERR_TOO_MANY_TOKENS
    }

    public static List<Pair<String, String>> go4it(final String filePath) throws DecodeError {

        FileReader fr = null;
        try {
            fr = new FileReader(filePath);
            return go4it(fr);
        } catch (FileNotFoundException ex) {
            throw new DecodeError("Not found input file of tokens", ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                throw new DecodeError("Issues were detected when closing a file reader", ex);
            }
        }
    }

    public static List<Pair<String, String>> go4it(InputStreamReader inReader) throws DecodeError {

        var isr = preprocess(inReader);
        PairExtractor ic = new PairExtractor();
        return ic.traverseBuffer(isr);
    }

    private List<Pair<String, String>> traverseBuffer(InputStreamReader inReader) throws DecodeError {
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
            throw new DecodeError("Issue found when traversing buffer of input tokens", ex);
        }

        return rset;
    }

    private Pair<String, String> parseLine(final int idx, final String line) throws DecodeError {

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
                        throw new DecodeError("Line " + idx + " is malformed (" + stage.toString() + ")");
                }
            } while (!done);
        }

        return rpair;
    }

    public static InputStreamReader preprocess(InputStreamReader isr) {

        InputStreamReader isr2 = null;

        try {
            var writer = new StringWriter();
            isr.transferTo(writer);
            var str = writer.toString();

            // Reemplazar algunas cadenas
            str = str.replaceAll("\r\n", "");
            str = str.replaceAll("> <", "><");
            str = str.replaceAll("<>", "< >");
            str = str.replaceAll("<\\.>", "< >");
            str = str.replaceAll("<->", "< >");
            str = str.replaceAll("=====CARTA PORTE===================", "");
            str = str.replaceAll("<SERVICIOS>", "");
            str = str.replaceAll("<COMENTARIOS>", "");
            str = str.replaceAll("<RELACIONADOS>", "");
            str = str.replaceAll("<MERCANCIAS>", "");

            var firstSign = false;
            var sw = new StringWriter();

            // Dejar un solo par <KEY><VALUE> por linea
            for (int i = 0; i < str.length(); i++) {

                char c = str.charAt(i);

                if (c == '>') {
                    if (firstSign) {
                        sw.append(c);
                        sw.append('\n');
                        firstSign = false;
                    } else {
                        sw.append(c);
                        firstSign = true;
                    }
                } else {
                    sw.append(c);
                }
            }

            var bais = new ByteArrayInputStream(sw.toString().getBytes(StandardCharsets.UTF_8));
            isr2 = new InputStreamReader(bais);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return isr2;
    }
}
