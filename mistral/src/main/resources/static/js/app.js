document.addEventListener('DOMContentLoaded', function() {
    const API_BASE = '/api';
    let currentUser = null;
    let allGroups = [];

    // Initialize the application
    init();

    async function init() {
        await loadUserInfo();
        await loadGroups();
        await loadCertificates();
        setupEventListeners();
    }

    async function loadUserInfo() {
        try {
            // In a real app, this would be a proper API call to get user info
            // For demo purposes, we'll simulate it
            const userInfo = {
                username: 'demo-user',
                roles: ['USER']
            };
            
            currentUser = userInfo;
            updateUserInfoDisplay();
        } catch (error) {
            console.error('Failed to load user info:', error);
        }
    }

    function updateUserInfoDisplay() {
        const userInfoElement = document.getElementById('userInfo');
        if (currentUser) {
            userInfoElement.textContent = `${currentUser.username} (${currentUser.roles.join(', ')})`;
        } else {
            userInfoElement.textContent = 'Not logged in';
        }
    }

    async function loadGroups() {
        try {
            // Simulate loading groups - in a real app this would be an API call
            allGroups = [
                { id: 1, name: 'Development' },
                { id: 2, name: 'Production' },
                { id: 3, name: 'Security' }
            ];
            
            populateGroupSelects();
        } catch (error) {
            console.error('Failed to load groups:', error);
        }
    }

    function populateGroupSelects() {
        const selects = ['groupSelect', 'fetchGroupSelect'];
        selects.forEach(selectId => {
            const select = document.getElementById(selectId);
            select.innerHTML = '<option value="">Select a group</option>';
            allGroups.forEach(group => {
                const option = document.createElement('option');
                option.value = group.id;
                option.textContent = group.name;
                select.appendChild(option);
            });
        });
    }

    async function loadCertificates(showExpiringOnly = false) {
        try {
            const tableBody = document.getElementById('certificatesTableBody');
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center">Loading certificates...</td></tr>';

            // Simulate API call - in a real app this would be:
            // const response = await fetch(`${API_BASE}/certificates?groupId=${selectedGroupId}`);
            // const certificates = await response.json();
            
            const mockCertificates = [
                {
                    id: 1,
                    name: 'example.com',
                    groupName: 'Production',
                    expiryDate: '2024-12-31',
                    daysUntilExpiry: 200,
                    isExpiringSoon: false
                },
                {
                    id: 2,
                    name: 'api.example.com',
                    groupName: 'Development',
                    expiryDate: '2024-06-15',
                    daysUntilExpiry: 30,
                    isExpiringSoon: true
                },
                {
                    id: 3,
                    name: 'secure.example.com',
                    groupName: 'Security',
                    expiryDate: '2024-05-01',
                    daysUntilExpiry: 5,
                    isExpiringSoon: true
                }
            ];

            const filteredCertificates = showExpiringOnly 
                ? mockCertificates.filter(cert => cert.isExpiringSoon)
                : mockCertificates;

            renderCertificates(filteredCertificates);
        } catch (error) {
            console.error('Failed to load certificates:', error);
            const tableBody = document.getElementById('certificatesTableBody');
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center text-danger">Failed to load certificates</td></tr>';
        }
    }

    function renderCertificates(certificates) {
        const tableBody = document.getElementById('certificatesTableBody');
        tableBody.innerHTML = '';

        if (certificates.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="6" class="text-center">No certificates found</td></tr>';
            return;
        }

        certificates.forEach(cert => {
            const row = document.createElement('tr');
            
            if (cert.isExpiringSoon) {
                row.classList.add('expiring-soon');
            }
            
            const daysLeft = cert.daysUntilExpiry;
            const status = daysLeft <= 0 ? 'Expired' : 
                          daysLeft <= 7 ? 'Critical' : 
                          daysLeft <= 30 ? 'Warning' : 'OK';
            
            const statusBadge = createStatusBadge(status);

            row.innerHTML = `
                <td>${cert.name}</td>
                <td>${cert.groupName}</td>
                <td>${cert.expiryDate}</td>
                <td>${daysLeft}</td>
                <td>${statusBadge.outerHTML}</td>
                <td>
                    <button class="btn btn-sm btn-outline-info view-btn" data-id="${cert.id}">View</button>
                </td>
            `;
            
            tableBody.appendChild(row);
        });

        // Add event listeners to view buttons
        document.querySelectorAll('.view-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const certId = this.getAttribute('data-id');
                viewCertificateDetails(certId);
            });
        });
    }

    function createStatusBadge(status) {
        const span = document.createElement('span');
        span.className = 'badge';
        
        switch(status) {
            case 'Expired':
                span.classList.add('bg-secondary');
                span.textContent = 'Expired';
                break;
            case 'Critical':
                span.classList.add('bg-danger');
                span.textContent = 'Critical';
                break;
            case 'Warning':
                span.classList.add('bg-warning');
                span.textContent = 'Warning';
                break;
            default:
                span.classList.add('bg-success');
                span.textContent = 'OK';
        }
        
        return span;
    }

    function viewCertificateDetails(certId) {
        // In a real app, this would fetch certificate details and show in a modal
        alert(`Viewing details for certificate ID: ${certId}`);
    }

    function setupEventListeners() {
        // Upload form
        document.getElementById('uploadForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            await handleCertificateUpload();
        });

        // Fetch form
        document.getElementById('fetchForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            await handleCertificateFetch();
        });

        // Show all certificates
        document.getElementById('showAllBtn').addEventListener('click', async function() {
            await loadCertificates(false);
        });

        // Show expiring certificates
        document.getElementById('showExpiringBtn').addEventListener('click', async function() {
            await loadCertificates(true);
        });
    }

    async function handleCertificateUpload() {
        const fileInput = document.getElementById('certFile');
        const groupSelect = document.getElementById('groupSelect');
        
        if (fileInput.files.length === 0) {
            alert('Please select a certificate file');
            return;
        }

        if (!groupSelect.value) {
            alert('Please select a group');
            return;
        }

        try {
            const formData = new FormData();
            formData.append('file', fileInput.files[0]);
            formData.append('groupId', groupSelect.value);

            // In a real app:
            // const response = await fetch(`${API_BASE}/certificates/upload`, {
            //     method: 'POST',
            //     body: formData,
            //     headers: {
            //         'Authorization': `Bearer ${getAuthToken()}`
            //     }
            // });
            
            alert('Certificate uploaded successfully (simulated)');
            await loadCertificates();
            
            // Reset form
            document.getElementById('uploadForm').reset();
        } catch (error) {
            console.error('Upload failed:', error);
            alert('Failed to upload certificate');
        }
    }

    async function handleCertificateFetch() {
        const urlInput = document.getElementById('certUrl');
        const groupSelect = document.getElementById('fetchGroupSelect');
        
        if (!urlInput.value) {
            alert('Please enter a URL');
            return;
        }

        if (!groupSelect.value) {
            alert('Please select a group');
            return;
        }

        try {
            // In a real app:
            // const response = await fetch(`${API_BASE}/certificates/fetch?url=${encodeURIComponent(urlInput.value)}&groupId=${groupSelect.value}`, {
            //     method: 'POST',
            //     headers: {
            //         'Authorization': `Bearer ${getAuthToken()}`
            //     }
            // });
            
            alert('Certificate fetched successfully (simulated)');
            await loadCertificates();
            
            // Reset form
            document.getElementById('fetchForm').reset();
        } catch (error) {
            console.error('Fetch failed:', error);
            alert('Failed to fetch certificate');
        }
    }

    function getAuthToken() {
        // In a real app, this would get the JWT token from storage
        return localStorage.getItem('authToken') || '';
    }
});