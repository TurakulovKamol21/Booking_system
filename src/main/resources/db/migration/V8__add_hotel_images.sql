ALTER TABLE hotels
ADD COLUMN IF NOT EXISTS image_url VARCHAR(400);

UPDATE hotels
SET image_url = CASE code
    WHEN 'AG-TAS-01' THEN 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=1800&q=80'
    WHEN 'AG-SAM-01' THEN 'https://images.unsplash.com/photo-1551882547-ff40c63fe5fa?auto=format&fit=crop&w=1800&q=80'
    WHEN 'AG-BHK-01' THEN 'https://images.unsplash.com/photo-1455587734955-081b22074882?auto=format&fit=crop&w=1800&q=80'
    ELSE 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?auto=format&fit=crop&w=1800&q=80'
END
WHERE image_url IS NULL;
