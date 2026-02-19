# Autoguide Frontend (Vue)

## Run
```bash
cd /Users/home/Documents/TEST/frontend
cp .env.example .env
npm install
npm run dev
```

Open: [http://localhost:5173](http://localhost:5173)

## Notes
- Backend API is proxied via Vite (`/api`, `/v3`, `/actuator`).
- Backend static assets are proxied via `/assets`.
- Keycloak is proxied via `/keycloak`.
- Adjust backend port in `.env`:
  - `8090` if backend uses default `application.yml` (default setup)
  - `18080` if backend started with `--server.port=18080`
- Landing page content is fully loaded from backend:
  - `GET /api/v1/public/home`
  - `GET /api/v1/public/rooms/highlights`
- Demo room cards and hero image use internet image URLs provided by backend seed data.

## Build
```bash
npm run build
npm run preview
```
