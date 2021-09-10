package com.immortalcrab.as400.webserver;

import com.immortalcrab.as400.engine.ErrorCodes;
import com.immortalcrab.as400.pipeline.Pipeline;
import com.immortalcrab.as400.pipeline.PipelineError;
import com.immortalcrab.as400.parser.PairExtractorError;
import com.immortalcrab.as400.request.CfdiRequestError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
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
    Map<String, Object> issue(
            @RequestParam String kind,
            @RequestPart MultipartFile tokensDoc) throws IOException {

        Map<String, Object> rhm = new HashMap<>();
        rhm.put("platcode", ErrorCodes.SUCCESS.toString());
        rhm.put("desccode", "");

        InputStream is = tokensDoc.getInputStream();

        try {
            Pipeline.issue(kind, new InputStreamReader(is));
        } catch (PairExtractorError | CfdiRequestError | PipelineError ex) {

            rhm.put("platcode", ex.getErrorCode());
            rhm.put("desccode", ex.getMessage());
        }

        return rhm;
    }

}
