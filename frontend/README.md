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
- Keycloak is proxied via `/keycloak`.
- Adjust backend port in `.env`:
  - `18080` if backend started with `--server.port=18080`
  - `8090` if backend uses default `application.yml`

## Build
```bash
npm run build
npm run preview
```
