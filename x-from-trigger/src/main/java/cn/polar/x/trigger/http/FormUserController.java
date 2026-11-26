package cn.polar.x.trigger.http;

import cn.polar.x.api.dto.FormFillDTO;
import cn.polar.x.api.dto.FormSubmitRequestDTO;
import cn.polar.x.api.response.Response;
import cn.polar.x.cases.form.IUserFormCaseService;
import cn.polar.x.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端表单接口（分享链接落地）
 */
@Slf4j
@CrossOrigin(originPatterns = "*")
@RestController
@RequestMapping("/api/form")
public class FormUserController {

    @Resource
    private IUserFormCaseService userFormCaseService;

    /** 通过分享链接打开表单：/api/form/{formNo} */
    @GetMapping("/{formNo}")
    public Response<FormFillDTO> detail(@PathVariable String formNo) {
        return Response.<FormFillDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(userFormCaseService.queryFillForm(formNo))
                .build();
    }

    /** 通过 query 参数打开表单：/api/form/detail?formNo=xxx */
    @GetMapping("/detail")
    public Response<FormFillDTO> detailByParam(@RequestParam String formNo) {
        return Response.<FormFillDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(userFormCaseService.queryFillForm(formNo))
                .build();
    }

    /** 提交表单数据 */
    @PostMapping("/submit")
    public Response<Long> submit(@RequestBody FormSubmitRequestDTO request, HttpServletRequest httpServletRequest) {
        String ip = clientIp(httpServletRequest);
        Long submissionId = userFormCaseService.submitForm(request, ip);
        return Response.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("提交成功")
                .data(submissionId)
                .build();
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return StringUtils.substringBefore(ip, ",").trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

}
