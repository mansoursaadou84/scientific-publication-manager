/* ── Sidebar toggle (mobile) ── */
(function () {
  var toggle = document.querySelector('.sidebar-toggle');
  var sidebar = document.querySelector('.sidebar');
  var overlay = document.querySelector('.sidebar-overlay');
  var closeBtn = document.querySelector('.sidebar-close');

  function openSidebar() {
    document.body.classList.add('sidebar-open');
    if (overlay) overlay.classList.add('show');
    if (toggle) toggle.setAttribute('aria-expanded', 'true');
    if (sidebar) {
      var firstLink = sidebar.querySelector('.sidebar-nav a');
      if (firstLink) firstLink.focus();
    }
  }

  function closeSidebar() {
    document.body.classList.remove('sidebar-open');
    if (overlay) overlay.classList.remove('show');
    if (toggle) {
      toggle.setAttribute('aria-expanded', 'false');
      toggle.focus();
    }
  }

  if (toggle) toggle.addEventListener('click', openSidebar);
  if (closeBtn) closeBtn.addEventListener('click', closeSidebar);
  if (overlay) overlay.addEventListener('click', closeSidebar);

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      if (document.body.classList.contains('sidebar-open')) closeSidebar();
      var modal = document.getElementById('rejectModal');
      if (modal && modal.classList.contains('show')) closeRejectModal();
    }
  });
})();

/* ── Rejection modal ── */
function openRejectModal(id, actionUrl) {
  var form = document.getElementById('rejectForm');
  if (form) form.action = actionUrl;
  var modal = document.getElementById('rejectModal');
  if (modal) {
    modal.classList.add('show');
    modal.setAttribute('aria-hidden', 'false');
    var textarea = modal.querySelector('textarea');
    if (textarea) textarea.focus();
  }
}

function closeRejectModal() {
  var modal = document.getElementById('rejectModal');
  if (modal) {
    modal.classList.remove('show');
    modal.setAttribute('aria-hidden', 'true');
  }
}