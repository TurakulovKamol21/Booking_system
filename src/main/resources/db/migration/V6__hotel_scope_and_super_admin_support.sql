CREATE TABLE IF NOT EXISTS hotels (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL UNIQUE,
    name VARCHAR(140) NOT NULL,
    city VARCHAR(80) NOT NULL,
    country VARCHAR(80) NOT NULL,
    address_line VARCHAR(200) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

INSERT INTO hotels (id, code, name, city, country, address_line, created_at)
VALUES
    ('99999999-9999-9999-9999-999999999901'::uuid, 'AG-TAS-01', 'Autoguide Grand Tashkent', 'Tashkent', 'Uzbekistan', 'Amir Temur Avenue 12', now() - interval '40 days'),
    ('99999999-9999-9999-9999-999999999902'::uuid, 'AG-SAM-01', 'Autoguide Silk Samarkand', 'Samarkand', 'Uzbekistan', 'Registan Street 7', now() - interval '38 days'),
    ('99999999-9999-9999-9999-999999999903'::uuid, 'AG-BHK-01', 'Autoguide Heritage Bukhara', 'Bukhara', 'Uzbekistan', 'Lyabi-Hauz Square 4', now() - interval '36 days')
ON CONFLICT (id) DO NOTHING;

ALTER TABLE guests ADD COLUMN IF NOT EXISTS hotel_id UUID;
ALTER TABLE rooms ADD COLUMN IF NOT EXISTS hotel_id UUID;
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS hotel_id UUID;

WITH ranked_rooms AS (
    SELECT id,
           (row_number() OVER (ORDER BY created_at NULLS LAST, room_number, id) - 1) % 3 AS bucket
    FROM rooms
)
UPDATE rooms r
SET hotel_id = CASE ranked_rooms.bucket
                   WHEN 0 THEN '99999999-9999-9999-9999-999999999901'::uuid
                   WHEN 1 THEN '99999999-9999-9999-9999-999999999902'::uuid
                   ELSE '99999999-9999-9999-9999-999999999903'::uuid
    END
FROM ranked_rooms
WHERE r.id = ranked_rooms.id
  AND r.hotel_id IS NULL;

WITH ranked_guests AS (
    SELECT id,
           (row_number() OVER (ORDER BY created_at NULLS LAST, email, id) - 1) % 3 AS bucket
    FROM guests
)
UPDATE guests g
SET hotel_id = CASE ranked_guests.bucket
                   WHEN 0 THEN '99999999-9999-9999-9999-999999999901'::uuid
                   WHEN 1 THEN '99999999-9999-9999-9999-999999999902'::uuid
                   ELSE '99999999-9999-9999-9999-999999999903'::uuid
    END
FROM ranked_guests
WHERE g.id = ranked_guests.id
  AND g.hotel_id IS NULL;

UPDATE bookings b
SET hotel_id = r.hotel_id
FROM rooms r
WHERE b.room_id = r.id
  AND b.hotel_id IS NULL;

UPDATE bookings
SET hotel_id = '99999999-9999-9999-9999-999999999901'::uuid
WHERE hotel_id IS NULL;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_guests_hotel_id') THEN
        ALTER TABLE guests
            ADD CONSTRAINT fk_guests_hotel_id FOREIGN KEY (hotel_id) REFERENCES hotels (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_rooms_hotel_id') THEN
        ALTER TABLE rooms
            ADD CONSTRAINT fk_rooms_hotel_id FOREIGN KEY (hotel_id) REFERENCES hotels (id);
    END IF;

    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_bookings_hotel_id') THEN
        ALTER TABLE bookings
            ADD CONSTRAINT fk_bookings_hotel_id FOREIGN KEY (hotel_id) REFERENCES hotels (id);
    END IF;
END
$$;

ALTER TABLE guests ALTER COLUMN hotel_id SET NOT NULL;
ALTER TABLE rooms ALTER COLUMN hotel_id SET NOT NULL;
ALTER TABLE bookings ALTER COLUMN hotel_id SET NOT NULL;

ALTER TABLE rooms DROP CONSTRAINT IF EXISTS rooms_room_number_key;
ALTER TABLE guests DROP CONSTRAINT IF EXISTS guests_email_key;

CREATE UNIQUE INDEX IF NOT EXISTS uq_rooms_hotel_room_number ON rooms(hotel_id, room_number);
CREATE UNIQUE INDEX IF NOT EXISTS uq_guests_hotel_email ON guests(hotel_id, email);

CREATE INDEX IF NOT EXISTS idx_rooms_hotel_id ON rooms(hotel_id);
CREATE INDEX IF NOT EXISTS idx_guests_hotel_id ON guests(hotel_id);
CREATE INDEX IF NOT EXISTS idx_bookings_hotel_id ON bookings(hotel_id);
CREATE INDEX IF NOT EXISTS idx_bookings_hotel_status ON bookings(hotel_id, status);

CREATE TABLE IF NOT EXISTS hotel_user_scopes (
    username VARCHAR(120) PRIMARY KEY,
    hotel_id UUID NOT NULL REFERENCES hotels(id),
    access_level VARCHAR(20) NOT NULL CHECK (access_level IN ('OWNER', 'STAFF')),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_hotel_user_scopes_hotel_id ON hotel_user_scopes(hotel_id);

INSERT INTO hotel_user_scopes (username, hotel_id, access_level, created_at)
VALUES
    ('admin_user', '99999999-9999-9999-9999-999999999901'::uuid, 'OWNER', now()),
    ('operator_user', '99999999-9999-9999-9999-999999999901'::uuid, 'STAFF', now())
ON CONFLICT (username) DO UPDATE
SET hotel_id = EXCLUDED.hotel_id,
    access_level = EXCLUDED.access_level;
