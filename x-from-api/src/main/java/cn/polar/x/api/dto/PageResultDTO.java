package cn.polar.x.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分页结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResultDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private int total;

    private int page;

    private int pageSize;

    private java.util.List<T> list;

}
