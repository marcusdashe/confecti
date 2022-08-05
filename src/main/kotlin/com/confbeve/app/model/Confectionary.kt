package com.confbeve.app.model

import javax.persistence.*

@Entity
class Confectionary {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Int = 0

    @Column(unique = true)
    var brandName: String = ""

    @Column
    var type: String = ""

    @Column
    var weight: Double = 0.0

    @Column
    var hasSugar: Boolean = true
}