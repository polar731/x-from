package cn.polar.x.infrastructure.adapter.repository;

import cn.polar.x.domain.form.adapter.repository.IFormSubmissionRepository;
import cn.polar.x.domain.form.model.entity.FormSubmissionEntity;
import cn.polar.x.infrastructure.dao.IFormSubmissionDao;
import cn.polar.x.infrastructure.dao.po.FormSubmissionPO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 表单填写记录仓储实现
 */
@Repository
public class FormSubmissionRepository implements IFormSubmissionRepository {

    @Resource
    private IFormSubmissionDao formSubmissionDao;

    @Override
    public Long save(FormSubmissionEntity submission) {
        FormSubmissionPO submissionPO = FormSubmissionPO.builder()
                .formId(submission.getFormId())
                .formNo(submission.getFormNo())
                .dataJson(JSON.toJSONString(submission.getData() == null ? new LinkedHashMap<>() : submission.getData()))
                .submitIp(submission.getSubmitIp())
                .build();
        formSubmissionDao.insert(submissionPO);
        return submissionPO.getId();
    }

    @Override
    public List<FormSubmissionEntity> queryListByFormNo(String formNo, int offset, int limit) {
        List<FormSubmissionPO> list = formSubmissionDao.queryListByFormNo(formNo, offset, limit);
        List<FormSubmissionEntity> result = new ArrayList<>(list.size());
        for (FormSubmissionPO submissionPO : list) {
            result.add(toEntity(submissionPO));
        }
        return result;
    }

    @Override
    public int countByFormNo(String formNo) {
        return formSubmissionDao.countByFormNo(formNo);
    }

    @Override
    public int countAll() {
        return formSubmissionDao.countAll();
    }

    private FormSubmissionEntity toEntity(FormSubmissionPO submissionPO) {
        Map<String, Object> data = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(submissionPO.getDataJson())) {
            data = JSON.parseObject(submissionPO.getDataJson(), new TypeReference<LinkedHashMap<String, Object>>() {
            });
        }
        return FormSubmissionEntity.builder()
                .id(submissionPO.getId())
                .formId(submissionPO.getFormId())
                .formNo(submissionPO.getFormNo())
                .data(data)
                .submitIp(submissionPO.getSubmitIp())
                .createTime(submissionPO.getCreateTime())
                .build();
    }

}
