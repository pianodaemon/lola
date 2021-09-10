package com.immortalcrab.cfdi.parser;

import com.immortalcrab.as400.parser.PairExtractor;
import com.immortalcrab.as400.parser.PairExtractorError;
import com.immortalcrab.as400.request.FacturaRequest;
import com.immortalcrab.as400.request.CfdiRequestError;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {

        try {

            System.out.println(FacturaRequest.render(PairExtractor.go4it("/home/j4nusx/xxx.txt")).getDs());
        } catch (CfdiRequestError ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);

        } catch (PairExtractorError ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
