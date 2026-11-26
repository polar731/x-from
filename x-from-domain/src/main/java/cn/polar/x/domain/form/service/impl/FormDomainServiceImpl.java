package cn.polar.x.domain.form.service.impl;

import cn.polar.x.domain.form.model.valobj.FormFieldVO;
import cn.polar.x.domain.form.service.IFormDomainService;
import cn.polar.x.types.enums.ResponseCode;
import cn.polar.x.types.exception.AppException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.*;

/**
 * 表单领域服务实现
 */
@Service
public class FormDomainServiceImpl implements IFormDomainService {

    /** 去掉易混淆字符 O/0/I/1 */
    private static final char[] CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();

    private static final SecureRandom RANDOM = new SecureRandom();

    /** 需要配置选项的字段类型 */
    private static final Set<String> OPTION_TYPES = Set.of("radio", "checkbox", "select");

    @Override
    public String generateFormNo() {
        StringBuilder builder = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            builder.append(CODE_CHARS[RANDOM.nextInt(CODE_CHARS.length)]);
        }
        return builder.toString();
    }

    @Override
    public void checkDefinition(List<FormFieldVO> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "表单至少需要配置一个字段");
        }
        Set<String> keys = new HashSet<>();
        for (FormFieldVO field : fields) {
            if (StringUtils.isBlank(field.getKey())) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "字段标识不能为空");
            }
            if (StringUtils.isBlank(field.getLabel())) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "字段名称不能为空");
            }
            if (!keys.add(field.getKey())) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "字段标识重复：" + field.getKey());
            }
            if (StringUtils.isBlank(field.getType())) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "字段类型不能为空：" + field.getLabel());
            }
            if (OPTION_TYPES.contains(field.getType()) && (field.getOptions() == null || field.getOptions().isEmpty())) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "「" + field.getLabel() + "」需要配置选项");
            }
        }
    }

    @Override
    public Map<String, Object> checkAnswer(List<FormFieldVO> fields, Map<String, Object> data) {
        if (fields == null || fields.isEmpty()) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "表单未配置字段，无法提交");
        }
        Map<String, Object> answer = new LinkedHashMap<>();
        for (FormFieldVO field : fields) {
            Object value = data == null ? null : data.get(field.getKey());
            boolean empty = isEmpty(value);
            if (Boolean.TRUE.equals(field.getRequired()) && empty) {
                throw new AppException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "「" + field.getLabel() + "」为必填项");
            }
            if (!empty) {
                answer.put(field.getKey(), value);
            }
        }
        return answer;
    }

    private boolean isEmpty(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof CharSequence cs) {
            return StringUtils.isBlank(cs);
        }
        if (value instanceof Collection<?> collection) {
            return collection.isEmpty();
        }
        return false;
    }

}
