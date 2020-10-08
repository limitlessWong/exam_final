package com.alvis.exam.viewmodel.admin.exam;

import com.alvis.exam.viewmodel.admin.question.QuestionEditRequestVM;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class ExamPaperTitleItemVM {

    private String name;

    private List<QuestionEditRequestVM> questionItems;
}
