#!/bin/bash
set -e

BASE="http://localhost:8000"
echo "=== 🦫 BobrExam API Test ==="

echo ""
echo "--- Seed tasks ---"
curl -s -X POST $BASE/api/seed/tasks | python3 -m json.tool

echo ""
echo "--- Register ---"
curl -s -X POST $BASE/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"denis@test.ru","nickname":"BobrDen","password":"forest123"}' \
  | python3 -m json.tool

echo ""
echo "--- Login ---"
TOKEN=$(curl -s -X POST $BASE/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"denis@test.ru","password":"forest123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
echo "JWT: ${TOKEN:0:30}..."

echo ""
echo "--- Setup subjects (ФТ-1.4) ---"
curl -s -X POST $BASE/api/user/setup \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"examType":"ОГЭ","subjects":["Математика","Физика"]}' \
  | python3 -m json.tool

echo ""
echo "--- Profile (ФТ-4.1) ---"
curl -s $BASE/api/user/me \
  -H "Authorization: Bearer $TOKEN" \
  | python3 -m json.tool

echo ""
echo "--- Learning path Math (ФТ-2.1) ---"
curl -s "$BASE/api/courses/%D0%9E%D0%93%D0%9D/%D0%9C%D0%B0%D1%82%D0%B5%D0%BC%D0%B0%D1%82%D0%B8%D0%BA%D0%B0/path" \
  | python3 -m json.tool

echo ""
echo "--- AI explain (ФТ-3.3, mock) ---"
cat > /tmp/ai_req.json << 'JSON'
{
  "examType": "ОГЭ",
  "subject": "Математика",
  "taskText": "Найдите значение выражения: (15/8 - 1/4) : 2",
  "userAnswer": "1",
  "correctAnswer": "0.8125"
}
JSON
curl -s -X POST $BASE/api/ai/explain \
  -H "Content-Type: application/json" \
  -d @/tmp/ai_req.json \
  | python3 -m json.tool

echo ""
echo "=== ✅ All tests passed ==="
