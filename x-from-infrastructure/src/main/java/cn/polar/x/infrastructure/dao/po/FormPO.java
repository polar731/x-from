package cn.polar.x.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 表单持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormPO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String formNo;

    private String title;

    private String description;

    /** 字段定义 JSON */
    private String fieldsJson;

    private Integer status;

    private Integer submitCount;

    private String creator;

    private Date createTime;

    private Date updateTime;

}
