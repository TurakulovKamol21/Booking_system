<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import { useAuthStore } from "../stores/auth";
import { config } from "../lib/config";
import {
  fetchBookings,
  fetchGuests,
  fetchHealth,
  fetchPublicHomeContent,
  fetchPublicRoomHighlights,
  fetchRooms,
  getErrorMessage
} from "../lib/api";
import { formatPrice } from "../lib/roomVisuals";

const auth = useAuthStore();

const loading = ref(false);
const error = ref("");
const health = ref("UNKNOWN");
const guestsCount = ref(0);
const roomsCount = ref(0);
const bookingsCount = ref(0);

const homeContent = ref({
  heroTitle: "",
  heroSubtitle: "",
  heroImageUrl: "/assets/rooms/hero-hotel.svg",
  amenities: [],
  offers: []
});

const featuredRooms = ref([]);

const canLoadProtectedData = computed(() => !config.requireAuth || auth.hasToken);

const amenities = computed(() => (Array.isArray(homeContent.value.amenities) ? homeContent.value.amenities : []));
const specialOffers = computed(() => (Array.isArray(homeContent.value.offers) ? homeContent.value.offers : []));

const heroStats = computed(() => [
  { label: "Backend", value: health.value },
  { label: "Live guests", value: guestsCount.value },
  { label: "Active rooms", value: roomsCount.value },
  { label: "Bookings", value: bookingsCount.value }
]);

const featuredRoomCards = computed(() =>
  featuredRooms.value.map((room) => ({
    ...room,
    priceLabel: formatPrice(room.nightlyRate)
  }))
);

async function loadDashboard() {
  loading.value = true;
  error.value = "";

  try {
    const shouldTryProtected = canLoadProtectedData.value;
    const protectedFallback = Promise.resolve([]);

    const [
      healthResult,
      homeResult,
      highlightsResult,
      guestsResult,
      roomsResult,
      bookingsResult
    ] = await Promise.allSettled([
      fetchHealth(),
      fetchPublicHomeContent(),
      fetchPublicRoomHighlights(12),
      shouldTryProtected ? fetchGuests() : protectedFallback,
      shouldTryProtected ? fetchRooms() : protectedFallback,
      shouldTryProtected ? fetchBookings("") : protectedFallback
    ]);

    health.value = healthResult.status === "fulfilled"
      ? String(healthResult.value?.status || "UNKNOWN").toUpperCase()
      : "UNKNOWN";

    if (homeResult.status === "fulfilled") {
      homeContent.value = {
        heroTitle: homeResult.value?.heroTitle || "",
        heroSubtitle: homeResult.value?.heroSubtitle || "",
        heroImageUrl: homeResult.value?.heroImageUrl || "/assets/rooms/hero-hotel.svg",
        amenities: Array.isArray(homeResult.value?.amenities) ? homeResult.value.amenities : [],
        offers: Array.isArray(homeResult.value?.offers) ? homeResult.value.offers : []
      };
    }

    featuredRooms.value = highlightsResult.status === "fulfilled" && Array.isArray(highlightsResult.value)
      ? highlightsResult.value
      : [];

    const guests = guestsResult.status === "fulfilled" ? guestsResult.value : [];
    const rooms = roomsResult.status === "fulfilled" ? roomsResult.value : [];
    const bookings = bookingsResult.status === "fulfilled" ? bookingsResult.value : [];

    guestsCount.value = Array.isArray(guests) ? guests.length : 0;
    roomsCount.value = Array.isArray(rooms) ? rooms.length : 0;
    bookingsCount.value = Array.isArray(bookings) ? bookings.length : 0;

    if (homeResult.status === "rejected" || highlightsResult.status === "rejected") {
      error.value = "Public content backenddan yuklanmadi. Backendni tekshiring.";
    }

    if (
      shouldTryProtected &&
      (guestsResult.status === "rejected" ||
        roomsResult.status === "rejected" ||
        bookingsResult.status === "rejected")
    ) {
      error.value = "Protected statistikalar uchun login qiling yoki auth sozlamasini tekshiring.";
    }
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

onMounted(loadDashboard);
</script>

<template>
  <section class="landing-page">
    <article class="hero-card">
      <div class="hero-content">
        <p class="hero-kicker">Backend Powered Landing</p>
        <h2 class="hero-title">{{ homeContent.heroTitle }}</h2>
        <p class="hero-subtitle">{{ homeContent.heroSubtitle }}</p>

        <div class="actions" style="margin-top: 18px">
          <RouterLink class="btn btn-primary" to="/bookings">Start Booking</RouterLink>
          <RouterLink class="btn btn-secondary" to="/rooms">Explore Rooms</RouterLink>
          <button class="btn btn-secondary" type="button" :disabled="loading" @click="loadDashboard">
            {{ loading ? "Refreshing..." : "Refresh Data" }}
          </button>
        </div>

        <div class="hero-stats">
          <div class="hero-stat" v-for="item in heroStats" :key="item.label">
            <p class="hero-stat-label">{{ item.label }}</p>
            <p class="hero-stat-value">{{ item.value }}</p>
          </div>
        </div>

        <p v-if="!canLoadProtectedData" class="message" style="margin-top: 12px">
          Login qilsangiz, live room/guest/booking statistikalarini ko'rasiz.
        </p>
        <p v-if="error" class="message error" style="margin-top: 10px">{{ error }}</p>
      </div>

      <div class="hero-media">
        <img :src="homeContent.heroImageUrl" alt="Hotel overview" />
      </div>
    </article>

    <article class="section-block">
      <div class="section-head">
        <div>
          <p class="brand-kicker">Featured Rooms</p>
          <h3>Top picks with clear nightly pricing</h3>
        </div>
        <RouterLink class="btn btn-secondary" to="/rooms">All rooms</RouterLink>
      </div>

      <div class="room-grid">
        <article
          v-for="(room, index) in featuredRoomCards"
          :key="room.id"
          class="room-card reveal-up"
          :style="{ animationDelay: `${index * 80}ms` }"
        >
          <img class="room-image" :src="room.imageUrl" :alt="room.roomType" />
          <div class="room-body">
            <div class="room-meta">
              <span class="mono">#{{ room.roomNumber }}</span>
              <span class="price-chip">{{ room.priceLabel }} / night</span>
            </div>
            <h4>{{ room.roomType }}</h4>
            <p>{{ room.shortDescription }}</p>
            <RouterLink class="btn btn-secondary" to="/bookings">Reserve now</RouterLink>
          </div>
        </article>

        <article v-if="!featuredRoomCards.length" class="panel">
          <p>Room showcase backenddan hali kelmadi.</p>
        </article>
      </div>
    </article>

    <article class="section-block">
      <div class="section-head">
        <div>
          <p class="brand-kicker">Amenities</p>
          <h3>Designed for comfort and performance</h3>
        </div>
      </div>
      <div class="benefits-grid">
        <article v-for="feature in amenities" :key="feature.id" class="benefit-card">
          <h4>{{ feature.title }}</h4>
          <p>{{ feature.description }}</p>
        </article>
        <article v-if="!amenities.length" class="benefit-card">
          <p>Amenities backenddan yuklanmadi.</p>
        </article>
      </div>
    </article>

    <article class="section-block">
      <div class="section-head">
        <div>
          <p class="brand-kicker">Special Offers</p>
          <h3>Packages your guests notice first</h3>
        </div>
      </div>
      <div class="offer-grid">
        <article v-for="offer in specialOffers" :key="offer.id" class="offer-card">
          <p class="offer-price">{{ offer.priceLabel }}</p>
          <h4>{{ offer.title }}</h4>
          <p>{{ offer.note }}</p>
          <RouterLink class="btn btn-primary" to="/bookings">Select package</RouterLink>
        </article>
        <article v-if="!specialOffers.length" class="offer-card">
          <p>Offers backenddan yuklanmadi.</p>
        </article>
      </div>
    </article>
  </section>
</template>
