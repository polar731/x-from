/* X-From 运营端 —— 对接 /api/admin/form 与 /api/form */
(function () {
  'use strict';

  const ADMIN_API = '/api/admin/form';
  const state = {
    page: 1,
    pageSize: 10,
    keyword: '',
    status: '',
    total: 0,
    rows: [],
    // 数据弹窗
    dataFormNo: null,
    dataPage: 1,
    dataPageSize: 10,
    dataTotal: 0,
    dataFields: [],
    // 字段编辑器
    fields: []
  };

  const $ = (id) => document.getElementById(id);

  /* ---------------- 基础工具 ---------------- */
  async function request(url, options) {
    const config = Object.assign({ headers: { 'Content-Type': 'application/json' } }, options || {});
    let resp;
    try {
      resp = await fetch(url, config);
    } catch (e) {
      throw new Error('网络异常，无法连接服务端');
    }
    let body;
    try {
      body = await resp.json();
    } catch (e) {
      throw new Error('服务端响应异常（HTTP ' + resp.status + '）');
    }
    if (!body || body.code !== '0000') {
      throw new Error((body && body.info) || ('请求失败（HTTP ' + resp.status + '）'));
    }
    return body.data;
  }

  let toastTimer = null;
  function toast(message, type) {
    const el = $('toast');
    el.textContent = message;
    el.className = 'toast show ' + (type || '');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { el.className = 'toast ' + (type || ''); }, 2600);
  }

  function fmtTime(value) {
    if (!value) return '-';
    const d = new Date(value);
    if (isNaN(d.getTime())) return String(value);
    const p = (n) => String(n).padStart(2, '0');
    return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()) + ' ' + p(d.getHours()) + ':' + p(d.getMinutes());
  }

  function escapeHtml(text) {
    return String(text == null ? '' : text)
      .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
  }

  function statusTag(status, desc) {
    const cls = status === 1 ? 'tag-published' : (status === 0 ? 'tag-draft' : 'tag-closed');
    return '<span class="tag ' + cls + '">' + escapeHtml(desc || '未知') + '</span>';
  }

  function copyText(text) {
    if (navigator.clipboard && window.isSecureContext) {
      return navigator.clipboard.writeText(text);
    }
    return new Promise((resolve, reject) => {
      const ta = document.createElement('textarea');
      ta.value = text;
      ta.style.position = 'fixed';
      ta.style.opacity = '0';
      document.body.appendChild(ta);
      ta.select();
      try { document.execCommand('copy'); resolve(); } catch (e) { reject(e); }
      document.body.removeChild(ta);
    });
  }

  /* ---------------- 概览 ---------------- */
  async function loadStats() {
    try {
      const data = await request(ADMIN_API + '/stats');
      $('statTotal').textContent = data.formTotal;
      $('statPublished').textContent = data.publishedCount;
      $('statDraft').innerHTML = data.draftCount + ' <small>/ ' + data.closedCount + ' 关闭</small>';
      $('statSubmissions').textContent = data.submissionTotal;
    } catch (e) {
      toast(e.message, 'error');
    }
  }

  /* ---------------- 列表 ---------------- */
  function skeleton() {
    $('formTableBody').innerHTML = '<tr class="skeleton-row"><td colspan="7"></td></tr>' +
      '<tr class="skeleton-row"><td colspan="7"></td></tr>' +
      '<tr class="skeleton-row"><td colspan="7"></td></tr>';
  }

  function renderRows() {
    const body = $('formTableBody');
    if (!state.rows.length) {
      body.innerHTML = '<tr><td colspan="7"><div class="empty"><span class="icon">📋</span>还没有符合条件的表单，点右上角「新建表单」开始</div></td></tr>';
      return;
    }
    body.innerHTML = state.rows.map((row) => {
      const ops = [];
      if (row.status === 1 && row.shareUrl) {
        ops.push('<button class="link-btn" data-act="copy" data-no="' + row.formNo + '">复制链接</button>');
        ops.push('<button class="link-btn" data-act="open" data-no="' + row.formNo + '">打开填写页</button>');
      }
      if (row.status === 1 || row.status === 2) {
        ops.push('<button class="link-btn" data-act="data" data-no="' + row.formNo + '">查看数据</button>');
      }
      if (row.status === 0) {
        ops.push('<button class="link-btn" data-act="publish" data-no="' + row.formNo + '">发布</button>');
      }
      if (row.status !== 2) {
        ops.push('<button class="link-btn muted" data-act="close" data-no="' + row.formNo + '">关闭</button>');
      }
      return '<tr>' +
        '<td><div class="cell-title">' + escapeHtml(row.title) + '</div>' +
        '<div class="cell-sub">' + escapeHtml(row.description || '无描述') + '</div></td>' +
        '<td><span class="code-chip">' + escapeHtml(row.formNo) + '</span></td>' +
        '<td>' + statusTag(row.status, row.statusDesc) + '</td>' +
        '<td>' + (row.fields ? row.fields.length : 0) + ' 个</td>' +
        '<td><b>' + (row.submitCount || 0) + '</b></td>' +
        '<td class="cell-sub" style="margin:0">' + fmtTime(row.createTime) + '</td>' +
        '<td><div class="ops">' + ops.join('') + '</div></td>' +
        '</tr>';
    }).join('');
  }

  function renderPager() {
    const totalPage = Math.max(1, Math.ceil(state.total / state.pageSize));
    $('pagerInfo').textContent = '共 ' + state.total + ' 条，第 ' + state.page + ' / ' + totalPage + ' 页';
    $('listTotal').textContent = state.total;
    const pages = [];
    pages.push('<button class="page-btn" data-page="' + (state.page - 1) + '"' + (state.page <= 1 ? ' disabled' : '') + '>‹</button>');
    const start = Math.max(1, state.page - 2);
    const end = Math.min(totalPage, start + 4);
    for (let i = start; i <= end; i++) {
      pages.push('<button class="page-btn' + (i === state.page ? ' active' : '') + '" data-page="' + i + '">' + i + '</button>');
    }
    pages.push('<button class="page-btn" data-page="' + (state.page + 1) + '"' + (state.page >= totalPage ? ' disabled' : '') + '>›</button>');
    $('pagerPages').innerHTML = pages.join('');
  }

  async function loadList() {
    skeleton();
    try {
      const query = new URLSearchParams();
      if (state.keyword) query.set('keyword', state.keyword);
      if (state.status !== '') query.set('status', state.status);
      query.set('page', state.page);
      query.set('pageSize', state.pageSize);
      const data = await request(ADMIN_API + '/list?' + query.toString());
      state.rows = data.list || [];
      state.total = data.total || 0;
      renderRows();
      renderPager();
    } catch (e) {
      $('formTableBody').innerHTML = '<tr><td colspan="7"><div class="empty"><span class="icon">⚠️</span>' + escapeHtml(e.message) + '</div></td></tr>';
      toast(e.message, 'error');
    }
  }

  async function publishForm(formNo) {
    try {
      const data = await request(ADMIN_API + '/publish?formNo=' + encodeURIComponent(formNo), { method: 'POST' });
      toast('已发布，分享链接：' + (data.shareUrl || '-'), 'success');
      refreshAll();
    } catch (e) {
      toast(e.message, 'error');
    }
  }

  async function closeForm(formNo) {
    if (!window.confirm('关闭后用户将无法继续填写，确认关闭表单 ' + formNo + ' ？')) return;
    try {
      await request(ADMIN_API + '/close?formNo=' + encodeURIComponent(formNo), { method: 'POST' });
      toast('表单已关闭', 'success');
      refreshAll();
    } catch (e) {
      toast(e.message, 'error');
    }
  }

  function refreshAll() {
    loadStats();
    loadList();
  }

  /* ---------------- 新建表单 ---------------- */
  const FIELD_TYPES = [
    { value: 'text', label: '单行文本' },
    { value: 'textarea', label: '多行文本' },
    { value: 'number', label: '数字' },
    { value: 'radio', label: '单选' },
    { value: 'checkbox', label: '多选' },
    { value: 'select', label: '下拉选择' },
    { value: 'date', label: '日期' }
  ];
  const OPTION_TYPES = ['radio', 'checkbox', 'select'];

  function renderEditor() {
    const box = $('fieldEditor');
    if (!state.fields.length) {
      box.innerHTML = '<div class="editor-empty">还没有字段，点击下方「添加字段」</div>';
      return;
    }
    box.innerHTML = state.fields.map((field, index) => {
      const needOptions = OPTION_TYPES.indexOf(field.type) >= 0;
      return '<div class="field-card" data-index="' + index + '">' +
        '<div class="field-card-head">' +
        '<span class="field-index">字段 ' + (index + 1) + '</span>' +
        '<button class="field-del" data-del="' + index + '">删除</button>' +
        '</div>' +
        '<div class="form-grid-2">' +
        '<input class="input" data-prop="label" data-index="' + index + '" placeholder="字段名称，如「你的姓名」" value="' + escapeHtml(field.label) + '"/>' +
        '<input class="input" data-prop="key" data-index="' + index + '" placeholder="字段标识，如 name" value="' + escapeHtml(field.key) + '"/>' +
        '</div>' +
        '<div class="form-grid-2">' +
        '<select class="select" data-prop="type" data-index="' + index + '">' +
        FIELD_TYPES.map((t) => '<option value="' + t.value + '"' + (t.value === field.type ? ' selected' : '') + '>' + t.label + '</option>').join('') +
        '</select>' +
        '<label class="checkbox"><input type="checkbox" data-prop="required" data-index="' + index + '"' + (field.required ? ' checked' : '') + '/> 必填</label>' +
        '</div>' +
        '<input class="input" data-prop="placeholder" data-index="' + index + '" placeholder="输入提示（选填）" value="' + escapeHtml(field.placeholder || '') + '"/>' +
        (needOptions ? '<input class="input" data-prop="options" data-index="' + index + '" placeholder="选项用英文逗号分隔，如：非常满意,满意,一般" value="' + escapeHtml((field.options || []).join(',')) + '"/>' : '') +
        '</div>';
    }).join('');
  }

  function addField() {
    const index = state.fields.length + 1;
    state.fields.push({ key: 'field_' + index, label: '', type: 'text', required: true, placeholder: '', options: [] });
    renderEditor();
  }

  function bindEditorEvents() {
    $('fieldEditor').addEventListener('input', (e) => {
      const target = e.target;
      const index = target.getAttribute('data-index');
      const prop = target.getAttribute('data-prop');
      if (index === null || !prop) return;
      const field = state.fields[Number(index)];
      if (!field) return;
      if (prop === 'required') {
        field.required = target.checked;
        return;
      }
      if (prop === 'options') {
        field.options = target.value.split(',').map((s) => s.trim()).filter(Boolean);
        return;
      }
      field[prop] = target.value;
      if (prop === 'type') {
        renderEditor();
      }
    });
    $('fieldEditor').addEventListener('click', (e) => {
      const del = e.target.getAttribute('data-del');
      if (del === null) return;
      state.fields.splice(Number(del), 1);
      renderEditor();
    });
  }

  function openCreateModal() {
    state.fields = [];
    $('formTitle').value = '';
    $('formDesc').value = '';
    renderEditor();
    $('createModal').classList.add('open');
    $('formTitle').focus();
  }

  async function submitCreate(publish) {
    const title = $('formTitle').value.trim();
    if (!title) { toast('请填写表单标题', 'error'); return; }
    const fields = state.fields.map((field, index) => ({
      key: (field.key || '').trim() || ('field_' + (index + 1)),
      label: (field.label || '').trim(),
      type: field.type,
      required: !!field.required,
      placeholder: field.placeholder || '',
      options: OPTION_TYPES.indexOf(field.type) >= 0 ? (field.options || []) : []
    }));
    if (!fields.length) { toast('请至少添加一个字段', 'error'); return; }
    const emptyLabel = fields.find((f) => !f.label);
    if (emptyLabel) { toast('字段名称不能为空（' + emptyLabel.key + '）', 'error'); return; }
    const emptyOption = fields.find((f) => OPTION_TYPES.indexOf(f.type) >= 0 && (!f.options || !f.options.length));
    if (emptyOption) { toast('「' + emptyOption.label + '」需要配置选项', 'error'); return; }

    const buttons = [$('btnSaveDraft'), $('btnCreatePublish')];
    buttons.forEach((b) => { b.disabled = true; });
    try {
      const data = await request(ADMIN_API + '/create', {
        method: 'POST',
        body: JSON.stringify({ title: title, description: $('formDesc').value.trim(), fields: fields, publish: publish, creator: '运营端' })
      });
      $('createModal').classList.remove('open');
      if (publish && data.shareUrl) {
        toast('发布成功，分享链接已复制', 'success');
        copyText(data.shareUrl).catch(() => {});
      } else {
        toast('草稿已保存：' + data.formNo, 'success');
      }
      refreshAll();
    } catch (e) {
      toast(e.message, 'error');
    } finally {
      buttons.forEach((b) => { b.disabled = false; });
    }
  }

  /* ---------------- 填写数据 ---------------- */
  async function openDataModal(formNo) {
    state.dataFormNo = formNo;
    state.dataPage = 1;
    $('dataModalTitle').textContent = '填写数据 · ' + formNo;
    $('dataModalSub').textContent = '加载中…';
    $('dataTableHead').innerHTML = '';
    $('dataTableBody').innerHTML = '<tr class="skeleton-row"><td></td></tr>';
    $('dataModal').classList.add('open');
    try {
      const detail = await request(ADMIN_API + '/detail?formNo=' + encodeURIComponent(formNo));
      state.dataFields = detail.fields || [];
      $('dataModalSub').textContent = detail.title + ' · 已回收 ' + (detail.submitCount || 0) + ' 条数据';
      await loadSubmissions();
    } catch (e) {
      $('dataModalSub').textContent = e.message;
      $('dataTableBody').innerHTML = '<tr><td><div class="empty">' + escapeHtml(e.message) + '</div></td></tr>';
    }
  }

  async function loadSubmissions() {
    if (!state.dataFormNo) return;
    const query = new URLSearchParams({
      formNo: state.dataFormNo,
      page: state.dataPage,
      pageSize: state.dataPageSize
    });
    try {
      const data = await request(ADMIN_API + '/submissions?' + query.toString());
      state.dataTotal = data.total || 0;
      const fields = state.dataFields;
      $('dataTableHead').innerHTML = '<tr><th style="width:60px">#</th>' +
        fields.map((f) => '<th>' + escapeHtml(f.label || f.key) + '</th>').join('') +
        '<th style="width:130px">IP</th><th style="width:150px">提交时间</th></tr>';
      if (!data.list || !data.list.length) {
        $('dataTableBody').innerHTML = '<tr><td colspan="' + (fields.length + 3) + '"><div class="empty"><span class="icon">📭</span>还没有人填写这份表单</div></td></tr>';
      } else {
        $('dataTableBody').innerHTML = data.list.map((row, i) => {
          const cells = fields.map((f) => {
            const value = row.data ? row.data[f.key] : '';
            let text;
            if (Array.isArray(value)) {
              text = value.map((v) => '<span class="value-pill">' + escapeHtml(v) + '</span>').join('');
            } else {
              text = value === null || value === undefined || value === '' ? '<span class="cell-sub">—</span>' : escapeHtml(value);
            }
            return '<td>' + text + '</td>';
          }).join('');
          const no = (state.dataPage - 1) * state.dataPageSize + i + 1;
          return '<tr><td>' + no + '</td>' + cells +
            '<td class="cell-sub" style="margin:0">' + escapeHtml(row.submitIp || '-') + '</td>' +
            '<td class="cell-sub" style="margin:0">' + fmtTime(row.createTime) + '</td></tr>';
        }).join('');
      }
      const totalPage = Math.max(1, Math.ceil(state.dataTotal / state.dataPageSize));
      $('dataPagerInfo').textContent = '共 ' + state.dataTotal + ' 条，第 ' + state.dataPage + ' / ' + totalPage + ' 页';
      const pages = [];
      pages.push('<button class="page-btn" data-dpage="' + (state.dataPage - 1) + '"' + (state.dataPage <= 1 ? ' disabled' : '') + '>‹</button>');
      for (let p = Math.max(1, state.dataPage - 2); p <= Math.min(totalPage, state.dataPage + 2); p++) {
        pages.push('<button class="page-btn' + (p === state.dataPage ? ' active' : '') + '" data-dpage="' + p + '">' + p + '</button>');
      }
      pages.push('<button class="page-btn" data-dpage="' + (state.dataPage + 1) + '"' + (state.dataPage >= totalPage ? ' disabled' : '') + '>›</button>');
      $('dataPagerPages').innerHTML = pages.join('');
    } catch (e) {
      toast(e.message, 'error');
    }
  }

  /* ---------------- 事件绑定 ---------------- */
  function bindEvents() {
    $('btnCreate').addEventListener('click', openCreateModal);
    $('btnRefresh').addEventListener('click', () => { refreshAll(); toast('已刷新'); });
    $('btnSearch').addEventListener('click', () => {
      state.keyword = $('keyword').value.trim();
      state.status = $('statusFilter').value;
      state.page = 1;
      loadList();
    });
    $('keyword').addEventListener('keydown', (e) => { if (e.key === 'Enter') $('btnSearch').click(); });
    $('statusFilter').addEventListener('change', () => $('btnSearch').click());
    $('btnAddField').addEventListener('click', addField);
    $('btnSaveDraft').addEventListener('click', () => submitCreate(false));
    $('btnCreatePublish').addEventListener('click', () => submitCreate(true));
    bindEditorEvents();

    // 列表操作
    $('formTableBody').addEventListener('click', async (e) => {
      const btn = e.target.closest('button[data-act]');
      if (!btn) return;
      const act = btn.getAttribute('data-act');
      const formNo = btn.getAttribute('data-no');
      const row = state.rows.find((r) => r.formNo === formNo);
      if (act === 'copy') {
        const url = row && row.shareUrl ? row.shareUrl : '';
        if (!url) { toast('该表单暂无分享链接', 'error'); return; }
        try { await copyText(url); toast('已复制：' + url, 'success'); } catch (err) { toast('复制失败，请手动复制：' + url, 'error'); }
      } else if (act === 'open') {
        if (row && row.shareUrl) window.open(row.shareUrl, '_blank');
      } else if (act === 'data') {
        openDataModal(formNo);
      } else if (act === 'publish') {
        publishForm(formNo);
      } else if (act === 'close') {
        closeForm(formNo);
      }
    });

    // 分页
    $('pagerPages').addEventListener('click', (e) => {
      const btn = e.target.closest('button[data-page]');
      if (!btn || btn.disabled) return;
      state.page = Number(btn.getAttribute('data-page'));
      loadList();
    });
    $('dataPagerPages').addEventListener('click', (e) => {
      const btn = e.target.closest('button[data-dpage]');
      if (!btn || btn.disabled) return;
      state.dataPage = Number(btn.getAttribute('data-dpage'));
      loadSubmissions();
    });

    // 关闭弹窗
    document.addEventListener('click', (e) => {
      const closer = e.target.closest('[data-close]');
      if (closer) {
        const id = closer.getAttribute('data-close');
        $(id).classList.remove('open');
      }
    });
    document.addEventListener('keydown', (e) => {
      if (e.key === 'Escape') {
        document.querySelectorAll('.modal.open').forEach((m) => m.classList.remove('open'));
      }
    });

    // 侧栏导航
    $('navDocs').addEventListener('click', () => $('docsModal').classList.add('open'));
    $('navData').addEventListener('click', () => {
      const first = state.rows.find((r) => r.status !== 0);
      if (!first) { toast('暂无可查看数据的表单', 'error'); return; }
      openDataModal(first.formNo);
    });
    $('navForms').addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
  }

  document.addEventListener('DOMContentLoaded', () => {
    bindEvents();
    refreshAll();
  });
})();
