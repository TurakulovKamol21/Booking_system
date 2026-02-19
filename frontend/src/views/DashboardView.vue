<script setup>
import { onMounted, ref } from "vue";
import {
  fetchBookings,
  fetchGuests,
  fetchHealth,
  fetchRooms,
  getErrorMessage
} from "../lib/api";

const loading = ref(false);
const error = ref("");
const health = ref("unknown");
const guestsCount = ref(0);
const roomsCount = ref(0);
const bookingsCount = ref(0);
const lastUpdate = ref("");

async function loadDashboard() {
  loading.value = true;
  error.value = "";

  try {
    const [healthData, guests, rooms, bookings] = await Promise.all([
      fetchHealth(),
      fetchGuests(),
      fetchRooms(),
      fetchBookings("")
    ]);

    health.value = healthData?.status || "unknown";
    guestsCount.value = guests.length;
    roomsCount.value = rooms.length;
    bookingsCount.value = bookings.length;
    lastUpdate.value = new Date().toLocaleString();
  } catch (err) {
    error.value = getErrorMessage(err);
  } finally {
    loading.value = false;
  }
}

onMounted(loadDashboard);
</script>

<template>
  <section class="page-grid">
    <article class="panel">
      <h2>Overview</h2>
      <p style="margin-top: 8px">Hotel booking boshqaruvi uchun tezkor ko'rsatkichlar.</p>

      <div class="card-grid" style="margin-top: 14px">
        <div class="kpi">
          <p class="kpi-label">Backend Health</p>
          <p class="kpi-value" style="font-size: 24px">{{ health }}</p>
        </div>
        <div class="kpi">
          <p class="kpi-label">Guests</p>
          <p class="kpi-value">{{ guestsCount }}</p>
        </div>
        <div class="kpi">
          <p class="kpi-label">Rooms</p>
          <p class="kpi-value">{{ roomsCount }}</p>
        </div>
        <div class="kpi">
          <p class="kpi-label">Bookings</p>
          <p class="kpi-value">{{ bookingsCount }}</p>
        </div>
      </div>

      <div class="actions" style="margin-top: 14px">
        <button class="btn btn-primary" type="button" :disabled="loading" @click="loadDashboard">
          {{ loading ? "Refreshing..." : "Refresh" }}
        </button>
        <span class="pill" v-if="lastUpdate">Last update: {{ lastUpdate }}</span>
      </div>

      <p v-if="error" class="message error" style="margin-top: 10px">{{ error }}</p>
    </article>
  </section>
</template>
