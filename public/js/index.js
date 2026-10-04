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

// Função para navegar para a página de rotinas
function navigateToDay(dayOfWeek) {
    window.location.href = `/day/${dayOfWeek}`;
}

// Função para adicionar eventos de clique aos cards
function addCardClickListeners() {
    const dayCards = document.querySelectorAll('.day-card');
    
    dayCards.forEach(card => {
        card.addEventListener('click', () => {
            const dayOfWeek = card.dataset.day;
            navigateToDay(dayOfWeek);
        });
        
        // Adicionar efeito de hover com teclado
        card.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                const dayOfWeek = card.dataset.day;
                navigateToDay(dayOfWeek);
            }
        });
        
        // Tornar os cards focáveis
        card.setAttribute('tabindex', '0');
    });
}

// Função para carregar contadores de rotinas (opcional)
async function loadRoutineCounts() {
    try {
        const dayCards = document.querySelectorAll('.day-card');
        
        for (const card of dayCards) {
            const dayOfWeek = card.dataset.day;
            const countElement = card.querySelector('.day-count');
            
            if (!dayOfWeek || !countElement) continue;
            
            try {
                const response = await fetch(`${API_BASE}/routines/${dayOfWeek}`);
                
                if (response.ok) {
                    const routines = await response.json();
                    const count = routines.length;
                    countElement.textContent = `${count} rotina${count !== 1 ? 's' : ''}`;
                    
                    // Adicionar classe visual baseada na quantidade
                    card.classList.remove('empty', 'few', 'many');
                    if (count === 0) {
                        card.classList.add('empty');
                    } else if (count <= 3) {
                        card.classList.add('few');
                    } else {
                        card.classList.add('many');
                    }
                }
                
            } catch (error) {
                console.warn(`Erro ao carregar rotinas para ${dayOfWeek}:`, error);
                // Manter o contador padrão
            }
        }
    } catch (error) {
        console.warn('Erro ao carregar contadores:', error);
    }
}

// Função principal de inicialização
async function init() {
    console.log('Inicializando aplicação...');
    
    // Verificar se os cards existem
    const dayCards = document.querySelectorAll('.day-card');
    if (dayCards.length === 0) {
        console.error('Cards dos dias não encontrados');
        return;
    }
    
    console.log(`Encontrados ${dayCards.length} cards de dias`);
    
    // Adicionar event listeners primeiro (para garantir que funcionem)
    addCardClickListeners();
    console.log('Event listeners adicionados');
    
    // Tentar carregar contadores em background (não bloquear)
    setTimeout(() => {
        loadRoutineCounts().then(() => {
            console.log('Contadores carregados com sucesso');
        }).catch(error => {
            console.warn('Erro ao carregar contadores:', error);
        });
    }, 100);
    
    console.log('Aplicação inicializada com sucesso');
}

// Adicionar estilos CSS dinâmicos para os estados dos cards
const style = document.createElement('style');
style.textContent = `
    .day-card.empty {
        opacity: 0.7;
    }
    
    .day-card.few {
        border-color: #28a745;
    }
    
    .day-card.many {
        border-color: #ffc107;
    }
    
    .error-message {
        text-align: center;
        color: white;
        padding: 40px;
    }
    
    .error-message h3 {
        margin-bottom: 15px;
        font-size: 1.5rem;
    }
    
    .error-message p {
        margin-bottom: 20px;
        opacity: 0.8;
    }
`;
document.head.appendChild(style);

// Inicializar quando o DOM estiver pronto
document.addEventListener('DOMContentLoaded', init); 