package com.sakethh.linkora.data.local.localization.language.translations

interface TranslationsRepo {
    suspend fun addLocalizedStrings(languageCode: String)

    suspend fun doesStringsPackForThisLanguageExists(languageCode: String): Boolean

    suspend fun getLocalizedStringValueFor(stringName: String, languageCode: String): String?

    suspend fun deleteAllLocalizedStringsForThisLanguage(languageCode: String)
}