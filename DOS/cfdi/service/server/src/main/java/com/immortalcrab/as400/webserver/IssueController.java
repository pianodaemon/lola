package com.immortalcrab.as400.webserver;

import com.immortalcrab.as400.misc.pipeline.Pipeline;
import com.immortalcrab.as400.parser.PairExtractorError;
import com.immortalcrab.as400.request.CfdiRequestError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class IssueController {

    @RequestMapping("/")
    String hellow() {
        return "Hello World!";
    }

    @RequestMapping(value = "uploadTokensDoc", method = RequestMethod.POST,
            headers = {"content-type=multipart/form-data"})
    Map<String, String> issue(
            @RequestParam String kind,
            @RequestPart MultipartFile tokensDoc) {

        Map<String, String> rhm = new HashMap<>();

        try {
            Pipeline.issue(kind, new InputStreamReader(tokensDoc.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(IssueController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PairExtractorError ex) {
            Logger.getLogger(IssueController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CfdiRequestError ex) {
            Logger.getLogger(IssueController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return rhm;
    }

}
