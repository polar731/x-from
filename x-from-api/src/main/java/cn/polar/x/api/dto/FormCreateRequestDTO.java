package cn.polar.x.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 创建表单请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormCreateRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 表单标题 */
    private String title;

    /** 表单描述 */
    private String description;

    /** 字段定义 */
    private List<FormFieldDTO> fields;

    /** 是否创建后立即发布 */
    private Boolean publish;

    /** 创建人 */
    private String creator;

}
