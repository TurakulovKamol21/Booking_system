import { defineStore } from "pinia";
import { loginWithKeycloak } from "../lib/api";

const TOKEN_KEY = "ag_access_token";
const USERNAME_KEY = "ag_username";

function parseRolesFromToken(token) {
  if (!token) {
    return [];
  }

  try {
    const payloadPart = token.split(".")[1];
    if (!payloadPart) {
      return [];
    }

    const normalized = payloadPart.replace(/-/g, "+").replace(/_/g, "/");
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
    const payload = JSON.parse(atob(padded));

    if (!Array.isArray(payload?.realm_access?.roles)) {
      return [];
    }

    return payload.realm_access.roles
      .filter((role) => typeof role === "string")
      .map((role) => role.toLowerCase());
  } catch {
    return [];
  }
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    accessToken: localStorage.getItem(TOKEN_KEY) || "",
    username: localStorage.getItem(USERNAME_KEY) || "",
    roles: parseRolesFromToken(localStorage.getItem(TOKEN_KEY) || "")
  }),
  getters: {
    hasToken(state) {
      return Boolean(state.accessToken);
    },
    hasSuperAdminRole(state) {
      return state.roles.includes("super_admin");
    },
    hasAdminRole(state) {
      return state.roles.includes("admin");
    },
    hasOperatorRole(state) {
      return state.roles.includes("operator");
    }
  },
  actions: {
    setSession(accessToken, username) {
      this.accessToken = accessToken;
      this.username = username;
      this.roles = parseRolesFromToken(accessToken);
      localStorage.setItem(TOKEN_KEY, this.accessToken);
      localStorage.setItem(USERNAME_KEY, this.username);
    },
    async login(credentials) {
      const tokenData = await loginWithKeycloak(credentials);
      this.setSession(tokenData.access_token, credentials.username);
    },
    logout() {
      this.accessToken = "";
      this.username = "";
      this.roles = [];
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USERNAME_KEY);
    },
    continueWithoutToken() {
      this.logout();
    }
  }
});
