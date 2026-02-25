<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import BaseModal from "../components/BaseModal.vue";
import {
  createHotel,
  deleteHotel,
  fetchPublicHotels,
  fetchPublicRoomHighlights,
  getErrorMessage,
  updateHotel
} from "../lib/api";
import { formatPrice } from "../lib/roomVisuals";

const auth = useAuthStore();
const hotels = ref([]);
const rooms = ref([]);
const loadingHotels = ref(false);
const loadingRooms = ref(false);
const error = ref("");
const manageSaving = ref(false);
const manageError = ref("");
const manageOk = ref("");
const editingHotelId = ref("");
const selectedHotelId = ref("");
const showDeleteModal = ref(false);
const deletingHotel = ref(false);
const pendingDeleteHotel = ref(null);

const fallbackHotelImage =
  "/assets/rooms/hero-hotel.svg";

const hotelForm = reactive({
  code: "",
  name: "",
  city: "",
  country: "",
  addressLine: "",
  imageUrl: ""
});

const selectedHotel = computed(() => hotels.value.find((hotel) => hotel.id === selectedHotelId.value) || null);

const roomCards = computed(() =>
  rooms.value.map((room) => ({
    ...room,
    priceLabel: formatPrice(room.nightlyRate)
  }))
);

async function loadHotels() {
  loadingHotels.value = true;
  error.value = "";
  try {
    hotels.value = await fetchPublicHotels();

    if (selectedHotelId.value && !hotels.value.some((hotel) => hotel.id === selectedHotelId.value)) {
      selectedHotelId.value = "";
      rooms.value = [];
    }
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    loadingHotels.value = false;
  }
}

async function selectHotel(hotelId) {
  selectedHotelId.value = hotelId;
  loadingRooms.value = true;
  error.value = "";

  try {
    rooms.value = await fetchPublicRoomHighlights(24, hotelId);
  } catch (err) {
    error.value = getErrorMessage(err);
    rooms.value = [];
  } finally {
    loadingRooms.value = false;
  }
}

async function refreshAll() {
  await loadHotels();
  if (selectedHotelId.value) {
    await selectHotel(selectedHotelId.value);
  } else {
    rooms.value = [];
  }
}

function resetHotelForm() {
  hotelForm.code = "";
  hotelForm.name = "";
  hotelForm.city = "";
  hotelForm.country = "";
  hotelForm.addressLine = "";
  hotelForm.imageUrl = "";
  editingHotelId.value = "";
}

function startEditHotel(hotel) {
  editingHotelId.value = hotel.id;
  hotelForm.code = hotel.code || "";
  hotelForm.name = hotel.name || "";
  hotelForm.city = hotel.city || "";
  hotelForm.country = hotel.country || "";
  hotelForm.addressLine = hotel.addressLine || "";
  hotelForm.imageUrl = hotel.imageUrl || "";
  manageError.value = "";
  manageOk.value = "";
}

function buildHotelPayload() {
  const payload = {
    code: hotelForm.code.trim().toUpperCase(),
    name: hotelForm.name.trim(),
    city: hotelForm.city.trim(),
    country: hotelForm.country.trim(),
    addressLine: hotelForm.addressLine.trim(),
    imageUrl: hotelForm.imageUrl.trim() || null
  };

  if (!payload.code || !payload.name || !payload.city || !payload.country || !payload.addressLine) {
    throw new Error("Code, name, city, country va address line majburiy.");
  }

  return payload;
}

async function submitHotel() {
  if (!auth.canManageHotels) {
    manageError.value = "Hotel create/edit/delete faqat SUPER_ADMIN uchun.";
    return;
  }

  manageSaving.value = true;
  manageError.value = "";
  manageOk.value = "";

  try {
    const payload = buildHotelPayload();

    if (editingHotelId.value) {
      await updateHotel(editingHotelId.value, payload);
      manageOk.value = "Hotel updated";
    } else {
      await createHotel(payload);
      manageOk.value = "Hotel created";
    }

    resetHotelForm();
    await refreshAll();
  } catch (err) {
    manageError.value = getErrorMessage(err);
  } finally {
    manageSaving.value = false;
  }
}

function requestDeleteHotel(hotel) {
  if (!auth.canManageHotels) {
    manageError.value = "Hotel delete faqat SUPER_ADMIN uchun.";
    return;
  }

  pendingDeleteHotel.value = hotel;
  showDeleteModal.value = true;
}

async function confirmDeleteHotel() {
  if (!pendingDeleteHotel.value) {
    showDeleteModal.value = false;
    return;
  }

  deletingHotel.value = true;
  manageSaving.value = true;
  manageError.value = "";
  manageOk.value = "";

  try {
    await deleteHotel(pendingDeleteHotel.value.id);
    if (selectedHotelId.value === pendingDeleteHotel.value.id) {
      selectedHotelId.value = "";
      rooms.value = [];
    }
    manageOk.value = "Hotel deleted";
    resetHotelForm();
    await refreshAll();
  } catch (err) {
    manageError.value = getErrorMessage(err);
  } finally {
    deletingHotel.value = false;
    manageSaving.value = false;
    showDeleteModal.value = false;
    pendingDeleteHotel.value = null;
  }
}

function cancelDeleteHotel() {
  showDeleteModal.value = false;
  pendingDeleteHotel.value = null;
}

onMounted(loadHotels);
</script>

<template>
  <section class="page-grid">
    <article class="panel">
      <h2>Hotels</h2>
      <p style="margin-top: 8px">Hotel card ustiga bosing, shu hotelga tegishli roomlar pastda chiqadi.</p>
      <div class="actions" style="margin-top: 12px">
        <button class="btn btn-secondary" type="button" :disabled="loadingHotels || loadingRooms" @click="refreshAll">
          {{ loadingHotels || loadingRooms ? "Loading..." : "Refresh" }}
        </button>
      </div>
      <p v-if="error" class="message error" style="margin-top: 10px">{{ error }}</p>
    </article>

    <article v-if="auth.canManageHotels" class="panel">
      <h2>Hotel Management (SUPER_ADMIN)</h2>
      <form class="form-grid" style="margin-top: 12px" @submit.prevent="submitHotel">
        <div class="field">
          <label>Code</label>
          <input v-model="hotelForm.code" type="text" maxlength="40" required />
        </div>
        <div class="field">
          <label>Name</label>
          <input v-model="hotelForm.name" type="text" maxlength="140" required />
        </div>
        <div class="field">
          <label>City</label>
          <input v-model="hotelForm.city" type="text" maxlength="80" required />
        </div>
        <div class="field">
          <label>Country</label>
          <input v-model="hotelForm.country" type="text" maxlength="80" required />
        </div>
        <div class="field" style="grid-column: 1 / -1">
          <label>Address line</label>
          <input v-model="hotelForm.addressLine" type="text" maxlength="200" required />
        </div>
        <div class="field" style="grid-column: 1 / -1">
          <label>Image URL</label>
          <input v-model="hotelForm.imageUrl" type="text" maxlength="400" placeholder="https://..." />
        </div>

        <div class="actions" style="grid-column: 1 / -1">
          <button class="btn btn-primary" type="submit" :disabled="manageSaving">
            {{ manageSaving ? "Saving..." : editingHotelId ? "Update hotel" : "Create hotel" }}
          </button>
          <button class="btn btn-secondary" type="button" :disabled="manageSaving" @click="resetHotelForm">
            Reset
          </button>
        </div>
      </form>

      <p v-if="manageError" class="message error" style="margin-top: 10px">{{ manageError }}</p>
      <p v-if="manageOk" class="message ok" style="margin-top: 10px">{{ manageOk }}</p>

      <div class="table-wrap" style="margin-top: 12px">
        <table>
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>City</th>
              <th>Country</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="hotel in hotels" :key="hotel.id">
              <td class="mono">{{ hotel.code }}</td>
              <td>{{ hotel.name }}</td>
              <td>{{ hotel.city }}</td>
              <td>{{ hotel.country }}</td>
              <td>
                <div class="table-actions">
                  <button class="btn btn-secondary" type="button" :disabled="manageSaving || deletingHotel" @click="startEditHotel(hotel)">
                    Edit
                  </button>
                  <button class="btn btn-danger" type="button" :disabled="manageSaving || deletingHotel" @click="requestDeleteHotel(hotel)">
                    Delete
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!hotels.length">
              <td colspan="5">No hotels available</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <article class="section-block">
      <div class="section-head">
        <div>
          <p class="brand-kicker">Hotel Catalog</p>
          <h3>Click to open rooms</h3>
        </div>
      </div>

      <div class="hotel-grid">
        <button
          v-for="(hotel, index) in hotels"
          :key="hotel.id"
          type="button"
          class="hotel-card reveal-up"
          :class="{ active: hotel.id === selectedHotelId }"
          :style="{ animationDelay: `${index * 60}ms` }"
          @click="selectHotel(hotel.id)"
        >
          <img class="hotel-image" :src="hotel.imageUrl || fallbackHotelImage" :alt="hotel.name" />
          <div class="hotel-body">
            <p class="mono">{{ hotel.code }}</p>
            <h4>{{ hotel.name }}</h4>
            <p>{{ hotel.city }}, {{ hotel.country }}</p>
            <p class="address">{{ hotel.addressLine }}</p>
          </div>
        </button>

        <article v-if="!hotels.length" class="panel">
          <p>No hotels available</p>
        </article>
      </div>
    </article>

    <article class="section-block">
      <div class="section-head">
        <div>
          <p class="brand-kicker">Rooms</p>
          <h3 v-if="selectedHotel">{{ selectedHotel.name }}</h3>
          <h3 v-else>Hotel tanlang</h3>
        </div>
      </div>

      <p v-if="!selectedHotel" class="message">Avval yuqoridan hotel tanlang.</p>
      <p v-else-if="loadingRooms" class="message">Rooms loading...</p>

      <div v-else class="room-grid">
        <article v-for="(room, index) in roomCards" :key="room.id" class="room-card reveal-up" :style="{ animationDelay: `${index * 70}ms` }">
          <img class="room-image" :src="room.imageUrl" :alt="room.roomType" />
          <div class="room-body">
            <div class="room-meta">
              <span class="mono">#{{ room.roomNumber }}</span>
              <span class="price-chip">{{ room.priceLabel }} / night</span>
            </div>
            <h4>{{ room.roomType }}</h4>
            <p>{{ room.shortDescription }}</p>
          </div>
        </article>

        <article v-if="selectedHotel && !roomCards.length" class="panel">
          <p>Bu hotel uchun room topilmadi.</p>
        </article>
      </div>
    </article>

    <BaseModal v-model="showDeleteModal" title="Delete hotel" :dismissible="!deletingHotel" @close="pendingDeleteHotel = null">
      <template #default>
        <p v-if="pendingDeleteHotel">
          <span class="mono">{{ pendingDeleteHotel.code }}</span> / {{ pendingDeleteHotel.name }} hotelini o'chirasizmi?
        </p>
      </template>
      <template #actions>
        <button class="btn btn-danger" type="button" :disabled="deletingHotel" @click="confirmDeleteHotel">
          {{ deletingHotel ? "Deleting..." : "Delete" }}
        </button>
        <button class="btn btn-secondary" type="button" :disabled="deletingHotel" @click="cancelDeleteHotel">
          Cancel
        </button>
      </template>
    </BaseModal>
  </section>
</template>
