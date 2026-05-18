# Империи: Классика — Веб-приложение (Соло-режим)

Реализация настольной игры **Imperium: Classics** с соло-режимом против бота.

## Стек технологий
- **Бэкенд**: Python 3.10+ / FastAPI (in-memory сессии)
- **Фронтенд**: React 18 + TypeScript + Zustand
- **IDE**: IntelliJ IDEA (с плагином Python и Node.js)

## Структура проекта
```
imperium/
├── .idea/              ← конфигурация IntelliJ IDEA
├── backend/
│   ├── requirements.txt
│   └── src/main/python/
│       ├── api/main.py         ← FastAPI эндпоинты
│       └── game/
│           ├── cards.py        ← все карты игры
│           ├── engine.py       ← движок игровых действий
│           ├── bot_logic.py    ← логика бота по таблицам правил
│           ├── setup.py        ← подготовка игры
│           ├── state.py        ← модели данных
│           ├── session.py      ← менеджер сессий
│           └── enums.py        ← перечисления
└── frontend/
    ├── package.json
    └── src/
        ├── components/
        │   ├── GameBoard.tsx   ← главный игровой интерфейс
        │   └── GameSetup.tsx   ← экран выбора народов
        ├── store/gameStore.ts  ← Zustand store
        ├── types/game.ts       ← TypeScript типы
        └── utils/api.ts        ← HTTP-клиент
```

## Запуск

### Бэкенд
```bash
cd backend
pip install -r requirements.txt
python -m uvicorn src.main.python.api.main:app --reload --port 8000
```
API доступно на `http://localhost:8000`  
Документация: `http://localhost:8000/docs`

### Фронтенд
```bash
cd frontend
npm install
npm start
```
Откроется в браузере на `http://localhost:3000`

## API эндпоинты
| Метод | URL | Описание |
|-------|-----|----------|
| GET | `/api/nations` | Список народов |
| GET | `/api/difficulties` | Уровни сложности |
| POST | `/api/games` | Создать игру |
| GET | `/api/games/{id}` | Получить состояние |
| POST | `/api/games/{id}/play-card` | Разыграть карту |
| POST | `/api/games/{id}/exploit-card` | Эксплуатировать карту |
| POST | `/api/games/{id}/innovation` | Инновация |
| POST | `/api/games/{id}/revolution` | Революция |
| POST | `/api/games/{id}/end-turn` | Завершить ход (запускает ход бота) |
| POST | `/api/games/{id}/acquire-card` | Приобрести карту с рынка |
| POST | `/api/games/{id}/accelerate-progress` | Ускорить прогресс |

## Народы (Классика)
- **Викинги** ★★☆☆☆ — быстрое прокручивание колоды, без летописи
- **Греки** ★★★★☆ — города и технологии
- **Карфагеняне** ★★☆☆☆ — торговля и накопление ресурсов
- **Кельты** ★★☆☆☆ — агрессивный стиль, беспорядки у противника
- **Македоняне** ★☆☆☆☆ — захват регионов
- **Персы** ★☆☆☆☆ — набеги и покорение народов
- **Римляне** ★☆☆☆☆ — быстрая экспансия
- **Скифы** ★★☆☆☆ — контроль регионов

## Уровни сложности
- **Вождь** — бот играет 3-4 карты за ход
- **Полководец** — как Вождь + дополнительный сброс
- **Император** — стандартный режим (4-5 карт)
- **Повелитель** — бот с дополнительными ресурсами
- **Властелин** — бот с 6 слотами карт
