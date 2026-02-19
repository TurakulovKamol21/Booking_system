import { createRouter, createWebHistory } from "vue-router";
import { config } from "../lib/config";
import DashboardView from "../views/DashboardView.vue";
import LoginView from "../views/LoginView.vue";
import RegisterView from "../views/RegisterView.vue";
import GuestsView from "../views/GuestsView.vue";
import RoomsView from "../views/RoomsView.vue";
import BookingsView from "../views/BookingsView.vue";
import HotelsView from "../views/HotelsView.vue";

const routes = [
  { path: "/", name: "dashboard", component: DashboardView, meta: { requiresAuth: false } },
  { path: "/guests", name: "guests", component: GuestsView, meta: { requiresAuth: true } },
  { path: "/hotels", name: "hotels", component: HotelsView, meta: { requiresAuth: false } },
  { path: "/rooms", name: "rooms", component: RoomsView, meta: { requiresAuth: true } },
  { path: "/bookings", name: "bookings", component: BookingsView, meta: { requiresAuth: true } },
  { path: "/login", name: "login", component: LoginView },
  { path: "/register", name: "register", component: RegisterView }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

router.beforeEach((to) => {
  if (!config.requireAuth) {
    return true;
  }

  const hasToken = Boolean(localStorage.getItem("ag_access_token"));
  if (to.meta.requiresAuth && !hasToken) {
    return { name: "login", query: { next: to.fullPath } };
  }
  if ((to.name === "login" || to.name === "register") && hasToken) {
    return { name: "dashboard" };
  }
  return true;
});

export default router;
