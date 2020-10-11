package com.alvis.exam.viewmodel.admin.exam;

import com.alvis.exam.domain.ExamPaper;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ExamPaperEditDTO {

    private Integer id;
    @NotNull
    private Integer level;
    @NotNull
    private Integer subjectId;
    @NotNull
    private Integer paperType;
    @NotBlank
    private String name;
    @NotNull
    private Integer suggestTime;
    @NotNull
    private Integer questionCount;

    //限制时间  时段试卷才会限制时间
    private List<String> limitDateTime;


}
