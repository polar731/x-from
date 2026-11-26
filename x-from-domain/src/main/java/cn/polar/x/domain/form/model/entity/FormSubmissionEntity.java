package cn.polar.x.domain.form.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * 表单填写记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmissionEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long formId;

    private String formNo;

    /** 填写内容：字段key -> 值 */
    private Map<String, Object> data;

    private String submitIp;

    private Date createTime;

}
