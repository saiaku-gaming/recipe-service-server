package com.valhallagame.valhalla.recipeserviceserver.model

import javax.persistence.*

@Entity
@Table(name = "recipe")
data class Recipe (
        @Id
        @SequenceGenerator(name = "recipe_recipe_id_seq", sequenceName = "recipe_recipe_id_seq", allocationSize = 1)
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recipe_recipe_id_seq")
        @Column(name = "recipe_id")
        val id: Long? = null,

        @Column(name = "character_name")
        val characterName: String
)