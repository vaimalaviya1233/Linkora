package com.sakethh.linkora.domain.model.localization

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity("localized_languages")
data class LocalizedLanguage(
    @PrimaryKey
    val languageCode: String,
    val languageName: String,
    val localizedStringsCount: Int,
    val contributionLink: String
)