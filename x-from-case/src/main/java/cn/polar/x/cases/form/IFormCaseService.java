package cn.polar.x.cases.form;

import cn.polar.x.api.dto.FormCreateRequestDTO;
import cn.polar.x.api.dto.FormDTO;
import cn.polar.x.api.dto.FormListRequestDTO;
import cn.polar.x.api.dto.FormStatsDTO;
import cn.polar.x.api.dto.FormSubmissionDTO;
import cn.polar.x.api.dto.PageResultDTO;

/**
 * 运营端表单用例服务
 */
public interface IFormCaseService {

    /** 创建表单（可同时发布） */
    FormDTO createForm(FormCreateRequestDTO request);

    /** 发布表单，生成可分享链接 */
    FormDTO publishForm(String formNo);

    /** 关闭表单，停止收集 */
    FormDTO closeForm(String formNo);

    /** 运营端表单列表 */
    PageResultDTO<FormDTO> queryFormList(FormListRequestDTO request);

    /** 表单详情 */
    FormDTO queryFormDetail(String formNo);

    /** 表单填写数据列表 */
    PageResultDTO<FormSubmissionDTO> querySubmissions(String formNo, Integer page, Integer pageSize);

    /** 运营端数据概览 */
    FormStatsDTO queryStats();

}
