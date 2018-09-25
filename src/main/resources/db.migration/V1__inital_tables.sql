CREATE TABLE recipe (
  recipe_id      SERIAL PRIMARY KEY,
  character_name TEXT NOT NULL,
  recipe_name    TEXT NOT NULL,
  claimed        BOOL NOT NULL
);