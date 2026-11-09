from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from typing import List
from uuid import UUID
from app.database import get_db
from app.models import Task, UserProgress, User
from app.schemas import TaskResponse, SubmitAnswer, ProgressResponse
from app.routers.users import get_current_user

router = APIRouter(prefix="/api", tags=["Tasks"])

# --- ФТ-5.1. GET /api/courses/{exam_type}/{subject}/path ---
@router.get("/courses/{exam_type}/{subject}/path", response_model=List[TaskResponse])
def get_learning_path(exam_type: str, subject: str, db: Session = Depends(get_db)):
    # В реальной реализации здесь будет логика построения дерева навыков
    # Сейчас просто возвращаем список заданий по предмету и типу экзамена
    tasks = db.query(Task).filter(
        Task.exam_type == exam_type,
        Task.subject == subject
    ).order_by(Task.difficulty).all()
    return tasks

# --- ФТ-5.1. GET /api/tasks/{task_id} ---
@router.get("/tasks/{task_id}", response_model=TaskResponse)
def get_task(task_id: UUID, db: Session = Depends(get_db)):
    task = db.query(Task).filter(Task.id == task_id).first()
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")
    return task

# --- ФТ-5.1. POST /api/progress/submit ---
@router.post("/progress/submit", response_model=ProgressResponse)
def submit_progress(
    submission: SubmitAnswer,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db)
):
    # Проверка существования задания
    task = db.query(Task).filter(Task.id == submission.task_id).first()
    if not task:
        raise HTTPException(status_code=404, detail="Task not found")

    # Обновление или создание прогресса
    progress = db.query(UserProgress).filter(
        UserProgress.user_id == current_user.id,
        UserProgress.task_id == submission.task_id
    ).first()

    if progress:
        progress.attempts += 1
        progress.status = "completed" if submission.is_correct else "failed"
    else:
        progress = UserProgress(
            user_id=current_user.id,
            task_id=submission.task_id,
            status="completed" if submission.is_correct else "failed",
            attempts=1
        )
        db.add(progress)

    # Начисление XP (ФТ-2.5)
    if submission.is_correct:
        current_user.xp += 10
    
    # Система жизней (ФТ-2.4)
    if not submission.is_correct and current_user.hearts > 0:
        current_user.hearts -= 1

    db.commit()
    db.refresh(progress)
    return progress
