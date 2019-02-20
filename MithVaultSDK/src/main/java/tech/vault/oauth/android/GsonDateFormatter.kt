package tech.vault.oauth.android

import android.content.Context
import android.os.Build
import com.google.gson.*
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class GsonDateAdapter(applicationContext: Context) : JsonDeserializer<Date>, JsonSerializer<Date> {

    private val configuration = applicationContext.resources.configuration

    private val locale =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                configuration.locales[0]
            else
                configuration.locale

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
        val dateString = json?.asString ?: throw JsonParseException("Gson parse date fail")

        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale)
                    .apply { timeZone = TimeZone.getTimeZone("UTC") }
                    .parse(dateString)
        } catch (e: ParseException) {
            throw JsonParseException("Gson parse date $dateString fail with exception: ${e.localizedMessage}")
        }
    }

    override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val date = src ?: throw JsonParseException("Gson serialize date fail")
        return JsonPrimitive(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", locale)
                .apply { timeZone = TimeZone.getTimeZone("UTC") }.format(date))
    }

}