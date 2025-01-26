package com.sakethh.linkora.domain.dto.server

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object LocalIdSerializer : KSerializer<Long> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "localId", PrimitiveKind.LONG)

    override fun deserialize(decoder: Decoder): Long {
        return decoder.decodeLong()
    }

    override fun serialize(encoder: Encoder, value: Long) {
        if (value > 0) {
            encoder.encodeLong(value)
        }
    }
}