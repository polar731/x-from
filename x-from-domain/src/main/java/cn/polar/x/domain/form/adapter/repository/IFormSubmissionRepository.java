package cn.polar.x.domain.form.adapter.repository;

import cn.polar.x.domain.form.model.entity.FormSubmissionEntity;

import java.util.List;

/**
 * 表单填写记录仓储接口
 */
public interface IFormSubmissionRepository {

    /** 新增填写记录，返回主键 */
    Long save(FormSubmissionEntity submission);

    /** 按表单编号分页查询填写记录 */
    List<FormSubmissionEntity> queryListByFormNo(String formNo, int offset, int limit);

    /** 按表单编号统计总数 */
    int countByFormNo(String formNo);

    /** 全量回收数据量 */
    int countAll();

}
