package bitdata.code.controller;

import bitdata.code.service.CallParseService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@Api(tags = "REST APIs")
@RequestMapping("/call")
public class CallController {

    @Autowired
    private CallParseService service;

    @PostMapping("/parse-jar")
    public Boolean parseJar(@RequestParam String jarFileName) throws IOException {
        List<String> jarFileNames = new ArrayList<>();
        jarFileNames.add(jarFileName);
        service.parseJars(jarFileNames);
        return true;
    }

    @PostMapping("/parse-jars")
    public Boolean parseJars(@RequestBody List<String> jarFileNames) throws IOException {
        service.parseJars(jarFileNames);
        return true;
    }

}
