package com.alvis.exam.service;

import com.alvis.exam.domain.Question;
import com.alvis.exam.viewmodel.admin.question.QuestionEditRequestVM;
import com.alvis.exam.viewmodel.admin.question.QuestionPageRequestVM;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface QuestionService extends BaseService<Question> {

    PageInfo<Question> page(QuestionPageRequestVM requestVM);

    Question insertFullQuestion(QuestionEditRequestVM model, Integer userId);

    Question updateFullQuestion(QuestionEditRequestVM model);

    QuestionEditRequestVM getQuestionEditRequestVM(Integer questionId);

    QuestionEditRequestVM getQuestionEditRequestVM(Question question);

    Integer selectAllCount();

    List<Integer> selectMothCount();

    /**
     * 从数据库里随机抽题先试试这样做可不可以
     * 只抽题数也是不可以的，也要根据试卷的科目id来抽
     */
    List<Question> selectQuestionByRand(Integer subjectId,Integer questionCount);

}
