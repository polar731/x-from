package cn.polar.x.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 表单状态
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum FormStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    CLOSED(2, "已关闭"),
    ;

    private Integer code;
    private String info;

    public static FormStatusEnum getByCode(Integer code) {
        for (FormStatusEnum value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

}
