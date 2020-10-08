package com.alvis.exam.service;

import com.alvis.exam.domain.ExamPaper;
import com.alvis.exam.domain.User;
import com.alvis.exam.viewmodel.admin.exam.ExamPaperEditDTO;
import com.alvis.exam.viewmodel.admin.exam.ExamPaperEditRequestVM;
import com.alvis.exam.viewmodel.admin.exam.ExamPaperPageRequestVM;
import com.alvis.exam.viewmodel.student.dashboard.PaperFilter;
import com.alvis.exam.viewmodel.student.dashboard.PaperInfo;
import com.alvis.exam.viewmodel.student.exam.ExamPaperPageResponseVM;
import com.alvis.exam.viewmodel.student.exam.ExamPaperPageVM;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface ExamPaperService extends BaseService<ExamPaper> {

    PageInfo<ExamPaper> page(ExamPaperPageRequestVM requestVM);

    PageInfo<ExamPaper> taskExamPage(ExamPaperPageRequestVM requestVM);

    PageInfo<ExamPaper> studentPage(ExamPaperPageVM requestVM);


    /**
     * 这个是原来手动添加题目时的逻辑
     * @param examPaperEditRequestVM
     * @param user
     * @return
     */
    ExamPaper savePaperFromVM(ExamPaperEditRequestVM examPaperEditRequestVM, User user);

    /**
     * 这个是修改后的保存试卷的逻辑
     * @param examPaperEditDTO
     * @param user
     * @return
     */
    ExamPaper savePaper(ExamPaperEditDTO examPaperEditDTO, User user);

    ExamPaperEditDTO  examPaperToDTO(ExamPaper examPaper);

    ExamPaperEditRequestVM examPaperToVM(Integer id);

    List<PaperInfo> indexPaper(PaperFilter paperFilter);

    Integer selectAllCount();

    List<Integer> selectMothCount();
}
