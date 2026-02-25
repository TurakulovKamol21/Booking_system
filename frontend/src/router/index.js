import { createRouter, createWebHistory } from "vue-router";
import { config } from "../lib/config";
import { useAuthStore } from "../stores/auth";
import DashboardView from "../views/DashboardView.vue";
import LoginView from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import GuestsView from "../views/GuestsView.vue";
import RoomsView from "../views/RoomsView.vue";
import BookingsView from "../views/BookingsView.vue";
import HotelsView from "../views/HotelsView.vue";

const routes = [
  { path: "/", name: "dashboard", component: DashboardView, meta: { requiresAuth: false } },
  { path: "/guests", name: "guests", component: GuestsView, meta: { requiresAuth: true, backofficeOnly: true } },
  { path: "/hotels", name: "hotels", component: HotelsView, meta: { requiresAuth: false } },
  { path: "/rooms", name: "rooms", component: RoomsView, meta: { requiresAuth: true, backofficeOnly: true } },
  { path: "/bookings", name: "bookings", component: BookingsView, meta: { requiresAuth: true, alwaysAuth: true } },
  { path: "/login", name: "login", component: LoginView },
  { path: "/register", name: "register", component: RegisterView },
  { path: "/:pathMatch(.*)*", redirect: "/" }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  const auth = useAuthStore();
  auth.syncSession();
  const hasToken = auth.hasToken;
  const isBackofficeUser = auth.hasSuperAdminRole || auth.hasAdminRole || auth.hasOperatorRole;
  const requiresAuth = Boolean(to.meta.requiresAuth) && (config.requireAuth || Boolean(to.meta.alwaysAuth));

  if (requiresAuth && !hasToken) {
    return { name: "login", query: { next: to.fullPath } };
  }

  if (to.meta.backofficeOnly && !isBackofficeUser) {
    return { name: "bookings" };
  }

  if (!config.requireAuth) {
    if ((to.name === "login" || to.name === "register") && hasToken) {
      return { name: "dashboard" };
    }
    return true;
  }

  if ((to.name === "login" || to.name === "register") && hasToken) {
    return { name: "dashboard" };
  }

  return true;
});

export default router;
