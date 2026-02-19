import axios from "axios";
import { config } from "./config";

export const http = axios.create({
  baseURL: config.apiBase,
  timeout: 15000
});

http.interceptors.request.use((request) => {
  const token = localStorage.getItem("ag_access_token");
  if (token) {
    request.headers.Authorization = `Bearer ${token}`;
  }
  return request;
});
