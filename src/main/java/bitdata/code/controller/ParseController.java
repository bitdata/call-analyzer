package bitdata.code.controller;

import bitdata.code.entity.SourceLine;
import bitdata.code.service.ParseService;
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

    @PostMapping("/parse-jars")
    @ApiOperation(value = "parse jars", httpMethod = "POST", produces = "application/json",
            notes = "Parse jar packages.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "jarFileNames", value = "jar file path", required = true, dataType = "String", allowMultiple = true, paramType = "body")
    })
    public Boolean parseJars(@RequestBody List<String> jarFileNames) throws IOException {
        service.parseJars(jarFileNames);
        return true;
    }

    @PostMapping("/analyze-by-source")
    @ApiOperation(value = "analyze by source", httpMethod = "POST", produces = "application/json",
            notes = "Analyze top-level methods affected by modifying source code after a specified time.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "sourceLines", value = "source line", required = true, dataType = "SourceLine", allowMultiple = true, paramType = "body")
    })
    public Collection<String> analyzeBySource(@RequestBody List<SourceLine> sourceLines) {
        return service.analyze(sourceLines);
    }

    @PostMapping("/analyze-by-commit-time")
    @ApiOperation(value = "get dirty source callers", httpMethod = "POST", produces = "application/json",
            notes = "find top-level methods changed after commit")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "gitDir", value = "git full path, always end with .git", required = true, dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "commitTime", value = "the time after which code committed, with yyyy-MM-dd HH:mm:ss format", required = true, dataType = "Date", paramType = "query")
    })
    public Collection<String> analyzeByCommitTime(
            @RequestParam String gitDir,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date commitTime) throws IOException, GitAPIException {
        List<SourceLine> sourceLines = GitUtil.getDirtyLines(gitDir, commitTime);
        if (sourceLines == null) {
            return null;
        }
        return service.analyze(sourceLines);
    }


}
