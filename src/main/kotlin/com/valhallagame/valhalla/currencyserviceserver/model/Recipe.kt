package com.valhallagame.valhalla.currencyserviceserver.model

import com.valhallagame.currencyserviceclient.model.CurrencyType
import javax.persistence.*

@Entity
@Table(name = "currency")
data class Recipe (
        @Id
        @SequenceGenerator(name = "currency_currency_id_seq", sequenceName = "currency_currency_id_seq", allocationSize = 1)
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "currency_currency_id_seq")
        @Column(name = "currency_id")
        val id: Long? = null,

        @Column(name = "character_name")
        val characterName: String,

        @Enumerated(EnumType.STRING)
        @Column(name = "type")
        val type: CurrencyType,

        @Column(name = "amount")
        var amount: Int
)