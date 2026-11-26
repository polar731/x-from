# X-From 表单系统

基于 **DDD 六边形架构** 的表单收集平台：运营端创建表单 → 生成分享链接 → 用户填写提交 → 运营端实时回收数据。

- 技术栈：Java 17 · Spring Boot 3.4.3 · MyBatis · MySQL 8.0 · Redis · 原生 HTML/CSS/JS · Nginx
- 架构：DDD 六边形分层（api / types / domain / case / trigger / infrastructure / app）
- 部署：Docker Compose + Nginx 反向代理

---

## 一、线上访问入口

| 端 | 地址 |
|---|---|
| 运营端（表单管理） | http://111.228.23.102:8088/admin/ |
| 用户端（填写页） | http://111.228.23.102:8088/user/index.html?formNo=**表单编号** |
| 健康检查 | http://111.228.23.102:8088/health |
| 后端 API | http://111.228.23.102:8088/api/**（经 Nginx 反代到 8091） |

> 根路径 `/` 会 302 跳转到 `/admin/`。
> 如需外网访问，云服务器安全组需放行 **TCP 8088**。

---

## 二、工程结构

```
x-from/
├── pom.xml                       # 聚合 POM（Java 17 / Spring Boot 3.4.3）
├── x-from-api/                   # 对外 DTO 与统一响应
├── x-from-types/                 # 通用枚举（表单状态等）
├── x-from-domain/                # 领域层：聚合 / 实体 / 值对象 / 仓储接口 / 领域服务
├── x-from-case/                  # 用例层：运营端、用户端表单用例编排
├── x-from-trigger/               # 触发层：REST Controller + 全局异常处理
├── x-from-infrastructure/        # 基础设施层：PO / DAO / 仓储实现 / Redis 缓存
├── x-from-app/                   # 启动模块：Application + 配置 + Mapper XML
└── docs/dev-ops/
    ├── docker-compose-app.yml            # 应用编排（app + nginx）
    ├── docker-compose-environment.yml    # 中间件编排（本地镜像）
    ├── docker-compose-environment-aliyun.yml  # 中间件编排（阿里云镜像）
    ├── mysql/sql/x_from.sql              # 建库建表脚本
    └── nginx/                            # nginx.conf + 运营端/用户端静态页
```

分层依赖方向：`trigger → case → domain ← infrastructure`，`domain` 不依赖任何外层实现。

---

## 三、接口清单

运营端 `/api/admin/form`：

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/create` | 创建表单（`publish=true` 可直接发布） |
| GET | `/list` | 分页列表（keyword / status / page / pageSize） |
| GET | `/detail?formNo=` | 表单详情 |
| POST | `/publish?formNo=` | 发布表单 |
| POST | `/close?formNo=` | 关闭表单 |
| GET | `/submissions?formNo=` | 填写记录分页 |
| GET | `/stats` | 全局统计（表单数 / 已发布 / 回收总数） |

用户端 `/api/form`：

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/{formNo}` | 获取表单结构（含访问次数累加） |
| GET | `/detail?formNo=` | 同上（参数形式） |
| POST | `/submit` | 提交填写数据，返回提交记录 ID |

统一响应：`{"code":"0000","info":"成功","data":...}`，`code` 非 `0000` 即失败。

### 字段类型（fields[].type）

`text`（默认）· `textarea` · `number` · `date` · `select` · `radio` · `checkbox`

### 表单状态（status）

`0` 草稿 · `1` 已发布 · `2` 已关闭

---

## 四、数据库

库名 `x_from`，表结构见 `docs/dev-ops/mysql/sql/x_from.sql`。

`form`：`id` / `form_no`(唯一) / `title` / `description` / `fields_json` / `status` / `submit_count` / `creator` / `create_time` / `update_time`

`form_submission`：`id` / `form_id` / `form_no` / `data_json` / `submit_ip` / `create_time`

字段定义与提交内容均以 JSON 存储，新增字段类型无需改表。

---

## 五、部署说明

服务器部署目录：`/root/dev-ops/x-from/`

```
x-from/
├── app/x-from-app.jar        # 后端可执行 JAR
├── nginx/nginx.conf          # 网关配置（8088）
├── nginx/html/               # 静态页（admin / user）
├── log/                      # 应用日志
├── sql/                      # 初始化 SQL
└── docker-compose-app.yml    # 应用编排
```

### 首次部署

```bash
# 1. 启动中间件（MySQL 13306 / Redis 16379）
docker compose -f docker-compose-environment-aliyun.yml up -d

# 2. 初始化数据库
docker exec -i mysql mysql -uroot -p123456 < sql/x_from.sql

# 3. 启动应用 + 网关
docker compose -f docker-compose-app.yml up -d

# 4. 验证
curl -s http://127.0.0.1:8088/api/admin/form/stats
```

### 更新版本

```bash
# 本地打包
mvn clean package -DskipTests

# 上传 JAR 后重建容器（bind mount 换了 inode，必须 force-recreate）
docker compose -f docker-compose-app.yml up -d --force-recreate x-from-app
```

### 关键约定

- 容器使用 **host 网络**：后端 8091、网关 8088 直接监听宿主机端口。
- 生产配置连 `127.0.0.1:13306` / `127.0.0.1:16379`：**不可用公网 IP 回环**，本机 NAT 会拦截。
- 活跃 profile 由 `SPRING_PROFILES_ACTIVE=prod` 环境变量下发。
- `pom.xml` 中 `maven-compiler-plugin` 必须开启 `<parameters>true</parameters>`，否则 `@RequestParam` / `@PathVariable` 反射取不到参数名。
- `spring-boot-maven-plugin` 的 `mainClass` 为 `cn.polar.x.Application`。

---

## 六、验收记录

2026-11-26 外网全链路复验通过：

| 检查项 | 结果 |
|---|---|
| `/admin/`、`/user/`、`/health` | 200 |
| 创建并发布表单 | `code=0000`，返回 formNo + shareUrl |
| 用户端获取表单 | `code=0000`，字段结构正确 |
| 用户端提交 | `code=0000`，返回提交 ID |
| 运营端统计 | 表单 3 / 发布 3 / 回收 5 |
| 运营端填写记录 | 明细含真实公网 IP |
| 浏览器真实提交 | 提交成功，运营端回收数实时 +1 |

数据访问示例：

```sql
-- 低于 3 万的在职员工（演示数据，位于 test_polar 库）
SELECT emp_no, emp_name, dept_name, job_title, salary
FROM test_polar.t_employee
WHERE salary < 30000
ORDER BY salary ASC;
```

---

## 七、致谢

- DDD 理论与实践参考：[小傅哥 DDD 教程](https://bugstack.cn/md/road-map/ddd.html)
- Docker 环境搭建：[Docker 使用文档](https://bugstack.cn/md/road-map/docker.html)
