# Arthas 在线诊断（X-From 课程演示）

容器内的 JVM 已通过 **javaagent（premain）** 方式预加载 Arthas，
应用启动即具备诊断能力，无需运行期 attach。

## 为什么不用 `arthas-boot.jar` 动态 attach

基础镜像 `eclipse-temurin:17-jre` 精简掉了 `jdk.attach` 模块
（`java --list-modules | grep attach` 无输出），
所以 `arthas-boot.jar` 会抛：

```
Caused by: java.lang.NoClassDefFoundError: com/sun/tools/attach/AgentLoadException
```

`javaagent` 方式走 premain，不依赖 `jdk.attach`，JRE 镜像即可满足。

## 目录说明

`./arthas` 挂载到容器 `/opt/arthas`，需要的是 Arthas **完整包**内容，
`arthas-boot.jar` 只是引导包，缺 `arthas-core.jar` 会报
`arthas-core.jar do not exist, arthas home: /opt/arthas`。

在服务器上获取完整包：

```bash
mkdir -p /root/dev-ops/x-from/arthas && cd /root/dev-ops/x-from/arthas
wget https://maven.aliyun.com/repository/public/com/taobao/arthas/arthas-packaging/4.3.5/arthas-packaging-4.3.5-bin.zip
# 宿主机无 unzip 时用 python 解压
python3 -c "import zipfile;zipfile.ZipFile('arthas-packaging-4.3.5-bin.zip').extractall('.')"
```

完整包内包含：`arthas-agent.jar`、`arthas-core.jar`、`arthas-spy.jar`、
`arthas-client.jar`、`lib/`、`async-profiler/`、`arthas.properties`。

> 这些 jar 体积约 17MB，不要提交到 Git，只提交本配置文件。

## 配置

`arthas.properties`（本目录）随容器挂载生效：

- `arthas.ip=0.0.0.0`：host 网络下允许宿主机/隧道访问
- `arthas.httpPort=8563` / `arthas.telnetPort=3658`
- `arthas.username` / `arthas.password`：远程访问认证（务必修改默认值）

## 访问方式

**推荐：SSH 隧道（不暴露公网端口）**

```bash
ssh -L 18563:127.0.0.1:8563 root@111.228.23.102
# 浏览器打开 http://127.0.0.1:18563
```

**或在服务器上直接连 telnet**

```bash
docker exec -it x-from-app telnet 127.0.0.1 3658
```

**若要公网直连 8563**：需在云厂商安全组放行该端口（服务器本身 ufw/iptables 未拦截），
但 Diagnostic 通道暴露公网风险较高，建议仅课程演示时临时开启并及时关闭。

## 常用命令

| 命令 | 用途 |
| --- | --- |
| `dashboard` | 实时看线程/内存/GC（Ctrl+C 退出） |
| `thread -n 3` | CPU 占用最高的 3 个线程及栈 |
| `thread --state BLOCKED` | 查找阻塞线程 |
| `heapdump /tmp/a.hprof` | 导出堆快照 |
| `jad cn.polar.x.xxx.FormService` | 反编译运行中的类 |
| `watch cn.polar.x.xxx.FormService query '{params,returnObj}' -x 2` | 观察方法入参/返回值 |
| `trace cn.polar.x.xxx.FormService query` | 追踪方法调用耗时 |
| `logger --name root --level DEBUG` | 动态改日志级别 |

## 验证记录

- `ss -tlnp | grep -E "8563|3658"` → 两个端口均由 java 进程监听
- `curl 127.0.0.1:8563` → 200，Web Console 正常加载
- Console 连接后 `thread -n 3` 正常输出 `lettuce-timer`/`Reference Handler` 等线程栈
