package cn.polar.x.infrastructure.redis;

/**
 * Redis Key 常量
 */
public class RedisKey {

    /** 表单 schema 缓存：x-from:form:schema:{formNo} */
    public static final String FORM_SCHEMA = "x-from:form:schema:";

    /** 分享链接访问计数：x-from:form:visit:{formNo} */
    public static final String FORM_VISIT = "x-from:form:visit:";

}
