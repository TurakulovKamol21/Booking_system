<script setup>
import { onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import { createGuest, fetchGuests, getErrorMessage } from "../lib/api";

const auth = useAuthStore();
const guests = ref([]);
const loading = ref(false);
const saving = ref(false);
const error = ref("");
const ok = ref("");
const showAuthModal = ref(false);
const authModalType = ref("auth");

const form = reactive({
  fullName: "",
  email: ""
});

async function loadGuests() {
  loading.value = true;
  error.value = "";
  try {
    guests.value = await fetchGuests();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

async function submitGuest() {
  if (!auth.hasToken) {
    authModalType.value = "auth";
    showAuthModal.value = true;
    error.value = "Guest yaratish uchun avval login yoki register qiling.";
    return;
  }
  if (!auth.hasAdminRole) {
    authModalType.value = "admin";
    showAuthModal.value = true;
    error.value = "Guest create/update/delete faqat ADMIN uchun.";
    return;
  }

  saving.value = true;
  error.value = "";
  ok.value = "";
  try {
    await createGuest(form);
    form.fullName = "";
    form.email = "";
    ok.value = "Guest created";
    await loadGuests();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    saving.value = false;
  }
}

onMounted(loadGuests);
</script>

<template>
  <section class="page-grid">
    <article class="panel">
      <h2>Guests</h2>
      <form class="form-grid" style="margin-top: 12px" @submit.prevent="submitGuest">
        <div class="field">
          <label>Full name</label>
          <input v-model="form.fullName" type="text" maxlength="120" required />
        </div>

        <div class="field">
          <label>Email</label>
          <input v-model="form.email" type="email" maxlength="180" required />
        </div>

        <div class="actions" style="grid-column: 1 / -1">
          <button class="btn btn-primary" type="submit" :disabled="saving">
            {{ saving ? "Saving..." : "Create guest" }}
          </button>
          <button class="btn btn-secondary" type="button" :disabled="loading" @click="loadGuests">
            {{ loading ? "Loading..." : "Refresh" }}
          </button>
        </div>
      </form>

      <p v-if="error" class="message error" style="margin-top: 10px">{{ error }}</p>
      <p v-if="ok" class="message ok" style="margin-top: 10px">{{ ok }}</p>
    </article>

    <article class="panel">
      <h2>Guest list</h2>
      <div class="table-wrap" style="margin-top: 12px">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Full name</th>
              <th>Email</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="guest in guests" :key="guest.id">
              <td class="mono">{{ guest.id }}</td>
              <td>{{ guest.fullName }}</td>
              <td>{{ guest.email }}</td>
              <td>{{ new Date(guest.createdAt).toLocaleString() }}</td>
            </tr>
            <tr v-if="!guests.length">
              <td colspan="4">No guests yet</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <div v-if="showAuthModal" class="auth-overlay">
      <article class="auth-modal panel">
        <h2>{{ authModalType === "admin" ? "Admin role required" : "Authentication required" }}</h2>
        <p style="margin-top: 8px">
          {{
            authModalType === "admin"
              ? "Bu amal faqat ADMIN roli uchun ruxsat etilgan."
              : "Guest qo'shishdan oldin tizimga kirishingiz kerak."
          }}
        </p>
        <div class="actions" style="margin-top: 14px">
          <RouterLink
            class="btn btn-primary"
            to="/login"
            @click="
              showAuthModal = false;
              if (authModalType === 'admin') auth.logout();
            "
          >
            {{ authModalType === "admin" ? "Login as Admin" : "Login" }}
          </RouterLink>
          <RouterLink
            v-if="authModalType !== 'admin'"
            class="btn btn-secondary"
            to="/register"
            @click="showAuthModal = false"
          >
            Register
          </RouterLink>
          <button class="btn btn-danger" type="button" @click="showAuthModal = false">
            Close
          </button>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.auth-overlay {
  position: fixed;
  inset: 0;
  background: rgba(3, 8, 14, 0.72);
  display: grid;
  place-items: center;
  padding: 16px;
  z-index: 1000;
}

.auth-modal {
  width: min(520px, 94vw);
}
</style>
