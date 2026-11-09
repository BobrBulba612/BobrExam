from pydantic import BaseModel, EmailStr
from typing import List, Optional, Dict, Any
from uuid import UUID

# --- Auth ---
class UserCreate(BaseModel):
    email: EmailStr
    nickname: str
    password: str

class UserLogin(BaseModel):
    email: EmailStr
    password: str

class Token(BaseModel):
    access_token: str
    token_type: str

class UserSetup(BaseModel):
    exam_type: str  # 'OGE' or 'EGE'
    subjects: List[str]

class UserResponse(BaseModel):
    id: UUID
    email: str
    nickname: str
    exam_type: Optional[str]
    subjects: Optional[List[str]]
    xp: int
    streak: int
    hearts: int

    class Config:
        from_attributes = True

# --- Tasks ---
class TaskResponse(BaseModel):
    id: UUID
    subject: str
    exam_type: str
    topic: Optional[str]
    difficulty: Optional[int]
    type: str
    question_text: str
    options_json: Optional[Dict[str, Any]]
    correct_answer: str
    explanation: Optional[str]

    class Config:
        from_attributes = True

class SubmitAnswer(BaseModel):
    task_id: UUID
    user_answer: str
    is_correct: bool

# --- Progress ---
class ProgressResponse(BaseModel):
    task_id: UUID
    status: str
    attempts: int
