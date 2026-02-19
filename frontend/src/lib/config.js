export const config = {
  apiBase: import.meta.env.VITE_API_BASE || "",
  keycloakBase: import.meta.env.VITE_KEYCLOAK_BASE || "/keycloak",
  keycloakRealm: import.meta.env.VITE_KEYCLOAK_REALM || "autoguide",
  keycloakClientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || "autoguide-api",
  requireAuth: import.meta.env.VITE_REQUIRE_AUTH === "true"
};
