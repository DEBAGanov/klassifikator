#!/bin/bash

# Update template in database using curl
echo "📝 Updating template in database..."

# Read template files
HTML_CONTENT=$(cat templates/landing-basic/index.html)
CSS_CONTENT=$(cat templates/landing-basic/styles.css)
JS_CONTENT=$(cat templates/landing-basic/order-form.js)

# Create JSON payload (escape quotes and newlines)
JSON_PAYLOAD=$(cat << EOF
{
  "name": "landing-basic",
  "description": "Basic Landing Page Template with Order Form",
  "htmlContent": $(echo "$HTML_CONTENT" | jq -Rs .),
  "cssStyles": $(echo "$CSS_CONTENT" | jq -Rs .),
  "jsScripts": $(echo "$JS_CONTENT" | jq -Rs .),
  "isActive": true
}
EOF
)

# Update template via API
curl -X PUT \
  -H "Content-Type: application/json" \
  -d "$JSON_PAYLOAD" \
  http://localhost:8083/api/v1/templates/1

echo ""
echo "✅ Template updated!"
echo ""
echo "🔄 Now restart Template Service and clear browser cache!"

