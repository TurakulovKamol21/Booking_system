<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import BaseModal from "../components/BaseModal.vue";
import { createGuest, fetchGuests, fetchHotels, getErrorMessage } from "../lib/api";

const auth = useAuthStore();
const guests = ref([]);
const hotels = ref([]);
const loading = ref(false);
const saving = ref(false);
const error = ref("");
const ok = ref("");
const showAuthModal = ref(false);
const authModalType = ref("auth");

const form = reactive({
  fullName: "",
  email: "",
  hotelId: ""
});

const hotelNameMap = computed(() =>
  hotels.value.reduce((acc, hotel) => {
    acc[hotel.id] = hotel.name;
    return acc;
  }, {})
);

const authModalTitle = computed(() =>
  authModalType.value === "admin" ? "Admin role required" : "Authentication required"
);

const authModalDescription = computed(() =>
  authModalType.value === "admin"
    ? "Bu amal faqat ADMIN yoki SUPER_ADMIN roli uchun ruxsat etilgan."
    : "Guest qo'shishdan oldin tizimga kirishingiz kerak."
);

function openAuthModal(type, message) {
  authModalType.value = type;
  showAuthModal.value = true;
  error.value = message;
}

async function loadGuests() {
  if (!auth.hasToken) {
    guests.value = [];
    return;
  }

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

async function loadHotels() {
  if (!auth.hasToken) {
    hotels.value = [];
    form.hotelId = "";
    return;
  }

  try {
    hotels.value = await fetchHotels();
    if (!form.hotelId && hotels.value.length) {
      form.hotelId = hotels.value[0].id;
    }
  } catch (err) {
    error.value = getErrorMessage(err);
  }
}

async function loadAll() {
  await Promise.all([loadGuests(), loadHotels()]);
}

function resetForm() {
  form.fullName = "";
  form.email = "";
  if (auth.hasSuperAdminRole && hotels.value.length) {
    form.hotelId = hotels.value[0].id;
  }
}

async function submitGuest() {
  if (!auth.ensureValidSession()) {
    openAuthModal("auth", "Guest yaratish uchun avval login yoki register qiling.");
    return;
  }

  if (!auth.canManageGuests) {
    openAuthModal("admin", "Guest create/update/delete faqat ADMIN yoki SUPER_ADMIN uchun.");
    return;
  }

  const fullName = form.fullName.trim();
  const email = form.email.trim().toLowerCase();

  if (!fullName) {
    error.value = "Full name majburiy.";
    return;
  }

  if (!email) {
    error.value = "Email majburiy.";
    return;
  }

  saving.value = true;
  error.value = "";
  ok.value = "";

  try {
    const payload = {
      fullName,
      email,
      hotelId: auth.hasSuperAdminRole ? form.hotelId || null : null
    };

    if (auth.hasSuperAdminRole && !payload.hotelId) {
      throw new Error("Super admin guest yaratishda hotel tanlashi kerak.");
    }

    await createGuest(payload);
    resetForm();
    ok.value = "Guest created";
    await loadGuests();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    saving.value = false;
  }
}

onMounted(loadAll);
</script>

<template>
  <section class="page-grid">
    <article class="panel">
      <h2>Guests</h2>
      <p v-if="!auth.hasToken" class="message" style="margin-top: 8px">Guest bo'limidan foydalanish uchun login qiling.</p>
      <form class="form-grid" style="margin-top: 12px" @submit.prevent="submitGuest">
        <div class="field">
          <label>Full name</label>
          <input v-model="form.fullName" type="text" maxlength="120" required />
        </div>

        <div class="field">
          <label>Email</label>
          <input v-model="form.email" type="email" maxlength="180" required />
        </div>

        <div v-if="auth.hasSuperAdminRole" class="field">
          <label>Hotel</label>
          <select v-model="form.hotelId" required>
            <option v-for="hotel in hotels" :key="hotel.id" :value="hotel.id">
              {{ hotel.code }} / {{ hotel.name }}
            </option>
          </select>
        </div>

        <div class="actions" style="grid-column: 1 / -1">
          <button class="btn btn-primary" type="submit" :disabled="saving">
            {{ saving ? "Saving..." : "Create guest" }}
          </button>
          <button class="btn btn-secondary" type="button" :disabled="loading" @click="loadAll">
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
              <th>Hotel</th>
              <th>Full name</th>
              <th>Email</th>
              <th>Created</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="guest in guests" :key="guest.id">
              <td class="mono">{{ guest.id }}</td>
              <td>{{ hotelNameMap[guest.hotelId] || guest.hotelId }}</td>
              <td>{{ guest.fullName }}</td>
              <td>{{ guest.email }}</td>
              <td>{{ new Date(guest.createdAt).toLocaleString() }}</td>
            </tr>
            <tr v-if="!guests.length">
              <td colspan="5">No guests yet</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <BaseModal v-model="showAuthModal" :title="authModalTitle" @close="authModalType = 'auth'">
      <template #default>
        <p>{{ authModalDescription }}</p>
      </template>
      <template #actions>
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
        <RouterLink v-if="authModalType !== 'admin'" class="btn btn-secondary" to="/register" @click="showAuthModal = false">
          Register
        </RouterLink>
        <button class="btn btn-danger" type="button" @click="showAuthModal = false">Close</button>
      </template>
    </BaseModal>
  </section>
</template>
