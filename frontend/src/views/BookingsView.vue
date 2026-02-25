<script setup>
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";
import {
  createBooking,
  createPublicBooking,
  fetchBookingRecommendations,
  fetchBookings,
  fetchGuests,
  fetchHotels,
  fetchMyBookings,
  fetchPublicHotels,
  fetchPublicRoomHighlights,
  fetchRooms,
  getErrorMessage,
  updateBookingStatus
} from "../lib/api";

const statuses = ["CREATED", "CONFIRMED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED"];
const paymentMethods = ["CARD", "CLICK", "PAYME", "BANK_TRANSFER"];

const auth = useAuthStore();
const router = useRouter();

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

const publicHotels = ref([]);
const publicRooms = ref([]);
const publicBookingResult = ref(null);

const filter = reactive({
  status: ""
});

const createForm = reactive({
  guestId: "",
  roomId: "",
  checkInDate: "",
  checkOutDate: ""
});

const publicForm = reactive({
  fullName: "",
  email: "",
  hotelId: "",
  roomId: "",
  checkInDate: "",
  checkOutDate: "",
  paymentMethod: "CARD",
  prepaymentAmount: "",
  paymentReference: ""
});

const recommendations = ref([]);
const selectedBookingId = ref("");
const recommendationsLoading = ref(false);

const isBackofficeUser = computed(() =>
  auth.hasSuperAdminRole || auth.hasAdminRole || auth.hasOperatorRole
);

const canCreateBooking = computed(() => {
  if (isBackofficeUser.value) {
    return Boolean(createForm.guestId && createForm.roomId && createForm.checkInDate && createForm.checkOutDate);
  }

  return Boolean(
    publicForm.fullName.trim() &&
      publicForm.email.trim() &&
      publicForm.hotelId &&
      publicForm.roomId &&
      publicForm.checkInDate &&
      publicForm.checkOutDate &&
      publicForm.paymentMethod &&
      publicForm.prepaymentAmount
  );
});

const selectedPublicRoom = computed(() =>
  publicRooms.value.find((room) => room.id === publicForm.roomId) || null
);

const estimatedTotalAmount = computed(() => {
  if (!selectedPublicRoom.value) {
    return 0;
  }

  const nights = calculateNights(publicForm.checkInDate, publicForm.checkOutDate);
  if (nights <= 0) {
    return 0;
  }

  const nightlyRate = Number(selectedPublicRoom.value.nightlyRate || 0);
  return Number((nightlyRate * nights).toFixed(2));
});

function ensureAuth() {
  if (auth.ensureValidSession()) {
    return true;
  }

  router.push({ name: "login", query: { next: "/bookings" } });
  return false;
}

function syncStatusDraft() {
  Object.keys(statusDraft).forEach((key) => delete statusDraft[key]);
  bookings.value.forEach((booking) => {
    statusDraft[booking.id] = booking.status;
  });
}

function resolveHotelName(hotelId) {
  const hotel = hotels.value.find((item) => item.id === hotelId) || publicHotels.value.find((item) => item.id === hotelId);
  return hotel ? hotel.name : hotelId;
}

function calculateNights(checkInDate, checkOutDate) {
  if (!checkInDate || !checkOutDate) {
    return 0;
  }

  const checkIn = new Date(checkInDate);
  const checkOut = new Date(checkOutDate);
  const ms = checkOut.getTime() - checkIn.getTime();
  if (!Number.isFinite(ms) || ms <= 0) {
    return 0;
  }

  return Math.round(ms / (24 * 60 * 60 * 1000));
}

function validateDates(checkInDate, checkOutDate) {
  if (!checkInDate || !checkOutDate) {
    return false;
  }

  return checkOutDate > checkInDate;
}

function setDefaultBackofficeSelections() {
  if (!createForm.guestId && guests.value.length) {
    createForm.guestId = guests.value[0].id;
  }
  if (!createForm.roomId && rooms.value.length) {
    createForm.roomId = rooms.value[0].id;
  }
}

function setDefaultPublicSelections() {
  if (!publicForm.hotelId && publicHotels.value.length) {
    publicForm.hotelId = publicHotels.value[0].id;
  }
}

async function loadBackofficeData() {
  const [guestData, roomData, hotelData] = await Promise.all([fetchGuests(), fetchRooms(), fetchHotels()]);
  guests.value = guestData;
  rooms.value = roomData;
  hotels.value = hotelData;
  setDefaultBackofficeSelections();
}

async function loadPublicRooms(hotelId) {
  if (!hotelId) {
    publicRooms.value = [];
    publicForm.roomId = "";
    return;
  }

  publicRooms.value = await fetchPublicRoomHighlights(50, hotelId);

  if (!publicRooms.value.some((room) => room.id === publicForm.roomId)) {
    publicForm.roomId = publicRooms.value[0]?.id || "";
  }
}

async function loadPublicData() {
  publicHotels.value = await fetchPublicHotels();
  setDefaultPublicSelections();
  await loadPublicRooms(publicForm.hotelId);
}

async function loadBookings() {
  if (!auth.hasToken) {
    bookings.value = [];
    syncStatusDraft();
    return;
  }

  bookings.value = isBackofficeUser.value
    ? await fetchBookings(filter.status)
    : await fetchMyBookings(filter.status);
  syncStatusDraft();
}

async function loadAll() {
  loading.value = true;
  error.value = "";

  try {
    if (!auth.hasToken) {
      guests.value = [];
      rooms.value = [];
      hotels.value = [];
      bookings.value = [];
      publicHotels.value = [];
      publicRooms.value = [];
      syncStatusDraft();
      return;
    }

    if (isBackofficeUser.value) {
      await Promise.all([loadBackofficeData(), loadBookings()]);
      publicHotels.value = [];
      publicRooms.value = [];
      return;
    }

    await Promise.all([loadPublicData(), loadBookings()]);
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

async function submitBackofficeBooking() {
  if (!validateDates(createForm.checkInDate, createForm.checkOutDate)) {
    error.value = "Check-out sanasi check-in sanasidan keyin bo'lishi kerak.";
    return;
  }

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
}

async function submitPublicBooking() {
  if (!validateDates(publicForm.checkInDate, publicForm.checkOutDate)) {
    error.value = "Check-out sanasi check-in sanasidan keyin bo'lishi kerak.";
    return;
  }

  if (!selectedPublicRoom.value) {
    error.value = "Room tanlang.";
    return;
  }

  const prepaymentAmount = Number(publicForm.prepaymentAmount);
  if (!Number.isFinite(prepaymentAmount) || prepaymentAmount <= 0) {
    error.value = "Oldindan to'lov summasi 0 dan katta bo'lishi kerak.";
    return;
  }

  if (prepaymentAmount < estimatedTotalAmount.value) {
    error.value = `Oldindan to'lov kamida ${estimatedTotalAmount.value.toFixed(2)} bo'lishi kerak.`;
    return;
  }

  const response = await createPublicBooking({
    fullName: publicForm.fullName.trim(),
    email: publicForm.email.trim().toLowerCase(),
    roomId: publicForm.roomId,
    checkInDate: publicForm.checkInDate,
    checkOutDate: publicForm.checkOutDate,
    paymentMethod: publicForm.paymentMethod,
    prepaymentAmount,
    paymentReference: publicForm.paymentReference.trim() || null
  });

  publicBookingResult.value = response;
  publicForm.checkInDate = "";
  publicForm.checkOutDate = "";
  publicForm.prepaymentAmount = "";
  publicForm.paymentReference = "";
  ok.value = `Booking confirmed. Payment: ${response.paymentStatus}`;
  await loadBookings();
}

async function submitBooking() {
  if (!ensureAuth()) {
    return;
  }

  saving.value = true;
  error.value = "";
  ok.value = "";

  try {
    if (isBackofficeUser.value) {
      await submitBackofficeBooking();
    } else {
      await submitPublicBooking();
    }
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

watch(
  () => publicForm.hotelId,
  async (hotelId) => {
    if (!auth.hasToken || isBackofficeUser.value) {
      return;
    }

    try {
      await loadPublicRooms(hotelId);
    } catch (err) {
      error.value = getErrorMessage(err);
    }
  }
);

watch(
  () => [publicForm.roomId, publicForm.checkInDate, publicForm.checkOutDate],
  () => {
    if (!estimatedTotalAmount.value) {
      return;
    }

    if (!publicForm.prepaymentAmount) {
      publicForm.prepaymentAmount = estimatedTotalAmount.value.toFixed(2);
    }
  }
);

onMounted(loadAll);
</script>

<template>
  <section class="page-grid">
    <article class="panel">
      <h2>{{ isBackofficeUser ? "Create booking" : "Book with prepayment" }}</h2>
      <form class="form-grid" style="margin-top: 12px" @submit.prevent="submitBooking">
        <template v-if="isBackofficeUser">
          <div class="field">
            <label>Guest</label>
            <select v-model="createForm.guestId" required :disabled="!auth.hasToken">
              <option v-for="guest in guests" :key="guest.id" :value="guest.id">
                {{ guest.fullName }} ({{ guest.email }})
              </option>
            </select>
          </div>

          <div class="field">
            <label>Room</label>
            <select v-model="createForm.roomId" required :disabled="!auth.hasToken">
              <option v-for="room in rooms" :key="room.id" :value="room.id">
                {{ room.roomNumber }} / {{ room.roomType }} / {{ room.nightlyRate }}
              </option>
            </select>
          </div>

          <div class="field">
            <label>Check-in</label>
            <input v-model="createForm.checkInDate" type="date" required :disabled="!auth.hasToken" />
          </div>

          <div class="field">
            <label>Check-out</label>
            <input v-model="createForm.checkOutDate" type="date" required :disabled="!auth.hasToken" />
          </div>
        </template>

        <template v-else>
          <div class="field">
            <label>Full name</label>
            <input v-model="publicForm.fullName" type="text" maxlength="120" required :disabled="!auth.hasToken" />
          </div>

          <div class="field">
            <label>Email</label>
            <input v-model="publicForm.email" type="email" maxlength="180" required :disabled="!auth.hasToken" />
          </div>

          <div class="field">
            <label>Hotel</label>
            <select v-model="publicForm.hotelId" required :disabled="!auth.hasToken">
              <option v-for="hotel in publicHotels" :key="hotel.id" :value="hotel.id">
                {{ hotel.code }} / {{ hotel.name }}
              </option>
            </select>
          </div>

          <div class="field">
            <label>Room</label>
            <select v-model="publicForm.roomId" required :disabled="!auth.hasToken || !publicRooms.length">
              <option v-for="room in publicRooms" :key="room.id" :value="room.id">
                {{ room.roomNumber }} / {{ room.roomType }} / {{ room.nightlyRate }}
              </option>
            </select>
          </div>

          <div class="field">
            <label>Check-in</label>
            <input v-model="publicForm.checkInDate" type="date" required :disabled="!auth.hasToken" />
          </div>

          <div class="field">
            <label>Check-out</label>
            <input v-model="publicForm.checkOutDate" type="date" required :disabled="!auth.hasToken" />
          </div>

          <div class="field">
            <label>Payment method</label>
            <select v-model="publicForm.paymentMethod" required :disabled="!auth.hasToken">
              <option v-for="method in paymentMethods" :key="method" :value="method">{{ method }}</option>
            </select>
          </div>

          <div class="field">
            <label>Prepayment amount</label>
            <input
              v-model="publicForm.prepaymentAmount"
              type="number"
              min="0.01"
              step="0.01"
              required
              :disabled="!auth.hasToken"
            />
          </div>

          <div class="field" style="grid-column: 1 / -1">
            <label>Payment reference (optional)</label>
            <input
              v-model="publicForm.paymentReference"
              type="text"
              maxlength="120"
              placeholder="txn id"
              :disabled="!auth.hasToken"
            />
          </div>

          <p class="message" style="grid-column: 1 / -1">
            Estimated total: <span class="mono">{{ estimatedTotalAmount.toFixed(2) }}</span>
          </p>
        </template>

        <div class="actions" style="grid-column: 1 / -1">
          <button class="btn btn-primary" type="submit" :disabled="saving || !canCreateBooking || !auth.hasToken">
            {{ saving ? "Saving..." : isBackofficeUser ? "Create booking" : "Pay and confirm booking" }}
          </button>
          <button class="btn btn-secondary" type="button" :disabled="loading" @click="loadAll">
            {{ loading ? "Loading..." : "Refresh all" }}
          </button>
        </div>
      </form>

      <p v-if="!auth.hasToken" class="message" style="margin-top: 10px">Booking formni ishlatish uchun login qiling.</p>
      <p v-if="error" class="message error" style="margin-top: 10px">{{ error }}</p>
      <p v-if="ok" class="message ok" style="margin-top: 10px">{{ ok }}</p>
    </article>

    <article v-if="!isBackofficeUser && publicBookingResult" class="panel">
      <h2>Latest booking</h2>
      <p style="margin-top: 8px">Booking ID: <span class="mono">{{ publicBookingResult.id }}</span></p>
      <p style="margin-top: 4px">Status: <span class="mono">{{ publicBookingResult.status }}</span></p>
      <p style="margin-top: 4px">Payment: <span class="mono">{{ publicBookingResult.paymentStatus }}</span></p>
      <p style="margin-top: 4px">Reference: <span class="mono">{{ publicBookingResult.paymentReference }}</span></p>
    </article>

    <article v-if="!isBackofficeUser" class="panel">
      <h2>My bookings</h2>
      <div class="actions" style="margin-top: 12px">
        <button class="btn btn-secondary" type="button" :disabled="loading" @click="loadBookings">
          {{ loading ? "Loading..." : "Refresh bookings" }}
        </button>
      </div>

      <div class="table-wrap" style="margin-top: 12px">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Hotel</th>
              <th>Room</th>
              <th>Stay</th>
              <th>Payment</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="booking in bookings" :key="booking.id">
              <td class="mono">{{ booking.id }}</td>
              <td>{{ resolveHotelName(booking.hotelId) }}</td>
              <td>{{ booking.roomNumber }}</td>
              <td>{{ booking.checkInDate }} -> {{ booking.checkOutDate }}</td>
              <td>
                <span class="mono">{{ booking.paymentStatus }}</span>
                <br />
                <span class="mono">{{ booking.prepaymentAmount }} / {{ booking.totalAmount }}</span>
              </td>
              <td><span class="mono">{{ booking.status }}</span></td>
            </tr>
            <tr v-if="!bookings.length">
              <td colspan="6">Sizda hozircha booking yo'q.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </article>

    <template v-if="isBackofficeUser">
      <article class="panel">
        <h2>Bookings</h2>
        <div class="actions" style="margin-top: 12px">
          <select v-model="filter.status" style="max-width: 220px" :disabled="!auth.hasToken">
            <option value="">All statuses</option>
            <option v-for="status in statuses" :key="status" :value="status">{{ status }}</option>
          </select>
          <button class="btn btn-secondary" type="button" :disabled="!auth.hasToken" @click="loadBookings">Apply filter</button>
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
                <th>Payment</th>
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
                  <span class="mono">{{ booking.paymentStatus }}</span>
                  <br />
                  <span class="mono">{{ booking.prepaymentAmount }} / {{ booking.totalAmount }}</span>
                </td>
                <td>
                  <select v-model="statusDraft[booking.id]" style="max-width: 170px" :disabled="!auth.hasToken">
                    <option v-for="status in statuses" :key="status" :value="status">{{ status }}</option>
                  </select>
                </td>
                <td>
                  <div class="actions">
                    <button
                      class="btn btn-secondary"
                      type="button"
                      :disabled="updating === booking.id || !auth.hasToken"
                      @click="applyStatus(booking.id)"
                    >
                      {{ updating === booking.id ? "Updating..." : "Update" }}
                    </button>
                    <button class="btn btn-primary" type="button" :disabled="!auth.hasToken" @click="loadRecommendations(booking.id)">
                      AI rec
                    </button>
                  </div>
                </td>
              </tr>
              <tr v-if="!bookings.length">
                <td colspan="8">No bookings yet</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>

      <article v-if="selectedBookingId" class="panel">
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
    </template>

  </section>
</template>
