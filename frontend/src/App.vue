<script setup>
import { computed } from "vue";
import { useRoute, useRouter, RouterLink, RouterView } from "vue-router";
import { useAuthStore } from "./stores/auth";
import { config } from "./lib/config";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const publicLinks = [
  { name: "Home", to: "/" },
  { name: "Hotels", to: "/hotels" }
];
const protectedLinks = [
  { name: "Guests", to: "/guests" },
  { name: "Rooms", to: "/rooms" },
  { name: "Bookings", to: "/bookings" }
];

const showProtectedLinks = computed(() => !config.requireAuth || auth.hasToken);

const authBadge = computed(() => {
  if (!config.requireAuth) {
    return "Auth optional mode";
  }
  if (!auth.hasToken) {
    return "Not signed in";
  }

  if (auth.hasSuperAdminRole) {
    return `Super Admin: ${auth.username}`;
  }

  if (auth.hasAdminRole) {
    return `Admin: ${auth.username}`;
  }

  return `Operator: ${auth.username}`;
});

function logout() {
  auth.logout();
  router.push("/login");
}
</script>

<template>
  <div class="app-shell">
    <header class="top-nav">
      <div class="brand-block">
        <p class="brand-kicker">Autoguide Collection</p>
        <h1>Modern Hotel Booking</h1>
      </div>

      <nav class="nav-links">
        <RouterLink
          v-for="link in publicLinks"
          :key="link.to"
          :to="link.to"
          class="nav-link"
          :class="{ active: route.path === link.to }"
        >
          {{ link.name }}
        </RouterLink>

        <RouterLink
          v-for="link in protectedLinks"
          v-show="showProtectedLinks"
          :key="link.to"
          :to="link.to"
          class="nav-link"
          :class="{ active: route.path === link.to }"
        >
          {{ link.name }}
        </RouterLink>
      </nav>

      <div class="auth-controls">
        <span class="pill">{{ authBadge }}</span>
        <RouterLink v-if="!auth.hasToken" class="btn btn-secondary" to="/login">Login</RouterLink>
        <button v-else class="btn btn-secondary" type="button" @click="logout">Logout</button>
      </div>
    </header>

    <main class="main-shell">
      <RouterView />
    </main>
  </div>
</template>
