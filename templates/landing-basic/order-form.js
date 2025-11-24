/**
 * Shopping Cart and Order Form Logic
 * Klassifikator Landing Page
 */

// Shopping Cart State
let cart = [];

// ============================================================================
// CRITICAL: Define functions as GLOBAL IMMEDIATELY to work with inline onclick
// ============================================================================

// Load cart from localStorage on init
function initCart() {
    const savedCart = localStorage.getItem('klassifikator_cart');
    if (savedCart) {
        try {
            cart = JSON.parse(savedCart);
            updateCart();
        } catch (e) {
            console.error('Failed to load cart:', e);
            cart = [];
        }
    }
}

// Save cart to localStorage
function saveCart() {
    try {
        localStorage.setItem('klassifikator_cart', JSON.stringify(cart));
    } catch (e) {
        console.error('Failed to save cart:', e);
    }
}

// Add product to cart - GLOBAL FUNCTION
window.addToCart = function(productId, productName, productPrice) {
    // Check if product already in cart
    const existingItem = cart.find(item => item.productId === productId);
    
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.push({
            productId: productId,
            productName: productName,
            price: productPrice,
            quantity: 1
        });
    }
    
    saveCart();
    updateCart();
    window.showNotification(`${productName} добавлен в корзину`);
    
    // Visual feedback on button
    const button = document.querySelector(`[data-product-id="${productId}"]`);
    if (button) {
        button.textContent = '✓ Добавлено';
        button.style.background = '#10b981';
        setTimeout(() => {
            button.textContent = 'В корзину';
            button.style.background = '';
        }, 1000);
    }
};

// Remove product from cart - GLOBAL FUNCTION
window.removeFromCart = function(productId) {
    cart = cart.filter(item => item.productId !== productId);
    saveCart();
    updateCart();
    window.showNotification('Товар удалён из корзины');
};

// Update product quantity in cart - GLOBAL FUNCTION
window.updateQuantity = function(productId, delta) {
    const item = cart.find(item => item.productId === productId);
    if (!item) return;
    
    item.quantity += delta;
    
    if (item.quantity <= 0) {
        window.removeFromCart(productId);
    } else {
        saveCart();
        updateCart();
    }
};

// Calculate cart total
function calculateTotal() {
    return cart.reduce((total, item) => total + (item.price * item.quantity), 0);
}

// Update cart UI
function updateCart() {
    const cartCountElement = document.getElementById('cartCount');
    const cartItemsElement = document.getElementById('cartItems');
    const cartTotalElement = document.getElementById('cartTotal');
    const checkoutBtn = document.getElementById('checkoutBtn');
    
    // Update cart count
    const totalItems = cart.reduce((sum, item) => sum + item.quantity, 0);
    cartCountElement.textContent = totalItems;
    cartCountElement.style.display = totalItems > 0 ? 'flex' : 'none';
    
    // Update cart items
    if (cart.length === 0) {
        cartItemsElement.innerHTML = '<p class="cart-empty">Корзина пуста</p>';
        checkoutBtn.disabled = true;
    } else {
        cartItemsElement.innerHTML = cart.map(item => `
            <div class="cart-item">
                <div class="cart-item-info">
                    <div class="cart-item-name">${item.productName}</div>
                    <div class="cart-item-price">${window.formatPrice(item.price)} ₽</div>
                </div>
                <div class="cart-item-controls">
                    <button class="quantity-btn" onclick="window.updateQuantity(${item.productId}, -1)">-</button>
                    <span class="quantity">${item.quantity}</span>
                    <button class="quantity-btn" onclick="window.updateQuantity(${item.productId}, 1)">+</button>
                    <button class="remove-btn" onclick="window.removeFromCart(${item.productId})">×</button>
                </div>
            </div>
        `).join('');
        checkoutBtn.disabled = false;
    }
    
    // Update total
    const total = calculateTotal();
    cartTotalElement.textContent = `${window.formatPrice(total)} ₽`;
}

// Toggle cart panel - GLOBAL FUNCTION
window.toggleCart = function() {
    const cartPanel = document.getElementById('cartPanel');
    cartPanel.classList.toggle('active');
};

// Open order modal - GLOBAL FUNCTION
window.openOrderModal = function() {
    if (cart.length === 0) {
        window.showNotification('Корзина пуста', 'warning');
        return;
    }
    
    const modal = document.getElementById('orderModal');
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
    
    // Update order summary
    updateOrderSummary();
    
    // Close cart panel
    const cartPanel = document.getElementById('cartPanel');
    cartPanel.classList.remove('active');
};

// Close order modal - GLOBAL FUNCTION
window.closeOrderModal = function() {
    const modal = document.getElementById('orderModal');
    modal.classList.remove('active');
    document.body.style.overflow = '';
};

// Update order summary in modal
function updateOrderSummary() {
    const summaryElement = document.getElementById('orderItemsSummary');
    const totalElement = document.getElementById('orderTotalAmount');
    
    summaryElement.innerHTML = `
        <h3>Состав заказа:</h3>
        ${cart.map(item => `
            <div class="order-item-row">
                <span>${item.productName} × ${item.quantity}</span>
                <span>${window.formatPrice(item.price * item.quantity)} ₽</span>
            </div>
        `).join('')}
    `;
    
    const total = calculateTotal();
    totalElement.textContent = `${window.formatPrice(total)} ₽`;
}

// Format phone number
function formatPhoneNumber(input) {
    let value = input.value.replace(/\D/g, '');
    
    if (value.length === 0) {
        input.value = '';
        return;
    }
    
    if (value[0] === '8') {
        value = '7' + value.slice(1);
    }
    
    if (value[0] !== '7') {
        value = '7' + value;
    }
    
    let formatted = '+7';
    
    if (value.length > 1) {
        formatted += ' (' + value.substring(1, 4);
    }
    if (value.length >= 5) {
        formatted += ') ' + value.substring(4, 7);
    }
    if (value.length >= 8) {
        formatted += '-' + value.substring(7, 9);
    }
    if (value.length >= 10) {
        formatted += '-' + value.substring(9, 11);
    }
    
    input.value = formatted;
}

// Setup phone formatting
document.addEventListener('DOMContentLoaded', function() {
    const phoneInput = document.getElementById('customerPhone');
    if (phoneInput) {
        phoneInput.addEventListener('input', function() {
            formatPhoneNumber(this);
        });
        
        phoneInput.addEventListener('focus', function() {
            if (this.value === '') {
                this.value = '+7 (';
            }
        });
    }
});

// Submit order
async function submitOrder(event) {
    event.preventDefault();
    
    const submitBtn = document.getElementById('submitOrderBtn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Отправка...';
    
    try {
        // Get form data
        const formData = new FormData(event.target);
        
        // Prepare order data
        const orderData = {
            organizationId: window.ORGANIZATION_ID,
            landingId: window.LANDING_ID,
            customerName: formData.get('customerName'),
            customerPhone: formData.get('customerPhone'),
            customerEmail: formData.get('customerEmail') || null,
            deliveryAddress: formData.get('deliveryAddress') || null,
            comment: formData.get('comment') || null,
            items: cart.map(item => ({
                productId: item.productId,
                quantity: item.quantity
            }))
        };
        
        // Send order to API
        const response = await fetch(`${window.API_URL}/orders`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(orderData)
        });
        
        if (!response.ok) {
            throw new Error('Ошибка при отправке заказа');
        }
        
        const result = await response.json();
        
        // Track purchase in Yandex Metrika
        if (window.ym && window.yandexMetrikaId) {
            ym(window.yandexMetrikaId, 'reachGoal', 'purchase', {
                order_price: calculateTotal(),
                currency: 'RUB'
            });
        }
        
        // Success!
        window.showNotification('Заказ успешно оформлен! Наш менеджер свяжется с вами в ближайшее время.', 'success');
        
        // Clear cart
        cart = [];
        saveCart();
        updateCart();
        
        // Close modal
        window.closeOrderModal();
        
        // Reset form
        event.target.reset();
        
    } catch (error) {
        console.error('Order submission error:', error);
        window.showNotification('Произошла ошибка при оформлении заказа. Пожалуйста, попробуйте позже или свяжитесь с нами по телефону.', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Оформить заказ';
    }
}

// Show notification - GLOBAL FUNCTION
window.showNotification = function(message, type = 'success') {
    const notification = document.getElementById('notification');
    const messageElement = document.getElementById('notificationMessage');
    
    messageElement.textContent = message;
    notification.className = 'notification active ' + type;
    
    setTimeout(() => {
        notification.classList.remove('active');
    }, 5000);
};

// Format price - GLOBAL FUNCTION
window.formatPrice = function(price) {
    return new Intl.NumberFormat('ru-RU').format(price);
};

// Setup order form
document.addEventListener('DOMContentLoaded', function() {
    // Initialize cart
    initCart();
    
    // Setup form submission
    const orderForm = document.getElementById('orderForm');
    if (orderForm) {
        orderForm.addEventListener('submit', submitOrder);
    }
    
    // Close modal on overlay click
    const modal = document.getElementById('orderModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal || e.target.classList.contains('modal-overlay')) {
                window.closeOrderModal();
            }
        });
    }
    
    // Close modal on Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            window.closeOrderModal();
        }
    });
    
    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute('href'));
            if (target) {
                target.scrollIntoView({
                    behavior: 'smooth',
                    block: 'start'
                });
            }
        });
    });
    
    // Setup category filters
    setupCategoryFilters();
});

// Category Filter Logic
function setupCategoryFilters() {
    const filterButtons = document.querySelectorAll('.category-filter');
    const productCards = document.querySelectorAll('.product-card');
    const productListItems = document.querySelectorAll('.product-list-item');
    
    if (filterButtons.length === 0 || (productCards.length === 0 && productListItems.length === 0)) {
        return;
    }
    
    filterButtons.forEach(button => {
        button.addEventListener('click', function() {
            const selectedCategory = this.getAttribute('data-category');
            
            // Update active button
            filterButtons.forEach(btn => btn.classList.remove('active'));
            this.classList.add('active');
            
            // Filter product cards (with images)
            productCards.forEach(card => {
                const productCategory = card.getAttribute('data-category');
                
                if (selectedCategory === 'all' || productCategory === selectedCategory) {
                    card.classList.remove('hidden');
                    // Smooth appearance animation
                    card.style.animation = 'fadeIn 0.3s ease';
                } else {
                    card.classList.add('hidden');
                }
            });
            
            // Filter product list items (without images)
            productListItems.forEach(item => {
                const productCategory = item.getAttribute('data-category');
                
                if (selectedCategory === 'all' || productCategory === selectedCategory) {
                    item.classList.remove('hidden');
                    // Smooth appearance animation
                    item.style.animation = 'fadeIn 0.3s ease';
                } else {
                    item.classList.add('hidden');
                }
            });
            
            // Smooth scroll to products section
            const productsSection = document.getElementById('products');
            if (productsSection && selectedCategory !== 'all') {
                setTimeout(() => {
                    const productsList = document.querySelector('.products-grid');
                    if (productsList) {
                        productsList.scrollIntoView({
                            behavior: 'smooth',
                            block: 'nearest'
                        });
                    }
                }, 100);
            }
        });
    });
}

// ============================================================================
// HERO SLIDER LOGIC
// ============================================================================

let currentSlide = 0;
let sliderInterval = null;

// Initialize slider - GLOBAL FUNCTION
window.initSlider = function() {
    const slides = document.querySelectorAll('.slide');
    if (slides.length <= 1) {
        return; // No need for slider with 0 or 1 slide
    }
    
    // Auto-play: change slide every 5 seconds
    sliderInterval = setInterval(() => {
        changeSlide(1);
    }, 5000);
    
    // Pause on hover
    const sliderContainer = document.querySelector('.hero-slider');
    if (sliderContainer) {
        sliderContainer.addEventListener('mouseenter', () => {
            if (sliderInterval) {
                clearInterval(sliderInterval);
                sliderInterval = null;
            }
        });
        
        sliderContainer.addEventListener('mouseleave', () => {
            if (!sliderInterval) {
                sliderInterval = setInterval(() => {
                    changeSlide(1);
                }, 5000);
            }
        });
    }
};

// Change slide - GLOBAL FUNCTION
window.changeSlide = function(direction) {
    const slides = document.querySelectorAll('.slide');
    const indicators = document.querySelectorAll('.indicator');
    
    if (slides.length === 0) {
        return;
    }
    
    // Remove active class from current slide and indicator
    slides[currentSlide].classList.remove('active');
    if (indicators[currentSlide]) {
        indicators[currentSlide].classList.remove('active');
    }
    
    // Calculate new slide index
    currentSlide = (currentSlide + direction + slides.length) % slides.length;
    
    // Add active class to new slide and indicator
    slides[currentSlide].classList.add('active');
    if (indicators[currentSlide]) {
        indicators[currentSlide].classList.add('active');
    }
};

// Go to specific slide - GLOBAL FUNCTION
window.goToSlide = function(index) {
    const slides = document.querySelectorAll('.slide');
    const indicators = document.querySelectorAll('.indicator');
    
    if (slides.length === 0 || index < 0 || index >= slides.length) {
        return;
    }
    
    // Remove active class from current slide and indicator
    slides[currentSlide].classList.remove('active');
    if (indicators[currentSlide]) {
        indicators[currentSlide].classList.remove('active');
    }
    
    // Set new slide index
    currentSlide = index;
    
    // Add active class to new slide and indicator
    slides[currentSlide].classList.add('active');
    if (indicators[currentSlide]) {
        indicators[currentSlide].classList.add('active');
    }
    
    // Reset auto-play timer
    if (sliderInterval) {
        clearInterval(sliderInterval);
        sliderInterval = setInterval(() => {
            changeSlide(1);
        }, 5000);
    }
};

// Initialize slider on DOM load
document.addEventListener('DOMContentLoaded', function() {
    initSlider();
});
