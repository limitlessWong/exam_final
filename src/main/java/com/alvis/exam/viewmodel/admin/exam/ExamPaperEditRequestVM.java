package com.alvis.exam.viewmodel.admin.exam;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Data
public class ExamPaperEditRequestVM {
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

    private String score;

    //限制时间
    private List<String> limitDateTime;

    //试卷标题 类似题型  里边有多种题目
    //@Size(min = 1,message = "请添加试卷标题")
    //@Valid
    //虽然之后会没有多大用 但还是要留着区分单选题和多选题
    private List<ExamPaperTitleItemVM> titleItems;



}
