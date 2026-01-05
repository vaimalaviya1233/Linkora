package com.sakethh.linkora.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RefreshLink(
    @PrimaryKey
    val refreshedLinkId: Long
)
