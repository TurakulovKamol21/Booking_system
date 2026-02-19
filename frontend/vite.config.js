import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), "");
  const backendTarget = env.VITE_BACKEND_TARGET || "http://localhost:8090";
  const keycloakTarget = env.VITE_KEYCLOAK_TARGET || "http://localhost:8081";

  return {
    plugins: [vue()],
    server: {
      port: 5173,
      proxy: {
        "/api": {
          target: backendTarget,
          changeOrigin: true
        },
        "/v3": {
          target: backendTarget,
          changeOrigin: true
        },
        "/actuator": {
          target: backendTarget,
          changeOrigin: true
        },
        "/swagger-ui": {
          target: backendTarget,
          changeOrigin: true
        },
        "/webjars": {
          target: backendTarget,
          changeOrigin: true
        },
        "/assets": {
          target: backendTarget,
          changeOrigin: true
        },
        "/uploads": {
          target: backendTarget,
          changeOrigin: true
        },
        "/keycloak": {
          target: keycloakTarget,
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/keycloak/, "")
        },
        "/resources": {
          target: keycloakTarget,
          changeOrigin: true
        },
        "/realms": {
          target: keycloakTarget,
          changeOrigin: true
        }
      }
    }
  };
});
