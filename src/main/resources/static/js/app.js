/**
 * ONLINE FOOD DELIVERY SYSTEM - CLIENT LOGIC & MICRO-INTERACTIONS
 * Features: Dark/Light Mode Engine, Metric Counter Animation, Live Table Search,
 *           Filter Chips, Quantity Steppers, Toast System, and Dynamic Order Pricing
 */

// Global Toast Engine
window.Toast = {
    container: null,
    init() {
        if (!this.container) {
            this.container = document.createElement('div');
            this.container.className = 'toast-container';
            document.body.appendChild(this.container);
        }
    },
    show(message, type = 'info', duration = 4000) {
        this.init();
        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        
        let icon = 'ℹ️';
        if (type === 'success') icon = '✓';
        if (type === 'error') icon = '⚠️';
        if (type === 'warning') icon = '⚡';

        toast.innerHTML = `
            <span style="font-weight: 700; font-size: 1.1rem;">${icon}</span>
            <span style="flex: 1;">${message}</span>
            <button type="button" style="background: none; border: none; cursor: pointer; color: var(--text-muted); font-size: 1.1rem; padding: 0 0.25rem;">&times;</button>
        `;

        const closeBtn = toast.querySelector('button');
        closeBtn.addEventListener('click', () => {
            this.dismiss(toast);
        });

        this.container.appendChild(toast);

        if (duration > 0) {
            setTimeout(() => {
                this.dismiss(toast);
            }, duration);
        }
    },
    success(msg) { this.show(msg, 'success'); },
    error(msg) { this.show(msg, 'error'); },
    info(msg) { this.show(msg, 'info'); },
    dismiss(toast) {
        toast.classList.add('fade-out');
        setTimeout(() => {
            if (toast.parentElement) {
                toast.parentElement.removeChild(toast);
            }
        }, 250);
    }
};

// Theme Engine (Light / Dark Mode)
const ThemeEngine = {
    STORAGE_KEY: 'foodflow_theme',

    init() {
        const savedTheme = localStorage.getItem(this.STORAGE_KEY);
        const systemPrefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        const activeTheme = savedTheme ? savedTheme : (systemPrefersDark ? 'dark' : 'light');
        this.apply(activeTheme);

        // Wire up all theme toggle buttons
        document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                this.toggle();
            });
        });

        // Listen for OS system theme changes
        if (window.matchMedia) {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', e => {
                if (!localStorage.getItem(this.STORAGE_KEY)) {
                    this.apply(e.matches ? 'dark' : 'light');
                }
            });
        }
    },

    apply(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem(this.STORAGE_KEY, theme);
        this.updateButtons(theme);
    },

    toggle() {
        const current = document.documentElement.getAttribute('data-theme') || 'light';
        const next = current === 'dark' ? 'light' : 'dark';
        this.apply(next);
        const icon = next === 'dark' ? '🌙' : '☀️';
        Toast.info(`${icon} Switched to ${next === 'dark' ? 'Dark' : 'Light'} Mode`);
    },

    updateButtons(theme) {
        document.querySelectorAll('.theme-toggle-btn').forEach(btn => {
            btn.setAttribute('aria-label', `Switch to ${theme === 'dark' ? 'Light' : 'Dark'} Mode`);
            btn.setAttribute('title', `Switch to ${theme === 'dark' ? 'Light' : 'Dark'} Mode`);
            if (theme === 'dark') {
                btn.innerHTML = `
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <circle cx="12" cy="12" r="5"></circle>
                        <line x1="12" y1="1" x2="12" y2="3"></line>
                        <line x1="12" y1="21" x2="12" y2="23"></line>
                        <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
                        <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
                        <line x1="1" y1="12" x2="3" y2="12"></line>
                        <line x1="21" y1="12" x2="23" y2="12"></line>
                        <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
                        <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
                    </svg>
                `;
            } else {
                btn.innerHTML = `
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
                    </svg>
                `;
            }
        });
    }
};

// Animated Number Counter on KPI Cards
function animateNumbers() {
    const cards = document.querySelectorAll('.metric-card .metric-value');
    cards.forEach(card => {
        const text = card.textContent.trim();
        const isLKR = text.includes('LKR');
        const numMatch = text.replace(/[^0-9.]/g, '');
        if (!numMatch) return;
        const target = parseFloat(numMatch);
        if (isNaN(target) || target <= 0) return;

        let start = 0;
        const duration = 900;
        const startTime = performance.now();

        function updateCounter(currentTime) {
            const elapsed = currentTime - startTime;
            const progress = Math.min(elapsed / duration, 1);
            // Ease-out cubic
            const easeProgress = 1 - Math.pow(1 - progress, 3);
            const currentVal = start + (target - start) * easeProgress;

            if (isLKR) {
                card.textContent = 'LKR ' + currentVal.toLocaleString('en-US', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                });
            } else {
                card.textContent = Math.round(currentVal).toString();
            }

            if (progress < 1) {
                requestAnimationFrame(updateCounter);
            }
        }
        requestAnimationFrame(updateCounter);
    });
}

// Live Table Synchronized Filter (Combines Filter Chips & Keystroke Search)
function applyTableFilter(table, explicitFilterVal) {
    if (!table) return;
    const tbody = table.querySelector('tbody');
    if (!tbody) return;

    const tableId = table.id ? `#${table.id}` : '';
    const chipContainer = tableId ? document.querySelector(`.filter-chips[data-table-target="${tableId}"]`) : document.querySelector('.filter-chips');
    const searchInput = tableId ? document.querySelector(`.live-table-search[data-table-target="${tableId}"], [data-table-search][data-table-target="${tableId}"]`) : document.querySelector('.live-table-search, [data-table-search]');

    const activeChip = chipContainer ? chipContainer.querySelector('.filter-chip.active') : null;
    const filterVal = (explicitFilterVal !== undefined && explicitFilterVal !== null)
        ? explicitFilterVal.toLowerCase().trim()
        : (activeChip ? (activeChip.getAttribute('data-filter') || 'all') : 'all').toLowerCase().trim();
    const query = searchInput ? searchInput.value.trim().toLowerCase() : '';

    const rows = Array.from(tbody.querySelectorAll('tr:not(.table-empty-state)'));
    let matches = 0;

    rows.forEach(row => {
        // Multi-source stock status detection
        const stockAttr = (row.getAttribute('data-stock') || '').toLowerCase().trim();
        const stockCell = row.querySelector('.stock-status-cell, .stock-badge');
        const stockCellText = stockCell ? stockCell.textContent.toLowerCase().trim() : '';

        const isSoldOut = (stockAttr === 'sold out') || stockCellText.includes('sold out') || stockCellText.includes('soldout');
        const isInStock = ((stockAttr === 'in stock') || stockCellText.includes('in stock') || stockCellText.includes('instock')) && !isSoldOut;

        // Category detection
        const catAttr = (row.getAttribute('data-category') || '').toLowerCase().trim();

        // 1. Chip matching
        let chipMatch = false;
        if (filterVal === 'all') {
            chipMatch = true;
        } else if (filterVal === 'in stock') {
            chipMatch = isInStock;
        } else if (filterVal === 'sold out') {
            chipMatch = isSoldOut;
        } else if (filterVal === 'main course') {
            chipMatch = catAttr.includes('main course');
        } else if (filterVal === 'beverage') {
            chipMatch = catAttr.includes('beverage');
        } else {
            const clone = row.cloneNode(true);
            const actions = clone.querySelector('.table-actions');
            if (actions) actions.remove();
            chipMatch = clone.textContent.toLowerCase().includes(filterVal);
        }

        // 2. Search query matching
        let searchMatch = false;
        if (!query) {
            searchMatch = true;
        } else if (query === 'sold out' || query === 'soldout' || query === 'sold-out' || query === 'unavailable' || query === 'out of stock') {
            searchMatch = isSoldOut;
        } else if (query === 'in stock' || query === 'instock' || query === 'in-stock' || query === 'available') {
            searchMatch = isInStock;
        } else {
            const clone = row.cloneNode(true);
            const actionsEl = clone.querySelector('.table-actions');
            if (actionsEl) actionsEl.remove();
            searchMatch = clone.textContent.toLowerCase().includes(query);
        }

        const isVisible = chipMatch && searchMatch;
        row.style.display = isVisible ? '' : 'none';
        if (isVisible) matches++;
    });

    // Handle Empty State
    let emptyState = tbody.querySelector('.table-empty-state');
    if (matches === 0) {
        if (!emptyState) {
            emptyState = document.createElement('tr');
            emptyState.className = 'table-empty-state';
            const colCount = table.querySelectorAll('thead th').length || 7;
            emptyState.innerHTML = `
                <td colspan="${colCount}" style="padding: 2.5rem; text-align: center; color: var(--text-muted);">
                    <div style="font-size: 2rem; margin-bottom: 0.5rem;">🔍</div>
                    <strong>No food items found matching current filters</strong>
                    <div style="font-size: 0.85rem; margin-top: 0.25rem;">Try selecting 'All Items' or clearing your search</div>
                </td>
            `;
            tbody.appendChild(emptyState);
        } else {
            emptyState.style.display = '';
            emptyState.querySelector('strong').textContent = query
                ? `No results found for "${query}"`
                : 'No food items found matching current filters';
        }
    } else if (emptyState) {
        emptyState.style.display = 'none';
    }
}

// Global alias for compatibility
window.applyTableFilter = applyTableFilter;
window.updateTableFilter = applyTableFilter;

// Live Table Instant Filter
function initLiveTableFilter() {
    const searchInputs = document.querySelectorAll('.live-table-search, [data-table-search]');
    searchInputs.forEach(input => {
        const table = document.querySelector(input.getAttribute('data-table-target') || '.custom-table');
        if (!table) return;

        input.addEventListener('input', () => {
            applyTableFilter(table);
        });
    });
}

// Filter Chips (e.g. Filter by Main Course vs Beverage vs Stock)
function initFilterChips() {
    const chipContainers = document.querySelectorAll('.filter-chips');
    chipContainers.forEach(container => {
        const chips = container.querySelectorAll('.filter-chip');
        const targetTable = document.querySelector(container.getAttribute('data-table-target') || '.custom-table');
        if (!targetTable) return;

        chips.forEach(chip => {
            chip.addEventListener('click', (e) => {
                e.preventDefault();
                chips.forEach(c => c.classList.remove('active'));
                chip.classList.add('active');
                const val = (chip.getAttribute('data-filter') || 'all').toLowerCase().trim();
                applyTableFilter(targetTable, val);
            });
        });

        // Initialize state on load
        const activeChip = container.querySelector('.filter-chip.active');
        const initialVal = activeChip ? activeChip.getAttribute('data-filter') : 'all';
        applyTableFilter(targetTable, initialVal);
    });
}

// Interactive Quantity Steppers
function initQuantitySteppers(root) {
    (root || document).querySelectorAll('.qty-stepper').forEach(stepper => {
        if (stepper.dataset.bound === 'true') return;
        stepper.dataset.bound = 'true';
        const input = stepper.querySelector('input');
        const btnMinus = stepper.querySelector('.qty-minus');
        const btnPlus = stepper.querySelector('.qty-plus');

        if (!input || !btnMinus || !btnPlus) return;

        btnMinus.addEventListener('click', (e) => {
            e.preventDefault();
            const min = parseInt(input.min || '1', 10);
            let val = parseInt(input.value || '1', 10);
            if (val > min) {
                input.value = val - 1;
                input.dispatchEvent(new Event('input', { bubbles: true }));
                input.dispatchEvent(new Event('change', { bubbles: true }));
            }
        });

        btnPlus.addEventListener('click', (e) => {
            e.preventDefault();
            const max = parseInt(input.max || '100', 10);
            let val = parseInt(input.value || '1', 10);
            if (val < max) {
                input.value = val + 1;
                input.dispatchEvent(new Event('input', { bubbles: true }));
                input.dispatchEvent(new Event('change', { bubbles: true }));
            }
        });
    });
}

// Click to Copy Codes (e.g. Tracking # or Order #)
function initClickToCopy() {
    document.querySelectorAll('[data-copy]').forEach(el => {
        el.style.cursor = 'pointer';
        el.setAttribute('title', 'Click to copy to clipboard');
        el.addEventListener('click', () => {
            const text = el.getAttribute('data-copy') || el.textContent.trim();
            navigator.clipboard.writeText(text).then(() => {
                Toast.success(`Copied: ${text}`);
            }).catch(() => {
                Toast.info(`Code: ${text}`);
            });
        });
    });
}

// Back to Top Button
function initBackToTop() {
    let btn = document.querySelector('.back-to-top');
    if (!btn) {
        btn = document.createElement('button');
        btn.className = 'back-to-top';
        btn.setAttribute('aria-label', 'Scroll to top');
        btn.setAttribute('title', 'Back to Top');
        btn.innerHTML = `
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="18 15 12 9 6 15"></polyline>
            </svg>
        `;
        document.body.appendChild(btn);
    }

    window.addEventListener('scroll', () => {
        if (window.scrollY > 300) {
            btn.classList.add('visible');
        } else {
            btn.classList.remove('visible');
        }
    });

    btn.addEventListener('click', () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });
}

// DOM Ready Lifecycle
document.addEventListener('DOMContentLoaded', () => {
    // 1. Initialize Theme
    ThemeEngine.init();

    // 2. Animate Numbers
    animateNumbers();

    // 3. Live Table Search
    initLiveTableFilter();

    // 4. Filter Chips
    initFilterChips();

    // 5. Quantity Steppers
    initQuantitySteppers();

    // 6. Click to copy
    initClickToCopy();

    // 7. Back to top button
    initBackToTop();

    // 8. Tab Switching Logic for Forms
    const tabButtons = document.querySelectorAll('[data-tab-target]');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', (e) => {
            e.preventDefault();
            const parent = btn.parentElement;
            const groupButtons = parent ? parent.querySelectorAll('[data-tab-target]') : tabButtons;
            const targetId = btn.getAttribute('data-tab-target');
            
            groupButtons.forEach(b => {
                b.classList.remove('active', 'btn-primary');
                b.classList.add('btn-secondary');
                const panel = document.querySelector(b.getAttribute('data-tab-target'));
                if (panel) {
                    panel.style.display = 'none';
                }
            });
            btn.classList.remove('btn-secondary');
            btn.classList.add('active', 'btn-primary');

            const targetPanel = document.querySelector(targetId);
            if (targetPanel) {
                targetPanel.style.display = 'block';
                targetPanel.classList.add('fade-in');
            }
        });
    });

    // 9. Dynamic Price Calculation for Order Creation Form
    const deliveryFeeInput = document.getElementById('deliveryFeeInput');
    const totalDisplay = document.getElementById('orderTotalEstimate');

    function formatLKR(amount) {
        return 'LKR ' + Number(amount || 0).toLocaleString('en-US', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    function updateOrderEstimate() {
        const panel = document.getElementById('onlineOrderPanel');
        if (!panel || !totalDisplay) return;
        let foodTotal = 0;
        panel.querySelectorAll('.order-item-row').forEach(row => {
            const select = row.querySelector('select[name="foodItemId"]');
            const qtyInput = row.querySelector('input[name="quantity"]');
            const option = select ? select.options[select.selectedIndex] : null;
            const price = parseFloat(option?.getAttribute('data-price') || '0');
            const qty = parseInt(qtyInput?.value || '1', 10);
            if (option && option.value) {
                foodTotal += price * qty;
            }
        });
        const deliveryFee = parseFloat(deliveryFeeInput?.value || '0');
        totalDisplay.textContent = formatLKR(foodTotal + deliveryFee);
    }

    document.querySelectorAll('[data-add-item]').forEach(button => {
        button.addEventListener('click', () => {
            const host = document.querySelector(button.getAttribute('data-add-item'));
            const row = host ? host.querySelector('.order-item-row') : null;
            if (!row) return;
            const copy = row.cloneNode(true);
            copy.querySelectorAll('[id]').forEach(field => field.removeAttribute('id'));
            copy.querySelectorAll('.qty-stepper').forEach(stepper => stepper.removeAttribute('data-bound'));
            copy.querySelectorAll('select').forEach(select => { select.selectedIndex = 0; });
            copy.querySelectorAll('input[name="quantity"]').forEach(input => { input.value = '1'; });
            const remove = document.createElement('button');
            remove.type = 'button';
            remove.className = 'btn btn-danger btn-sm remove-item-row';
            remove.textContent = 'Remove';
            remove.addEventListener('click', () => {
                copy.remove();
                updateOrderEstimate();
            });
            copy.appendChild(remove);
            host.insertBefore(copy, button);
            initQuantitySteppers(copy);
            updateOrderEstimate();
        });
    });

    const onlinePanel = document.getElementById('onlineOrderPanel');
    if (onlinePanel && totalDisplay) {
        onlinePanel.addEventListener('change', updateOrderEstimate);
        onlinePanel.addEventListener('input', updateOrderEstimate);
        updateOrderEstimate();
    }
});
