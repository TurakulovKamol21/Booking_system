<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../stores/auth";
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
const fallbackHotelImage =
  "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1800&q=80";

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

async function submitHotel() {
  if (!auth.hasSuperAdminRole) {
    manageError.value = "Hotel create/edit/delete faqat SUPER_ADMIN uchun.";
    return;
  }

  manageSaving.value = true;
  manageError.value = "";
  manageOk.value = "";

  try {
    const payload = {
      code: hotelForm.code,
      name: hotelForm.name,
      city: hotelForm.city,
      country: hotelForm.country,
      addressLine: hotelForm.addressLine,
      imageUrl: hotelForm.imageUrl || null
    };

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

async function removeHotel(hotel) {
  if (!auth.hasSuperAdminRole) {
    manageError.value = "Hotel delete faqat SUPER_ADMIN uchun.";
    return;
  }

  const agreed = window.confirm(`Delete hotel "${hotel.name}"?`);
  if (!agreed) {
    return;
  }

  manageSaving.value = true;
  manageError.value = "";
  manageOk.value = "";

  try {
    await deleteHotel(hotel.id);
    if (selectedHotelId.value === hotel.id) {
      selectedHotelId.value = "";
      rooms.value = [];
    }
    manageOk.value = "Hotel deleted";
    resetHotelForm();
    await refreshAll();
  } catch (err) {
    manageError.value = getErrorMessage(err);
  } finally {
    manageSaving.value = false;
  }
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

    <article v-if="auth.hasSuperAdminRole" class="panel">
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
                  <button class="btn btn-secondary" type="button" :disabled="manageSaving" @click="startEditHotel(hotel)">
                    Edit
                  </button>
                  <button class="btn btn-danger" type="button" :disabled="manageSaving" @click="removeHotel(hotel)">
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
  </section>
</template>

<style scoped>
.hotel-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.hotel-card {
  border: 1px solid #e2e6f0;
  border-radius: 16px;
  background: #fff;
  text-align: left;
  padding: 0;
  cursor: pointer;
  display: grid;
  gap: 0;
  transition: transform 160ms ease, box-shadow 160ms ease, border-color 160ms ease;
  overflow: hidden;
}

.hotel-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 22px rgba(31, 45, 71, 0.1);
  border-color: #ffd2c7;
}

.hotel-card.active {
  border-color: #ff8f76;
  background: #fff8f5;
  box-shadow: 0 12px 24px rgba(240, 68, 39, 0.12);
}

.hotel-card h4 {
  margin: 0;
  font-size: 20px;
  color: #1a2235;
}

.hotel-card p {
  margin: 0;
}

.hotel-image {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.hotel-body {
  display: grid;
  gap: 8px;
  padding: 12px 14px 14px;
}

.address {
  color: #6a7388;
}

.table-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
