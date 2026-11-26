package cn.polar.x.test;

import cn.polar.x.types.enums.FormStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 单元测试：不启动 Spring 上下文，无需外部 MySQL/Redis 依赖
 */
@Slf4j
public class ApiTest {

    @Test
    public void test() {
        log.info("测试完成");
    }

    @Test
    public void testFormStatusEnum() {
        Assertions.assertEquals(FormStatusEnum.DRAFT, FormStatusEnum.getByCode(0));
        Assertions.assertEquals(FormStatusEnum.PUBLISHED, FormStatusEnum.getByCode(1));
        Assertions.assertEquals(FormStatusEnum.CLOSED, FormStatusEnum.getByCode(2));
        Assertions.assertNull(FormStatusEnum.getByCode(99));
        Assertions.assertEquals("已发布", FormStatusEnum.getByCode(1).getInfo());
        log.info("枚举校验通过");
    }

}
