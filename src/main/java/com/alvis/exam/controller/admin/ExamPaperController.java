package com.alvis.exam.controller.admin;

import com.alvis.exam.base.BaseApiController;
import com.alvis.exam.base.RestResponse;
import com.alvis.exam.base.SystemCode;
import com.alvis.exam.domain.ExamPaper;
import com.alvis.exam.service.ExamPaperService;
import com.alvis.exam.utility.DateTimeUtil;
import com.alvis.exam.utility.PageInfoHelper;
import com.alvis.exam.viewmodel.admin.exam.ExamPaperEditDTO;
import com.alvis.exam.viewmodel.admin.exam.ExamPaperPageRequestVM;
import com.alvis.exam.viewmodel.admin.exam.ExamPaperEditRequestVM;
import com.alvis.exam.viewmodel.admin.exam.ExamResponseVM;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController("AdminExamPaperController")
@RequestMapping(value = "/api/admin/exam/paper")
@AllArgsConstructor
public class ExamPaperController extends BaseApiController {

    private final ExamPaperService examPaperService;

    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public RestResponse<PageInfo<ExamResponseVM>> pageList(@RequestBody ExamPaperPageRequestVM model) {
        PageInfo<ExamPaper> pageInfo = examPaperService.page(model);
        PageInfo<ExamResponseVM> page = PageInfoHelper.copyMap(pageInfo, e -> {
            ExamResponseVM vm = modelMapper.map(e, ExamResponseVM.class);
            //这两个类的时间类型不用 所以不能直接modelMapper过来，需要set
            vm.setCreateTime(DateTimeUtil.dateFormat(e.getCreateTime()));
            return vm;
        });
        return RestResponse.ok(page);
    }


    /**
     * 在创建任务时显示出来的试卷列表  可能不需要显示很多详细信息
     * @param model
     * @return
     */
    @RequestMapping(value = "/taskExamPage", method = RequestMethod.POST)
    public RestResponse<PageInfo<ExamResponseVM>> taskExamPageList(@RequestBody ExamPaperPageRequestVM model) {
        PageInfo<ExamPaper> pageInfo = examPaperService.taskExamPage(model);
        PageInfo<ExamResponseVM> page = PageInfoHelper.copyMap(pageInfo, e -> {
            ExamResponseVM vm = modelMapper.map(e, ExamResponseVM.class);
            vm.setCreateTime(DateTimeUtil.dateFormat(e.getCreateTime()));
            return vm;
        });
        return RestResponse.ok(page);
    }

    /**
     * 这个是之前的旧逻辑，没有用了
     * @param model
     * @return
     */
    @RequestMapping(value = "/editOld", method = RequestMethod.POST)
    public RestResponse<ExamPaperEditRequestVM> edit(@RequestBody @Valid ExamPaperEditRequestVM model) {
        ExamPaper examPaper = examPaperService.savePaperFromVM(model, getCurrentUser());
        ExamPaperEditRequestVM newVM = examPaperService.examPaperToVM(examPaper.getId());
        return RestResponse.ok(newVM);
    }

    //todo:保存创建的试卷  上边的的/edit是原有逻辑  现在不用那么复杂了只需要保存学科题数等信息 题目在创建任务的时候自动添加
    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public RestResponse editNew(@RequestBody ExamPaperEditDTO query){
        ExamPaper examPaper  = examPaperService.savePaper(query,getCurrentUser());
        if(null!=examPaper)
            return RestResponse.ok();
        else return RestResponse.fail(SystemCode.EXAMPAPERFAILINSERT.getCode(),SystemCode.EXAMPAPERFAILINSERT.getMessage());
    }

    /*@RequestMapping(value = "/selectOld/{id}", method = RequestMethod.POST)
    public RestResponse<ExamPaperEditRequestVM> select(@PathVariable Integer id) {
        ExamPaperEditRequestVM vm = examPaperService.examPaperToVM(id);
        return RestResponse.ok(vm);
    }*/

    //todo 同理编辑试卷信息回写的时候也不用那么多信息了 只回写试卷的基本信息 试卷的题目信息
    @RequestMapping(value = "/select/{id}", method = RequestMethod.POST)
    public RestResponse<ExamPaperEditDTO> selectById(@PathVariable Integer id){
        ExamPaper examPaper = examPaperService.selectById(id);
        ExamPaperEditDTO examPaperEditDTO = examPaperService.examPaperToDTO(examPaper);
        return RestResponse.ok(examPaperEditDTO);
    }


    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public RestResponse delete(@PathVariable Integer id) {
        ExamPaper examPaper = examPaperService.selectById(id);
        examPaper.setDeleted(true);
        //软删除，而且只删除了paper表里的，没有删除试卷详细信息里的
        examPaperService.updateByIdFilter(examPaper);
        return RestResponse.ok();
    }
}
