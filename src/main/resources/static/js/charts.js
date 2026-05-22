/* ── Chart.js initialization ── */
/* Data is injected via Thymeleaf in the page's page-scripts fragment */
function initCharts(data) {
  if (typeof Chart === 'undefined') {
    console.error('Chart.js is not loaded. Charts will not render.');
    return;
  }

  var statusColors = {
    BROUILLON: '#78909c',
    SOUMISE: '#1976d2',
    EN_VALIDATION: '#f57c00',
    APPROUVEE: '#2e7d32',
    REJETEE: '#d32f2f',
    PUBLIEE: '#7b1fa2'
  };

  var typeColors = {
    ARTICLE_SCIENTIFIQUE: '#1976d2',
    COMMUNICATION_CONFERENCE: '#f57c00',
    RAPPORT_RECHERCHE: '#78909c',
    THESE_MEMOIRE: '#7b1fa2',
    OUVRAGE: '#2e7d32'
  };

  // Publications by type (doughnut)
  if (data.byType && document.getElementById('chartType')) {
    new Chart(document.getElementById('chartType'), {
      type: 'doughnut',
      data: {
        labels: Object.keys(data.byType),
        datasets: [{
          data: Object.values(data.byType),
          backgroundColor: Object.keys(data.byType).map(function (k) { return typeColors[k] || '#78909c'; })
        }]
      },
      options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
    });
  }

  // Publications by category (pie)
  if (data.byCategory && document.getElementById('chartCategory')) {
    new Chart(document.getElementById('chartCategory'), {
      type: 'pie',
      data: {
        labels: Object.keys(data.byCategory),
        datasets: [{
          data: Object.values(data.byCategory),
          backgroundColor: ['#1976d2', '#2e7d32', '#f57c00', '#7b1fa2', '#d32f2f', '#78909c', '#00897b', '#5c6bc0']
        }]
      },
      options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
    });
  }

  // Publications by year (bar)
  if (data.byYear && document.getElementById('chartYear')) {
    new Chart(document.getElementById('chartYear'), {
      type: 'bar',
      data: {
        labels: Object.keys(data.byYear),
        datasets: [{
          label: 'Publications',
          data: Object.values(data.byYear),
          backgroundColor: '#1e3a5f'
        }]
      },
      options: { responsive: true, scales: { y: { beginAtZero: true } } }
    });
  }

  // Publications by status (doughnut)
  if (data.byStatus && document.getElementById('chartStatus')) {
    new Chart(document.getElementById('chartStatus'), {
      type: 'doughnut',
      data: {
        labels: Object.keys(data.byStatus),
        datasets: [{
          data: Object.values(data.byStatus),
          backgroundColor: Object.keys(data.byStatus).map(function (k) { return statusColors[k] || '#78909c'; })
        }]
      },
      options: { responsive: true, plugins: { legend: { position: 'bottom' } } }
    });
  }

  // Publications by researcher (horizontal bar)
  if (data.byResearcher && document.getElementById('chartResearcher')) {
    new Chart(document.getElementById('chartResearcher'), {
      type: 'bar',
      data: {
        labels: Object.keys(data.byResearcher),
        datasets: [{
          label: 'Publications',
          data: Object.values(data.byResearcher),
          backgroundColor: '#2d6a4f'
        }]
      },
      options: { indexAxis: 'y', responsive: true, scales: { x: { beginAtZero: true } } }
    });
  }
}