package com.alvis.exam.viewmodel.admin.exam;

import com.alvis.exam.domain.ExamPaper;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

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


}
