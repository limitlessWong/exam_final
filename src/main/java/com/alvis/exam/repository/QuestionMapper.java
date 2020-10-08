package com.alvis.exam.repository;

import com.alvis.exam.domain.other.KeyValue;
import com.alvis.exam.domain.Question;
import com.alvis.exam.viewmodel.admin.question.QuestionEditRequestVM;
import com.alvis.exam.viewmodel.admin.question.QuestionPageRequestVM;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
    int deleteByPrimaryKey(Integer id);

    int insert(Question record);

    int insertSelective(Question record);

    Question selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Question record);

    int updateByPrimaryKey(Question record);

    List<Question> page(QuestionPageRequestVM requestVM);

    List<Question> selectByIds(@Param("ids") List<Integer> ids);

    Integer selectAllCount();

    List<KeyValue> selectCountByDate(@Param("startTime") Date startTime,@Param("endTime") Date endTime);

    /**
     * 随机查题
     * @param questionCount
     * @return 返回question就行 不需要复杂的选项之类的
     * 之前的逻辑是需要
     */
    List<Question> selectQuestionByRand(@Param("subjectId") Integer subjectId,@Param("questionCount") Integer questionCount);

    /**
     * 下边两个函数一个是查最大id一个是查最小id 
     */
/*    Integer selectMaxId();

    Integer selectMinId();*/

}
