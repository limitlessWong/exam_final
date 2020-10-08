package com.alvis.exam.controller.admin;


import com.alvis.exam.base.BaseApiController;
import com.alvis.exam.base.RestResponse;
import com.alvis.exam.domain.TaskExam;
import com.alvis.exam.service.TaskExamService;
import com.alvis.exam.utility.DateTimeUtil;
import com.alvis.exam.utility.PageInfoHelper;
import com.alvis.exam.viewmodel.admin.task.TaskPageRequestVM;
import com.alvis.exam.viewmodel.admin.task.TaskPageResponseVM;
import com.alvis.exam.viewmodel.admin.task.TaskRequestVM;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;

import static com.alvis.exam.base.SystemCode.LIMITDATETIMEERROR;

@RestController("AdminTaskController")
@RequestMapping(value = "/api/admin/task")
@AllArgsConstructor
public class TaskController extends BaseApiController {

    private final TaskExamService taskExamService;

    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public RestResponse<PageInfo<TaskPageResponseVM>> pageList(@RequestBody TaskPageRequestVM model) {
        PageInfo<TaskExam> pageInfo = taskExamService.page(model);
        PageInfo<TaskPageResponseVM> page = PageInfoHelper.copyMap(pageInfo, m -> {
            TaskPageResponseVM vm = modelMapper.map(m, TaskPageResponseVM.class);
            vm.setCreateTime(DateTimeUtil.dateFormat(m.getCreateTime()));
            return vm;
        });
        return RestResponse.ok(page);
    }



    //todo 创建和编辑任务的代码这里边要加一些逻辑  获取选择试卷的id和题数  然后从数据库里随机抽题 放到试卷里
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public RestResponse edit(@RequestBody @Valid TaskRequestVM model) {
        //检查起始时间的大小是否正确
        List<String> dateTimes = model.getLimitDateTime();
        Date start = DateTimeUtil.parse(dateTimes.get(0), DateTimeUtil.STANDER_FORMAT);
        Date end = DateTimeUtil.parse(dateTimes.get(1), DateTimeUtil.STANDER_FORMAT);
        int compareTo = start.compareTo(end);
        System.out.println(compareTo);
        if(compareTo!=-1) return RestResponse.fail(LIMITDATETIMEERROR.getCode(),LIMITDATETIMEERROR.getMessage());

        taskExamService.edit(model, getCurrentUser());
        TaskRequestVM vm = taskExamService.taskExamToVM(model.getId());
        return RestResponse.ok(vm);
    }


    @RequestMapping(value = "/select/{id}", method = RequestMethod.POST)
    public RestResponse<TaskRequestVM> select(@PathVariable Integer id) {
        TaskRequestVM vm = taskExamService.taskExamToVM(id);
        return RestResponse.ok(vm);
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public RestResponse delete(@PathVariable Integer id) {
        TaskExam taskExam = taskExamService.selectById(id);
        taskExam.setDeleted(true);
        taskExamService.updateByIdFilter(taskExam);
        return RestResponse.ok();
    }
}
