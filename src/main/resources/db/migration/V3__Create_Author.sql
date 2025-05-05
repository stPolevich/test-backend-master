CREATE TABLE author (
                        id SERIAL PRIMARY KEY,
                        full_name TEXT NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT now()
);