package cn.polar.x.domain.form.model.entity;

import cn.polar.x.domain.form.model.valobj.FormFieldVO;
import cn.polar.x.types.enums.FormStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 表单实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** 表单编号（分享短码） */
    private String formNo;

    private String title;

    private String description;

    /** 字段定义 */
    private List<FormFieldVO> fields;

    /** 状态：0草稿 1已发布 2已关闭 */
    private Integer status;

    private Integer submitCount;

    private String creator;

    private Date createTime;

    private Date updateTime;

    /** 是否可被用户端填写 */
    public boolean fillable() {
        return FormStatusEnum.PUBLISHED.getCode().equals(this.status);
    }

    /** 是否已发布（可继续关闭/查看数据） */
    public boolean published() {
        return FormStatusEnum.PUBLISHED.getCode().equals(this.status);
    }

}
