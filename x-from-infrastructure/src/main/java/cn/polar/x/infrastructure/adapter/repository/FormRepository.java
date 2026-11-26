package cn.polar.x.infrastructure.adapter.repository;

import cn.polar.x.domain.form.adapter.repository.IFormRepository;
import cn.polar.x.domain.form.model.entity.FormEntity;
import cn.polar.x.domain.form.model.valobj.FormFieldVO;
import cn.polar.x.infrastructure.dao.IFormDao;
import cn.polar.x.infrastructure.dao.po.FormPO;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * 表单仓储实现
 */
@Repository
public class FormRepository implements IFormRepository {

    @Resource
    private IFormDao formDao;

    @Override
    public Long save(FormEntity form) {
        FormPO formPO = FormPO.builder()
                .formNo(form.getFormNo())
                .title(form.getTitle())
                .description(form.getDescription())
                .fieldsJson(JSON.toJSONString(form.getFields()))
                .status(form.getStatus())
                .submitCount(form.getSubmitCount() == null ? 0 : form.getSubmitCount())
                .creator(form.getCreator())
                .build();
        formDao.insert(formPO);
        return formPO.getId();
    }

    @Override
    public FormEntity queryByFormNo(String formNo) {
        if (StringUtils.isBlank(formNo)) {
            return null;
        }
        return toEntity(formDao.queryByFormNo(formNo));
    }

    @Override
    public FormEntity queryById(Long id) {
        if (id == null) {
            return null;
        }
        return toEntity(formDao.queryById(id));
    }

    @Override
    public List<FormEntity> queryList(String keyword, Integer status, int offset, int limit) {
        List<FormPO> list = formDao.queryList(keyword, status, offset, limit);
        List<FormEntity> result = new ArrayList<>(list.size());
        for (FormPO formPO : list) {
            result.add(toEntity(formPO));
        }
        return result;
    }

    @Override
    public int countList(String keyword, Integer status) {
        return formDao.countList(keyword, status);
    }

    @Override
    public int updateStatus(Long id, Integer status) {
        return formDao.updateStatus(id, status);
    }

    @Override
    public int increaseSubmitCount(Long id) {
        return formDao.increaseSubmitCount(id);
    }

    private FormEntity toEntity(FormPO formPO) {
        if (formPO == null) {
            return null;
        }
        List<FormFieldVO> fields = new ArrayList<>();
        if (StringUtils.isNotBlank(formPO.getFieldsJson())) {
            fields = JSON.parseArray(formPO.getFieldsJson(), FormFieldVO.class);
        }
        return FormEntity.builder()
                .id(formPO.getId())
                .formNo(formPO.getFormNo())
                .title(formPO.getTitle())
                .description(formPO.getDescription())
                .fields(fields)
                .status(formPO.getStatus())
                .submitCount(formPO.getSubmitCount())
                .creator(formPO.getCreator())
                .createTime(formPO.getCreateTime())
                .updateTime(formPO.getUpdateTime())
                .build();
    }

}
