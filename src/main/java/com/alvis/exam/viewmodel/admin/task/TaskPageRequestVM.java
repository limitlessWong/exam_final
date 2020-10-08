package com.alvis.exam.viewmodel.admin.task;

import com.alvis.exam.base.BasePage;
import lombok.Data;

@Data
public class TaskPageRequestVM extends BasePage {
    //还有继承过来的属性
    private Integer gradeLevel;
}
