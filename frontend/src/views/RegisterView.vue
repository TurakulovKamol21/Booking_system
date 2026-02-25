<script setup>
import { computed, reactive, ref } from "vue";
import { useRoute } from "vue-router";
import { getKeycloakRegistrationUrl } from "../lib/api";

const route = useRoute();
const opened = ref(false);

const form = reactive({
  username: "",
  email: ""
});

const nextPath = computed(() => {
  const next = typeof route.query.next === "string" ? route.query.next : "/";
  return next.startsWith("/") ? next : "/";
});

const registrationUrl = computed(() =>
  getKeycloakRegistrationUrl({
    next: nextPath.value,
    loginHint: form.username.trim() || form.email.trim()
  })
);

function onOpenRegistration() {
  opened.value = true;
}
</script>

<template>
  <section class="page-grid">
    <article class="panel" style="max-width: 840px; margin: 0 auto; width: 100%">
      <h2>Create account</h2>
      <p style="margin-top: 8px">
        Booking qilish uchun akkaunt kerak. Quyidagi ma'lumotlarni kiriting va Keycloak registration sahifasini oching.
      </p>

      <form class="form-grid" style="margin-top: 12px" @submit.prevent>
        <div class="field">
          <label>Username (recommended)</label>
          <input
            v-model="form.username"
            type="text"
            maxlength="120"
            autocomplete="username"
            placeholder="your_username"
          />
        </div>

        <div class="field">
          <label>Email</label>
          <input
            v-model="form.email"
            type="email"
            maxlength="180"
            autocomplete="email"
            placeholder="you@example.com"
          />
        </div>
      </form>

      <div class="actions" style="margin-top: 14px">
        <a class="btn btn-primary" :href="registrationUrl" rel="noreferrer" @click="onOpenRegistration">
          Open Registration
        </a>
        <RouterLink class="btn btn-secondary" :to="{ name: 'login', query: { next: nextPath } }">
          Back to Login
        </RouterLink>
      </div>

      <article class="panel" style="margin-top: 14px; background: var(--bg-soft-2)">
        <p class="message"><span class="mono">Next page after login:</span> {{ nextPath }}</p>
        <p class="message" style="margin-top: 8px">
          1. Registration sahifasida username, password va emailni to'ldiring.
        </p>
        <p class="message" style="margin-top: 4px">
          2. Ro'yxatdan o'tgach login qiling, tizim sizni booking sahifasiga qaytaradi.
        </p>
        <p v-if="opened" class="message ok" style="margin-top: 8px">
          Registration sahifasi ochildi. Tugatgach login qiling.
        </p>
      </article>
    </article>
  </section>
</template>
