// Mapeamento dos dias da semana
const dayNames = {
    monday: 'Segunda-feira',
    tuesday: 'Terça-feira',
    wednesday: 'Quarta-feira',
    thursday: 'Quinta-feira',
    friday: 'Sexta-feira',
    saturday: 'Sábado',
    sunday: 'Domingo'
};

// API base URL
const API_BASE = '/api';

// Variáveis globais
let currentDayOfWeek = '';
let routines = [];
let editingRoutineId = null;

// Elementos do DOM
const dayTitle = document.getElementById('day-title');
const routinesContainer = document.getElementById('routines-container');
const emptyState = document.getElementById('empty-state');
const addRoutineBtn = document.getElementById('add-routine-btn');
const routineModal = document.getElementById('routine-modal');
const deleteModal = document.getElementById('delete-modal');
const routineForm = document.getElementById('routine-form');

// Função para obter o dia da semana da URL
function getDayFromURL() {
    const pathParts = window.location.pathname.split('/');
    return pathParts[pathParts.length - 1];
}

// Função para atualizar o título da página
function updatePageTitle() {
    const dayName = dayNames[currentDayOfWeek] || currentDayOfWeek;
    dayTitle.textContent = `Rotinas de ${dayName}`;
    document.title = `Rotinas de ${dayName}`;
}

// Função para carregar rotinas
async function loadRoutines() {
    try {
        const response = await fetch(`${API_BASE}/routines/${currentDayOfWeek}`);
        if (!response.ok) {
            throw new Error('Erro ao carregar rotinas');
        }
        
        routines = await response.json();
        renderRoutines();
    } catch (error) {
        console.error('Erro ao carregar rotinas:', error);
        showError('Erro ao carregar rotinas. Tente novamente.');
    }
}

// Função para renderizar rotinas
function renderRoutines() {
    if (routines.length === 0) {
        routinesContainer.style.display = 'none';
        emptyState.style.display = 'block';
        return;
    }

    routinesContainer.style.display = 'grid';
    emptyState.style.display = 'none';

    // Ordenar rotinas por horário de início
    const sortedRoutines = [...routines].sort((a, b) => a.startTime.localeCompare(b.startTime));

    routinesContainer.innerHTML = sortedRoutines.map(routine => `
        <div class="routine-card" data-id="${routine.id}">
            <div class="routine-header">
                <div>
                    <div class="routine-title">${routine.title}</div>
                    <div class="routine-time">${formatTime(routine.startTime)} - ${formatTime(routine.endTime)}</div>
                </div>
            </div>
            ${routine.description ? `<div class="routine-description">${routine.description}</div>` : ''}
            <div class="routine-actions">
                <button class="btn btn-primary btn-small" onclick="editRoutine(${routine.id})" title="Editar rotina">
                    <svg class="icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                        <path d="m18.5 2.5 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                    Editar
                </button>
                <button class="btn btn-danger btn-small" onclick="showDeleteConfirmation(${routine.id})" title="Excluir rotina">
                    <svg class="icon-sm" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                    </svg>
                    Excluir
                </button>
            </div>
        </div>
    `).join('');
}

// Função para formatar horário
function formatTime(time) {
    return time.substring(0, 5); // Remove segundos se houver
}

// Função para mostrar modal de adicionar rotina
function showAddRoutineModal() {
    editingRoutineId = null;
    document.getElementById('modal-title').textContent = 'Adicionar Rotina';
    routineForm.reset();
    routineModal.style.display = 'flex';
}

// Função para mostrar modal de editar rotina
async function editRoutine(id) {
    try {
        const response = await fetch(`${API_BASE}/routines/${currentDayOfWeek}/${id}`);
        if (!response.ok) {
            throw new Error('Erro ao carregar rotina');
        }
        
        const routine = await response.json();
        editingRoutineId = id;
        
        // Preencher formulário
        document.getElementById('modal-title').textContent = 'Editar Rotina';
        document.getElementById('routine-title').value = routine.title;
        document.getElementById('start-time').value = routine.startTime;
        document.getElementById('end-time').value = routine.endTime;
        document.getElementById('routine-description').value = routine.description || '';
        
        routineModal.style.display = 'flex';
    } catch (error) {
        console.error('Erro ao carregar rotina:', error);
        showError('Erro ao carregar rotina para edição.');
    }
}

// Função para salvar rotina
async function saveRoutine(formData) {
    try {
        const routineData = {
            dayOfWeek: currentDayOfWeek,
            title: formData.get('title'),
            startTime: formData.get('startTime'),
            endTime: formData.get('endTime'),
            description: formData.get('description')
        };

        // Validação básica
        if (!routineData.title || !routineData.startTime || !routineData.endTime) {
            throw new Error('Todos os campos obrigatórios devem ser preenchidos');
        }

        if (routineData.startTime >= routineData.endTime) {
            throw new Error('O horário de início deve ser anterior ao horário de fim');
        }

        const url = editingRoutineId 
            ? `${API_BASE}/routines/${editingRoutineId}`
            : `${API_BASE}/routines`;
        
        const method = editingRoutineId ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(routineData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.error || 'Erro ao salvar rotina');
        }

        closeModal();
        await loadRoutines();
        showSuccess(editingRoutineId ? 'Rotina atualizada com sucesso!' : 'Rotina criada com sucesso!');
        
    } catch (error) {
        console.error('Erro ao salvar rotina:', error);
        showError(error.message);
    }
}

// Função para mostrar confirmação de exclusão
function showDeleteConfirmation(id) {
    const routine = routines.find(r => r.id === id);
    if (!routine) return;

    document.querySelector('.delete-routine-info').textContent = 
        `${routine.title} (${formatTime(routine.startTime)} - ${formatTime(routine.endTime)})`;
    
    document.getElementById('confirm-delete-btn').onclick = () => deleteRoutine(id);
    deleteModal.style.display = 'flex';
}

// Função para excluir rotina
async function deleteRoutine(id) {
    try {
        const response = await fetch(`${API_BASE}/routines/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Erro ao excluir rotina');
        }

        closeDeleteModal();
        await loadRoutines();
        showSuccess('Rotina excluída com sucesso!');
        
    } catch (error) {
        console.error('Erro ao excluir rotina:', error);
        showError('Erro ao excluir rotina.');
    }
}

// Função para fechar modal
function closeModal() {
    routineModal.style.display = 'none';
    editingRoutineId = null;
}

// Função para fechar modal de exclusão
function closeDeleteModal() {
    deleteModal.style.display = 'none';
}

// Função para mostrar mensagem de sucesso
function showSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'success-message';
    successDiv.textContent = message;
    successDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #28a745;
        color: white;
        padding: 15px 20px;
        border-radius: 8px;
        z-index: 1001;
        animation: slideIn 0.3s ease;
    `;
    
    document.body.appendChild(successDiv);
    
    setTimeout(() => {
        successDiv.remove();
    }, 3000);
}

// Função para mostrar erro
function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error-message';
    errorDiv.textContent = message;
    errorDiv.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: #dc3545;
        color: white;
        padding: 15px 20px;
        border-radius: 8px;
        z-index: 1001;
        animation: slideIn 0.3s ease;
    `;
    
    document.body.appendChild(errorDiv);
    
    setTimeout(() => {
        errorDiv.remove();
    }, 5000);
}

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
    currentDayOfWeek = getDayFromURL();
    updatePageTitle();
    loadRoutines();

    // Event listeners para botões
    addRoutineBtn.addEventListener('click', showAddRoutineModal);
    
    // Event listeners para modais
    document.getElementById('close-modal').addEventListener('click', closeModal);
    document.getElementById('close-delete-modal').addEventListener('click', closeDeleteModal);
    document.getElementById('cancel-btn').addEventListener('click', closeModal);
    document.getElementById('cancel-delete-btn').addEventListener('click', closeDeleteModal);
    
    // Fechar modal ao clicar fora
    routineModal.addEventListener('click', (e) => {
        if (e.target === routineModal) closeModal();
    });
    
    deleteModal.addEventListener('click', (e) => {
        if (e.target === deleteModal) closeDeleteModal();
    });
    
    // Form submit
    routineForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const formData = new FormData(routineForm);
        saveRoutine(formData);
    });
});

// Adicionar estilos CSS para animações
const style = document.createElement('style');
style.textContent = `
    @keyframes slideIn {
        from {
            transform: translateX(100%);
            opacity: 0;
        }
        to {
            transform: translateX(0);
            opacity: 1;
        }
    }
    
    .success-message, .error-message {
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }
`;
document.head.appendChild(style); 