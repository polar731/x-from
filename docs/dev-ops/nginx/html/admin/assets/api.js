/*!
 * X-From 运营端 —— 接口对接层
 * 所有 /api/admin/form 与 /api/form 的调用都收敛在此文件，
 * 页面逻辑（app.js）只调用这里的方法，不再直接写 fetch / URL 拼接。
 */
(function (global) {
  'use strict';

  /* 接口前缀 */
  const ADMIN_BASE = '/api/admin/form';
  const CLIENT_BASE = '/api/form';
  /* 服务端统一响应中代表成功的 code */
  const SUCCESS_CODE = '0000';

  const enc = encodeURIComponent;

  /**
   * 通用请求
   * @param {string} url    接口地址
   * @param {object} [opts] { method, body, query }
   *                        query 会自动跳过 undefined / null / '' 的参数
   * @returns {Promise<*>}  成功时返回响应体中的 data，失败抛出可读错误
   */
  async function request(url, opts) {
    const options = opts || {};
    let fullUrl = url;

    if (options.query) {
      const search = new URLSearchParams();
      Object.keys(options.query).forEach(function (key) {
        const value = options.query[key];
        if (value !== undefined && value !== null && value !== '') {
          search.set(key, value);
        }
      });
      const qs = search.toString();
      if (qs) fullUrl += (fullUrl.indexOf('?') >= 0 ? '&' : '?') + qs;
    }

    const config = {
      method: options.method || 'GET',
      /* 接口数据不走浏览器缓存，避免读到过期的列表 / 概览 */
      cache: 'no-store',
      headers: { 'Content-Type': 'application/json' }
    };
    if (options.body !== undefined) {
      config.body = typeof options.body === 'string' ? options.body : JSON.stringify(options.body);
    }

    let resp;
    try {
      resp = await fetch(fullUrl, config);
    } catch (e) {
      throw new Error('网络异常，无法连接服务端');
    }

    let payload;
    try {
      payload = await resp.json();
    } catch (e) {
      throw new Error('服务端响应异常（HTTP ' + resp.status + '）');
    }

    if (!payload || payload.code !== SUCCESS_CODE) {
      throw new Error((payload && payload.info) || ('请求失败（HTTP ' + resp.status + '）'));
    }
    return payload.data;
  }

  /* ==================== 表单接口 ==================== */
  const FormApi = {
    /** 数据概览 GET /api/admin/form/stats */
    stats() {
      return request(ADMIN_BASE + '/stats');
    },

    /** 表单列表 GET /api/admin/form/list?keyword=&status=&page=&pageSize= */
    list(params) {
      const p = params || {};
      return request(ADMIN_BASE + '/list', {
        query: {
          keyword: p.keyword,
          status: p.status,
          page: p.page,
          pageSize: p.pageSize
        }
      });
    },

    /** 表单详情 GET /api/admin/form/detail?formNo= */
    detail(formNo) {
      return request(ADMIN_BASE + '/detail', { query: { formNo: formNo } });
    },

    /** 创建表单 POST /api/admin/form/create */
    create(payload) {
      return request(ADMIN_BASE + '/create', { method: 'POST', body: payload });
    },

    /** 发布表单 POST /api/admin/form/publish?formNo= */
    publish(formNo) {
      return request(ADMIN_BASE + '/publish', { method: 'POST', query: { formNo: formNo } });
    },

    /** 关闭表单 POST /api/admin/form/close?formNo= */
    close(formNo) {
      return request(ADMIN_BASE + '/close', { method: 'POST', query: { formNo: formNo } });
    },

    /** 填写数据分页 GET /api/admin/form/submissions?formNo=&page=&pageSize= */
    submissions(params) {
      const p = params || {};
      return request(ADMIN_BASE + '/submissions', {
        query: { formNo: p.formNo, page: p.page, pageSize: p.pageSize }
      });
    },

    /** 用户端表单结构 GET /api/form/{formNo}（运营端预览填写页用） */
    fillForm(formNo) {
      return request(CLIENT_BASE + '/' + enc(formNo));
    }
  };

  /* ==================== 模拟登录 ==================== */
  /* 服务端未提供鉴权接口，此处为前端演示级登录：本地校验 + localStorage 记住会话 */
  const SESSION_KEY = 'xfrom.admin.session';
  const DEMO_ACCOUNT = { username: 'admin', password: '123456' };

  const AuthApi = {
    demoAccount: DEMO_ACCOUNT,

    /**
     * 模拟登录（带 260ms 延迟模拟真实请求耗时）
     * @returns {Promise<{username:string, loginAt:string}>}
     */
    async login(username, password) {
      await new Promise(function (resolve) { setTimeout(resolve, 260); });

      const user = String(username == null ? '' : username).trim();
      const pass = String(password == null ? '' : password);
      if (!user || !pass) {
        throw new Error('请输入账号和密码');
      }
      if (user !== DEMO_ACCOUNT.username || pass !== DEMO_ACCOUNT.password) {
        throw new Error('账号或密码不正确（演示账号 admin / 123456）');
      }

      const session = { username: user, loginAt: new Date().toISOString() };
      try {
        localStorage.setItem(SESSION_KEY, JSON.stringify(session));
      } catch (e) { /* 隐私模式下忽略 */ }
      return session;
    },

    /** 退出登录 */
    logout() {
      try {
        localStorage.removeItem(SESSION_KEY);
      } catch (e) { /* ignore */ }
    },

    /** 读取当前会话，未登录返回 null */
    current() {
      try {
        const raw = localStorage.getItem(SESSION_KEY);
        return raw ? JSON.parse(raw) : null;
      } catch (e) {
        return null;
      }
    }
  };

  global.XFromApi = {
    request: request,
    FormApi: FormApi,
    AuthApi: AuthApi,
    ADMIN_BASE: ADMIN_BASE,
    CLIENT_BASE: CLIENT_BASE
  };
})(window);
