package cn.polar.x.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 运营端数据概览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FormStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 表单总数 */
    private int formTotal;

    /** 已发布 */
    private int publishedCount;

    /** 草稿 */
    private int draftCount;

    /** 已关闭 */
    private int closedCount;

    /** 累计回收数据量 */
    private int submissionTotal;

}
