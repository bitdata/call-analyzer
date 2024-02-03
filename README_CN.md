# call-analyzer

## 介绍

Call-Analyzer是一个针对Java程序的测试辅助工具，用于判断代码的修改影响了哪些方法，需要进行怎样的测试才能覆盖这些修改内容。假设方法A调用了方法B,方法B又调用了方法C，则修改方法C会影响到方法A和方法B。一个方法可能会被大量的方法调用。为了简化起见，本工具仅输出被影响的顶级方法，即在指定范围内没有被其他方法调用的方法。测试者通过全面测试这些顶级方法来检测代码修改是否正确。在基于Spring Boot的程序中，这通常是Controller中的方法，或者是由定时器触发的方法。

本工具是纯Spring Boot程序，没有前端界面（读者有兴趣可以自己开发前端页面），操作是在Swagger中调用RestController的方法，也可以使用PostMan之类的工具调用。也许是简陋了点，不过考虑到使用者是Java开发人员，应该影响不大。本工具假定使用者能够熟练使用Java开发。

在有一定规模的工程里面，修改的内容可能影响大量的方法，即使是顶级方法数量也不少。测试人员难以验证所有这些方法。为此，对输出结果中的方法进行分组和排序。覆盖范围相同的方法分在一组，而覆盖范围较大的方法先输出，且输出每组方法前先以k/m/n格式输出覆盖范围。
- n: 需要覆盖的修改点数量。通常一个方法为一个修改点。
- k: 本组方法覆盖的修改点数量。
- m: 本组方法及前面输出的方法一共覆盖的修改点数量。

## 使用步骤

- 重新编译要分析java工程（注意增量编译被分析的jar包可能导致分析不准确）
- 执行 java -jar call-analyzer.jar
- 在浏览器中打开http://localhost:8888/doc.html
- 在swagger中展开左侧的Rest APIs，依次调用parse-jars和analyze-by-commit-time。

## API

### parse-jars

解析Jar包。 

POST /api/parse-jars

请求参数
| 参数名称     | 参数说明        | 请求类型 | 是否必须 | 数据类型   |
| ------------ | --------------- | -------- | -------- | ---------- |
| jarFileNames | jar文件路径列表 | body     | 是       | 字符串数组 |

说明：
在Windows下，路径中的\要用\\代替。
对于spring boot工程，应指定打包成fat jar之前的jar包，通常是在原文件名后面加上.original。比如x:\\...\\call-analyzer\\target\\call-analyzer.jar.original。

### analyze-by-source

分析受到指定位置的源代码顶级方法。

POST /api/analyze-by-source

请求参数
| 参数名称    |                | 参数说明       | 请求类型 | 是否必须 | 数据类型           |
| ----------- | -------------- | -------------- | -------- | -------- | ------------------ |
| sourceLines |                | 源代码行号列表 | body     | 是       | SourceLine类型数组 |
|             | sourceFilePath | 源文件路径     |          | 是       | 字符串             |
|             | line           | 行号           |          | 是       | 整数               |

### analyze-by-commit-time

分析受指定时间后修改的源代码影响顶级方法。

POST /api/analyze-by-commit-time

请求参数
| 参数名称   | 参数说明                    | 请求类型 | 是否必须 | 数据类型                      |
| ---------- | --------------------------- | -------- | -------- | ----------------------------- |
| commitTime | 修改起始时间                | query    | 是       | 时间，yyyy-MM-dd HH:mm:ss格式 |
| gitDir     | git目录路径，通常后缀为.git | query    | 是       | 字符串                        |

## 尚未解决的问题

- 对不在方法内的修改内容的分析。
- 对复杂继承关系的分析，如泛型类的继承。

## 致谢

本工具的开发部分参考了java-callgraph2(https://github.com/Adrninistrator/java-callgraph2)，特此致谢。