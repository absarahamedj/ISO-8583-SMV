# Dynamic ISO 8583 Field Validator - Frontend Setup

This frontend provides the testing interface for the ISO 8583 Card Payment Simulator Validator.

## Two Ways to Run the UI

### Option 1: Embedded in Spring Boot (Zero Setup Required)
The UI is automatically embedded in `src/main/resources/static/index.html`.
Simply start the Spring Boot backend:
```bash
mvn spring-boot:run
```
Then open your browser to:
```
http://localhost:8080/
```
No Node.js or npm installation is required!

---

### Option 2: Standalone Vite + React + TypeScript Development
If you want hot-reloading (HMR) during frontend development:
1. Navigate to this directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Run Vite dev server:
   ```bash
   npm run dev
   ```
4. Open your browser to:
   ```
   http://localhost:5173/
   ```
   API requests to `/api/*` are automatically proxied to Spring Boot at `http://localhost:8080`.
