<script setup>
import { computed } from "vue";
import { getKeycloakRegistrationUrl } from "../lib/api";

const registrationUrl = computed(() => getKeycloakRegistrationUrl());
</script>

<template>
  <section class="page-grid">
    <article class="panel" style="max-width: 760px; margin: 0 auto; width: 100%">
      <h2>Register</h2>
      <p style="margin-top: 8px">
        Bu loyiha auth uchun Keycloak ishlatadi. Register jarayoni Keycloak sahifasida bajariladi.
      </p>

      <div class="actions" style="margin-top: 14px">
        <a class="btn btn-primary" :href="registrationUrl" target="_blank" rel="noreferrer">
          Open Keycloak Registration
        </a>
        <RouterLink class="btn btn-secondary" to="/login">Back to Login</RouterLink>
      </div>

      <p class="message" style="margin-top: 14px">
        Eslatma: signup Keycloak ichida ochilgan bo'lishi kerak (`registrationAllowed=true`).
      </p>
    </article>
  </section>
</template>
