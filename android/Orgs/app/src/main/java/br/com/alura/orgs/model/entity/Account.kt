package br.com.alura.orgs.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "account")
data class Account(
    @PrimaryKey
    val username: String,
    val password: String
)