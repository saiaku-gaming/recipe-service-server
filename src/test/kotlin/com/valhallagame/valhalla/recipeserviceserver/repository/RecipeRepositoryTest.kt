package com.valhallagame.valhalla.recipeserviceserver.repository

import com.valhallagame.valhalla.recipeserviceserver.model.Recipe
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
@DataJpaTest
@ActiveProfiles("in-memory-db", "test")
class RecipeRepositoryTest(@Autowired val recipeRepository: RecipeRepository) {

    @Test
    fun findByCharacterNameAndClaimed_ignoreUnclaimed() {
        recipeRepository.save(Recipe(null, "nisse", "bed", false))
        val findByCharacterNameAndClaimed = recipeRepository.findByCharacterNameAndClaimed("nisse", true)
        assertThat(findByCharacterNameAndClaimed).isEmpty()
    }

    @Test
    fun findByCharacterNameAndClaimed() {
        recipeRepository.save(Recipe(null, "nisse", "bed", false))

        val recipeUnclaimed = recipeRepository.findByCharacterNameAndClaimed("nisse", true)
        assertThat(recipeUnclaimed).isEmpty()

        val recipe = recipeRepository.findByCharacterNameAndRecipeName("nisse", "bed")

        assertThat(recipe!!).isNotNull

        assertThat(recipe.recipeName).isEqualTo("bed")
        assertThat(recipe.characterName).isEqualTo("nisse")
        recipe.claimed = true
        recipeRepository.save(recipe)

        val recipeClaimed = recipeRepository.findByCharacterNameAndClaimed("nisse", true)
        assertThat(recipeClaimed).isNotNull
    }

    @Test
    fun deleteByCharacterName() {
        recipeRepository.save(Recipe(null, "nisse", "bed", false))
        val recipeBefore = recipeRepository.findByCharacterNameAndRecipeName("nisse", "bed")!!
        assertThat(recipeBefore.characterName).isEqualTo("nisse")

        recipeRepository.deleteByCharacterName("nisse")
        val recipeAfter = recipeRepository.findByCharacterNameAndRecipeName("nisse", "bed")
        assertThat(recipeAfter).isNull()
    }
}