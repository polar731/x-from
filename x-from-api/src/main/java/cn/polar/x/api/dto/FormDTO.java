package cn.polar.x.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 表单信息（运营端返回）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String formNo;

    private String title;

    private String description;

    private List<FormFieldDTO> fields;

    private Integer status;

    private String statusDesc;

    private Integer submitCount;

    private String creator;

    private Date createTime;

    private Date updateTime;

    /** 分享链接（已发布时返回） */
    private String shareUrl;

}
