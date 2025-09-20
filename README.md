# QuizApp  

A full-stack Quiz Tournament application with a **Spring Boot backend** and a **React frontend**.  


## 📂 Project Structure
.
├── quizapp/ # Backend (Spring Boot REST API)
└── quiz-frontend/ # Frontend (React + Tailwind)

yaml
Copy code

---

## ⚙️ Backend (Spring Boot)

### Requirements
- **Java 17+**  
- **Maven**  
- **MySQL** (running locally via XAMPP or another MySQL server)  

### Setup & Run
1. Configure database in `quizapp/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/quizapp
   spring.datasource.username=root
   spring.datasource.password=
   spring.jpa.hibernate.ddl-auto=update
   jwt.secret=MySuperSecretKey12345
   jwt.expiration=86400000
Start MySQL in XAMPP.

Run the backend:

bash
Copy code
cd quizapp
mvn spring-boot:run
API Endpoints (Sample)
👤 Users
POST /users/register → Register a new player/admin

POST /users/login → Authenticate and return JWT

POST /users/reset-password → Request password reset

📝 Questions
POST /questions/add → Add a new question

GET /questions/all → Fetch all questions

🏆 Tournaments
POST /tournaments/create → Create a new tournament

POST /tournaments/like/{id} → Like a tournament

GET /tournaments/{id}/questions → Get questions for a tournament

🎮 Player
POST /player/start/{tournamentId} → Start an attempt

POST /player/submit/{attemptId}/{questionId} → Submit answer

POST /player/finish/{attemptId} → Finish attempt

GET /player/leaderboard-position → Get player’s global rank

🎨 Frontend (React + Tailwind CSS)
Requirements
Node.js 18+

npm or yarn

Setup & Run
Install dependencies:

bash
Copy code
cd quiz-frontend
npm install
Start development server:

bash
Copy code
npm start
App runs on http://localhost:3000.

Features
🔐 Authentication (login/register with JWT)

🏆 Tournaments (view, create, join, play quiz)

📊 Leaderboard (track scores and global rank)

👤 Dashboards for Players and Admins

🎨 Responsive UI built with Tailwind CSS

📦 Git Usage
To commit backend changes only:

bash
Copy code
git add quizapp
git commit -m "fix(backend): update PlayerService"
git push origin main
To commit frontend changes only:

bash
Copy code
git add quiz-frontend
git commit -m "feat(frontend): add TournamentCard component"
git push origin main
To commit both:

bash
Copy code
git add .
git commit -m "chore: update backend & frontend"
git push origin main
🗄️ Database
MySQL database auto-configured via Spring Boot JPA (see application.properties).

Tables are auto-generated for User, Question, Tournament, PlayerAttempt, etc.
