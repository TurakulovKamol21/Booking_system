UPDATE bookings b
SET hotel_id = r.hotel_id
FROM rooms r
WHERE b.room_id = r.id
  AND b.hotel_id IS DISTINCT FROM r.hotel_id;

WITH booking_guest_scope AS (
    SELECT DISTINCT ON (guest_id) guest_id, hotel_id
    FROM bookings
    ORDER BY guest_id, created_at DESC, id DESC
)
UPDATE guests g
SET hotel_id = booking_guest_scope.hotel_id
FROM booking_guest_scope
WHERE g.id = booking_guest_scope.guest_id
  AND g.hotel_id IS DISTINCT FROM booking_guest_scope.hotel_id;
