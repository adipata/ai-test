document.addEventListener('DOMContentLoaded', function() {
    const API_BASE = '/api';
    let allGroups = [];

    // Initialize the application
    init();

    async function init() {
        await loadGroups();
        await loadUsers();
        await loadGroupsTable();
        setupEventListeners();
    }

    async function loadGroups() {
        try {
            // Simulate loading groups - in a real app this would be an API call
            allGroups = [
                { id: 1, name: 'Development', description: 'Development team certificates' },
                { id: 2, name: 'Production', description: 'Production environment certificates' },
                { id: 3, name: 'Security', description: 'Security team certificates' }
            ];
            
            populateGroupSelects();
        } catch (error) {
            console.error('Failed to load groups:', error);
        }
    }

    function populateGroupSelects() {
        const selects = ['newUserGroup', 'alertGroupSelect'];
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

    async function loadUsers() {
        try {
            const tableBody = document.getElementById('usersTableBody');
            tableBody.innerHTML = '<tr><td colspan="4" class="text-center">Loading users...</td></tr>';

            // Simulate API call
            const mockUsers = [
                { id: 1, username: 'admin', role: 'ADMIN', enabled: true },
                { id: 2, username: 'manager1', role: 'MANAGER', enabled: true },
                { id: 3, username: 'user1', role: 'USER', enabled: true },
                { id: 4, username: 'disabled_user', role: 'USER', enabled: false }
            ];

            renderUsers(mockUsers);
        } catch (error) {
            console.error('Failed to load users:', error);
            const tableBody = document.getElementById('usersTableBody');
            tableBody.innerHTML = '<tr><td colspan="4" class="text-center text-danger">Failed to load users</td></tr>';
        }
    }

    function renderUsers(users) {
        const tableBody = document.getElementById('usersTableBody');
        tableBody.innerHTML = '';

        if (users.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="4" class="text-center">No users found</td></tr>';
            return;
        }

        users.forEach(user => {
            const row = document.createElement('tr');
            
            const statusBadge = user.enabled 
                ? '<span class="badge bg-success">Active</span>'
                : '<span class="badge bg-secondary">Disabled</span>';

            row.innerHTML = `
                <td>${user.username}</td>
                <td>${user.role}</td>
                <td>${statusBadge}</td>
                <td>
                    <button class="btn btn-sm btn-outline-info edit-user-btn" data-id="${user.id}">Edit</button>
                    <button class="btn btn-sm btn-outline-danger delete-user-btn" data-id="${user.id}">Delete</button>
                </td>
            `;
            
            tableBody.appendChild(row);
        });

        // Add event listeners to action buttons
        document.querySelectorAll('.edit-user-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const userId = this.getAttribute('data-id');
                editUser(userId);
            });
        });

        document.querySelectorAll('.delete-user-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const userId = this.getAttribute('data-id');
                deleteUser(userId);
            });
        });
    }

    async function loadGroupsTable() {
        try {
            const tableBody = document.getElementById('groupsTableBody');
            tableBody.innerHTML = '<tr><td colspan="3" class="text-center">Loading groups...</td></tr>';

            // Use the already loaded groups
            renderGroups(allGroups);
        } catch (error) {
            console.error('Failed to load groups:', error);
            const tableBody = document.getElementById('groupsTableBody');
            tableBody.innerHTML = '<tr><td colspan="3" class="text-center text-danger">Failed to load groups</td></tr>';
        }
    }

    function renderGroups(groups) {
        const tableBody = document.getElementById('groupsTableBody');
        tableBody.innerHTML = '';

        if (groups.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="3" class="text-center">No groups found</td></tr>';
            return;
        }

        groups.forEach(group => {
            const row = document.createElement('tr');
            
            row.innerHTML = `
                <td>${group.name}</td>
                <td>${group.description}</td>
                <td>
                    <button class="btn btn-sm btn-outline-info edit-group-btn" data-id="${group.id}">Edit</button>
                    <button class="btn btn-sm btn-outline-danger delete-group-btn" data-id="${group.id}">Delete</button>
                </td>
            `;
            
            tableBody.appendChild(row);
        });

        // Add event listeners to action buttons
        document.querySelectorAll('.edit-group-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const groupId = this.getAttribute('data-id');
                editGroup(groupId);
            });
        });

        document.querySelectorAll('.delete-group-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const groupId = this.getAttribute('data-id');
                deleteGroup(groupId);
            });
        });
    }

    function editUser(userId) {
        alert(`Edit user with ID: ${userId}`);
        // In a real app, this would open a modal with user details
    }

    function deleteUser(userId) {
        if (confirm('Are you sure you want to delete this user?')) {
            alert(`User ${userId} deleted`);
            // In a real app, this would make an API call to delete the user
            loadUsers();
        }
    }

    function editGroup(groupId) {
        alert(`Edit group with ID: ${groupId}`);
        // In a real app, this would open a modal with group details
    }

    function deleteGroup(groupId) {
        if (confirm('Are you sure you want to delete this group?')) {
            alert(`Group ${groupId} deleted`);
            // In a real app, this would make an API call to delete the group
            loadGroupsTable();
        }
    }

    function setupEventListeners() {
        // Save user button
        document.getElementById('saveUserBtn').addEventListener('click', async function() {
            await saveUser();
        });

        // Save group button
        document.getElementById('saveGroupBtn').addEventListener('click', async function() {
            await saveGroup();
        });

        // Save alert configuration button
        document.getElementById('saveAlertConfigBtn').addEventListener('click', async function() {
            await saveAlertConfiguration();
        });
    }

    async function saveUser() {
        const username = document.getElementById('newUsername').value;
        const password = document.getElementById('newPassword').value;
        const groupId = document.getElementById('newUserGroup').value;
        const role = document.getElementById('newUserRole').value;
        const enabled = document.getElementById('newUserEnabled').checked;

        if (!username || !password || !groupId) {
            alert('Please fill in all required fields');
            return;
        }

        try {
            // In a real app:
            // const response = await fetch(`${API_BASE}/users`, {
            //     method: 'POST',
            //     headers: {
            //         'Content-Type': 'application/json',
            //         'Authorization': `Bearer ${getAuthToken()}`
            //     },
            //     body: JSON.stringify({
            //         username,
            //         password,
            //         groupId,
            //         role,
            //         enabled
            //     })
            // });

            alert('User created successfully (simulated)');
            
            // Reset form and close modal
            document.getElementById('addUserForm').reset();
            const modal = bootstrap.Modal.getInstance(document.getElementById('addUserModal'));
            modal.hide();
            
            await loadUsers();
        } catch (error) {
            console.error('Failed to create user:', error);
            alert('Failed to create user');
        }
    }

    async function saveGroup() {
        const name = document.getElementById('newGroupName').value;
        const description = document.getElementById('newGroupDescription').value;

        if (!name || !description) {
            alert('Please fill in all required fields');
            return;
        }

        try {
            // In a real app:
            // const response = await fetch(`${API_BASE}/groups`, {
            //     method: 'POST',
            //     headers: {
            //         'Content-Type': 'application/json',
            //         'Authorization': `Bearer ${getAuthToken()}`
            //     },
            //     body: JSON.stringify({ name, description })
            // });

            alert('Group created successfully (simulated)');
            
            // Reset form and close modal
            document.getElementById('addGroupForm').reset();
            const modal = bootstrap.Modal.getInstance(document.getElementById('addGroupModal'));
            modal.hide();
            
            await loadGroups();
            await loadGroupsTable();
        } catch (error) {
            console.error('Failed to create group:', error);
            alert('Failed to create group');
        }
    }

    async function saveAlertConfiguration() {
        const groupId = document.getElementById('alertGroupSelect').value;
        const thresholdDays = document.getElementById('thresholdDays').value;
        const emailEnabled = document.getElementById('emailEnabled').checked;
        const emailRecipients = document.getElementById('emailRecipients').value;

        if (!groupId) {
            alert('Please select a group');
            return;
        }

        try {
            // In a real app:
            // const response = await fetch(`${API_BASE}/alerts/config?groupId=${groupId}&thresholdDays=${thresholdDays}&emailEnabled=${emailEnabled}&emailRecipients=${emailRecipients}`, {
            //     method: 'PUT',
            //     headers: {
            //         'Authorization': `Bearer ${getAuthToken()}`
            //     }
            // });

            alert('Alert configuration saved successfully (simulated)');
        } catch (error) {
            console.error('Failed to save alert configuration:', error);
            alert('Failed to save alert configuration');
        }
    }

    function getAuthToken() {
        // In a real app, this would get the JWT token from storage
        return localStorage.getItem('authToken') || '';
    }
});