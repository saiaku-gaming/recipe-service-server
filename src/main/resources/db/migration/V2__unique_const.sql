DROP TABLE recipe;

CREATE TABLE recipe (
  recipe_id      SERIAL PRIMARY KEY,
  character_name VARCHAR NOT NULL,
  recipe_name    VARCHAR NOT NULL,
  claimed        BOOL NOT NULL
);

-- I wasn't able to get H2 to create sequence from serial. But this should work until I kill of H2 with
-- some kind of awesome in memory postgres thingy...
CREATE SEQUENCE IF NOT EXISTS recipe_recipe_id_seq;


ALTER TABLE recipe ADD CONSTRAINT recipe_name_character_name_unique UNIQUE (recipe_name, character_name);
