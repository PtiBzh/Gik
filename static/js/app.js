// ==================== CONFIGURATION ====================
const API_BASE = window.location.origin + '/api';

// ==================== UTILITIES ====================
function formatDate(dateString) {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR');
}

function showToast(message, type = 'success') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.className = 'toast';
    }, 3000);
}

function openModal(modalId) {
    document.getElementById(modalId).classList.add('active');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

function closeAllModals() {
    document.querySelectorAll('.modal').forEach(modal => {
        modal.classList.remove('active');
    });
}

// ==================== TAB MANAGEMENT ====================
function initTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    const tabContents = document.querySelectorAll('.tab-content');

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const tabId = button.getAttribute('data-tab');
            
            // Remove active class from all
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabContents.forEach(content => content.classList.remove('active'));
            
            // Add active class to selected
            button.classList.add('active');
            document.getElementById(tabId).classList.add('active');
            
            // Load data for the tab
            if (tabId === 'gilets') {
                loadGilets();
            } else if (tabId === 'tests') {
                loadTests();
            } else if (tabId === 'stats') {
                loadStats();
            }
        });
    });
}

// ==================== GILETS MANAGEMENT ====================
async function loadGilets() {
    const tableBody = document.getElementById('gilets-list');
    tableBody.innerHTML = '<tr class="loading"><td colspan="8"><span class="loading-spinner"></span> Chargement...</td></tr>';
    
    try {
        const response = await fetch(`${API_BASE}/gilets`);
        const data = await response.json();
        
        if (data.success && data.data) {
            displayGilets(data.data);
        } else {
            showToast(data.error || 'Erreur lors du chargement', 'error');
        }
    } catch (error) {
        showToast('Erreur réseau: ' + error.message, 'error');
    }
}

function displayGilets(gilets) {
    const tableBody = document.getElementById('gilets-list');
    
    if (gilets.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: #999;">Aucun gilet trouvé</td></tr>';
        return;
    }
    
    tableBody.innerHTML = gilets.map(gilet => `
        <tr>
            <td>${gilet.id}</td>
            <td><strong>${gilet.num_serie}</strong></td>
            <td>${gilet.modele}</td>
            <td>${gilet.taille}</td>
            <td>${formatDate(gilet.date_achat)}</td>
            <td><span class="status-${gilet.etat === 'Bon' ? 'ok' : gilet.etat === 'À réparer' || gilet.etat === 'Hors service' ? 'ko' : ''}">${gilet.etat}</span></td>
            <td>${gilet.dernier_test ? formatDate(gilet.dernier_test) : 'N/A'}</td>
            <td>
                <div class="action-btns">
                    <button class="btn btn-sm" onclick="viewGiletTests(${gilet.id})">📊 Voir tests</button>
                    <button class="btn btn-sm" onclick="editGilet(${gilet.id})">✏️ Modifier</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteGilet(${gilet.id})">🗑️ Supprimer</button>
                </div>
            </td>
        </tr>
    `).join('');
}

function editGilet(id) {
    // Fetch gilet data
    fetch(`${API_BASE}/gilets/${id}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                const gilet = data.data;
                
                // Fill form
                document.getElementById('gilet-id').value = gilet.id;
                document.getElementById('num_serie').value = gilet.num_serie;
                document.getElementById('modele').value = gilet.modele;
                document.getElementById('taille').value = gilet.taille;
                document.getElementById('date_achat').value = gilet.date_achat;
                document.getElementById('etat').value = gilet.etat;
                document.getElementById('dernier_test').value = gilet.dernier_test || '';
                
                // Change modal title
                document.getElementById('gilet-modal-title').textContent = 'Modifier le Gilet';
                
                // Open modal
                openModal('gilet-modal');
            } else {
                showToast(data.error || 'Erreur lors du chargement', 'error');
            }
        })
        .catch(error => {
            showToast('Erreur réseau: ' + error.message, 'error');
        });
}

function deleteGilet(id) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce gilet ?')) {
        fetch(`${API_BASE}/gilets/${id}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast('Gilet supprimé avec succès');
                loadGilets();
            } else {
                showToast(data.error || 'Erreur lors de la suppression', 'error');
            }
        })
        .catch(error => {
            showToast('Erreur réseau: ' + error.message, 'error');
        });
    }
}

function viewGiletTests(giletId) {
    // Store current gilet ID for test creation
    localStorage.setItem('currentGiletId', giletId);
    
    // Load tests for this gilet
    loadTestsForGilet(giletId);
    
    // Switch to tests tab
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    
    document.querySelector('.tab-btn[data-tab="tests"]').classList.add('active');
    document.getElementById('tests').classList.add('active');
}

async function loadTestsForGilet(giletId) {
    const tableBody = document.getElementById('tests-list');
    tableBody.innerHTML = '<tr class="loading"><td colspan="7"><span class="loading-spinner"></span> Chargement...</td></tr>';
    
    try {
        const response = await fetch(`${API_BASE}/tests/by-gilet/${giletId}`);
        const data = await response.json();
        
        if (data.success && data.data) {
            displayTests(data.data);
        } else {
            showToast(data.error || 'Erreur lors du chargement', 'error');
        }
    } catch (error) {
        showToast('Erreur réseau: ' + error.message, 'error');
    }
}

// ==================== TESTS MANAGEMENT ====================
async function loadTests() {
    const tableBody = document.getElementById('tests-list');
    tableBody.innerHTML = '<tr class="loading"><td colspan="7"><span class="loading-spinner"></span> Chargement...</td></tr>';
    
    try {
        const response = await fetch(`${API_BASE}/tests`);
        const data = await response.json();
        
        if (data.success && data.data) {
            displayTests(data.data);
        } else {
            showToast(data.error || 'Erreur lors du chargement', 'error');
        }
    } catch (error) {
        showToast('Erreur réseau: ' + error.message, 'error');
    }
}

function displayTests(tests) {
    const tableBody = document.getElementById('tests-list');
    
    if (tests.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: #999;">Aucun test trouvé</td></tr>';
        return;
    }
    
    // Get all gilets for display
    fetch(`${API_BASE}/gilets`)
        .then(response => response.json())
        .then(giletsData => {
            if (giletsData.success && giletsData.data) {
                const giletsMap = {};
                giletsData.data.forEach(g => {
                    giletsMap[g.id] = g;
                });
                
                tableBody.innerHTML = tests.map(test => {
                    const gilet = giletsMap[test.gilet_id];
                    return `
                        <tr>
                            <td>${test.id}</td>
                            <td>${gilet ? `${gilet.num_serie} (${gilet.modele})` : 'N/A'}</td>
                            <td>${formatDate(test.date_test)}</td>
                            <td><span class="status-${test.resultat === 'OK' ? 'ok' : 'ko'}">${test.resultat}</span></td>
                            <td>${test.responsable}</td>
                            <td>${test.commentaire || 'N/A'}</td>
                            <td>
                                <div class="action-btns">
                                    <button class="btn btn-sm" onclick="editTest(${test.id})">✏️ Modifier</button>
                                    <button class="btn btn-sm btn-danger" onclick="deleteTest(${test.id})">🗑️ Supprimer</button>
                                </div>
                            </td>
                        </tr>
                    `;
                }).join('');
            }
        });
}

function editTest(id) {
    fetch(`${API_BASE}/tests/${id}`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                const test = data.data;
                
                // Fill form
                document.getElementById('test-id').value = test.id;
                document.getElementById('test-gilet-id').value = test.gilet_id;
                document.getElementById('date_test').value = test.date_test;
                document.getElementById('resultat').value = test.resultat;
                document.getElementById('responsable').value = test.responsable;
                document.getElementById('commentaire').value = test.commentaire || '';
                
                // Change modal title
                document.getElementById('test-modal-title').textContent = 'Modifier le Test';
                
                // Open modal
                openModal('test-modal');
            } else {
                showToast(data.error || 'Erreur lors du chargement', 'error');
            }
        })
        .catch(error => {
            showToast('Erreur réseau: ' + error.message, 'error');
        });
}

function deleteTest(id) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce test ?')) {
        fetch(`${API_BASE}/tests/${id}`, {
            method: 'DELETE'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showToast('Test supprimé avec succès');
                loadTests();
            } else {
                showToast(data.error || 'Erreur lors de la suppression', 'error');
            }
        })
        .catch(error => {
            showToast('Erreur réseau: ' + error.message, 'error');
        });
    }
}

// ==================== STATS MANAGEMENT ====================
async function loadStats() {
    try {
        // Load all data
        const [giletsResponse, testsResponse] = await Promise.all([
            fetch(`${API_BASE}/gilets`),
            fetch(`${API_BASE}/tests`)
        ]);
        
        const giletsData = await giletsResponse.json();
        const testsData = await testsResponse.json();
        
        if (giletsData.success && testsData.success) {
            displayStats(giletsData.data || [], testsData.data || []);
        }
    } catch (error) {
        showToast('Erreur réseau: ' + error.message, 'error');
    }
}

function displayStats(gilets, tests) {
    const totalGilets = gilets.length;
    const totalTests = tests.length;
    
    // Count OK/KO tests
    const okTests = tests.filter(t => t.resultat === 'OK').length;
    const koTests = tests.filter(t => t.resultat === 'KO').length;
    
    // Calculate success rate
    const successRate = totalTests > 0 ? ((okTests / totalTests) * 100).toFixed(1) + '%' : '0%';
    
    // Count gilets with recent tests (within 12 months)
    const now = new Date();
    const oneYearAgo = new Date(now.getFullYear() - 1, now.getMonth(), now.getDate());
    const giletsWithRecentTests = gilets.filter(g => {
        if (!g.dernier_test) return false;
        const lastTestDate = new Date(g.dernier_test);
        return lastTestDate >= oneYearAgo;
    }).length;
    
    const testsToDo = totalGilets - giletsWithRecentTests;
    
    // Count gilets by status
    const giletsOk = gilets.filter(g => {
        const giletTests = tests.filter(t => t.gilet_id === g.id && t.resultat === 'KO');
        return giletTests.length === 0;
    }).length;
    
    const giletsKo = totalGilets - giletsOk;
    
    document.getElementById('total-gilets').textContent = totalGilets;
    document.getElementById('gilets-ok').textContent = giletsOk;
    document.getElementById('gilets-ko').textContent = giletsKo;
    document.getElementById('total-tests').textContent = totalTests;
    document.getElementById('success-rate').textContent = successRate;
    document.getElementById('tests-to-do').textContent = testsToDo;
}

// ==================== FORM HANDLERS ====================
function initForms() {
    // Gilet form
    document.getElementById('gilet-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const id = document.getElementById('gilet-id').value;
        const gilet = {
            num_serie: document.getElementById('num_serie').value,
            modele: document.getElementById('modele').value,
            taille: document.getElementById('taille').value,
            date_achat: document.getElementById('date_achat').value,
            etat: document.getElementById('etat').value,
            dernier_test: document.getElementById('dernier_test').value || null
        };
        
        try {
            let response;
            if (id) {
                // Update
                response = await fetch(`${API_BASE}/gilets/${id}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(gilet)
                });
            } else {
                // Create
                response = await fetch(`${API_BASE}/gilets`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(gilet)
                });
            }
            
            const data = await response.json();
            if (data.success) {
                showToast(id ? 'Gilet modifié avec succès' : 'Gilet ajouté avec succès');
                closeModal('gilet-modal');
                loadGilets();
                
                // Reset form
                document.getElementById('gilet-form').reset();
                document.getElementById('gilet-id').value = '';
                document.getElementById('gilet-modal-title').textContent = 'Ajouter un Gilet';
            } else {
                showToast(data.error || 'Erreur lors de l\'enregistrement', 'error');
            }
        } catch (error) {
            showToast('Erreur réseau: ' + error.message, 'error');
        }
    });
    
    // Test form
    document.getElementById('test-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        
        const id = document.getElementById('test-id').value;
        const test = {
            gilet_id: parseInt(document.getElementById('test-gilet-id').value),
            date_test: document.getElementById('date_test').value,
            resultat: document.getElementById('resultat').value,
            responsable: document.getElementById('responsable').value,
            commentaire: document.getElementById('commentaire').value || null
        };
        
        try {
            let response;
            if (id) {
                // Update
                response = await fetch(`${API_BASE}/tests/${id}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(test)
                });
            } else {
                // Create
                response = await fetch(`${API_BASE}/tests`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(test)
                });
            }
            
            const data = await response.json();
            if (data.success) {
                showToast(id ? 'Test modifié avec succès' : 'Test ajouté avec succès');
                closeModal('test-modal');
                loadTests();
                
                // Reset form
                document.getElementById('test-form').reset();
                document.getElementById('test-id').value = '';
                document.getElementById('test-modal-title').textContent = 'Ajouter un Test';
            } else {
                showToast(data.error || 'Erreur lors de l\'enregistrement', 'error');
            }
        } catch (error) {
            showToast('Erreur réseau: ' + error.message, 'error');
        }
    });
}

// ==================== MODAL BUTTONS ====================
function initModalButtons() {
    // Close buttons
    document.querySelectorAll('.close-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            closeAllModals();
        });
    });
    
    // Click outside to close
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                closeAllModals();
            }
        });
    });
    
    // Add Gilet button
    document.getElementById('add-gilet-btn').addEventListener('click', () => {
        document.getElementById('gilet-modal-title').textContent = 'Ajouter un Gilet';
        document.getElementById('gilet-form').reset();
        document.getElementById('gilet-id').value = '';
        openModal('gilet-modal');
    });
    
    // Add Test button
    document.getElementById('add-test-btn').addEventListener('click', () => {
        document.getElementById('test-modal-title').textContent = 'Ajouter un Test';
        document.getElementById('test-form').reset();
        document.getElementById('test-id').value = '';
        
        // Load gilets into select
        loadGiletsIntoSelect();
        
        openModal('test-modal');
    });
    
    // Search Gilet button
    document.getElementById('search-gilet-btn').addEventListener('click', () => {
        const field = document.getElementById('gilet-search-field').value;
        const value = document.getElementById('gilet-search').value;
        
        if (value) {
            fetch(`${API_BASE}/gilets/search?field=${field}&value=${encodeURIComponent(value)}`)
                .then(response => response.json())
                .then(data => {
                    if (data.success && data.data) {
                        displayGilets(data.data);
                    } else {
                        showToast(data.error || 'Aucun résultat trouvé', 'error');
                    }
                })
                .catch(error => {
                    showToast('Erreur réseau: ' + error.message, 'error');
                });
        } else {
            loadGilets();
        }
    });
    
    // Search on Enter key
    document.getElementById('gilet-search').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            document.getElementById('search-gilet-btn').click();
        }
    });
}

function loadGiletsIntoSelect() {
    const select = document.getElementById('test-gilet-id');
    select.innerHTML = '<option value="">Sélectionnez un gilet</option>';
    
    fetch(`${API_BASE}/gilets`)
        .then(response => response.json())
        .then(data => {
            if (data.success && data.data) {
                data.data.forEach(gilet => {
                    const option = document.createElement('option');
                    option.value = gilet.id;
                    option.textContent = `${gilet.num_serie} - ${gilet.modele} (${gilet.taille})`;
                    select.appendChild(option);
                });
            }
        });
}

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initForms();
    initModalButtons();
    
    // Load initial data
    loadGilets();
    
    // Set today's date as default for date inputs
    const today = new Date().toISOString().split('T')[0];
    document.querySelectorAll('input[type="date"]').forEach(input => {
        if (!input.value) {
            input.value = today;
        }
    });
});
