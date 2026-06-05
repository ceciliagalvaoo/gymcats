from fastapi import FastAPI, HTTPException, Depends, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from jose import JWTError, jwt
from datetime import datetime, timedelta
from deep_translator import GoogleTranslator
from dotenv import load_dotenv
import httpx
import os

load_dotenv()

app = FastAPI(title="GymCats API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"]
)

SECRET_KEY = os.getenv("SECRET_KEY", "gymcats-secret-key-troque-em-producao")
ALGORITHM = "HS256"
TOKEN_EXPIRE_HOURS = 24 * 30

EXERCISEDB_KEY = os.getenv("EXERCISEDB_KEY", "")
EXERCISEDB_HOST = "exercisedb.p.rapidapi.com"
EXERCISEDB_HEADERS = {
    "X-RapidAPI-Key": EXERCISEDB_KEY,
    "X-RapidAPI-Host": EXERCISEDB_HOST
}

security = HTTPBearer()
translator = GoogleTranslator(source="en", target="pt")


def create_token(device_id: str) -> str:
    expire = datetime.utcnow() + timedelta(hours=TOKEN_EXPIRE_HOURS)
    return jwt.encode({"sub": device_id, "exp": expire}, SECRET_KEY, algorithm=ALGORITHM)


def verify_token(credentials: HTTPAuthorizationCredentials = Depends(security)) -> str:
    try:
        payload = jwt.decode(credentials.credentials, SECRET_KEY, algorithms=[ALGORITHM])
        return payload["sub"]
    except JWTError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Token inválido")


def translate_safe(text: str) -> str:
    try:
        return translator.translate(text)
    except Exception:
        return text


def translate_exercise(data: dict) -> dict:
    if isinstance(data.get("instructions"), list):
        data["instructions"] = [translate_safe(i) for i in data["instructions"]]
    if data.get("description"):
        data["description"] = translate_safe(data["description"])
    return data


class TokenRequest(BaseModel):
    device_id: str


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/auth/token")
def get_token(body: TokenRequest):
    return {"access_token": create_token(body.device_id), "token_type": "bearer"}


@app.get("/bodyparts")
async def get_bodyparts(_: str = Depends(verify_token)):
    async with httpx.AsyncClient() as client:
        r = await client.get(
            f"https://{EXERCISEDB_HOST}/exercises/bodyPartList",
            headers=EXERCISEDB_HEADERS,
            timeout=10.0
        )
        r.raise_for_status()
        return r.json()


@app.get("/targets")
async def get_targets(_: str = Depends(verify_token)):
    async with httpx.AsyncClient() as client:
        r = await client.get(
            f"https://{EXERCISEDB_HOST}/exercises/targetList",
            headers=EXERCISEDB_HEADERS,
            timeout=10.0
        )
        r.raise_for_status()
        return r.json()


@app.get("/equipment")
async def get_equipment(_: str = Depends(verify_token)):
    async with httpx.AsyncClient() as client:
        r = await client.get(
            f"https://{EXERCISEDB_HOST}/exercises/equipmentList",
            headers=EXERCISEDB_HEADERS,
            timeout=10.0
        )
        r.raise_for_status()
        return r.json()


@app.get("/exercise/search/{name}")
async def search_by_name(name: str, _: str = Depends(verify_token)):
    async with httpx.AsyncClient() as client:
        r = await client.get(
            f"https://{EXERCISEDB_HOST}/exercises/name/{name}",
            headers=EXERCISEDB_HEADERS,
            timeout=10.0
        )
        r.raise_for_status()
        return r.json()


@app.get("/exercise/bodypart/{bodypart}")
async def get_by_bodypart(bodypart: str, _: str = Depends(verify_token)):
    async with httpx.AsyncClient() as client:
        r = await client.get(
            f"https://{EXERCISEDB_HOST}/exercises/bodyPart/{bodypart}",
            headers=EXERCISEDB_HEADERS,
            timeout=10.0
        )
        r.raise_for_status()
        return r.json()


@app.get("/exercise/target/{target}")
async def get_by_target(target: str, _: str = Depends(verify_token)):
    async with httpx.AsyncClient() as client:
        r = await client.get(
            f"https://{EXERCISEDB_HOST}/exercises/target/{target}",
            headers=EXERCISEDB_HEADERS,
            timeout=10.0
        )
        r.raise_for_status()
        return r.json()


@app.get("/exercise/equipment/{equipment}")
async def get_by_equipment(equipment: str, _: str = Depends(verify_token)):
    async with httpx.AsyncClient() as client:
        r = await client.get(
            f"https://{EXERCISEDB_HOST}/exercises/equipment/{equipment}",
            headers=EXERCISEDB_HEADERS,
            timeout=10.0
        )
        r.raise_for_status()
        return r.json()


@app.get("/exercise/detail/{exercise_id}")
async def get_exercise_detail(exercise_id: str, _: str = Depends(verify_token)):
    async with httpx.AsyncClient() as client:
        r = await client.get(
            f"https://{EXERCISEDB_HOST}/exercises/exercise/{exercise_id}",
            headers=EXERCISEDB_HEADERS,
            timeout=10.0
        )
        r.raise_for_status()
        data = r.json()
        return translate_exercise(data)


@app.get("/tips")
def get_tips(_: str = Depends(verify_token)):
    return [
        "Registre suas cargas para acompanhar a evolução.",
        "Observe como energia e sono influenciam seu desempenho.",
        "Variações ao longo do ciclo são normais e esperadas.",
        "Priorize recuperação na fase menstrual.",
        "A fase folicular é ótima para treinos mais intensos."
    ]


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
