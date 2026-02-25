ALTER TABLE bookings
ADD COLUMN IF NOT EXISTS total_amount NUMERIC(12,2) NOT NULL DEFAULT 0;

ALTER TABLE bookings
ADD COLUMN IF NOT EXISTS prepayment_amount NUMERIC(12,2) NOT NULL DEFAULT 0;

ALTER TABLE bookings
ADD COLUMN IF NOT EXISTS payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID';

ALTER TABLE bookings
ADD COLUMN IF NOT EXISTS payment_method VARCHAR(30);

ALTER TABLE bookings
ADD COLUMN IF NOT EXISTS payment_reference VARCHAR(120);

ALTER TABLE bookings
ADD COLUMN IF NOT EXISTS prepaid BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE bookings b
SET total_amount = ROUND((GREATEST(1, b.check_out_date - b.check_in_date) * r.nightly_rate)::numeric, 2)
FROM rooms r
WHERE b.room_id = r.id
  AND b.total_amount = 0;
