package cn.polar.x.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 填写记录（运营端查看）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmissionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String formNo;

    /** 字段key -> 填写值 */
    private Map<String, Object> data;

    private String submitIp;

    private java.util.Date createTime;

}
