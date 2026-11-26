package cn.polar.x.domain.form.service;

import cn.polar.x.domain.form.model.valobj.FormFieldVO;

import java.util.List;
import java.util.Map;

/**
 * 表单领域服务：负责编号生成、字段定义校验、填写内容校验
 */
public interface IFormDomainService {

    /** 生成表单分享短码 */
    String generateFormNo();

    /** 校验表单字段定义 */
    void checkDefinition(List<FormFieldVO> fields);

    /** 校验填写内容并返回清洗后的答案（仅保留已定义字段） */
    Map<String, Object> checkAnswer(List<FormFieldVO> fields, Map<String, Object> data);

}
