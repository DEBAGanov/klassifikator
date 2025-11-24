#!/usr/bin/env python3
"""
Update template in PostgreSQL via kubectl exec
"""
import subprocess
import os
import sys

def read_file(filepath):
    """Read file content"""
    with open(filepath, 'r', encoding='utf-8') as f:
        return f.read()

def escape_sql_string(s):
    """Escape string for SQL"""
    # Replace single quotes with double single quotes
    return s.replace("'", "''").replace("\\", "\\\\")

def main():
    print("📦 Обновление шаблона в базе данных через psql...")
    
    # Change to project root
    os.chdir('/Users/bagano/Downloads/Cursor/klassifikator')
    
    # Read template files
    print("📄 Чтение файлов шаблона...")
    html = read_file('templates/landing-basic/index.html')
    css = read_file('templates/landing-basic/combined-styles.css')
    js = read_file('templates/landing-basic/combined-scripts.js')
    
    print(f"  HTML: {len(html)} bytes")
    print(f"  CSS: {len(css)} bytes")
    print(f"  JS: {len(js)} bytes")
    
    # Escape for SQL
    print("🔒 Экранирование данных...")
    html_escaped = escape_sql_string(html)
    css_escaped = escape_sql_string(css)
    js_escaped = escape_sql_string(js)
    
    # Create SQL UPDATE statement
    sql = f"""UPDATE templates SET 
  name = 'Modern Business Template',
  description = 'Современный адаптивный шаблон с корзиной, слайдером и полным функционалом',
  version = '2.0.0',
  html_structure = '{html_escaped}',
  css_styles = '{css_escaped}',
  js_scripts = '{js_escaped}',
  config = '{{"features": ["slider", "cart", "gallery", "reviews", "maps"]}}',
  is_active = true,
  updated_at = NOW()
WHERE id = 1;"""
    
    # Write SQL to temp file
    sql_file = '/tmp/update-template.sql'
    with open(sql_file, 'w', encoding='utf-8') as f:
        f.write(sql)
    
    print(f"💾 SQL записан в {sql_file} ({len(sql)} bytes)")
    
    # Get postgres pod name
    print("🔍 Поиск PostgreSQL пода...")
    result = subprocess.run(
        ['kubectl', 'get', 'pods', '-n', 'klassifikator', '-l', 'app=postgres', '-o', 'jsonpath={.items[0].metadata.name}'],
        capture_output=True, text=True, check=True
    )
    pod_name = result.stdout.strip()
    print(f"  Pod: {pod_name}")
    
    # Copy SQL file to pod
    print("📤 Копирование SQL в под...")
    subprocess.run(
        ['kubectl', 'cp', sql_file, f'klassifikator/{pod_name}:/tmp/update-template.sql'],
        check=True
    )
    
    # Execute SQL
    print("⚙️  Выполнение UPDATE...")
    result = subprocess.run(
        ['kubectl', 'exec', '-n', 'klassifikator', pod_name, '--', 
         'psql', '-U', 'klassifikator', '-d', 'klassifikator', '-f', '/tmp/update-template.sql'],
        capture_output=True, text=True
    )
    
    if result.returncode == 0:
        print("✅ Шаблон успешно обновлен!")
        print(result.stdout)
    else:
        print("❌ Ошибка при обновлении:")
        print(result.stderr)
        sys.exit(1)
    
    # Clean up
    print("🧹 Очистка временных файлов...")
    subprocess.run(
        ['kubectl', 'exec', '-n', 'klassifikator', pod_name, '--', 'rm', '-f', '/tmp/update-template.sql'],
        check=True
    )
    os.remove(sql_file)
    
    # Restart Template Service to clear cache
    print("🔄 Перезапуск Template Service для очистки кеша...")
    subprocess.run(
        ['kubectl', 'rollout', 'restart', 'deployment', 'template-service', '-n', 'klassifikator'],
        check=True
    )
    
    print("\n🎉 Готово! Подождите 30 секунд и обновите страницу:")
    print("   https://modernissimo.volzhck.ru")

if __name__ == '__main__':
    main()

