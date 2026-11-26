package cn.polar.x.cases.form;

import cn.polar.x.api.dto.FormCreateRequestDTO;
import cn.polar.x.api.dto.FormDTO;
import cn.polar.x.api.dto.FormFieldDTO;
import cn.polar.x.api.dto.FormListRequestDTO;
import cn.polar.x.api.dto.FormStatsDTO;
import cn.polar.x.api.dto.FormSubmissionDTO;
import cn.polar.x.api.dto.PageResultDTO;
import cn.polar.x.domain.form.adapter.port.IFormCachePort;
import cn.polar.x.domain.form.adapter.repository.IFormRepository;
import cn.polar.x.domain.form.adapter.repository.IFormSubmissionRepository;
import cn.polar.x.domain.form.model.aggregate.FormAggregate;
import cn.polar.x.domain.form.model.entity.FormEntity;
import cn.polar.x.domain.form.model.entity.FormSubmissionEntity;
import cn.polar.x.domain.form.model.valobj.FormFieldVO;
import cn.polar.x.domain.form.service.IFormDomainService;
import cn.polar.x.types.enums.FormStatusEnum;
import cn.polar.x.types.enums.ResponseCode;
import cn.polar.x.types.exception.AppException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 运营端表单用例服务实现
 */
@Slf4j
@Service
public class FormCaseService implements IFormCaseService {

    /** 表单 schema 缓存时长（秒） */
    private static final long SCHEMA_CACHE_SECONDS = 1800L;

    @Resource
    private IFormDomainService formDomainService;

    @Resource
    private IFormRepository formRepository;

    @Resource
    private IFormSubmissionRepository formSubmissionRepository;

    @Resource
    private IFormCachePort formCachePort;

    @Value("${x-from.share.base-url:}")
    private String shareBaseUrl;

    @Override
    public FormDTO createForm(FormCreateRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getTitle())) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "表单标题不能为空");
        }
        List<FormFieldVO> fields = convertFields(request.getFields());
        formDomainService.checkDefinition(fields);

        boolean publish = Boolean.TRUE.equals(request.getPublish());
        FormEntity entity = FormEntity.builder()
                .formNo(generateUniqueFormNo())
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .fields(fields)
                .status(publish ? FormStatusEnum.PUBLISHED.getCode() : FormStatusEnum.DRAFT.getCode())
                .submitCount(0)
                .creator(StringUtils.defaultIfBlank(request.getCreator(), "运营"))
                .build();
        Long id = formRepository.save(entity);
        entity.setId(id);
        if (publish) {
            formCachePort.put(entity.getFormNo(), entity, SCHEMA_CACHE_SECONDS);
        }
        log.info("创建表单完成 formNo:{} title:{} publish:{}", entity.getFormNo(), entity.getTitle(), publish);
        return toDTO(entity);
    }

    @Override
    public FormDTO publishForm(String formNo) {
        FormEntity entity = queryExistForm(formNo);
        new FormAggregate(entity).publish();
        formRepository.updateStatus(entity.getId(), entity.getStatus());
        formCachePort.put(entity.getFormNo(), entity, SCHEMA_CACHE_SECONDS);
        log.info("发布表单完成 formNo:{}", entity.getFormNo());
        return toDTO(entity);
    }

    @Override
    public FormDTO closeForm(String formNo) {
        FormEntity entity = queryExistForm(formNo);
        new FormAggregate(entity).close();
        formRepository.updateStatus(entity.getId(), entity.getStatus());
        formCachePort.evict(entity.getFormNo());
        log.info("关闭表单完成 formNo:{}", entity.getFormNo());
        return toDTO(entity);
    }

    @Override
    public PageResultDTO<FormDTO> queryFormList(FormListRequestDTO request) {
        String keyword = request == null ? null : StringUtils.trimToNull(request.getKeyword());
        Integer status = request == null ? null : request.getStatus();
        int page = normalizePage(request == null ? null : request.getPage());
        int pageSize = normalizePageSize(request == null ? null : request.getPageSize());

        int total = formRepository.countList(keyword, status);
        List<FormDTO> list = new ArrayList<>();
        if (total > 0) {
            List<FormEntity> entities = formRepository.queryList(keyword, status, (page - 1) * pageSize, pageSize);
            for (FormEntity entity : entities) {
                list.add(toDTO(entity));
            }
        }
        return PageResultDTO.<FormDTO>builder()
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .list(list)
                .build();
    }

    @Override
    public FormDTO queryFormDetail(String formNo) {
        return toDTO(queryExistForm(formNo));
    }

    @Override
    public PageResultDTO<FormSubmissionDTO> querySubmissions(String formNo, Integer page, Integer pageSize) {
        FormEntity entity = queryExistForm(formNo);
        int currentPage = normalizePage(page);
        int currentPageSize = normalizePageSize(pageSize);

        int total = formSubmissionRepository.countByFormNo(entity.getFormNo());
        List<FormSubmissionDTO> list = new ArrayList<>();
        if (total > 0) {
            List<FormSubmissionEntity> entities = formSubmissionRepository.queryListByFormNo(
                    entity.getFormNo(), (currentPage - 1) * currentPageSize, currentPageSize);
            for (FormSubmissionEntity submission : entities) {
                list.add(FormSubmissionDTO.builder()
                        .id(submission.getId())
                        .formNo(submission.getFormNo())
                        .data(submission.getData())
                        .submitIp(submission.getSubmitIp())
                        .createTime(submission.getCreateTime())
                        .build());
            }
        }
        return PageResultDTO.<FormSubmissionDTO>builder()
                .total(total)
                .page(currentPage)
                .pageSize(currentPageSize)
                .list(list)
                .build();
    }

    @Override
    public FormStatsDTO queryStats() {
        return FormStatsDTO.builder()
                .formTotal(formRepository.countList(null, null))
                .publishedCount(formRepository.countList(null, FormStatusEnum.PUBLISHED.getCode()))
                .draftCount(formRepository.countList(null, FormStatusEnum.DRAFT.getCode()))
                .closedCount(formRepository.countList(null, FormStatusEnum.CLOSED.getCode()))
                .submissionTotal(formSubmissionRepository.countAll())
                .build();
    }

    private FormEntity queryExistForm(String formNo) {
        if (StringUtils.isBlank(formNo)) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "表单编号不能为空");
        }
        FormEntity entity = formRepository.queryByFormNo(formNo.trim());
        if (entity == null) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "表单不存在：" + formNo);
        }
        return entity;
    }

    private String generateUniqueFormNo() {
        for (int i = 0; i < 5; i++) {
            String formNo = formDomainService.generateFormNo();
            if (formRepository.queryByFormNo(formNo) == null) {
                return formNo;
            }
        }
        throw new AppException(ResponseCode.UN_ERROR.getCode(), "表单编号生成失败，请稍后重试");
    }

    private List<FormFieldVO> convertFields(List<FormFieldDTO> fieldDTOS) {
        List<FormFieldVO> fields = new ArrayList<>();
        if (fieldDTOS == null) {
            return fields;
        }
        for (FormFieldDTO dto : fieldDTOS) {
            fields.add(FormFieldVO.builder()
                    .key(StringUtils.trimToNull(dto.getKey()))
                    .label(StringUtils.trimToNull(dto.getLabel()))
                    .type(StringUtils.trimToNull(dto.getType()))
                    .required(dto.getRequired())
                    .placeholder(dto.getPlaceholder())
                    .options(dto.getOptions())
                    .build());
        }
        return fields;
    }

    private FormDTO toDTO(FormEntity entity) {
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
        FormStatusEnum statusEnum = FormStatusEnum.getByCode(entity.getStatus());
        String shareUrl = null;
        if (FormStatusEnum.PUBLISHED.getCode().equals(entity.getStatus())) {
            shareUrl = StringUtils.defaultString(shareBaseUrl) + entity.getFormNo();
        }
        return FormDTO.builder()
                .id(entity.getId())
                .formNo(entity.getFormNo())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .fields(fields)
                .status(entity.getStatus())
                .statusDesc(statusEnum == null ? "未知" : statusEnum.getInfo())
                .submitCount(entity.getSubmitCount())
                .creator(entity.getCreator())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .shareUrl(shareUrl)
                .build();
    }

    private int normalizePage(Integer page) {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

}
