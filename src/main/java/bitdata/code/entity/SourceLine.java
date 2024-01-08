package bitdata.code.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SourceLine {

    private String sourceFilePath;

    private Integer line;
}
