package cn.polar.x.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 表单列表查询请求（运营端）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormListRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 标题关键字或表单编号 */
    private String keyword;

    /** 状态：0草稿 1已发布 2已关闭；null 表示全部 */
    private Integer status;

    private Integer page;

    private Integer pageSize;

}
