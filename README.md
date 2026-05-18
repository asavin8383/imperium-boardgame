# Бэкенд
cd backend && pip install -r requirements.txt
python -m uvicorn src.main.python.api.main:app --reload --port 8000

# Фронтенд (в другом терминале)
cd frontend && npm install && npm start
