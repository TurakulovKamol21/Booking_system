ALTER TABLE rooms ADD COLUMN IF NOT EXISTS image_url VARCHAR(300);
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS short_description VARCHAR(280);

CREATE TABLE IF NOT EXISTS landing_amenities (
    id UUID PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(280) NOT NULL,
    sort_order INT NOT NULL
);

CREATE TABLE IF NOT EXISTS landing_offers (
    id UUID PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    note VARCHAR(280) NOT NULL,
    price_label VARCHAR(40) NOT NULL,
    sort_order INT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_landing_amenities_sort_order ON landing_amenities(sort_order);
CREATE INDEX IF NOT EXISTS idx_landing_offers_sort_order ON landing_offers(sort_order);

INSERT INTO landing_amenities (id, title, description, sort_order)
SELECT *
FROM (
         VALUES
             ('11111111-1111-1111-1111-111111111101'::uuid, 'Skyline Infinity Pool', 'Sunset rooftop sessions with curated lounge music and private cabanas.', 1),
             ('11111111-1111-1111-1111-111111111102'::uuid, 'Chef Curated Dining', 'Daily tasting menu featuring seasonal ingredients and express service.', 2),
             ('11111111-1111-1111-1111-111111111103'::uuid, 'Smart Check-in', 'Digital key, no-paper registration, and rapid arrival flow in under 2 minutes.', 3),
             ('11111111-1111-1111-1111-111111111104'::uuid, 'Wellness Studio', '24/7 fitness, guided recovery sessions, sauna, and mobility zone.', 4)
     ) AS seed(id, title, description, sort_order)
WHERE NOT EXISTS (SELECT 1 FROM landing_amenities);

INSERT INTO landing_offers (id, title, note, price_label, sort_order)
SELECT *
FROM (
         VALUES
             ('22222222-2222-2222-2222-222222222201'::uuid, 'Weekend Escape', '2 nights, breakfast included, and late checkout.', '$299', 1),
             ('22222222-2222-2222-2222-222222222202'::uuid, 'Business Plus', 'Workspace suite, early check-in, and transfer service.', '$349', 2),
             ('22222222-2222-2222-2222-222222222203'::uuid, 'Family Comfort', 'Extended family setup with activity bundle and extra amenities.', '$429', 3)
     ) AS seed(id, title, note, price_label, sort_order)
WHERE NOT EXISTS (SELECT 1 FROM landing_offers);

INSERT INTO rooms (id, room_number, room_type, nightly_rate, image_url, short_description, created_at)
SELECT *
FROM (
         VALUES
             ('33333333-3333-3333-3333-333333333301'::uuid, 'A-502', 'Sunset Suite', 220.00::numeric, '/assets/rooms/room-suite.svg', 'Premium suite with panoramic city view and private lounge corner.', now()),
             ('33333333-3333-3333-3333-333333333302'::uuid, 'B-301', 'Deluxe Room', 170.00::numeric, '/assets/rooms/room-deluxe.svg', 'Refined interior with balanced work-and-rest environment.', now()),
             ('33333333-3333-3333-3333-333333333303'::uuid, 'C-210', 'Family Room', 190.00::numeric, '/assets/rooms/room-family.svg', 'Spacious setup designed for group or long-stay accommodation.', now())
     ) AS seed(id, room_number, room_type, nightly_rate, image_url, short_description, created_at)
WHERE NOT EXISTS (SELECT 1 FROM rooms);

UPDATE rooms
SET image_url =
        CASE
            WHEN LOWER(room_type) LIKE '%suite%' THEN '/assets/rooms/room-suite.svg'
            WHEN LOWER(room_type) LIKE '%deluxe%' OR LOWER(room_type) LIKE '%premium%' THEN '/assets/rooms/room-deluxe.svg'
            WHEN LOWER(room_type) LIKE '%family%' THEN '/assets/rooms/room-family.svg'
            ELSE '/assets/rooms/room-default.svg'
            END,
    short_description =
        CASE
            WHEN LOWER(room_type) LIKE '%suite%' THEN 'Premium suite with panoramic city view and private lounge corner.'
            WHEN LOWER(room_type) LIKE '%deluxe%' OR LOWER(room_type) LIKE '%premium%' THEN 'Refined interior with balanced work-and-rest environment.'
            WHEN LOWER(room_type) LIKE '%family%' THEN 'Spacious setup designed for group or long-stay accommodation.'
            ELSE 'Balanced comfort with essential in-room amenities.'
            END
WHERE image_url IS NULL
   OR short_description IS NULL;
