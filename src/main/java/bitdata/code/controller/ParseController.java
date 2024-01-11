package bitdata.code.controller;

import bitdata.code.entity.SourceLine;
import bitdata.code.service.ParseService;
import bitdata.code.util.ClassMethod;
import bitdata.code.util.GitUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/api")
public class ParseController {

    @Autowired
    private ParseService service;

    @PostMapping("/set-outer-attributes")
    @ApiOperation(value = "set outer attributes", httpMethod = "POST", produces = "application/json",
            notes = "set attributes to judge if a method is outer method")
    public Boolean setOuterAttributes(@RequestBody Collection<String> outerAttributes) {
        service.setOuterAttributes(outerAttributes);
        return true;
    }

    @PostMapping("/parse-jar")
    @ApiOperation(value = "parse jar", httpMethod = "POST", produces = "application/json", notes = "parse a jar file")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jarFileName", value = "jar file path", required = true, dataType = "String", paramType = "query")
    })
    public Boolean parseJar(@RequestParam String jarFileName) throws IOException {
        List<String> jarFileNames = new ArrayList<>();
        jarFileNames.add(jarFileName);
        service.parseJars(jarFileNames);
        return true;
    }

    @PostMapping("/parse-jars")
    @ApiOperation(value = "parse jars", httpMethod = "POST", produces = "application/json", notes = "parse many jar files")
    public Boolean parseJars(@RequestBody List<String> jarFileNames) throws IOException {
        service.parseJars(jarFileNames);
        return true;
    }

    @PostMapping("/get-outer-callers")
    @ApiOperation(value = "get outer callers", httpMethod = "POST", produces = "application/json",
            notes = "find outer callers by source line")
    public Collection<ClassMethod> getOuterCallers(@RequestBody List<SourceLine> sourceLines) throws IOException {
        return service.getOuterCallers(sourceLines);
    }

    @PostMapping("/get-dirty-outer-callers")
    @ApiOperation(value = "get dirty outer callers", httpMethod = "POST", produces = "application/json",
            notes = "find outer callers changed after some time")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gitDir", value = "git full path, always end with .git", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "commitTime", value = "the time after which code committed, with yyyy-MM-dd HH:mm:ss format", required = true, dataType = "Date", paramType = "query")
    })
    public Collection<ClassMethod> getDirtyOuterCallers(
            @RequestParam String gitDir,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date commitTime) throws IOException, GitAPIException {
        List<SourceLine> sourceLines = GitUtil.getDirtyLines(gitDir, "src/main/java/", commitTime);
        if (sourceLines == null) {
            return null;
        }
        return service.getOuterCallers(sourceLines);
    }


}
