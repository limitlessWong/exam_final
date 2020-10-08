package com.alvis.exam.controller.student;

import com.alvis.exam.base.BaseApiController;
import com.alvis.exam.base.RestResponse;
import com.alvis.exam.domain.TaskExam;
import com.alvis.exam.domain.TaskExamCustomerAnswer;
import com.alvis.exam.domain.TextContent;
import com.alvis.exam.domain.User;
import com.alvis.exam.domain.enums.ExamPaperTypeEnum;
import com.alvis.exam.domain.task.TaskItemAnswerObject;
import com.alvis.exam.domain.task.TaskItemObject;
import com.alvis.exam.service.*;
import com.alvis.exam.utility.DateTimeUtil;
import com.alvis.exam.utility.JsonUtil;
import com.alvis.exam.viewmodel.student.dashboard.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController("StudentDashboardController")
@RequestMapping(value = "/api/student/dashboard")
@AllArgsConstructor
public class DashboardController extends BaseApiController {

    private final UserService userService;
    private final ExamPaperService examPaperService;
    private final QuestionService questionService;
    private final TaskExamService taskExamService;
    private final TaskExamCustomerAnswerService taskExamCustomerAnswerService;
    private final TextContentService textContentService;

    @RequestMapping(value = "/index", method = RequestMethod.POST)
    public RestResponse<IndexVM> index() {
        IndexVM indexVM = new IndexVM();
        User user = getCurrentUser();//这个就是获取的token是吧

        //todo 这部分是肯定要改的
        //查固定试卷，这个是可以重复做的
        PaperFilter fixedPaperFilter = new PaperFilter();
        fixedPaperFilter.setGradeLevel(user.getUserLevel());//查当前部门的试卷
        fixedPaperFilter.setExamPaperType(ExamPaperTypeEnum.Fixed.getCode());//查固定试卷
        indexVM.setFixedPaper(examPaperService.indexPaper(fixedPaperFilter));//返回值之一

        //查时段试卷
        PaperFilter timeLimitPaperFilter = new PaperFilter();
        timeLimitPaperFilter.setDateTime(new Date());//获取时间 当前时间在时段试卷之内
        timeLimitPaperFilter.setGradeLevel(user.getUserLevel());
        timeLimitPaperFilter.setExamPaperType(ExamPaperTypeEnum.TimeLimit.getCode());
        List<PaperInfo> limitPaper = examPaperService.indexPaper(timeLimitPaperFilter);

        //
        List<PaperInfoVM> paperInfoVMS = limitPaper.stream().map(d -> {
            PaperInfoVM vm = modelMapper.map(d, PaperInfoVM.class);
            vm.setStartTime(DateTimeUtil.dateFormat(d.getLimitStartTime()));
            vm.setEndTime(DateTimeUtil.dateFormat(d.getLimitEndTime()));
            return vm;
        }).collect(Collectors.toList());
        indexVM.setTimeLimitPaper(paperInfoVMS);
        return RestResponse.ok(indexVM);
    }


    @RequestMapping(value = "/task", method = RequestMethod.POST)
    public RestResponse<List<TaskItemVm>> task() {
        User user = getCurrentUser();
        //先找出该部门下的所有任务
        List<TaskExam> taskExams = taskExamService.getByGradeLevel(user.getUserLevel());
        if (taskExams.size() == 0) {
            return RestResponse.ok(new ArrayList<>());
        }
        //获取所有任务的id
        List<Integer> tIds = taskExams.stream().map(taskExam -> taskExam.getId()).collect(Collectors.toList());
        //todo 这个还不是很理解
        List<TaskExamCustomerAnswer> taskExamCustomerAnswers = taskExamCustomerAnswerService.selectByTUid(tIds, user.getId());
        List<TaskItemVm> vm = taskExams.stream().map(t -> {
            TaskItemVm itemVm = new TaskItemVm();
            itemVm.setId(t.getId());
            itemVm.setTitle(t.getTitle());
            TaskExamCustomerAnswer taskExamCustomerAnswer = taskExamCustomerAnswers.stream()
                    .filter(tc -> tc.getTaskExamId().equals(t.getId())).findFirst().orElse(null);
            List<TaskItemPaperVm> paperItemVMS = getTaskItemPaperVm(t.getFrameTextContentId(), taskExamCustomerAnswer);
            itemVm.setPaperItems(paperItemVMS);
            return itemVm;
        }).collect(Collectors.toList());
        return RestResponse.ok(vm);
    }


    private List<TaskItemPaperVm> getTaskItemPaperVm(Integer tFrameId, TaskExamCustomerAnswer taskExamCustomerAnswers) {
        TextContent textContent = textContentService.selectById(tFrameId);
        List<TaskItemObject> paperItems = JsonUtil.toJsonListObject(textContent.getContent(), TaskItemObject.class);

        List<TaskItemAnswerObject> answerPaperItems = null;
        if (null != taskExamCustomerAnswers) {
            TextContent answerTextContent = textContentService.selectById(taskExamCustomerAnswers.getTextContentId());
            answerPaperItems = JsonUtil.toJsonListObject(answerTextContent.getContent(), TaskItemAnswerObject.class);
        }


        List<TaskItemAnswerObject> finalAnswerPaperItems = answerPaperItems;
        return paperItems.stream().map(p -> {
                    TaskItemPaperVm ivm = new TaskItemPaperVm();
                    ivm.setExamPaperId(p.getExamPaperId());
                    ivm.setExamPaperName(p.getExamPaperName());
                    if (null != finalAnswerPaperItems) {
                        finalAnswerPaperItems.stream()
                                .filter(a -> a.getExamPaperId().equals(p.getExamPaperId()))
                                .findFirst()
                                .ifPresent(a -> {
                                    ivm.setExamPaperAnswerId(a.getExamPaperAnswerId());
                                    ivm.setStatus(a.getStatus());
                                });
                    }
                    return ivm;
                }
        ).collect(Collectors.toList());
    }
}
