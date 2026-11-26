package cn.polar.x.infrastructure.dao;

import cn.polar.x.infrastructure.dao.po.FormSubmissionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 表单填写记录 DAO
 */
@Mapper
public interface IFormSubmissionDao {

    /** 新增填写记录，回填主键 */
    int insert(FormSubmissionPO submissionPO);

    /** 按表单编号分页查询 */
    List<FormSubmissionPO> queryListByFormNo(@Param("formNo") String formNo,
                                             @Param("offset") int offset,
                                             @Param("limit") int limit);

    /** 按表单编号统计 */
    int countByFormNo(@Param("formNo") String formNo);

    /** 全量回收数据量 */
    int countAll();

}
