/* X-From 用户端填写页 —— 对接 GET /api/form/{formNo} 与 POST /api/form/submit */
(function () {
  'use strict';

  const FormFillApi = window.XFromUserApi.FormFillApi;
  const state = { formNo: '', form: null, values: {}, submitting: false };
  const $ = (id) => document.getElementById(id);

  function resolveFormNo() {
    const params = new URLSearchParams(window.location.search);
    const fromQuery = params.get('formNo') || params.get('no') || params.get('code');
    if (fromQuery) return fromQuery.trim();
    /* 兼容 /f/{formNo} 这一种路径写法。
       注意不能直接取 pathname 最后一段：访问 /user/ 时会拿到 "user"，
       会被当成表单号去请求接口，从而盖住「未携带 formNo」的正常分支。 */
    const match = window.location.pathname.match(/\/f\/([^/]+)\/?$/);
    if (match) return decodeURIComponent(match[1]).trim();
    return '';
  }

  let toastTimer = null;
  function toast(message, type) {
    const el = $('toast');
    el.textContent = message;
    el.className = 'toast show ' + (type || '');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { el.className = 'toast ' + (type || ''); }, 2600);
  }

  function escapeHtml(text) {
    return String(text == null ? '' : text)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
  }

  function showError(title, message) {
    $('formCard').classList.add('hidden');
    $('successCard').classList.add('hidden');
    $('errorCard').classList.remove('hidden');
    $('errorTitle').textContent = title || '表单不可用';
    $('errorMsg').textContent = message || '分享链接可能已失效';
  }

  /* ---------------- 渲染字段 ---------------- */
  function renderField(field, index) {
    const key = field.key;
    const required = !!field.required;
    const label = '<div class="field-label">' +
      '<span>' + escapeHtml(field.label || key) + '</span>' +
      (required ? '<span class="required">*</span>' : '') +
      '</div>';
    const ph = escapeHtml(field.placeholder || '');
    const id = 'f_' + index;
    let control = '';

    switch (field.type) {
      case 'textarea':
        control = '<textarea class="textarea" id="' + id + '" data-key="' + escapeHtml(key) + '" placeholder="' + ph + '"></textarea>';
        break;
      case 'number':
        control = '<input class="input" type="number" id="' + id + '" data-key="' + escapeHtml(key) + '" placeholder="' + ph + '"/>';
        break;
      case 'date':
        control = '<input class="input" type="date" id="' + id + '" data-key="' + escapeHtml(key) + '"/>';
        break;
      case 'select':
        control = '<select class="select" id="' + id + '" data-key="' + escapeHtml(key) + '">' +
          '<option value="">' + (ph || '请选择') + '</option>' +
          (field.options || []).map((o) => '<option value="' + escapeHtml(o) + '">' + escapeHtml(o) + '</option>').join('') +
          '</select>';
        break;
      case 'radio':
        control = '<div class="options" data-key="' + escapeHtml(key) + '" data-type="radio">' +
          (field.options || []).map((o, i) => '<label class="option">' +
            '<input type="radio" name="' + id + '" value="' + escapeHtml(o) + '"' + (i === 0 ? '' : '') + '/>' +
            '<span>' + escapeHtml(o) + '</span></label>').join('') +
          '</div>';
        break;
      case 'checkbox':
        control = '<div class="options" data-key="' + escapeHtml(key) + '" data-type="checkbox">' +
          (field.options || []).map((o) => '<label class="option">' +
            '<input type="checkbox" value="' + escapeHtml(o) + '"/>' +
            '<span>' + escapeHtml(o) + '</span></label>').join('') +
          '</div>';
        break;
      default:
        control = '<input class="input" type="text" id="' + id + '" data-key="' + escapeHtml(key) + '" placeholder="' + ph + '"/>';
    }

    return '<div class="field" data-field-index="' + index + '" data-field-key="' + escapeHtml(key) + '" data-field-type="' + escapeHtml(field.type) + '" data-required="' + required + '">' +
      label + control +
      '<span class="field-error">该项为必填</span>' +
      '</div>';
  }

  function renderForm(form) {
    state.form = form;
    const card = $('formCard');
    const fieldsHtml = (form.fields || []).map(renderField).join('');
    card.innerHTML = fieldsHtml +
      '<div class="submit-bar">' +
      '<button class="btn-submit" id="btnSubmit">提交表单</button>' +
      '<div class="tip">提交后数据将实时同步到运营端</div>' +
      '</div>';
    $('btnSubmit').addEventListener('click', submit);
    card.classList.remove('hidden');

    $('heroTitle').textContent = form.title || '未命名表单';
    $('heroDesc').textContent = form.description || '请如实填写以下内容，感谢你的支持';
    $('heroFormNo').textContent = '表单编号 ' + form.formNo;
    $('heroVisits').textContent = form.visits ? ('访问 ' + form.visits + ' 次') : '';
    document.title = (form.title || '填写表单') + ' · X-From';

    // 选项高亮联动
    card.addEventListener('change', (e) => {
      const label = e.target.closest('.option');
      if (!label) return;
      const type = e.target.type;
      if (type === 'checkbox') {
        label.classList.toggle('checked', e.target.checked);
      } else if (type === 'radio') {
        label.parentElement.querySelectorAll('.option').forEach((el) => el.classList.remove('checked'));
        label.classList.add('checked');
      }
      const wrap = e.target.closest('.field');
      if (wrap) wrap.querySelector('.field-error').classList.remove('show');
    });
  }

  /* ---------------- 收集与提交 ---------------- */
  function collect() {
    const card = $('formCard');
    const data = {};
    let firstInvalid = null;

    card.querySelectorAll('.field').forEach((wrap) => {
      const key = wrap.getAttribute('data-field-key');
      const type = wrap.getAttribute('data-field-type');
      const required = wrap.getAttribute('data-required') === 'true';
      let value = null;

      if (type === 'radio') {
        const checked = wrap.querySelector('input[type=radio]:checked');
        value = checked ? checked.value : null;
      } else if (type === 'checkbox') {
        const list = Array.from(wrap.querySelectorAll('input[type=checkbox]:checked')).map((el) => el.value);
        value = list.length ? list : null;
      } else {
        const input = wrap.querySelector('[data-key]');
        value = input ? input.value.trim() : null;
      }

      const empty = value === null || value === '' || (Array.isArray(value) && !value.length);
      const error = wrap.querySelector('.field-error');
      if (required && empty) {
        if (error) error.classList.add('show');
        if (!firstInvalid) firstInvalid = wrap;
        return;
      }
      if (error) error.classList.remove('show');
      if (!empty) data[key] = type === 'number' ? Number(value) : value;
    });

    return { data: data, firstInvalid: firstInvalid };
  }

  async function submit() {
    if (state.submitting) return;
    const result = collect();
    if (result.firstInvalid) {
      result.firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
      toast('请完成必填项', 'error');
      return;
    }
    const btn = $('btnSubmit');
    state.submitting = true;
    btn.disabled = true;
    btn.textContent = '提交中…';
    try {
      await FormFillApi.submit(state.formNo, result.data);
      $('formCard').classList.add('hidden');
      $('errorCard').classList.add('hidden');
      $('successCard').classList.remove('hidden');
      $('heroTitle').textContent = '提交完成';
      $('heroDesc').textContent = '你的填写已成功提交';
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (e) {
      toast(e.message, 'error');
      btn.disabled = false;
      btn.textContent = '提交表单';
    } finally {
      state.submitting = false;
    }
  }

  /* 未携带 formNo（如直接打开 /user/）：列出已发布表单供选择，避免进入死路 */
  async function renderPicker() {
    $('heroTitle').textContent = '请选择要填写的表单';
    $('heroDesc').textContent = '当前链接未携带表单编号，下面是正在收集的表单';
    $('heroFormNo').textContent = '';
    $('heroVisits').textContent = '';
    document.title = '选择表单 · X-From';

    let list = [];
    try {
      const data = await FormFillApi.listPublished();
      list = (data && data.list) || [];
    } catch (e) {
      $('heroTitle').textContent = '暂时无法加载表单';
      $('heroDesc').textContent = e.message;
      showError('暂时无法加载表单', e.message);
      return;
    }

    if (!list.length) {
      $('heroTitle').textContent = '暂无可用表单';
      $('heroDesc').textContent = '还没有已发布的表单';
      showError('暂无可用表单', '还没有正在收集的表单，请向发起人索取分享链接');
      return;
    }

    /* 只有一个表单时直接进入，省去一次点击 */
    if (list.length === 1) {
      window.location.replace('?formNo=' + encodeURIComponent(list[0].formNo));
      return;
    }

    const card = $('formCard');
    card.innerHTML = '<div class="picker">' +
      '<div class="picker-title">可填写的表单（' + list.length + '）</div>' +
      list.map(function (item) {
        const formNo = String(item.formNo || '');
        return '<a class="picker-item" href="?formNo=' + encodeURIComponent(formNo) + '">' +
          '<div class="picker-main">' +
          '<div class="picker-name">' + escapeHtml(item.title || '未命名表单') + '</div>' +
          '<div class="picker-desc">' + escapeHtml(item.description || '无描述') + '</div>' +
          '</div>' +
          '<div class="picker-side">' +
          '<span class="picker-no">' + escapeHtml(formNo) + '</span>' +
          '<span class="picker-arrow">›</span>' +
          '</div></a>';
      }).join('') +
      '</div>';
    card.classList.remove('hidden');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  async function load() {
    state.formNo = resolveFormNo();
    if (!state.formNo) {
      await renderPicker();
      return;
    }
    try {
      const form = await FormFillApi.getForm(state.formNo);
      renderForm(form);
    } catch (e) {
      showError('表单不可用', e.message);
      $('heroTitle').textContent = '表单不可用';
      $('heroDesc').textContent = e.message;
    }
  }

  document.addEventListener('DOMContentLoaded', () => {
    $('btnRetry').addEventListener('click', () => {
      $('errorCard').classList.add('hidden');
      $('formCard').classList.remove('hidden');
      load();
    });
    $('btnAgain').addEventListener('click', () => {
      state.values = {};
      $('successCard').classList.add('hidden');
      $('formCard').classList.remove('hidden');
      load();
    });
    load();
  });
})();
