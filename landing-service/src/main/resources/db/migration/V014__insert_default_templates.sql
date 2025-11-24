-- Insert default landing page templates
-- V014__insert_default_templates.sql

INSERT INTO templates (name, description, version, html_structure, css_styles, js_scripts, config, is_active, created_at, updated_at)
VALUES 
(
    'Modern Business Template',
    'Современный бизнес-шаблон с hero section, преимуществами и контактной формой',
    '1.0.0',
    '<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{{title}}</title>
</head>
<body>
    <header>
        <nav>
            <div class="logo">{{organization.name}}</div>
            <ul>
                <li><a href="#about">О нас</a></li>
                <li><a href="#services">Услуги</a></li>
                <li><a href="#contact">Контакты</a></li>
            </ul>
        </nav>
    </header>
    
    <section class="hero">
        <h1>{{title}}</h1>
        <p>{{description}}</p>
        <button class="cta-button">Связаться с нами</button>
    </section>
    
    <section id="about">
        <h2>О нас</h2>
        <div class="content">
            {{content.about}}
        </div>
    </section>
    
    <section id="services">
        <h2>Наши услуги</h2>
        <div class="services-grid">
            {{#each services}}
            <div class="service-card">
                <h3>{{name}}</h3>
                <p>{{description}}</p>
            </div>
            {{/each}}
        </div>
    </section>
    
    <section id="contact">
        <h2>Контакты</h2>
        <form class="contact-form">
            <input type="text" placeholder="Ваше имя" required>
            <input type="email" placeholder="Email" required>
            <textarea placeholder="Сообщение" required></textarea>
            <button type="submit">Отправить</button>
        </form>
    </section>
    
    <footer>
        <p>&copy; 2025 {{organization.name}}. Все права защищены.</p>
    </footer>
</body>
</html>',
    'body {
    margin: 0;
    padding: 0;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    line-height: 1.6;
    color: #333;
}

header {
    background: {{themeSettings.primaryColor}};
    color: white;
    padding: 1rem 2rem;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

nav {
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 1200px;
    margin: 0 auto;
}

.logo {
    font-size: 1.5rem;
    font-weight: bold;
}

nav ul {
    display: flex;
    list-style: none;
    margin: 0;
    padding: 0;
}

nav ul li {
    margin-left: 2rem;
}

nav a {
    color: white;
    text-decoration: none;
    transition: opacity 0.3s;
}

nav a:hover {
    opacity: 0.8;
}

.hero {
    background: linear-gradient(135deg, {{themeSettings.primaryColor}} 0%, {{themeSettings.secondaryColor}} 100%);
    color: white;
    text-align: center;
    padding: 6rem 2rem;
}

.hero h1 {
    font-size: 3rem;
    margin-bottom: 1rem;
}

.hero p {
    font-size: 1.25rem;
    margin-bottom: 2rem;
}

.cta-button {
    background: white;
    color: {{themeSettings.primaryColor}};
    border: none;
    padding: 1rem 2rem;
    font-size: 1.1rem;
    border-radius: 5px;
    cursor: pointer;
    transition: transform 0.3s;
}

.cta-button:hover {
    transform: translateY(-2px);
}

section {
    padding: 4rem 2rem;
    max-width: 1200px;
    margin: 0 auto;
}

h2 {
    text-align: center;
    font-size: 2.5rem;
    margin-bottom: 3rem;
    color: {{themeSettings.primaryColor}};
}

.services-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 2rem;
}

.service-card {
    padding: 2rem;
    border: 1px solid #ddd;
    border-radius: 10px;
    text-align: center;
    transition: box-shadow 0.3s;
}

.service-card:hover {
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.contact-form {
    max-width: 600px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.contact-form input,
.contact-form textarea {
    padding: 1rem;
    border: 1px solid #ddd;
    border-radius: 5px;
    font-size: 1rem;
}

.contact-form textarea {
    min-height: 150px;
    resize: vertical;
}

.contact-form button {
    background: {{themeSettings.primaryColor}};
    color: white;
    border: none;
    padding: 1rem;
    font-size: 1.1rem;
    border-radius: 5px;
    cursor: pointer;
    transition: opacity 0.3s;
}

.contact-form button:hover {
    opacity: 0.9;
}

footer {
    background: #333;
    color: white;
    text-align: center;
    padding: 2rem;
}

@media (max-width: 768px) {
    .hero h1 {
        font-size: 2rem;
    }
    
    nav ul {
        display: none;
    }
}',
    'document.addEventListener("DOMContentLoaded", function() {
    // Smooth scrolling for navigation links
    document.querySelectorAll(''a[href^="#"]'').forEach(anchor => {
        anchor.addEventListener("click", function(e) {
            e.preventDefault();
            const target = document.querySelector(this.getAttribute("href"));
            if (target) {
                target.scrollIntoView({ behavior: "smooth" });
            }
        });
    });

    // Contact form submission
    const contactForm = document.querySelector(".contact-form");
    if (contactForm) {
        contactForm.addEventListener("submit", function(e) {
            e.preventDefault();
            alert("Спасибо за ваше сообщение! Мы свяжемся с вами в ближайшее время.");
            contactForm.reset();
        });
    }

    // CTA button click
    const ctaButton = document.querySelector(".cta-button");
    if (ctaButton) {
        ctaButton.addEventListener("click", function() {
            document.querySelector("#contact").scrollIntoView({ behavior: "smooth" });
        });
    }
});',
    '{
        "sections": {
            "header": {
                "enabled": true,
                "showNavigation": true
            },
            "hero": {
                "enabled": true,
                "showCta": true
            },
            "about": {
                "enabled": true
            },
            "services": {
                "enabled": true
            },
            "contact": {
                "enabled": true,
                "showForm": true
            },
            "footer": {
                "enabled": true
            }
        },
        "responsive": true,
        "animations": true
    }',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Add comments
COMMENT ON TABLE templates IS 'Landing page templates with default data';

