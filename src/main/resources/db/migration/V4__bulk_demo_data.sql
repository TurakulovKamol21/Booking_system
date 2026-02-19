INSERT INTO guests (id, full_name, email, created_at)
VALUES
    ('44444444-4444-4444-4444-444444444401'::uuid, 'Azizbek Qodirov', 'guest01@example.com', now() - interval '24 days'),
    ('44444444-4444-4444-4444-444444444402'::uuid, 'Madina Rakhimova', 'guest02@example.com', now() - interval '23 days'),
    ('44444444-4444-4444-4444-444444444403'::uuid, 'Behruz Karimov', 'guest03@example.com', now() - interval '22 days'),
    ('44444444-4444-4444-4444-444444444404'::uuid, 'Nilufar Ergasheva', 'guest04@example.com', now() - interval '21 days'),
    ('44444444-4444-4444-4444-444444444405'::uuid, 'Sardor Tursunov', 'guest05@example.com', now() - interval '20 days'),
    ('44444444-4444-4444-4444-444444444406'::uuid, 'Shahnoza Islomova', 'guest06@example.com', now() - interval '19 days'),
    ('44444444-4444-4444-4444-444444444407'::uuid, 'Akmal Tojiev', 'guest07@example.com', now() - interval '18 days'),
    ('44444444-4444-4444-4444-444444444408'::uuid, 'Farida Sultonova', 'guest08@example.com', now() - interval '17 days'),
    ('44444444-4444-4444-4444-444444444409'::uuid, 'Jahongir Sobirov', 'guest09@example.com', now() - interval '16 days'),
    ('44444444-4444-4444-4444-444444444410'::uuid, 'Dilnoza Aminova', 'guest10@example.com', now() - interval '15 days'),
    ('44444444-4444-4444-4444-444444444411'::uuid, 'Murod Kamolov', 'guest11@example.com', now() - interval '14 days'),
    ('44444444-4444-4444-4444-444444444412'::uuid, 'Sevara Usmonova', 'guest12@example.com', now() - interval '13 days')
ON CONFLICT DO NOTHING;

INSERT INTO rooms (id, room_number, room_type, nightly_rate, image_url, short_description, created_at)
VALUES
    ('55555555-5555-5555-5555-555555555501'::uuid, 'S-101', 'Sunset Suite', 260.00, 'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1600&q=80', 'Panoramic city-facing suite with premium lounge zone.', now() - interval '12 days'),
    ('55555555-5555-5555-5555-555555555502'::uuid, 'S-102', 'Skyline Suite', 250.00, 'https://images.unsplash.com/photo-1590490360182-c33d57733427?auto=format&fit=crop&w=1600&q=80', 'Large suite with skyline view and high-floor privacy.', now() - interval '11 days'),
    ('55555555-5555-5555-5555-555555555503'::uuid, 'D-201', 'Deluxe Room', 185.00, 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1600&q=80', 'Elegant design and calm atmosphere for focused stays.', now() - interval '10 days'),
    ('55555555-5555-5555-5555-555555555504'::uuid, 'D-202', 'Deluxe Room', 179.00, 'https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=1600&q=80', 'Refined deluxe room with curated lighting and workspace.', now() - interval '9 days'),
    ('55555555-5555-5555-5555-555555555505'::uuid, 'ST-301', 'Standard Room', 140.00, 'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1600&q=80', 'Comfort-focused room with essential amenities.', now() - interval '8 days'),
    ('55555555-5555-5555-5555-555555555506'::uuid, 'ST-302', 'Standard Room', 138.00, 'https://images.unsplash.com/photo-1566665797739-1674de7a421a?auto=format&fit=crop&w=1600&q=80', 'Efficient layout for short and medium stays.', now() - interval '7 days'),
    ('55555555-5555-5555-5555-555555555507'::uuid, 'F-401', 'Family Room', 205.00, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1600&q=80', 'Spacious setup for family trips and group stays.', now() - interval '6 days'),
    ('55555555-5555-5555-5555-555555555508'::uuid, 'F-402', 'Family Room', 210.00, 'https://images.unsplash.com/photo-1578683010236-d716f9a3f461?auto=format&fit=crop&w=1600&q=80', 'Family configuration with flexible bedding options.', now() - interval '5 days'),
    ('55555555-5555-5555-5555-555555555509'::uuid, 'P-501', 'Premium Deluxe', 220.00, 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1600&q=80', 'Premium room with upgraded interior and amenities.', now() - interval '4 days'),
    ('55555555-5555-5555-5555-555555555510'::uuid, 'P-502', 'Premium Deluxe', 225.00, 'https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1600&q=80', 'High comfort category for business and leisure.', now() - interval '3 days'),
    ('55555555-5555-5555-5555-555555555511'::uuid, 'B-601', 'Business Room', 195.00, 'https://images.unsplash.com/photo-1468824357306-a439d58ccb1c?auto=format&fit=crop&w=1600&q=80', 'Business-ready space with dedicated work corner.', now() - interval '2 days'),
    ('55555555-5555-5555-5555-555555555512'::uuid, 'B-602', 'Business Room', 198.00, 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1600&q=80', 'Modern business room with ergonomic setup.', now() - interval '1 day')
ON CONFLICT DO NOTHING;

INSERT INTO bookings (id, guest_id, room_id, check_in_date, check_out_date, status, created_at, updated_at)
VALUES
    ('66666666-6666-6666-6666-666666666601'::uuid, '44444444-4444-4444-4444-444444444401'::uuid, '55555555-5555-5555-5555-555555555501'::uuid, current_date + 1, current_date + 4, 'CREATED', now() - interval '12 hours', now() - interval '12 hours'),
    ('66666666-6666-6666-6666-666666666602'::uuid, '44444444-4444-4444-4444-444444444402'::uuid, '55555555-5555-5555-5555-555555555502'::uuid, current_date + 2, current_date + 5, 'CONFIRMED', now() - interval '11 hours', now() - interval '11 hours'),
    ('66666666-6666-6666-6666-666666666603'::uuid, '44444444-4444-4444-4444-444444444403'::uuid, '55555555-5555-5555-5555-555555555503'::uuid, current_date + 3, current_date + 7, 'CREATED', now() - interval '10 hours', now() - interval '10 hours'),
    ('66666666-6666-6666-6666-666666666604'::uuid, '44444444-4444-4444-4444-444444444404'::uuid, '55555555-5555-5555-5555-555555555504'::uuid, current_date + 4, current_date + 8, 'CONFIRMED', now() - interval '9 hours', now() - interval '9 hours'),
    ('66666666-6666-6666-6666-666666666605'::uuid, '44444444-4444-4444-4444-444444444405'::uuid, '55555555-5555-5555-5555-555555555505'::uuid, current_date + 1, current_date + 3, 'CHECKED_IN', now() - interval '8 hours', now() - interval '8 hours'),
    ('66666666-6666-6666-6666-666666666606'::uuid, '44444444-4444-4444-4444-444444444406'::uuid, '55555555-5555-5555-5555-555555555506'::uuid, current_date + 2, current_date + 6, 'CREATED', now() - interval '7 hours', now() - interval '7 hours'),
    ('66666666-6666-6666-6666-666666666607'::uuid, '44444444-4444-4444-4444-444444444407'::uuid, '55555555-5555-5555-5555-555555555507'::uuid, current_date + 3, current_date + 6, 'CONFIRMED', now() - interval '6 hours', now() - interval '6 hours'),
    ('66666666-6666-6666-6666-666666666608'::uuid, '44444444-4444-4444-4444-444444444408'::uuid, '55555555-5555-5555-5555-555555555508'::uuid, current_date + 4, current_date + 7, 'CREATED', now() - interval '5 hours', now() - interval '5 hours'),
    ('66666666-6666-6666-6666-666666666609'::uuid, '44444444-4444-4444-4444-444444444409'::uuid, '55555555-5555-5555-5555-555555555509'::uuid, current_date + 5, current_date + 9, 'CONFIRMED', now() - interval '4 hours', now() - interval '4 hours'),
    ('66666666-6666-6666-6666-666666666610'::uuid, '44444444-4444-4444-4444-444444444410'::uuid, '55555555-5555-5555-5555-555555555510'::uuid, current_date + 1, current_date + 2, 'CHECKED_OUT', now() - interval '3 hours', now() - interval '3 hours'),
    ('66666666-6666-6666-6666-666666666611'::uuid, '44444444-4444-4444-4444-444444444411'::uuid, '55555555-5555-5555-5555-555555555511'::uuid, current_date + 2, current_date + 4, 'CREATED', now() - interval '2 hours', now() - interval '2 hours'),
    ('66666666-6666-6666-6666-666666666612'::uuid, '44444444-4444-4444-4444-444444444412'::uuid, '55555555-5555-5555-5555-555555555512'::uuid, current_date + 3, current_date + 5, 'CANCELLED', now() - interval '1 hours', now() - interval '1 hours')
ON CONFLICT DO NOTHING;

INSERT INTO landing_amenities (id, title, description, sort_order)
VALUES
    ('77777777-7777-7777-7777-777777777701'::uuid, 'Private Transfer', 'Airport and city transfer with fixed scheduling.', 10),
    ('77777777-7777-7777-7777-777777777702'::uuid, 'Executive Lounge', 'All-day refreshments with quiet meeting zones.', 11),
    ('77777777-7777-7777-7777-777777777703'::uuid, 'Fast Laundry', 'Same-day laundry and express ironing service.', 12),
    ('77777777-7777-7777-7777-777777777704'::uuid, 'Co-working Area', 'Shared productivity area with strong Wi-Fi.', 13),
    ('77777777-7777-7777-7777-777777777705'::uuid, 'Family Zone', 'Indoor play area and child-friendly support.', 14),
    ('77777777-7777-7777-7777-777777777706'::uuid, 'Spa Therapy', 'Massage and recovery programs with booking slots.', 15),
    ('77777777-7777-7777-7777-777777777707'::uuid, 'Rooftop Bar', 'Evening bar with panoramic view and live music.', 16),
    ('77777777-7777-7777-7777-777777777708'::uuid, '24/7 Concierge', 'Continuous assistance for travel and reservation requests.', 17),
    ('77777777-7777-7777-7777-777777777709'::uuid, 'Meeting Hall', 'Conference-ready hall with AV equipment.', 18),
    ('77777777-7777-7777-7777-777777777710'::uuid, 'Pet Friendly', 'Selected rooms available for travelers with pets.', 19),
    ('77777777-7777-7777-7777-777777777711'::uuid, 'Fitness Studio', 'Modern gym equipment and trainer sessions.', 20),
    ('77777777-7777-7777-7777-777777777712'::uuid, 'Late Checkout', 'Flexible checkout windows based on availability.', 21)
ON CONFLICT DO NOTHING;

INSERT INTO landing_offers (id, title, note, price_label, sort_order)
VALUES
    ('88888888-8888-8888-8888-888888888801'::uuid, 'Early Bird Saver', 'Book 14 days earlier and save up to 20%.', '$189', 10),
    ('88888888-8888-8888-8888-888888888802'::uuid, 'Romance Weekend', 'Suite decor package and late breakfast.', '$329', 11),
    ('88888888-8888-8888-8888-888888888803'::uuid, 'Workcation Pack', 'Business room + co-working credits.', '$279', 12),
    ('88888888-8888-8888-8888-888888888804'::uuid, 'City Explorer', 'Hotel stay + city tour transfer.', '$259', 13),
    ('88888888-8888-8888-8888-888888888805'::uuid, 'Long Stay 5+', 'Discount for 5 nights or more.', '$169', 14),
    ('88888888-8888-8888-8888-888888888806'::uuid, 'Family Weekend', 'Family room + kids menu bundle.', '$359', 15),
    ('88888888-8888-8888-8888-888888888807'::uuid, 'Spa Retreat', 'Deluxe room + wellness program.', '$309', 16),
    ('88888888-8888-8888-8888-888888888808'::uuid, 'Business Priority', 'Priority check-in and meeting room access.', '$289', 17),
    ('88888888-8888-8888-8888-888888888809'::uuid, 'Honeymoon Set', 'Suite + romantic amenities package.', '$399', 18),
    ('88888888-8888-8888-8888-888888888810'::uuid, 'Student Traveler', 'Smart budget option with breakfast.', '$149', 19),
    ('88888888-8888-8888-8888-888888888811'::uuid, 'Festival Days', 'Peak date package with transfer.', '$339', 20),
    ('88888888-8888-8888-8888-888888888812'::uuid, 'Winter Comfort', 'Seasonal package with warm welcome set.', '$269', 21)
ON CONFLICT DO NOTHING;
