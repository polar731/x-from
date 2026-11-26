package cn.polar.x.domain.form.adapter.port;

import cn.polar.x.domain.form.model.entity.FormEntity;

/**
 * 表单缓存端口（由 infrastructure 层用 Redis 实现）
 */
public interface IFormCachePort {

    /** 缓存已发布表单的 schema（用户端高频读取） */
    void put(String formNo, FormEntity form, long ttlSeconds);

    /** 读取缓存，未命中返回 null */
    FormEntity get(String formNo);

    /** 失效缓存 */
    void evict(String formNo);

    /** 记录一次分享链接访问，返回累计访问次数 */
    long increaseVisit(String formNo);

    /** 读取分享链接访问次数 */
    long queryVisit(String formNo);

}
