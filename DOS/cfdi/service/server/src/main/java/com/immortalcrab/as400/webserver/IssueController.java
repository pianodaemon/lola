package com.immortalcrab.as400.webserver;

import com.immortalcrab.as400.pipeline.Pipeline;
import com.immortalcrab.as400.error.PipelineError;
import com.immortalcrab.as400.error.PairExtractorError;
import com.immortalcrab.as400.error.CfdiRequestError;
import com.immortalcrab.as400.error.FormatError;
import com.immortalcrab.as400.error.StorageError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class IssueController {

    @RequestMapping(
            path = "/{kind}",
            method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    ResponseEntity<Map<String, Object>> issue(
            @PathVariable("kind") String kind,
            @RequestPart MultipartFile tokensDoc) throws IOException {

        Map<String, Object> rhm = new HashMap<>() {
            {
                put("code", 0);
                put("desc", "");
            }
        };

        InputStream is = tokensDoc.getInputStream();

        try {
            Pipeline.issue(kind, new InputStreamReader(is));
        } catch (FormatError | PairExtractorError | CfdiRequestError | PipelineError | StorageError ex) {

            rhm.put("code", ex.getErrorCode());
            rhm.put("desc", ex.getMessage());

            return new ResponseEntity<>(rhm, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(rhm, HttpStatus.CREATED);
    }

}
