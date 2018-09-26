package com.valhallagame.valhalla.recipeserviceserver.model

import javax.persistence.*

@Entity
@Table(name = "recipe")
data class Recipe (
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "recipe_id")
        val id: Long? = null,

        @Column(name = "character_name")
        val characterName: String,

        @Column(name = "recipe_name")
        val recipeName: String,

        @Column(name = "claimed")
        var claimed: Boolean
)