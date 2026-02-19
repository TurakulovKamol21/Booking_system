UPDATE rooms
SET image_url = CASE
                    WHEN LOWER(room_type) LIKE '%suite%'
                        THEN 'https://images.unsplash.com/photo-1445019980597-93fa8acb246c?auto=format&fit=crop&w=1600&q=80'
                    WHEN LOWER(room_type) LIKE '%deluxe%' OR LOWER(room_type) LIKE '%premium%'
                        THEN 'https://images.unsplash.com/photo-1618773928121-c32242e63f39?auto=format&fit=crop&w=1600&q=80'
                    WHEN LOWER(room_type) LIKE '%family%'
                        THEN 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=1600&q=80'
                    ELSE 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1600&q=80'
    END
WHERE image_url IS NULL
   OR image_url LIKE '/assets/%';
