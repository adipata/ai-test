document.addEventListener('DOMContentLoaded', function() {
    const API_BASE = '/api';

    // Initialize the application
    init();

    async function init() {
        await loadAlerts();
        setupEventListeners();
    }

    async function loadAlerts(showCriticalOnly = false) {
        try {
            const alertsContainer = document.getElementById('alertsContainer');
            alertsContainer.innerHTML = '<div class="alert alert-info">Loading alerts...</div>';

            // Simulate API call - in a real app this would be:
            // const response = await fetch(`${API_BASE}/certificates/expiring?threshold=30`);
            // const certificates = await response.json();
            
            const mockAlerts = [
                {
                    id: 1,
                    certificateName: 'secure.example.com',
                    groupName: 'Security',
                    expiryDate: '2024-05-05',
                    daysUntilExpiry: 3,
                    severity: 'CRITICAL'
                },
                {
                    id: 2,
                    certificateName: 'api.example.com',
                    groupName: 'Development',
                    expiryDate: '2024-05-15',
                    daysUntilExpiry: 13,
                    severity: 'WARNING'
                },
                {
                    id: 3,
                    certificateName: 'legacy.example.com',
                    groupName: 'Production',
                    expiryDate: '2024-06-01',
                    daysUntilExpiry: 30,
                    severity: 'WARNING'
                },
                {
                    id: 4,
                    certificateName: 'example.com',
                    groupName: 'Production',
                    expiryDate: '2024-12-31',
                    daysUntilExpiry: 200,
                    severity: 'INFO'
                }
            ];

            const filteredAlerts = showCriticalOnly 
                ? mockAlerts.filter(alert => alert.severity === 'CRITICAL')
                : mockAlerts.filter(alert => alert.severity !== 'INFO');

            renderAlerts(filteredAlerts);
            updateAlertCounts(mockAlerts);
        } catch (error) {
            console.error('Failed to load alerts:', error);
            const alertsContainer = document.getElementById('alertsContainer');
            alertsContainer.innerHTML = '<div class="alert alert-danger">Failed to load alerts</div>';
        }
    }

    function renderAlerts(alerts) {
        const alertsContainer = document.getElementById('alertsContainer');
        alertsContainer.innerHTML = '';

        if (alerts.length === 0) {
            alertsContainer.innerHTML = '<div class="alert alert-success">No active alerts</div>';
            return;
        }

        alerts.forEach(alert => {
            const alertElement = document.createElement('div');
            alertElement.className = 'card alert-card';
            
            // Add severity-based styling
            if (alert.severity === 'CRITICAL') {
                alertElement.classList.add('alert-critical');
            } else if (alert.severity === 'WARNING') {
                alertElement.classList.add('alert-warning');
            } else {
                alertElement.classList.add('alert-info');
            }

            const severityBadge = createSeverityBadge(alert.severity);

            alertElement.innerHTML = `
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-start">
                        <div class="flex-grow-1">
                            <h5 class="card-title">${alert.certificateName}</h5>
                            <h6 class="card-subtitle mb-2 text-muted">${alert.groupName}</h6>
                        </div>
                        <div class="ms-3">
                            ${severityBadge.outerHTML}
                        </div>
                    </div>
                    
                    <div class="row mt-3">
                        <div class="col-md-6">
                            <p class="card-text mb-1"><strong>Expiry Date:</strong> ${alert.expiryDate}</p>
                            <p class="card-text mb-1"><strong>Days Until Expiry:</strong> ${alert.daysUntilExpiry}</p>
                        </div>
                        <div class="col-md-6 text-end">
                            <button class="btn btn-sm btn-outline-info view-cert-btn" data-id="${alert.id}">View Certificate</button>
                            <button class="btn btn-sm btn-outline-secondary acknowledge-btn" data-id="${alert.id}">Acknowledge</button>
                        </div>
                    </div>
                </div>
            `;

            alertsContainer.appendChild(alertElement);
        });

        // Add event listeners to buttons
        document.querySelectorAll('.view-cert-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const certId = this.getAttribute('data-id');
                viewCertificate(certId);
            });
        });

        document.querySelectorAll('.acknowledge-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const alertId = this.getAttribute('data-id');
                acknowledgeAlert(alertId);
            });
        });
    }

    function createSeverityBadge(severity) {
        const span = document.createElement('span');
        span.className = 'badge';
        
        switch(severity) {
            case 'CRITICAL':
                span.classList.add('bg-danger');
                span.textContent = 'CRITICAL';
                break;
            case 'WARNING':
                span.classList.add('bg-warning');
                span.textContent = 'WARNING';
                break;
            default:
                span.classList.add('bg-info');
                span.textContent = 'INFO';
        }
        
        return span;
    }

    function updateAlertCounts(alerts) {
        const criticalCount = alerts.filter(a => a.daysUntilExpiry <= 7).length;
        const warningCount = alerts.filter(a => a.daysUntilExpiry > 7 && a.daysUntilExpiry <= 30).length;
        const monitoredCount = alerts.length;

        document.getElementById('criticalAlertCount').textContent = criticalCount;
        document.getElementById('warningAlertCount').textContent = warningCount;
        document.getElementById('monitoredCertCount').textContent = monitoredCount;
    }

    function viewCertificate(certId) {
        alert(`Viewing certificate with ID: ${certId}`);
        // In a real app, this would navigate to the certificate details or open a modal
    }

    function acknowledgeAlert(alertId) {
        if (confirm('Acknowledge this alert?')) {
            alert(`Alert ${alertId} acknowledged`);
            // In a real app, this would make an API call to acknowledge the alert
            loadAlerts();
        }
    }

    function setupEventListeners() {
        // Show all alerts
        document.getElementById('showAllAlertsBtn').addEventListener('click', async function() {
            await loadAlerts(false);
        });

        // Show critical alerts only
        document.getElementById('showCriticalAlertsBtn').addEventListener('click', async function() {
            await loadAlerts(true);
        });
    }

    function getAuthToken() {
        // In a real app, this would get the JWT token from storage
        return localStorage.getItem('authToken') || '';
    }
});