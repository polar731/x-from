package cn.polar.x.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 用户端提交表单请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormSubmitRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 表单编号 */
    private String formNo;

    /** 字段key -> 填写值 */
    private Map<String, Object> data;

}
