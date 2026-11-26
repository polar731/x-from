package cn.polar.x.trigger.http;

import cn.polar.x.api.dto.FormCreateRequestDTO;
import cn.polar.x.api.dto.FormDTO;
import cn.polar.x.api.dto.FormListRequestDTO;
import cn.polar.x.api.dto.FormStatsDTO;
import cn.polar.x.api.dto.FormSubmissionDTO;
import cn.polar.x.api.dto.PageResultDTO;
import cn.polar.x.api.response.Response;
import cn.polar.x.cases.form.IFormCaseService;
import cn.polar.x.types.enums.ResponseCode;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 运营端表单接口
 */
@Slf4j
@CrossOrigin(originPatterns = "*")
@RestController
@RequestMapping("/api/admin/form")
public class FormAdminController {

    @Resource
    private IFormCaseService formCaseService;

    /** 创建表单（publish=true 时同时发布并生成分享链接） */
    @PostMapping("/create")
    public Response<FormDTO> create(@RequestBody FormCreateRequestDTO request) {
        log.info("运营端创建表单 title:{}", request == null ? null : request.getTitle());
        return Response.<FormDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(formCaseService.createForm(request))
                .build();
    }

    /** 数据概览 */
    @GetMapping("/stats")
    public Response<FormStatsDTO> stats() {
        return Response.<FormStatsDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(formCaseService.queryStats())
                .build();
    }

    /** 表单列表 */
    @GetMapping("/list")
    public Response<PageResultDTO<FormDTO>> list(@RequestParam(required = false) String keyword,
                                                 @RequestParam(required = false) Integer status,
                                                 @RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer pageSize) {
        FormListRequestDTO request = FormListRequestDTO.builder()
                .keyword(keyword)
                .status(status)
                .page(page)
                .pageSize(pageSize)
                .build();
        return Response.<PageResultDTO<FormDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(formCaseService.queryFormList(request))
                .build();
    }

    /** 表单详情 */
    @GetMapping("/detail")
    public Response<FormDTO> detail(@RequestParam String formNo) {
        return Response.<FormDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(formCaseService.queryFormDetail(formNo))
                .build();
    }

    /** 发布表单 */
    @PostMapping("/publish")
    public Response<FormDTO> publish(@RequestParam String formNo) {
        log.info("运营端发布表单 formNo:{}", formNo);
        return Response.<FormDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(formCaseService.publishForm(formNo))
                .build();
    }

    /** 关闭表单 */
    @PostMapping("/close")
    public Response<FormDTO> close(@RequestParam String formNo) {
        log.info("运营端关闭表单 formNo:{}", formNo);
        return Response.<FormDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(formCaseService.closeForm(formNo))
                .build();
    }

    /** 查看填写数据 */
    @GetMapping("/submissions")
    public Response<PageResultDTO<FormSubmissionDTO>> submissions(@RequestParam String formNo,
                                                                  @RequestParam(required = false) Integer page,
                                                                  @RequestParam(required = false) Integer pageSize) {
        return Response.<PageResultDTO<FormSubmissionDTO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getInfo())
                .data(formCaseService.querySubmissions(formNo, page, pageSize))
                .build();
    }

}
