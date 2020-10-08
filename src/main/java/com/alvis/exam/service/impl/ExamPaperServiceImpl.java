package com.alvis.exam.service.impl;

import ch.qos.logback.core.net.SyslogOutputStream;
import com.alvis.exam.domain.*;
import com.alvis.exam.domain.enums.ExamPaperTypeEnum;
import com.alvis.exam.domain.exam.ExamPaperQuestionItemObject;
import com.alvis.exam.domain.exam.ExamPaperTitleItemObject;
import com.alvis.exam.domain.other.KeyValue;
import com.alvis.exam.repository.ExamPaperMapper;
import com.alvis.exam.repository.QuestionMapper;
import com.alvis.exam.service.ExamPaperService;
import com.alvis.exam.service.QuestionService;
import com.alvis.exam.service.SubjectService;
import com.alvis.exam.service.TextContentService;
import com.alvis.exam.service.enums.ActionEnum;
import com.alvis.exam.utility.DateTimeUtil;
import com.alvis.exam.utility.JsonUtil;
import com.alvis.exam.utility.ModelMapperSingle;
import com.alvis.exam.utility.ExamUtil;
import com.alvis.exam.viewmodel.admin.exam.ExamPaperEditDTO;
import com.alvis.exam.viewmodel.admin.exam.ExamPaperEditRequestVM;
import com.alvis.exam.viewmodel.admin.exam.ExamPaperPageRequestVM;
import com.alvis.exam.viewmodel.admin.exam.ExamPaperTitleItemVM;
import com.alvis.exam.viewmodel.admin.question.QuestionEditRequestVM;
import com.alvis.exam.viewmodel.student.dashboard.PaperFilter;
import com.alvis.exam.viewmodel.student.dashboard.PaperInfo;
import com.alvis.exam.viewmodel.student.exam.ExamPaperPageVM;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ExamPaperServiceImpl extends BaseServiceImpl<ExamPaper> implements ExamPaperService {

    protected final static ModelMapper modelMapper = ModelMapperSingle.Instance();
    private final ExamPaperMapper examPaperMapper;
    private final QuestionMapper questionMapper;
    private final TextContentService textContentService;
    private final QuestionService questionService;
    private final SubjectService subjectService;

    @Autowired
    public ExamPaperServiceImpl(ExamPaperMapper examPaperMapper, QuestionMapper questionMapper, TextContentService textContentService, QuestionService questionService, SubjectService subjectService) {
        super(examPaperMapper);
        this.examPaperMapper = examPaperMapper;
        this.questionMapper = questionMapper;
        this.textContentService = textContentService;
        this.questionService = questionService;
        this.subjectService = subjectService;
    }


    @Override
    public PageInfo<ExamPaper> page(ExamPaperPageRequestVM requestVM) {
        return PageHelper.startPage(requestVM.getPageIndex(), requestVM.getPageSize(), "id desc").doSelectPageInfo(() ->
                examPaperMapper.page(requestVM));
    }

    @Override
    public PageInfo<ExamPaper> taskExamPage(ExamPaperPageRequestVM requestVM) {
        return PageHelper.startPage(requestVM.getPageIndex(), requestVM.getPageSize(), "id desc").doSelectPageInfo(() ->
                examPaperMapper.taskExamPage(requestVM));
    }

    @Override
    public PageInfo<ExamPaper> studentPage(ExamPaperPageVM requestVM) {
        return PageHelper.startPage(requestVM.getPageIndex(), requestVM.getPageSize(), "id desc").doSelectPageInfo(() ->
                examPaperMapper.studentPage(requestVM));
    }


    @Override
    @Transactional
    public ExamPaper savePaperFromVM(ExamPaperEditRequestVM examPaperEditRequestVM, User user) {//原有逻辑
        //保存试卷时没有传id过来，编辑试卷时才会传id过来
        ActionEnum actionEnum = (examPaperEditRequestVM.getId() == null) ? ActionEnum.ADD : ActionEnum.UPDATE;
        Date now = new Date();
        //从前端传的值里获取标题，注意这里也是VM
        List<ExamPaperTitleItemVM> titleItemsVM = examPaperEditRequestVM.getTitleItems();

        //一摸一样的字段应该就不需要这一步了吧
        List<ExamPaperTitleItemObject> frameTextContentList = frameTextContentFromVM(titleItemsVM);

        String frameTextContentStr = JsonUtil.toJsonStr(frameTextContentList);

        //System.out.println(frameTextContentStr);

        //把试卷存到数据库里，要分两步存
        ExamPaper examPaper;
        if (actionEnum == ActionEnum.ADD) {
            //第一步：存json字符串---这个itemOrder是做什么的
            examPaper = modelMapper.map(examPaperEditRequestVM, ExamPaper.class);
            TextContent frameTextContent = new TextContent(frameTextContentStr, now);
            textContentService.insertByFilter(frameTextContent);
            //所以在添加题目的时候在把
            //第二步：存试卷信息
            examPaper.setFrameTextContentId(frameTextContent.getId());
            examPaper.setCreateTime(now);
            examPaper.setCreateUser(user.getId());
            examPaper.setDeleted(false);
            //把实体类缺的字段给补上
            examPaperFromVM(examPaperEditRequestVM, examPaper, titleItemsVM);
            //插入试卷到数据库
            examPaperMapper.insertSelective(examPaper);
        } else {
            //更新试卷信息
            //先获取试卷的id
            examPaper = examPaperMapper.selectByPrimaryKey(examPaperEditRequestVM.getId());
            //获取试卷的json字符串信息
            TextContent frameTextContent = textContentService.selectById(examPaper.getFrameTextContentId());
            //更新content
            frameTextContent.setContent(frameTextContentStr);
            textContentService.updateByIdFilter(frameTextContent);//哪个不为空更新哪个

            modelMapper.map(examPaperEditRequestVM, examPaper);
            examPaperFromVM(examPaperEditRequestVM, examPaper, titleItemsVM);
            examPaperMapper.updateByPrimaryKeySelective(examPaper);
        }
        return examPaper;
    }

    /**
     * 修改后的逻辑
     * @param examPaperEditDTO
     * @param user
     * @return
     */
    @Override
    public ExamPaper savePaper(ExamPaperEditDTO examPaperEditDTO, User user) {
        //保存试卷时没有传id过来，编辑试卷时才会传id过来
        ActionEnum actionEnum = (examPaperEditDTO.getId() == null) ? ActionEnum.ADD : ActionEnum.UPDATE;
        Date now = new Date();

        //把试卷存到数据库，修改后只需要存主要信息了
        ExamPaper examPaper = new ExamPaper();
        if (actionEnum == ActionEnum.ADD) {
            examPaper.setGradeLevel(examPaperEditDTO.getLevel());
            examPaper.setSubjectId(examPaperEditDTO.getSubjectId());
            examPaper.setPaperType(examPaperEditDTO.getPaperType());
            examPaper.setName(examPaperEditDTO.getName());
            examPaper.setSuggestTime(examPaperEditDTO.getSuggestTime());
            examPaper.setQuestionCount(examPaperEditDTO.getQuestionCount());

            //试卷分数还是等插入题目的时候在生成吧
            //examPaper.setScore(examPaperEditDTO.getQuestionCount());
            examPaper.setCreateTime(now);
            examPaper.setCreateUser(user.getId());
            examPaper.setDeleted(false);
            examPaperMapper.insertSelective(examPaper);
        } else {
            //更新试卷的话前端会传id过来
            examPaper.setId(examPaperEditDTO.getId());

            examPaper.setGradeLevel(examPaperEditDTO.getLevel());
            examPaper.setSubjectId(examPaperEditDTO.getSubjectId());
            examPaper.setPaperType(examPaperEditDTO.getPaperType());
            examPaper.setName(examPaperEditDTO.getName());
            examPaper.setSuggestTime(examPaperEditDTO.getSuggestTime());
            examPaper.setQuestionCount(examPaperEditDTO.getQuestionCount());
            examPaperMapper.updateByPrimaryKeySelective(examPaper);
        }
        return examPaper;
    }

    //查看试卷详细信息
    @Override
    public ExamPaperEditRequestVM examPaperToVM(Integer id) {
        //先查试卷的主要信息
        ExamPaper examPaper = examPaperMapper.selectByPrimaryKey(id);
        //开始组装
        ExamPaperEditRequestVM vm = modelMapper.map(examPaper, ExamPaperEditRequestVM.class);
        //他俩的level名不一样 一个叫gradelevel  一个叫level
        vm.setLevel(examPaper.getGradeLevel());

        //查试卷的json字符串
        TextContent frameTextContent = textContentService.selectById(examPaper.getFrameTextContentId());
        //转换为对象
        List<ExamPaperTitleItemObject> examPaperTitleItemObjects = JsonUtil.toJsonListObject(frameTextContent.getContent(), ExamPaperTitleItemObject.class);
        //获取每个标题下的试题id集合
        List<Integer> questionIds = examPaperTitleItemObjects.stream()
                .flatMap(t -> t.getQuestionItems().stream()
                        .map(q -> q.getId()))
                .collect(Collectors.toList());

        //根据试题id查出试题的基本信息，这个不包含题干
        List<Question> questions = questionMapper.selectByIds(questionIds);
        //一步步组装试卷里的标题
        List<ExamPaperTitleItemVM> examPaperTitleItemVMS = examPaperTitleItemObjects.stream().map(t -> {
            ExamPaperTitleItemVM tTitleVM = modelMapper.map(t, ExamPaperTitleItemVM.class);
            //集合还需要自己map
            List<QuestionEditRequestVM> questionItemsVM = t.getQuestionItems().stream().map(i -> {
                //筛选出符合条件的question
                Question question = questions.stream().filter(q -> q.getId().equals(i.getId())).findFirst().get();
                //还需要查出question的选项信息
                QuestionEditRequestVM questionEditRequestVM = questionService.getQuestionEditRequestVM(question);
                //这个应该是顺序
                questionEditRequestVM.setItemOrder(i.getItemOrder());
                return questionEditRequestVM;
            }).collect(Collectors.toList());
            tTitleVM.setQuestionItems(questionItemsVM);
            return tTitleVM;
        }).collect(Collectors.toList());
        vm.setTitleItems(examPaperTitleItemVMS);
        vm.setScore(ExamUtil.scoreToVM(examPaper.getScore()));
        //如果是时段试卷的话
        if (ExamPaperTypeEnum.TimeLimit == ExamPaperTypeEnum.fromCode(examPaper.getPaperType())) {
            List<String> limitDateTime = Arrays.asList(DateTimeUtil.dateFormat(examPaper.getLimitStartTime()), DateTimeUtil.dateFormat(examPaper.getLimitEndTime()));
            vm.setLimitDateTime(limitDateTime);
        }
        return vm;
    }

    @Override
    public List<PaperInfo> indexPaper(PaperFilter paperFilter) {
        return examPaperMapper.indexPaper(paperFilter);
    }


    @Override
    public Integer selectAllCount() {
        return examPaperMapper.selectAllCount();
    }

    @Override
    public List<Integer> selectMothCount() {
        Date startTime = DateTimeUtil.getMonthStartDay();
        Date endTime = DateTimeUtil.getMonthEndDay();
        List<KeyValue> mouthCount = examPaperMapper.selectCountByDate(startTime, endTime);
        List<String> mothStartToNowFormat = DateTimeUtil.MothStartToNowFormat();
        return mothStartToNowFormat.stream().map(md -> {
            KeyValue keyValue = mouthCount.stream().filter(kv -> kv.getName().equals(md)).findAny().orElse(null);
            return null == keyValue ? 0 : keyValue.getValue();
        }).collect(Collectors.toList());
    }

    private void examPaperFromVM(ExamPaperEditRequestVM examPaperEditRequestVM, ExamPaper examPaper, List<ExamPaperTitleItemVM> titleItemsVM) {

        //获取年级id
        Integer gradeLevel = subjectService.levelBySubjectId(examPaperEditRequestVM.getSubjectId());
        //获取题数
        Integer questionCount = titleItemsVM.stream()
                .mapToInt(t -> t.getQuestionItems().size()).sum();

        Integer score = titleItemsVM.stream().
                flatMapToInt(t -> t.getQuestionItems().stream()
                        .mapToInt(q -> ExamUtil.scoreFromVM(q.getScore()))
                ).sum();
        examPaper.setQuestionCount(questionCount);
        examPaper.setScore(score);
        examPaper.setGradeLevel(gradeLevel);
        List<String> dateTimes = examPaperEditRequestVM.getLimitDateTime();
        //处理一下时间
        if (ExamPaperTypeEnum.TimeLimit == ExamPaperTypeEnum.fromCode(examPaper.getPaperType())) {
            examPaper.setLimitStartTime(DateTimeUtil.parse(dateTimes.get(0), DateTimeUtil.STANDER_FORMAT));
            examPaper.setLimitEndTime(DateTimeUtil.parse(dateTimes.get(1), DateTimeUtil.STANDER_FORMAT));
        }
    }

    private List<ExamPaperTitleItemObject> frameTextContentFromVM(List<ExamPaperTitleItemVM> titleItems) {
        //生成原子数的原因是题目顺序
        AtomicInteger index = new AtomicInteger(1);

        //为什么要有这一步 主要是这两个对象中的List的对象不一样，
        return titleItems.stream().map(t -> {
            ExamPaperTitleItemObject titleItem = modelMapper.map(t, ExamPaperTitleItemObject.class);
            List<ExamPaperQuestionItemObject> questionItems = t.getQuestionItems().stream()
                    .map(q -> {
                        ExamPaperQuestionItemObject examPaperQuestionItemObject = modelMapper.map(q, ExamPaperQuestionItemObject.class);
                        //需要这个ItemOrder？？这个是什么？？
                        examPaperQuestionItemObject.setItemOrder(index.getAndIncrement());
                        return examPaperQuestionItemObject;
                    })
                    .collect(Collectors.toList());
            titleItem.setQuestionItems(questionItems);
            return titleItem;
        }).collect(Collectors.toList());
    }

    public ExamPaperEditDTO  examPaperToDTO(ExamPaper examPaper){
        ExamPaperEditDTO examPaperEditDTO = new ExamPaperEditDTO();
        examPaperEditDTO.setId(examPaper.getId()) ;
        examPaperEditDTO.setLevel(examPaper.getGradeLevel());
        examPaperEditDTO.setSubjectId(examPaper.getSubjectId());
        examPaperEditDTO.setPaperType(examPaper.getPaperType());
        examPaperEditDTO.setName(examPaper.getName());
        examPaperEditDTO.setSuggestTime(examPaper.getSuggestTime());
        examPaperEditDTO.setQuestionCount(examPaper.getQuestionCount());
        return examPaperEditDTO;
    }
}
