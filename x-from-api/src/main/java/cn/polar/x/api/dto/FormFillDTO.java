package cn.polar.x.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 用户端待填写表单信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormFillDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String formNo;

    private String title;

    private String description;

    private List<FormFieldDTO> fields;

    /** 分享链接累计访问次数（Redis 统计） */
    private Long visits;

}
