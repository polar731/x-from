package cn.polar.x.infrastructure.dao;

import cn.polar.x.infrastructure.dao.po.FormPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 表单 DAO
 */
@Mapper
public interface IFormDao {

    /** 新增表单，回填主键 */
    int insert(FormPO formPO);

    /** 按表单编号查询 */
    FormPO queryByFormNo(@Param("formNo") String formNo);

    /** 按主键查询 */
    FormPO queryById(@Param("id") Long id);

    /** 运营端列表 */
    List<FormPO> queryList(@Param("keyword") String keyword,
                           @Param("status") Integer status,
                           @Param("offset") int offset,
                           @Param("limit") int limit);

    /** 列表总数 */
    int countList(@Param("keyword") String keyword,
                  @Param("status") Integer status);

    /** 更新状态 */
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status);

    /** 提交数 +1 */
    int increaseSubmitCount(@Param("id") Long id);

}
