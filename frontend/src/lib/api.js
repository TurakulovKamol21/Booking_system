import axios from "axios";
import { http } from "./http";
import { config } from "./config";

export function getErrorMessage(error) {
  if (error?.response?.data?.message) {
    return error.response.data.message;
  }
  if (typeof error?.response?.data === "string" && error.response.data.trim().length > 0) {
    return error.response.data;
  }
  if (error?.message) {
    return error.message;
  }
  return "Unexpected error";
}

export async function loginWithKeycloak({ username, password }) {
  const form = new URLSearchParams();
  form.set("client_id", config.keycloakClientId);
  form.set("grant_type", "password");
  form.set("username", username);
  form.set("password", password);

  const { data } = await axios.post(
    `${config.keycloakBase}/realms/${config.keycloakRealm}/protocol/openid-connect/token`,
    form,
    {
      headers: {
        "Content-Type": "application/x-www-form-urlencoded"
      }
    }
  );

  return data;
}

export function getKeycloakRegistrationUrl(options = {}) {
  const next = typeof options.next === "string" && options.next.trim() ? options.next.trim() : "/";
  const redirectUri = `${window.location.origin}/login?next=${encodeURIComponent(next)}`;
  const params = new URLSearchParams();
  params.set("client_id", config.keycloakClientId);
  params.set("response_type", "code");
  params.set("scope", "openid");
  params.set("redirect_uri", redirectUri);

  if (typeof options.loginHint === "string" && options.loginHint.trim()) {
    params.set("login_hint", options.loginHint.trim());
  }

  return `${config.keycloakBase}/realms/${config.keycloakRealm}/protocol/openid-connect/registrations?${params.toString()}`;
}

export async function fetchHealth() {
  const { data } = await http.get("/actuator/health");
  return data;
}

export async function fetchPublicHomeContent() {
  const { data } = await http.get("/api/v1/public/home");
  return data;
}

export async function fetchPublicRoomHighlights(limit = 6, hotelId = null) {
  const params = { limit };
  if (hotelId) {
    params.hotelId = hotelId;
  }
  const { data } = await http.get("/api/v1/public/rooms/highlights", { params });
  return data;
}

export async function fetchPublicHotels() {
  const { data } = await http.get("/api/v1/public/hotels");
  return data;
}

export async function fetchGuests() {
  const { data } = await http.get("/api/v1/guests");
  return data;
}

export async function fetchHotels() {
  const { data } = await http.get("/api/v1/hotels");
  return data;
}

export async function createHotel(payload) {
  const { data } = await http.post("/api/v1/hotels", payload);
  return data;
}

export async function updateHotel(id, payload) {
  const { data } = await http.put(`/api/v1/hotels/${id}`, payload);
  return data;
}

export async function deleteHotel(id) {
  await http.delete(`/api/v1/hotels/${id}`);
}

export async function createGuest(payload) {
  const { data } = await http.post("/api/v1/guests", payload);
  return data;
}

export async function fetchRooms() {
  const { data } = await http.get("/api/v1/rooms");
  return data;
}

export async function createRoom(payload) {
  const { data } = await http.post("/api/v1/rooms", payload);
  return data;
}

export async function updateRoom(id, payload) {
  const { data } = await http.put(`/api/v1/rooms/${id}`, payload);
  return data;
}

export async function deleteRoom(id) {
  await http.delete(`/api/v1/rooms/${id}`);
}

export async function fetchBookings(status) {
  const params = {};
  if (status) {
    params.status = status;
  }
  const { data } = await http.get("/api/v1/bookings", { params });
  return data;
}

export async function fetchMyBookings(status) {
  const params = {};
  if (status) {
    params.status = status;
  }
  const { data } = await http.get("/api/v1/bookings/my", { params });
  return data;
}

export async function fetchBookingById(id) {
  const { data } = await http.get(`/api/v1/bookings/${id}`);
  return data;
}

export async function createBooking(payload) {
  const { data } = await http.post("/api/v1/bookings", payload);
  return data;
}

export async function createPublicBooking(payload) {
  const { data } = await http.post("/api/v1/public/bookings", payload);
  return data;
}

export async function updateBookingStatus(id, status) {
  const { data } = await http.patch(`/api/v1/bookings/${id}/status`, { status });
  return data;
}

export async function fetchBookingRecommendations(bookingId) {
  const { data } = await http.get(`/api/v1/booking-recommendations/${bookingId}`);
  return data;
}
