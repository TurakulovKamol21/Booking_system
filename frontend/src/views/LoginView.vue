<script setup>
import { computed, reactive, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { getErrorMessage } from "../lib/api";
import { config } from "../lib/config";

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const form = reactive({
  username: "",
  password: ""
});

const loading = ref(false);
const error = ref("");

const canSubmit = computed(() => Boolean(form.username.trim() && form.password));
const registerTarget = computed(() => {
  const next = typeof route.query.next === "string" ? route.query.next : "/";
  return { name: "register", query: { next } };
});

async function submitLogin() {
  if (!canSubmit.value) {
    error.value = "Username va password kiriting.";
    return;
  }

  loading.value = true;
  error.value = "";

  try {
    await auth.login({
      username: form.username.trim(),
      password: form.password
    });

    const next = typeof route.query.next === "string" ? route.query.next : "/";
    router.push(next);
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

function continueWithoutToken() {
  auth.continueWithoutToken();
  router.push("/");
}
</script>

<template>
  <section class="page-grid">
    <article class="panel" style="max-width: 540px; margin: 0 auto; width: 100%">
      <h2>Sign in</h2>
      <p style="margin-top: 8px">Login Keycloak orqali qilinadi.</p>

      <form class="form-grid" style="margin-top: 14px" @submit.prevent="submitLogin">
        <div class="field" style="grid-column: 1 / -1">
          <label>Username</label>
          <input v-model="form.username" type="text" autocomplete="username" required />
        </div>

        <div class="field" style="grid-column: 1 / -1">
          <label>Password</label>
          <input v-model="form.password" type="password" autocomplete="current-password" required />
        </div>

        <div class="actions" style="grid-column: 1 / -1">
          <button class="btn btn-primary" type="submit" :disabled="loading || !canSubmit">
            {{ loading ? "Signing in..." : "Login" }}
          </button>
          <RouterLink class="btn btn-secondary" :to="registerTarget">Register</RouterLink>
          <button
            v-if="!config.requireAuth"
            class="btn btn-secondary"
            type="button"
            @click="continueWithoutToken"
          >
            Continue without token
          </button>
        </div>
      </form>

      <p v-if="error" class="message error" style="margin-top: 10px">{{ error }}</p>
    </article>
  </section>
</template>
