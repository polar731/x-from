package cn.polar.x.domain.form.adapter.repository;

import cn.polar.x.domain.form.model.entity.FormEntity;

import java.util.List;

/**
 * 表单仓储接口
 */
public interface IFormRepository {

    /** 新增表单，返回主键 */
    Long save(FormEntity form);

    /** 按表单编号查询（唯一键） */
    FormEntity queryByFormNo(String formNo);

    /** 按主键查询 */
    FormEntity queryById(Long id);

    /** 运营端列表查询 */
    List<FormEntity> queryList(String keyword, Integer status, int offset, int limit);

    /** 列表总数 */
    int countList(String keyword, Integer status);

    /** 更新状态 */
    int updateStatus(Long id, Integer status);

    /** 提交数 +1 */
    int increaseSubmitCount(Long id);

}
