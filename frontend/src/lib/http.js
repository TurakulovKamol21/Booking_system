import axios from "axios";
import { config } from "./config";
import { TOKEN_KEY, clearStoredSession, isTokenExpired } from "../stores/auth";

export const http = axios.create({
  baseURL: config.apiBase,
  timeout: 15000
});

http.interceptors.request.use((request) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (!token) {
    return request;
  }

  if (isTokenExpired(token)) {
    clearStoredSession();
    return request;
  }

  request.headers.Authorization = `Bearer ${token}`;
  return request;
});

let authRedirectInProgress = false;

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    const hadToken = Boolean(localStorage.getItem(TOKEN_KEY));

    if (status === 401 && hadToken) {
      clearStoredSession();

      if (config.requireAuth && !authRedirectInProgress) {
        const currentPath = `${window.location.pathname}${window.location.search}`;
        const isAuthRoute = currentPath.startsWith("/login") || currentPath.startsWith("/register");

        if (!isAuthRoute) {
          authRedirectInProgress = true;
          const next = encodeURIComponent(currentPath || "/");
          window.location.assign(`/login?next=${next}`);
        }
      }
    }

    return Promise.reject(error);
  }
);
