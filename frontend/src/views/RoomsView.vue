<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import { createRoom, deleteRoom, fetchHotels, fetchRooms, getErrorMessage, updateRoom } from "../lib/api";
import { formatPrice } from "../lib/roomVisuals";

const auth = useAuthStore();
const rooms = ref([]);
const hotels = ref([]);
const loading = ref(false);
const saving = ref(false);
const error = ref("");
const ok = ref("");
const showAuthModal = ref(false);
const authModalType = ref("auth");
const imageInputRef = ref(null);
const editingRoomId = ref("");
const actionLoading = ref(false);

const form = reactive({
  roomNumber: "",
  roomType: "",
  nightlyRate: "",
  imageFile: null,
  shortDescription: "",
  hotelId: ""
});

const editForm = reactive({
  roomNumber: "",
  roomType: "",
  nightlyRate: "",
  shortDescription: ""
});

const hotelNameMap = computed(() =>
  hotels.value.reduce((acc, hotel) => {
    acc[hotel.id] = hotel.name;
    return acc;
  }, {})
);

const roomCards = computed(() =>
  rooms.value.map((room) => ({
    ...room,
    hotelName: hotelNameMap.value[room.hotelId] || room.hotelId,
    priceLabel: formatPrice(room.nightlyRate)
  }))
);

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

async function loadHotels() {
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
  await Promise.all([loadRooms(), loadHotels()]);
}

function onImageSelected(event) {
  const file = event.target.files?.[0] || null;
  form.imageFile = file;
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
    if (!form.imageFile) {
      throw new Error("Room rasm faylini tanlang.");
    }

    const payload = new FormData();
    payload.set("roomNumber", form.roomNumber);
    payload.set("roomType", form.roomType);
    payload.set("nightlyRate", String(Number(form.nightlyRate)));
    payload.set("imageFile", form.imageFile);
    if (form.shortDescription) {
      payload.set("shortDescription", form.shortDescription);
    }
    if (auth.hasSuperAdminRole && form.hotelId) {
      payload.set("hotelId", form.hotelId);
    }

    if (auth.hasSuperAdminRole && !form.hotelId) {
      throw new Error("Super admin room yaratishda hotel tanlashi kerak.");
    }

    await createRoom(payload);

    form.roomNumber = "";
    form.roomType = "";
    form.nightlyRate = "";
    form.imageFile = null;
    form.shortDescription = "";
    if (imageInputRef.value) {
      imageInputRef.value.value = "";
    }
    if (auth.hasSuperAdminRole && hotels.value.length) {
      form.hotelId = hotels.value[0].id;
    }

    ok.value = "Room created";
    await loadRooms();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    saving.value = false;
  }
}

function startEditRoom(room) {
  if (!room) {
    return;
  }
  editingRoomId.value = room.id;
  editForm.roomNumber = room.roomNumber;
  editForm.roomType = room.roomType;
  editForm.nightlyRate = room.nightlyRate;
  editForm.shortDescription = room.shortDescription || "";
  error.value = "";
  ok.value = "";
}

function selectEditRoom() {
  const room = rooms.value.find((item) => item.id === editingRoomId.value);
  if (!room) {
    cancelEditRoom();
    return;
  }
  startEditRoom(room);
}

function cancelEditRoom() {
  editingRoomId.value = "";
  editForm.roomNumber = "";
  editForm.roomType = "";
  editForm.nightlyRate = "";
  editForm.shortDescription = "";
}

async function submitRoomUpdate() {
  if (!auth.hasAdminRole || !editingRoomId.value) {
    error.value = "Room edit faqat ADMIN uchun.";
    return;
  }

  actionLoading.value = true;
  error.value = "";
  ok.value = "";

  try {
    await updateRoom(editingRoomId.value, {
      roomNumber: editForm.roomNumber,
      roomType: editForm.roomType,
      nightlyRate: Number(editForm.nightlyRate),
      shortDescription: editForm.shortDescription || null
    });
    ok.value = "Room updated";
    cancelEditRoom();
    await loadRooms();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

async function removeRoom(room) {
  if (!auth.hasAdminRole) {
    error.value = "Room delete faqat ADMIN uchun.";
    return;
  }
  const agreed = window.confirm(`Delete room ${room.roomNumber}?`);
  if (!agreed) {
    return;
  }

  actionLoading.value = true;
  error.value = "";
  ok.value = "";

  try {
    await deleteRoom(room.id);
    if (editingRoomId.value === room.id) {
      cancelEditRoom();
    }
    ok.value = "Room deleted";
    await loadRooms();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    actionLoading.value = false;
  }
}

onMounted(loadAll);
</script>

<template>
  <section class="page-grid">
    <article class="panel">
      <h2>Rooms Management</h2>
      <p style="margin-top: 8px">Create/update/delete amallari faqat ADMIN roli uchun ochiq.</p>

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

        <div class="field">
          <label>Room image file</label>
          <input ref="imageInputRef" type="file" accept="image/*" required @change="onImageSelected" />
        </div>

        <div class="field" style="grid-column: 1 / -1">
          <label>Short description</label>
          <input v-model="form.shortDescription" type="text" maxlength="280" placeholder="Room card description" />
        </div>

        <div class="field" v-if="auth.hasSuperAdminRole">
          <label>Hotel</label>
          <select v-model="form.hotelId" required>
            <option v-for="hotel in hotels" :key="hotel.id" :value="hotel.id">
              {{ hotel.code }} / {{ hotel.name }}
            </option>
          </select>
        </div>

        <div class="actions" style="grid-column: 1 / -1">
          <button class="btn btn-primary" type="submit" :disabled="saving">
            {{ saving ? "Saving..." : "Create room" }}
          </button>
          <button class="btn btn-secondary" type="button" :disabled="loading" @click="loadAll">
            {{ loading ? "Loading..." : "Refresh" }}
          </button>
        </div>
      </form>

      <p v-if="error" class="message error" style="margin-top: 10px">{{ error }}</p>
      <p v-if="ok" class="message ok" style="margin-top: 10px">{{ ok }}</p>
    </article>

    <article v-if="auth.hasAdminRole" class="panel">
      <h2>Room Edit/Delete (ADMIN)</h2>

      <form class="form-grid" style="margin-top: 12px" @submit.prevent="submitRoomUpdate">
        <div class="field">
          <label>Room to edit</label>
          <select v-model="editingRoomId" required @change="selectEditRoom">
            <option value="" disabled>Select room</option>
            <option v-for="room in rooms" :key="room.id" :value="room.id">
              {{ room.roomNumber }} / {{ room.roomType }}
            </option>
          </select>
        </div>

        <div class="field">
          <label>Room number</label>
          <input v-model="editForm.roomNumber" type="text" maxlength="20" required :disabled="!editingRoomId" />
        </div>

        <div class="field">
          <label>Room type</label>
          <input v-model="editForm.roomType" type="text" maxlength="50" required :disabled="!editingRoomId" />
        </div>

        <div class="field">
          <label>Nightly rate</label>
          <input
            v-model="editForm.nightlyRate"
            type="number"
            min="0"
            step="0.01"
            required
            :disabled="!editingRoomId"
          />
        </div>

        <div class="field" style="grid-column: 1 / -1">
          <label>Short description</label>
          <input
            v-model="editForm.shortDescription"
            type="text"
            maxlength="280"
            placeholder="Room card description"
            :disabled="!editingRoomId"
          />
        </div>

        <div class="actions" style="grid-column: 1 / -1">
          <button class="btn btn-primary" type="submit" :disabled="!editingRoomId || actionLoading">
            {{ actionLoading ? "Saving..." : "Update room" }}
          </button>
          <button class="btn btn-secondary" type="button" :disabled="!editingRoomId || actionLoading" @click="cancelEditRoom">
            Cancel edit
          </button>
        </div>
      </form>
    </article>

    <article class="section-block">
      <div class="section-head">
        <div>
          <p class="brand-kicker">Visual Catalog</p>
          <h3>Room photos and pricing cards</h3>
        </div>
      </div>

      <div class="room-grid">
        <article
          v-for="(room, index) in roomCards"
          :key="room.id"
          class="room-card reveal-up"
          :style="{ animationDelay: `${index * 70}ms` }"
        >
          <img class="room-image" :src="room.imageUrl" :alt="room.roomType" />
          <div class="room-body">
            <div class="room-meta">
              <span class="mono">#{{ room.roomNumber }}</span>
              <span class="price-chip">{{ room.priceLabel }} / night</span>
            </div>
            <h4>{{ room.roomType }}</h4>
            <p>{{ room.shortDescription }}</p>
            <p class="mono" style="margin-top: 8px">Hotel: {{ room.hotelName }}</p>
          </div>
        </article>
      </div>
    </article>

    <article class="panel">
      <h2>Room table</h2>
      <div class="table-wrap" style="margin-top: 12px">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Hotel</th>
              <th>Number</th>
              <th>Type</th>
              <th>Nightly rate</th>
              <th>Image URL</th>
              <th>Created</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="room in rooms" :key="room.id">
              <td class="mono">{{ room.id }}</td>
              <td>{{ hotelNameMap[room.hotelId] || room.hotelId }}</td>
              <td>{{ room.roomNumber }}</td>
              <td>{{ room.roomType }}</td>
              <td>{{ room.nightlyRate }}</td>
              <td class="mono">{{ room.imageUrl }}</td>
              <td>{{ new Date(room.createdAt).toLocaleString() }}</td>
              <td>
                <div class="table-actions" v-if="auth.hasAdminRole">
                  <button class="btn btn-secondary" type="button" :disabled="actionLoading" @click="startEditRoom(room)">
                    Edit
                  </button>
                  <button class="btn btn-danger" type="button" :disabled="actionLoading" @click="removeRoom(room)">
                    Delete
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!rooms.length">
              <td colspan="8">No rooms yet</td>
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
  background: rgba(18, 28, 45, 0.58);
  backdrop-filter: blur(4px);
  display: grid;
  place-items: center;
  padding: 16px;
  z-index: 1000;
}

.auth-modal {
  width: min(520px, 94vw);
}

.table-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
