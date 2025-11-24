import psycopg2

# Read HTML file
with open('templates/landing-basic/index.html', 'r', encoding='utf-8') as f:
    html_content = f.read()

# Connect to database
conn = psycopg2.connect(
    host='localhost',
    port=5432,
    database='klassifikator_dev',
    user='klassifikator',
    password='klassifikator_dev_password'
)

cur = conn.cursor()

# Update template
cur.execute("""
    UPDATE templates 
    SET html_structure = %s
    WHERE id = 1
""", (html_content,))

conn.commit()

# Verify
cur.execute("SELECT LENGTH(html_structure) FROM templates WHERE id = 1")
length = cur.fetchone()[0]
print(f"✅ HTML updated! Length: {length} characters")

cur.close()
conn.close()
