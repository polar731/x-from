package cn.polar.x.infrastructure.adapter.port;

import cn.polar.x.domain.form.adapter.port.IFormCachePort;
import cn.polar.x.domain.form.model.entity.FormEntity;
import cn.polar.x.infrastructure.redis.RedisKey;
import com.alibaba.fastjson.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * 表单缓存端口实现（Redis）
 */
@Slf4j
@Repository
public class FormCachePort implements IFormCachePort {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void put(String formNo, FormEntity form, long ttlSeconds) {
        if (StringUtils.isBlank(formNo) || form == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(RedisKey.FORM_SCHEMA + formNo, JSON.toJSONString(form),
                    Duration.ofSeconds(ttlSeconds));
        } catch (Exception e) {
            // 缓存不可用不影响主流程
            log.warn("表单schema写入缓存失败 formNo:{}", formNo, e);
        }
    }

    @Override
    public FormEntity get(String formNo) {
        try {
            String json = stringRedisTemplate.opsForValue().get(RedisKey.FORM_SCHEMA + formNo);
            if (StringUtils.isBlank(json)) {
                return null;
            }
            return JSON.parseObject(json, FormEntity.class);
        } catch (Exception e) {
            log.warn("表单schema读取缓存失败 formNo:{}", formNo, e);
            return null;
        }
    }

    @Override
    public void evict(String formNo) {
        try {
            stringRedisTemplate.delete(RedisKey.FORM_SCHEMA + formNo);
        } catch (Exception e) {
            log.warn("表单schema清除缓存失败 formNo:{}", formNo, e);
        }
    }

    @Override
    public long increaseVisit(String formNo) {
        try {
            Long count = stringRedisTemplate.opsForValue().increment(RedisKey.FORM_VISIT + formNo);
            return count == null ? 0L : count;
        } catch (Exception e) {
            log.warn("分享链接访问计数失败 formNo:{}", formNo, e);
            return 0L;
        }
    }

    @Override
    public long queryVisit(String formNo) {
        try {
            String value = stringRedisTemplate.opsForValue().get(RedisKey.FORM_VISIT + formNo);
            return StringUtils.isBlank(value) ? 0L : Long.parseLong(value);
        } catch (Exception e) {
            return 0L;
        }
    }

}
