<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import BaseModal from "../components/BaseModal.vue";
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
const deleteLoading = ref(false);
const showDeleteModal = ref(false);
const pendingDeleteRoom = ref(null);

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

const authModalTitle = computed(() =>
  authModalType.value === "admin" ? "Admin role required" : "Authentication required"
);

const authModalDescription = computed(() =>
  authModalType.value === "admin"
    ? "Bu amal faqat ADMIN roli uchun ruxsat etilgan."
    : "Room qo'shishdan oldin tizimga kirishingiz kerak."
);

function openAuthModal(type, message) {
  authModalType.value = type;
  showAuthModal.value = true;
  error.value = message;
}

async function loadRooms() {
  if (!auth.hasToken) {
    rooms.value = [];
    return;
  }

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
  await Promise.all([loadRooms(), loadHotels()]);
}

function onImageSelected(event) {
  const file = event.target.files?.[0] || null;

  if (file && !file.type.startsWith("image/")) {
    form.imageFile = null;
    error.value = "Faqat rasm faylini yuklash mumkin.";
    if (imageInputRef.value) {
      imageInputRef.value.value = "";
    }
    return;
  }

  form.imageFile = file;
}

function resetCreateForm() {
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
}

async function submitRoom() {
  if (!auth.ensureValidSession()) {
    openAuthModal("auth", "Room yaratish uchun avval login yoki register qiling.");
    return;
  }

  if (!auth.canManageRooms) {
    openAuthModal("admin", "Room create/update/delete faqat ADMIN uchun.");
    return;
  }

  const nightlyRate = Number(form.nightlyRate);
  if (!Number.isFinite(nightlyRate) || nightlyRate <= 0) {
    error.value = "Nightly rate 0 dan katta bo'lishi kerak.";
    return;
  }

  if (!form.imageFile) {
    error.value = "Room rasm faylini tanlang.";
    return;
  }

  saving.value = true;
  error.value = "";
  ok.value = "";

  try {
    const payload = new FormData();
    payload.set("roomNumber", form.roomNumber.trim());
    payload.set("roomType", form.roomType.trim());
    payload.set("nightlyRate", String(nightlyRate));
    payload.set("imageFile", form.imageFile);

    const shortDescription = form.shortDescription.trim();
    if (shortDescription) {
      payload.set("shortDescription", shortDescription);
    }

    if (auth.hasSuperAdminRole && form.hotelId) {
      payload.set("hotelId", form.hotelId);
    }

    if (auth.hasSuperAdminRole && !form.hotelId) {
      throw new Error("Super admin room yaratishda hotel tanlashi kerak.");
    }

    await createRoom(payload);
    resetCreateForm();
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
  editForm.nightlyRate = String(room.nightlyRate);
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
  if (!auth.canManageRooms || !editingRoomId.value) {
    error.value = "Room edit faqat ADMIN uchun.";
    return;
  }

  const nightlyRate = Number(editForm.nightlyRate);
  if (!Number.isFinite(nightlyRate) || nightlyRate <= 0) {
    error.value = "Nightly rate 0 dan katta bo'lishi kerak.";
    return;
  }

  actionLoading.value = true;
  error.value = "";
  ok.value = "";

  try {
    await updateRoom(editingRoomId.value, {
      roomNumber: editForm.roomNumber.trim(),
      roomType: editForm.roomType.trim(),
      nightlyRate,
      shortDescription: editForm.shortDescription.trim() || null
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

function requestDeleteRoom(room) {
  if (!auth.canManageRooms) {
    error.value = "Room delete faqat ADMIN uchun.";
    return;
  }

  pendingDeleteRoom.value = room;
  showDeleteModal.value = true;
}

function cancelDeleteRoom() {
  showDeleteModal.value = false;
  pendingDeleteRoom.value = null;
}

async function confirmDeleteRoom() {
  if (!pendingDeleteRoom.value) {
    showDeleteModal.value = false;
    return;
  }

  deleteLoading.value = true;
  error.value = "";
  ok.value = "";

  try {
    await deleteRoom(pendingDeleteRoom.value.id);
    if (editingRoomId.value === pendingDeleteRoom.value.id) {
      cancelEditRoom();
    }
    ok.value = "Room deleted";
    await loadRooms();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    deleteLoading.value = false;
    showDeleteModal.value = false;
    pendingDeleteRoom.value = null;
  }
}

onMounted(loadAll);
</script>

<template>
  <section class="page-grid">
    <article class="panel">
      <h2>Rooms Management</h2>
      <p style="margin-top: 8px">Create/update/delete amallari faqat ADMIN roli uchun ochiq.</p>
      <p v-if="!auth.hasToken" class="message" style="margin-top: 8px">Room bo'limini ishlatish uchun login qiling.</p>

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
          <input v-model="form.nightlyRate" type="number" min="0.01" step="0.01" required />
        </div>

        <div class="field">
          <label>Room image file</label>
          <input ref="imageInputRef" type="file" accept="image/*" required @change="onImageSelected" />
        </div>

        <div class="field" style="grid-column: 1 / -1">
          <label>Short description</label>
          <input v-model="form.shortDescription" type="text" maxlength="280" placeholder="Room card description" />
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

    <article v-if="auth.canManageRooms" class="panel">
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
            min="0.01"
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
                <div v-if="auth.canManageRooms" class="table-actions">
                  <button class="btn btn-secondary" type="button" :disabled="actionLoading || deleteLoading" @click="startEditRoom(room)">
                    Edit
                  </button>
                  <button class="btn btn-danger" type="button" :disabled="actionLoading || deleteLoading" @click="requestDeleteRoom(room)">
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

    <BaseModal
      v-model="showDeleteModal"
      title="Delete room"
      :dismissible="!deleteLoading"
      @close="pendingDeleteRoom = null"
    >
      <template #default>
        <p v-if="pendingDeleteRoom">
          Room <span class="mono">{{ pendingDeleteRoom.roomNumber }}</span> ni o'chirishni tasdiqlaysizmi?
        </p>
      </template>
      <template #actions>
        <button class="btn btn-danger" type="button" :disabled="deleteLoading" @click="confirmDeleteRoom">
          {{ deleteLoading ? "Deleting..." : "Delete" }}
        </button>
        <button class="btn btn-secondary" type="button" :disabled="deleteLoading" @click="cancelDeleteRoom">
          Cancel
        </button>
      </template>
    </BaseModal>
  </section>
</template>
