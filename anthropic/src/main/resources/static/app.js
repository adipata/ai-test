// Thin client for the cert-alert REST API.
// Keeps the JWT in sessionStorage so a reload restores the session within the tab.

const state = {
    token: sessionStorage.getItem('jwt') || null,
    user: null,
    threshold: null,
    order: 'desc'
};

const el = (id) => document.getElementById(id);

async function api(path, opts = {}) {
    const headers = opts.headers ? { ...opts.headers } : {};
    if (state.token) headers['Authorization'] = `Bearer ${state.token}`;
    if (opts.body && !(opts.body instanceof FormData) && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }
    const res = await fetch(path, { ...opts, headers });
    if (res.status === 401) {
        logout();
        throw new Error('session expired — please sign in again');
    }
    const ct = res.headers.get('content-type') || '';
    const body = ct.includes('application/json') ? await res.json() : await res.text();
    if (!res.ok) {
        const message = (body && body.message) || (typeof body === 'string' ? body : res.statusText);
        throw new Error(message);
    }
    return body;
}

/* -------------------------------------------------------------- auth flow */

async function login(username, password) {
    const body = await api('/api/auth/token', {
        method: 'POST',
        body: JSON.stringify({ username, password })
    });
    state.token = body.accessToken;
    sessionStorage.setItem('jwt', state.token);
    await bootstrap();
}

function logout() {
    state.token = null;
    state.user = null;
    sessionStorage.removeItem('jwt');
    el('login-view').classList.remove('hidden');
    el('app-view').classList.add('hidden');
    el('user-bar').classList.add('hidden');
}

async function bootstrap() {
    try {
        const me = await api('/api/me');
        state.user = me;
        el('user-label').textContent = `${me.username} (${me.group}, ${me.roles.join('+')})`;
        el('user-bar').classList.remove('hidden');
        el('login-view').classList.add('hidden');
        el('app-view').classList.remove('hidden');
        // Hide the add card for users without MANAGER role.
        if (!me.roles.includes('MANAGER')) {
            el('add-card').classList.add('hidden');
            el('threshold-save').disabled = true;
        }
        await Promise.all([loadThreshold(), loadAlerts(), loadCertificates()]);
    } catch (e) {
        logout();
        el('login-error').textContent = e.message;
    }
}

/* ---------------------------------------------------------- data loaders */

async function loadThreshold() {
    const body = await api('/api/config/threshold');
    state.threshold = body.thresholdDays;
    el('threshold-input').value = body.thresholdDays;
}

async function loadAlerts() {
    const body = await api('/api/alerts');
    const list = el('alerts-list');
    list.innerHTML = '';
    el('alerts-summary').textContent =
        `${body.expiring.length} certificate(s) within ${body.thresholdDays} day(s) of expiry.`;
    for (const c of body.expiring) {
        const li = document.createElement('li');
        li.textContent = `${c.alias} — ${c.subject} (in ${c.daysUntilExpiry} day${c.daysUntilExpiry === 1 ? '' : 's'})`;
        list.appendChild(li);
    }
}

async function loadCertificates() {
    const rows = await api(`/api/certificates?order=${state.order}`);
    const tbody = el('cert-table').querySelector('tbody');
    tbody.innerHTML = '';
    for (const c of rows) {
        const tr = document.createElement('tr');
        const days = c.daysUntilExpiry;
        if (days < 0 || days <= (state.threshold || 30) / 3) tr.classList.add('alert');
        else if (days <= (state.threshold || 30)) tr.classList.add('warn');
        tr.innerHTML = `
          <td>${escape(c.alias)}</td>
          <td>${escape(c.subject)}</td>
          <td>${escape(c.issuer)}</td>
          <td>${new Date(c.notAfter).toLocaleString()}</td>
          <td>${daysBadge(days)}</td>
          <td>${c.source}<br/><small>${escape(c.sourceRef)}</small></td>
          <td>${state.user && state.user.roles.includes('MANAGER')
                ? `<button class="action-link" data-id="${c.id}">delete</button>`
                : ''}</td>`;
        tbody.appendChild(tr);
    }
    tbody.querySelectorAll('button.action-link').forEach(b => {
        b.addEventListener('click', async () => {
            if (!confirm('Delete this certificate?')) return;
            await api(`/api/certificates/${b.dataset.id}`, { method: 'DELETE' });
            await Promise.all([loadAlerts(), loadCertificates()]);
        });
    });
}

function daysBadge(days) {
    const cls = days < 0 ? 'days-critical' : days <= 7 ? 'days-critical' : days <= 30 ? 'days-warn' : 'days-ok';
    const text = days < 0 ? `expired (${Math.abs(days)}d ago)` : `${days}d`;
    return `<span class="days-badge ${cls}">${text}</span>`;
}

function escape(s) {
    return (s ?? '').toString()
        .replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;').replaceAll("'", '&#39;');
}

/* ----------------------------------------------------------------- wire-up */

el('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    el('login-error').textContent = '';
    try {
        await login(el('username').value.trim(), el('password').value);
    } catch (err) {
        el('login-error').textContent = err.message;
    }
});

el('logout-btn').addEventListener('click', logout);

el('threshold-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    try {
        const body = await api('/api/config/threshold', {
            method: 'PUT',
            body: JSON.stringify({ thresholdDays: parseInt(el('threshold-input').value, 10) })
        });
        state.threshold = body.thresholdDays;
        el('threshold-msg').textContent = `threshold updated to ${body.thresholdDays} days`;
        await Promise.all([loadAlerts(), loadCertificates()]);
    } catch (err) {
        el('threshold-msg').textContent = err.message;
    }
});

el('upload-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const file = el('file-input').files[0];
    if (!file) return;
    const fd = new FormData();
    fd.append('file', file);
    try {
        await api('/api/certificates/upload', { method: 'POST', body: fd });
        el('add-msg').textContent = `uploaded ${file.name}`;
        el('file-input').value = '';
        await Promise.all([loadAlerts(), loadCertificates()]);
    } catch (err) {
        el('add-msg').textContent = err.message;
    }
});

el('fetch-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const url = el('url-input').value.trim();
    try {
        await api('/api/certificates/fetch', {
            method: 'POST',
            body: JSON.stringify({ url })
        });
        el('add-msg').textContent = `fetched ${url}`;
        el('url-input').value = '';
        await Promise.all([loadAlerts(), loadCertificates()]);
    } catch (err) {
        el('add-msg').textContent = err.message;
    }
});

document.querySelectorAll('input[name=order]').forEach(r => {
    r.addEventListener('change', async (e) => {
        state.order = e.target.value;
        await loadCertificates();
    });
});

if (state.token) bootstrap();
