import { defineStore } from "pinia";
import { loginWithKeycloak } from "../lib/api";

export const TOKEN_KEY = "ag_access_token";
export const USERNAME_KEY = "ag_username";
const TOKEN_EXP_SKEW_MS = 30_000;

function decodeBase64Url(encoded) {
  const normalized = encoded.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, "=");
  return atob(padded);
}

export function parseTokenPayload(token) {
  if (!token) {
    return null;
  }

  try {
    const payloadPart = token.split(".")[1];
    if (!payloadPart) {
      return null;
    }

    return JSON.parse(decodeBase64Url(payloadPart));
  } catch {
    return null;
  }
}

export function parseRolesFromToken(token) {
  const payload = parseTokenPayload(token);
  if (!Array.isArray(payload?.realm_access?.roles)) {
    return [];
  }

  return payload.realm_access.roles
    .filter((role) => typeof role === "string")
    .map((role) => role.toLowerCase());
}

export function parseTokenExpiresAt(token) {
  const payload = parseTokenPayload(token);
  if (!payload || typeof payload.exp !== "number") {
    return 0;
  }

  return payload.exp * 1000;
}

export function isTokenExpired(token) {
  const expiresAt = parseTokenExpiresAt(token);
  if (!expiresAt) {
    return false;
  }

  return Date.now() >= expiresAt - TOKEN_EXP_SKEW_MS;
}

export function clearStoredSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USERNAME_KEY);
}

function readStoredSession() {
  const accessToken = localStorage.getItem(TOKEN_KEY) || "";
  const username = localStorage.getItem(USERNAME_KEY) || "";

  if (!accessToken || isTokenExpired(accessToken)) {
    clearStoredSession();
    return {
      accessToken: "",
      username: "",
      roles: [],
      expiresAt: 0
    };
  }

  return {
    accessToken,
    username,
    roles: parseRolesFromToken(accessToken),
    expiresAt: parseTokenExpiresAt(accessToken)
  };
}

export const useAuthStore = defineStore("auth", {
  state: () => readStoredSession(),
  getters: {
    hasToken(state) {
      return Boolean(state.accessToken) && !isTokenExpired(state.accessToken);
    },
    hasSuperAdminRole(state) {
      return state.roles.includes("super_admin");
    },
    hasAdminRole(state) {
      return state.roles.includes("admin");
    },
    hasOperatorRole(state) {
      return state.roles.includes("operator");
    },
    canManageGuests() {
      return this.hasAdminRole || this.hasSuperAdminRole;
    },
    canManageRooms() {
      return this.hasAdminRole;
    },
    canManageHotels() {
      return this.hasSuperAdminRole;
    }
  },
  actions: {
    setSession(accessToken, username) {
      if (!accessToken || isTokenExpired(accessToken)) {
        this.logout();
        throw new Error("Session token is invalid or expired. Login again.");
      }

      this.accessToken = accessToken;
      this.username = username;
      this.roles = parseRolesFromToken(accessToken);
      this.expiresAt = parseTokenExpiresAt(accessToken);
      localStorage.setItem(TOKEN_KEY, this.accessToken);
      localStorage.setItem(USERNAME_KEY, this.username);
    },
    syncSession() {
      if (!this.accessToken) {
        return;
      }

      if (isTokenExpired(this.accessToken)) {
        this.logout();
        return;
      }

      this.roles = parseRolesFromToken(this.accessToken);
      this.expiresAt = parseTokenExpiresAt(this.accessToken);
    },
    ensureValidSession() {
      if (!this.accessToken) {
        return false;
      }

      if (isTokenExpired(this.accessToken)) {
        this.logout();
        return false;
      }

      return true;
    },
    async login(credentials) {
      const tokenData = await loginWithKeycloak(credentials);
      this.setSession(tokenData.access_token, credentials.username);
    },
    logout() {
      this.accessToken = "";
      this.username = "";
      this.roles = [];
      this.expiresAt = 0;
      clearStoredSession();
    },
    continueWithoutToken() {
      this.logout();
    }
  }
});
