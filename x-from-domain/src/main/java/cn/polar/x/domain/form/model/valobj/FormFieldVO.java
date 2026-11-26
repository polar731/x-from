package cn.polar.x.domain.form.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 表单字段定义值对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormFieldVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 字段标识，同一表单内唯一，如 name、phone */
    private String key;

    /** 字段名称，展示给填写者，如「你的姓名」 */
    private String label;

    /** 字段类型：text/textarea/number/radio/checkbox/select/date */
    private String type;

    /** 是否必填 */
    private Boolean required;

    /** 占位提示 */
    private String placeholder;

    /** 选项（radio/checkbox/select 使用） */
    private List<String> options;

}
