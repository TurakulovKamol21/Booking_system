<script setup>
import { onMounted, reactive, ref } from "vue";
import { useAuthStore } from "../stores/auth";
import {
  createBooking,
  fetchBookingRecommendations,
  fetchBookings,
  fetchGuests,
  fetchHotels,
  fetchRooms,
  getErrorMessage,
  updateBookingStatus
} from "../lib/api";

const statuses = ["CREATED", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"];
const auth = useAuthStore();

const loading = ref(false);
const saving = ref(false);
const updating = ref("");
const error = ref("");
const ok = ref("");

const guests = ref([]);
const rooms = ref([]);
const hotels = ref([]);
const bookings = ref([]);
const statusDraft = reactive({});

const filter = reactive({
  status: ""
});

const createForm = reactive({
  guestId: "",
  roomId: "",
  checkInDate: "",
  checkOutDate: ""
});

const recommendations = ref([]);
const selectedBookingId = ref("");
const recommendationsLoading = ref(false);
const showAuthModal = ref(false);

function ensureAuth() {
  if (auth.hasToken) {
    return true;
  }
  showAuthModal.value = true;
  error.value = "Booking amali uchun avval login yoki register qiling.";
  return false;
}

function syncStatusDraft() {
  Object.keys(statusDraft).forEach((key) => delete statusDraft[key]);
  bookings.value.forEach((booking) => {
    statusDraft[booking.id] = booking.status;
  });
}

async function loadBaseData() {
  const [guestData, roomData, hotelData] = await Promise.all([fetchGuests(), fetchRooms(), fetchHotels()]);
  guests.value = guestData;
  rooms.value = roomData;
  hotels.value = hotelData;

  if (!createForm.guestId && guests.value.length) {
    createForm.guestId = guests.value[0].id;
  }
  if (!createForm.roomId && rooms.value.length) {
    createForm.roomId = rooms.value[0].id;
  }
}

function resolveHotelName(hotelId) {
  const hotel = hotels.value.find((item) => item.id === hotelId);
  return hotel ? hotel.name : hotelId;
}

async function loadBookings() {
  bookings.value = await fetchBookings(filter.status);
  syncStatusDraft();
}

async function loadAll() {
  loading.value = true;
  error.value = "";
  try {
    await Promise.all([loadBaseData(), loadBookings()]);
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

async function submitBooking() {
  if (!ensureAuth()) {
    return;
  }

  saving.value = true;
  error.value = "";
  ok.value = "";

  try {
    await createBooking({
      guestId: createForm.guestId,
      roomId: createForm.roomId,
      checkInDate: createForm.checkInDate,
      checkOutDate: createForm.checkOutDate
    });

    createForm.checkInDate = "";
    createForm.checkOutDate = "";
    ok.value = "Booking created";
    await loadBookings();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    saving.value = false;
  }
}

async function applyStatus(bookingId) {
  if (!ensureAuth()) {
    return;
  }

  updating.value = bookingId;
  error.value = "";
  ok.value = "";

  try {
    await updateBookingStatus(bookingId, statusDraft[bookingId]);
    ok.value = `Booking ${bookingId} status updated`;
    await loadBookings();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    updating.value = "";
  }
}

async function loadRecommendations(bookingId) {
  if (!ensureAuth()) {
    return;
  }

  recommendationsLoading.value = true;
  error.value = "";
  selectedBookingId.value = bookingId;

  try {
    recommendations.value = await fetchBookingRecommendations(bookingId);
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    recommendationsLoading.value = false;
  }
}

onMounted(loadAll);
</script>

<template>
  <section class="page-grid">
    <article class="panel">
      <h2>Create booking</h2>
      <form class="form-grid" style="margin-top: 12px" @submit.prevent="submitBooking">
        <div class="field">
          <label>Guest</label>
          <select v-model="createForm.guestId" required>
            <option v-for="guest in guests" :key="guest.id" :value="guest.id">
              {{ guest.fullName }} ({{ guest.email }})
            </option>
          </select>
        </div>

        <div class="field">
          <label>Room</label>
          <select v-model="createForm.roomId" required>
            <option v-for="room in rooms" :key="room.id" :value="room.id">
              {{ room.roomNumber }} / {{ room.roomType }} / {{ room.nightlyRate }}
            </option>
          </select>
        </div>

        <div class="field">
          <label>Check-in</label>
          <input v-model="createForm.checkInDate" type="date" required />
        </div>

        <div class="field">
          <label>Check-out</label>
          <input v-model="createForm.checkOutDate" type="date" required />
        </div>

        <div class="actions" style="grid-column: 1 / -1">
          <button class="btn btn-primary" type="submit" :disabled="saving">
            {{ saving ? "Saving..." : "Create booking" }}
          </button>
          <button class="btn btn-secondary" type="button" :disabled="loading" @click="loadAll">
            {{ loading ? "Loading..." : "Refresh all" }}
          </button>
        </div>
      </form>

      <p v-if="error" class="message error" style="margin-top: 10px">{{ error }}</p>
      <p v-if="ok" class="message ok" style="margin-top: 10px">{{ ok }}</p>
    </article>

    <article class="panel">
      <h2>Bookings</h2>
      <div class="actions" style="margin-top: 12px">
        <select v-model="filter.status" style="max-width: 220px">
          <option value="">All statuses</option>
          <option v-for="status in statuses" :key="status" :value="status">{{ status }}</option>
        </select>
        <button class="btn btn-secondary" type="button" @click="loadBookings">Apply filter</button>
      </div>

      <div class="table-wrap" style="margin-top: 12px">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Guest</th>
              <th>Hotel</th>
              <th>Room</th>
              <th>Stay</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="booking in bookings" :key="booking.id">
              <td class="mono">{{ booking.id }}</td>
              <td>{{ booking.guestFullName }}</td>
              <td>{{ resolveHotelName(booking.hotelId) }}</td>
              <td>{{ booking.roomNumber }}</td>
              <td>{{ booking.checkInDate }} -> {{ booking.checkOutDate }}</td>
              <td>
                <select v-model="statusDraft[booking.id]" style="max-width: 170px">
                  <option v-for="status in statuses" :key="status" :value="status">{{ status }}</option>
                </select>
              </td>
              <td>
                <div class="actions">
                  <button
                    class="btn btn-secondary"
                    type="button"
                    :disabled="updating === booking.id"
                    @click="applyStatus(booking.id)"
                  >
                    {{ updating === booking.id ? "Updating..." : "Update" }}
                  </button>
                  <button class="btn btn-primary" type="button" @click="loadRecommendations(booking.id)">
                    AI rec
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!bookings.length">
              <td colspan="7">No bookings yet</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <article class="panel" v-if="selectedBookingId">
      <h2>Recommendations for booking</h2>
      <p class="mono" style="margin-top: 8px">{{ selectedBookingId }}</p>

      <p v-if="recommendationsLoading" class="message" style="margin-top: 10px">Loading...</p>

      <div v-else class="table-wrap" style="margin-top: 12px">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Suggestion</th>
              <th>Model</th>
              <th>Confidence</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rec in recommendations" :key="rec.id">
              <td class="mono">{{ rec.id }}</td>
              <td>{{ rec.suggestion }}</td>
              <td>{{ rec.model }}</td>
              <td>{{ rec.confidence }}</td>
            </tr>
            <tr v-if="!recommendations.length">
              <td colspan="4">No recommendations for this booking</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <div v-if="showAuthModal" class="auth-overlay">
      <article class="auth-modal panel">
        <h2>Authentication required</h2>
        <p style="margin-top: 8px">Booking qilishdan oldin tizimga kirishingiz kerak.</p>
        <div class="actions" style="margin-top: 14px">
          <RouterLink class="btn btn-primary" to="/login" @click="showAuthModal = false">
            Login
          </RouterLink>
          <RouterLink class="btn btn-secondary" to="/register" @click="showAuthModal = false">
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
