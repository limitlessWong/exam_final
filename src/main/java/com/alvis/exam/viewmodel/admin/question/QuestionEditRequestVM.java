package com.alvis.exam.viewmodel.admin.question;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

@Data
public class QuestionEditRequestVM {

    private Integer id;

    private Integer questionType;

    private Integer subjectId;

    //题干

    private String title;

    private Integer gradeLevel;

    //选项

    private List<QuestionEditItemVM> items;

    private String analyze;

    private List<String> correctArray;

    //正确答案
    private String correct;

    //score
    private String score;


    private Integer difficult;

    private Integer itemOrder;
}
