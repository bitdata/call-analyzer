package bitdata.code.entity;

import lombok.Data;

@Data
public class SourceLine {

    private String sourceFilePath;

    private Integer line;
}
