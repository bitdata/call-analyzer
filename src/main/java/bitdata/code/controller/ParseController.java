package bitdata.code.controller;

import bitdata.code.entity.SourceLine;
import bitdata.code.service.ParseService;
import bitdata.code.util.ClassMethod;
import bitdata.code.util.GitUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
@Api(tags = "REST APIs")
@RequestMapping("/call")
public class ParseController {

    @Autowired
    private ParseService service;

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

    @PostMapping("/get-outer-callers")
    public Collection<ClassMethod> getOuterCallers(@RequestBody List<SourceLine> sourceLines) throws IOException {
        return service.getOuterCallers(sourceLines);
    }

    @PostMapping("/get-dirty-outer-callers")
    public Collection<ClassMethod> getDirtyOuterCallers(
            @RequestParam String gitDir,
            @RequestParam(required = false) String pathPrefix,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date commitTime) throws IOException, GitAPIException {
        if (StringUtils.isEmpty(pathPrefix)) {
            pathPrefix = "src/main/java/";
        }
        List<SourceLine> sourceLines = GitUtil.getDirtyLines(gitDir, pathPrefix, commitTime);
        return service.getOuterCallers(sourceLines);
    }


}
