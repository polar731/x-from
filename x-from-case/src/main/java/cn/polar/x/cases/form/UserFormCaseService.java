package cn.polar.x.cases.form;

import cn.polar.x.api.dto.FormFieldDTO;
import cn.polar.x.api.dto.FormFillDTO;
import cn.polar.x.api.dto.FormSubmitRequestDTO;
import cn.polar.x.domain.form.adapter.port.IFormCachePort;
import cn.polar.x.domain.form.adapter.repository.IFormRepository;
import cn.polar.x.domain.form.adapter.repository.IFormSubmissionRepository;
import cn.polar.x.domain.form.model.aggregate.FormAggregate;
import cn.polar.x.domain.form.model.entity.FormEntity;
import cn.polar.x.domain.form.model.entity.FormSubmissionEntity;
import cn.polar.x.domain.form.model.valobj.FormFieldVO;
import cn.polar.x.domain.form.service.IFormDomainService;
import cn.polar.x.types.enums.ResponseCode;
import cn.polar.x.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户端表单用例服务实现
 */
@Slf4j
@Service
public class UserFormCaseService implements IUserFormCaseService {

    private static final long SCHEMA_CACHE_SECONDS = 1800L;

    @Resource
    private IFormDomainService formDomainService;

    @Resource
    private IFormRepository formRepository;

    @Resource
    private IFormSubmissionRepository formSubmissionRepository;

    @Resource
    private IFormCachePort formCachePort;

    @Override
    public FormFillDTO queryFillForm(String formNo) {
        FormEntity entity = loadFillableForm(formNo);
        long visits = formCachePort.increaseVisit(entity.getFormNo());

        List<FormFieldDTO> fields = new ArrayList<>();
        if (entity.getFields() != null) {
            for (FormFieldVO field : entity.getFields()) {
                fields.add(FormFieldDTO.builder()
                        .key(field.getKey())
                        .label(field.getLabel())
                        .type(field.getType())
                        .required(field.getRequired())
                        .placeholder(field.getPlaceholder())
                        .options(field.getOptions())
                        .build());
            }
        }
        return FormFillDTO.builder()
                .formNo(entity.getFormNo())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .fields(fields)
                .visits(visits)
                .build();
    }

    @Override
    public Long submitForm(FormSubmitRequestDTO request, String submitIp) {
        if (request == null || StringUtils.isBlank(request.getFormNo())) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "表单编号不能为空");
        }
        FormEntity entity = loadFillableForm(request.getFormNo());
        Map<String, Object> answer = formDomainService.checkAnswer(entity.getFields(), request.getData());

        Long id = formSubmissionRepository.save(FormSubmissionEntity.builder()
                .formId(entity.getId())
                .formNo(entity.getFormNo())
                .data(answer)
                .submitIp(submitIp)
                .build());
        formRepository.increaseSubmitCount(entity.getId());
        log.info("表单提交成功 formNo:{} submissionId:{} ip:{}", entity.getFormNo(), id, submitIp);
        return id;
    }

    /**
     * 优先读 Redis 缓存，未命中回源 DB 并回填；随后校验表单可填写状态
     */
    private FormEntity loadFillableForm(String formNo) {
        if (StringUtils.isBlank(formNo)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "表单编号不能为空");
        }
        String trimmed = formNo.trim();
        FormEntity entity = formCachePort.get(trimmed);
        if (entity == null) {
            entity = formRepository.queryByFormNo(trimmed);
            if (entity == null) {
                throw new AppException(ResponseCode.UN_ERROR.getCode(), "表单不存在或分享链接已失效");
            }
            if (entity.published()) {
                formCachePort.put(trimmed, entity, SCHEMA_CACHE_SECONDS);
            }
        }
        new FormAggregate(entity).assertFillable();
        return entity;
    }

}
