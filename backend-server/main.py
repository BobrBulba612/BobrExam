from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.database import engine, Base
from app.routers import ai, users, tasks

# Создание таблиц при старте
Base.metadata.create_all(bind=engine)

app = FastAPI(title="BobrExam API", version="1.0")

# CORS для разработки (разрешаем запросы с Android-эмулятора)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Подключаем роутеры
app.include_router(users.router)
app.include_router(tasks.router)
app.include_router(ai.router)

@app.get("/")
def read_root():
    return {"message": "Welcome to BobrExam API! 🦫🌲"}
