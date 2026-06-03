package com.personal.biji.android.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.time.Instant
import java.util.concurrent.TimeUnit

object ApiFactory {
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantAdapter)
        .create()

    fun create(baseUrl: String): BijiApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(OkHttpClient.Builder().callTimeout(8, TimeUnit.SECONDS).build())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
        .create(BijiApi::class.java)

    private object InstantAdapter : JsonSerializer<Instant>, JsonDeserializer<Instant> {
        override fun serialize(src: Instant, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
            context.serialize(src.toString())
        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Instant =
            Instant.parse(json.asString)
    }
}
