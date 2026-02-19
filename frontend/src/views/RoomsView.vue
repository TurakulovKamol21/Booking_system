<script setup>
import { onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import { createRoom, fetchRooms, getErrorMessage } from "../lib/api";

const auth = useAuthStore();
const rooms = ref([]);
const loading = ref(false);
const saving = ref(false);
const error = ref("");
const ok = ref("");
const showAuthModal = ref(false);
const authModalType = ref("auth");

const form = reactive({
  roomNumber: "",
  roomType: "",
  nightlyRate: ""
});

async function loadRooms() {
  loading.value = true;
  error.value = "";
  try {
    rooms.value = await fetchRooms();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

async function submitRoom() {
  if (!auth.hasToken) {
    authModalType.value = "auth";
    showAuthModal.value = true;
    error.value = "Room yaratish uchun avval login yoki register qiling.";
    return;
  }
  if (!auth.hasAdminRole) {
    authModalType.value = "admin";
    showAuthModal.value = true;
    error.value = "Room create/update/delete faqat ADMIN uchun.";
    return;
  }

  saving.value = true;
  error.value = "";
  ok.value = "";
  try {
    await createRoom({
      roomNumber: form.roomNumber,
      roomType: form.roomType,
      nightlyRate: Number(form.nightlyRate)
    });
    form.roomNumber = "";
    form.roomType = "";
    form.nightlyRate = "";
    ok.value = "Room created";
    await loadRooms();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    saving.value = false;
  }
}

onMounted(loadRooms);
</script>

<template>
  <section class="page-grid">
    <article class="panel">
      <h2>Rooms</h2>
      <form class="form-grid" style="margin-top: 12px" @submit.prevent="submitRoom">
        <div class="field">
          <label>Room number</label>
          <input v-model="form.roomNumber" type="text" maxlength="20" required />
        </div>

        <div class="field">
          <label>Room type</label>
          <input v-model="form.roomType" type="text" maxlength="50" required />
        </div>

        <div class="field">
          <label>Nightly rate</label>
          <input v-model="form.nightlyRate" type="number" min="0" step="0.01" required />
        </div>

        <div class="actions" style="grid-column: 1 / -1">
          <button class="btn btn-primary" type="submit" :disabled="saving">
            {{ saving ? "Saving..." : "Create room" }}
          </button>
          <button class="btn btn-secondary" type="button" :disabled="loading" @click="loadRooms">
            {{ loading ? "Loading..." : "Refresh" }}
          </button>
        </div>
      </form>

      <p v-if="error" class="message error" style="margin-top: 10px">{{ error }}</p>
      <p v-if="ok" class="message ok" style="margin-top: 10px">{{ ok }}</p>
    </article>

    <article class="panel">
      <h2>Room list</h2>
      <div class="table-wrap" style="margin-top: 12px">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Number</th>
              <th>Type</th>
              <th>Nightly rate</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="room in rooms" :key="room.id">
              <td class="mono">{{ room.id }}</td>
              <td>{{ room.roomNumber }}</td>
              <td>{{ room.roomType }}</td>
              <td>{{ room.nightlyRate }}</td>
              <td>{{ new Date(room.createdAt).toLocaleString() }}</td>
            </tr>
            <tr v-if="!rooms.length">
              <td colspan="5">No rooms yet</td>
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
              : "Room qo'shishdan oldin tizimga kirishingiz kerak."
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
