<script setup>
import { computed } from "vue";
import { useRoute, useRouter, RouterLink, RouterView } from "vue-router";
import { useAuthStore } from "./stores/auth";
import { config } from "./lib/config";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

const links = [
  { name: "Dashboard", to: "/" },
  { name: "Guests", to: "/guests" },
  { name: "Rooms", to: "/rooms" },
  { name: "Bookings", to: "/bookings" }
];

const showAppLinks = computed(() => {
  if (!config.requireAuth) {
    return true;
  }
  return auth.hasToken;
});

const authBadge = computed(() => {
  if (!config.requireAuth) {
    return "Auth optional mode";
  }
  return auth.hasToken ? `Signed in as ${auth.username}` : "Not signed in";
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
        <p class="brand-kicker">Autoguide</p>
        <h1>Hotel Booking Console</h1>
      </div>

      <nav v-if="showAppLinks" class="nav-links">
        <RouterLink
          v-for="link in links"
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
