/*!
 * X-From 用户端 —— 接口对接层
 * 分享链接落地页涉及的全部接口都收敛在此文件，页面逻辑（app.js）只负责渲染与交互。
 */
(function (global) {
  'use strict';

  const BASE = '/api/form';
  const SUCCESS_CODE = '0000';

  const enc = encodeURIComponent;

  /**
   * 通用请求
   * @param {string} url
   * @param {object} [opts] { method, body, query }
   * @returns {Promise<*>} 成功返回 data，失败抛出可读错误
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
      /* 接口数据不走浏览器缓存，避免读到过期的表单结构 */
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
      throw new Error('网络异常，请检查网络后重试');
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

  const FormFillApi = {
    /** 打开表单 GET /api/form/{formNo} → FormFillDTO */
    getForm(formNo) {
      return request(BASE + '/' + enc(formNo));
    },

    /** 提交填写 POST /api/form/submit */
    submit(formNo, data) {
      return request(BASE + '/submit', {
        method: 'POST',
        body: { formNo: formNo, data: data }
      });
    }
  };

  global.XFromUserApi = {
    request: request,
    FormFillApi: FormFillApi,
    BASE: BASE
  };
})(window);
