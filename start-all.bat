@echo off
echo Starting Book Drift System...

REM Start backend (Spring Boot)
echo Starting backend service...
start "Backend Service" cmd /k "cd /d %~dp0book_drift && mvn spring-boot:run"

REM Wait a few seconds for backend to start
timeout /t 5 /nobreak >nul

REM Start frontend (Nuxt.js)
echo Starting frontend service...
start "Frontend Service" cmd /k "cd /d %~dp0book-qianduan && npm run dev"

echo All services started!
echo Backend: http://localhost:8080
echo Frontend: http://localhost:3000
timeout /t 3 /nobreak >nul
