package cn.polar.x.cases.form;

import cn.polar.x.api.dto.FormFillDTO;
import cn.polar.x.api.dto.FormSubmitRequestDTO;

/**
 * 用户端表单用例服务
 */
public interface IUserFormCaseService {

    /** 用户端通过分享链接打开表单 */
    FormFillDTO queryFillForm(String formNo);

    /** 用户端提交表单数据，返回填写记录ID */
    Long submitForm(FormSubmitRequestDTO request, String submitIp);

}
