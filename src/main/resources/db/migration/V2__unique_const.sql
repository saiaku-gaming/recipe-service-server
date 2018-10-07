DELETE FROM recipe;
ALTER TABLE recipe ADD CONSTRAINT recipe_name_character_name_unique UNIQUE (recipe_name, character_name);
