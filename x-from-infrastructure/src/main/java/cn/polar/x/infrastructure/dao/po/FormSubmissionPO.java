package cn.polar.x.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 表单填写记录持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmissionPO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long formId;

    private String formNo;

    /** 填写内容 JSON */
    private String dataJson;

    private String submitIp;

    private Date createTime;

}
