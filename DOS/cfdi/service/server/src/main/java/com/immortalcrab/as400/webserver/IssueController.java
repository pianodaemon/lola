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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class IssueController {

    @RequestMapping("/")
    String hellow() {
        return "Hello World!";
    }

    @RequestMapping(
            path = "/{kind}",
            value = "uploadTokensDoc",
            method = RequestMethod.POST,
            headers = {"content-type=multipart/form-data"})
    ResponseEntity<Map<String, Object>> issue(
            @PathVariable("name") String kind,
            @RequestPart MultipartFile tokensDoc) throws IOException {

        Map<String, Object> rhm = new HashMap<>() {
            {
                put("code", ErrorCodes.SUCCESS.toString());
                put("desc", "");
            }
        };

        InputStream is = tokensDoc.getInputStream();

        try {
            Pipeline.issue(kind, new InputStreamReader(is));
        } catch (PairExtractorError | CfdiRequestError | PipelineError ex) {

            rhm.put("code", ex.getErrorCode());
            rhm.put("desc", ex.getMessage());

            return new ResponseEntity<>(rhm, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(rhm, HttpStatus.CREATED);
    }

}
