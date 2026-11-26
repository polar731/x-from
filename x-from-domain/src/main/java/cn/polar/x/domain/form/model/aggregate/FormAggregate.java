package cn.polar.x.domain.form.model.aggregate;

import cn.polar.x.domain.form.model.entity.FormEntity;
import cn.polar.x.types.enums.FormStatusEnum;
import cn.polar.x.types.exception.AppException;
import cn.polar.x.types.enums.ResponseCode;

/**
 * 表单聚合根：承载表单状态流转与填写准入规则
 */
public class FormAggregate {

    private final FormEntity form;

    public FormAggregate(FormEntity form) {
        this.form = form;
    }

    public FormEntity getForm() {
        return form;
    }

    /** 发布表单：草稿 -> 已发布 */
    public void publish() {
        if (form == null) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "表单不存在");
        }
        if (FormStatusEnum.PUBLISHED.getCode().equals(form.getStatus())) {
            return;
        }
        if (!FormStatusEnum.DRAFT.getCode().equals(form.getStatus())) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "已关闭的表单不可再次发布");
        }
        form.setStatus(FormStatusEnum.PUBLISHED.getCode());
    }

    /** 关闭表单：已发布 -> 已关闭 */
    public void close() {
        if (form == null) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "表单不存在");
        }
        if (FormStatusEnum.CLOSED.getCode().equals(form.getStatus())) {
            return;
        }
        form.setStatus(FormStatusEnum.CLOSED.getCode());
    }

    /** 校验当前表单是否允许用户端填写 */
    public void assertFillable() {
        if (form == null) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "表单不存在或分享链接已失效");
        }
        if (FormStatusEnum.DRAFT.getCode().equals(form.getStatus())) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "表单尚未发布，暂不可填写");
        }
        if (FormStatusEnum.CLOSED.getCode().equals(form.getStatus())) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "表单已关闭，停止收集");
        }
    }

    /** 累加提交数 */
    public void increaseSubmitCount() {
        int current = form.getSubmitCount() == null ? 0 : form.getSubmitCount();
        form.setSubmitCount(current + 1);
    }

}
