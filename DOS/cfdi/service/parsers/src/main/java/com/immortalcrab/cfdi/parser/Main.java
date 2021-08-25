package com.immortalcrab.cfdi.parser;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;


public class Main {


    public static void main(String[] args) {
        try {
            FacturaParser fp = new FacturaParser("/home/userd/Downloads/NV139010.XML");
            Map<String, Object> ds = fp.getDs();
            System.out.println(ds);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
