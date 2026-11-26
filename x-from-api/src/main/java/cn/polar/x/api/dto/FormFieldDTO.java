package cn.polar.x.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 表单字段（接口层）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormFieldDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 字段标识 */
    private String key;

    /** 字段名称 */
    private String label;

    /** 类型：text/textarea/number/radio/checkbox/select/date */
    private String type;

    /** 是否必填 */
    private Boolean required;

    /** 占位提示 */
    private String placeholder;

    /** 选项 */
    private List<String> options;

}
