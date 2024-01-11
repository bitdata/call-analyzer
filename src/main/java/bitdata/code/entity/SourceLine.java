package bitdata.code.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel
public class SourceLine {

    @ApiModelProperty("source file path")
    private String sourceFilePath;

    @ApiModelProperty("start line number")
    private Integer line;
}
