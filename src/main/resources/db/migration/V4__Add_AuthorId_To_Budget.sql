ALTER TABLE budget ADD COLUMN author_id INTEGER REFERENCES author(id);
UPDATE budget SET author_id = NULL WHERE author_id IS NULL;