# Call-Analyzer

[中文文档](README_CN.md)

PS: The project has been discontinued and will be replaced by a redesigned tool of the same type in the future.

## Introduction

Call-Analyzer is an assistant tool for testing Java programs that determines which methods are affected by code modifications and to determine what kind of testing is necessary to cover these changes effectively. Suppose method A calls method B, and method B in turn calls method C, then modifying method C will affect both method A and method B. A single method may be invoked by numerous other methods. For simplicity, this tool only outputs the top-level affected methods, i.e. those that are not called by any other methods within the specified scope. Testers can thoroughly test these top-level methods to verify if the code modifications are correct. In Spring Boot-based applications, these typically include methods in Controllers or methods triggered by timers.

This tool is a pure Spring Boot application without a front-end interface (although readers are welcome to develop one themselves). Operations are performed by invoking methods of RestController through Swagger, or using tools like PostMan. It might seem rudimentary, but considering that most users are Java developers, this should not significantly impact its usability. This tool assumes that users are proficient in Java development.

In large-scale projects, modifications may affect a significant number of methods, with even the upper-level method counts being substantial. It can be challenging for testers to verify all these methods individually. To address this issue, the output results are grouped and sorted based on the affected methods. Methods that share the same coverage scope are grouped together, and those with a broader coverage range are prioritized in the output. Prior to each group of methods being displayed, their coverage scope is presented in the format of k/m/n:

- n: The total number of modification points requiring coverage, typically with one method equating to one modification point.
- k: The number of modification points covered by the methods within this specific group.
- m: The cumulative number of modification points covered by the methods within this group and all previously outputted groups.

It should be noted that, despite being grouped together, testing methods within the same group is not necessarily entirely equivalent due to variations in the parameters passed to these methods.

## Steps to Use

1. Full recompile the target Java project for analysis (note that incremental compilation of the analyzed jar package might lead to inaccurate results).
2. Execute: `java -jar call-analyzer.jar`
3. Open http://localhost:8888/doc.html in your web browser.
4. In Swagger page, expand the left-side Rest APIs section, and invoke `parse-jars` and `analyze-by-commit-time` sequentially.

## APIs

### parse-jars

Parse Jar packages.

POST /api/parse-jars

Request Parameters
| argument     | note                   | request type | required | data type    |
| ------------ | ---------------------- | ------------ | -------- | ------------ |
| jarFileNames | List of jar file paths | body         | Yes      | String Array |

Note:
In Windows, replace \ with \\ in the path.
For Spring Boot projects, specify the jar package before it's packaged into a fat jar, usually with the original filename suffixed with `.original`. For example: `x:\\...\\call-analyzer\\target\\call-analyzer.jar.original`.

### analyze-by-source

Analyze top-level methods affected by source code at specific locations.

POST /api/analyze-by-source

Request Parameters
| argument    |                | note                        | request type | required | data type             |
| ----------- | -------------- | --------------------------- | ------------ | -------- | --------------------- |
| sourceLines |                | List of source line numbers | body         | Yes      | SourceLine type array |
|             | sourceFilePath | Path to the source file     |              | Yes      | String                |
|             | line           | Line number                 |              | Yes      | Integer               |

### analyze-by-commit-time

Analyze top-level methods affected by modifying source code after a specified time.

POST /api/analyze-by-commit-time

Request Parameters
| argument   | note                                                | request type | required | data type                          |
| ---------- | --------------------------------------------------- | ------------ | -------- | ---------------------------------- |
| commitTime | Starting modification time                          | query        | Yes      | Time in yyyy-MM-dd HH:mm:ss format |
| gitDir     | Path to the git directory, usually ending with .git | query        | Yes      | String                             |

## Unresolved Issues

- Analysis of modifications made outside the context of methods.
- Analysis of complex inheritance relationships, such as those involving generic classes.

## Acknowledgments

The development of this tool has been inspired by java-callgraph2 (https://github.com/Adrninistrator/java-callgraph2), and we extend our sincere gratitude to the authors.
