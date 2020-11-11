package com.example.umbrella

import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import com.squareup.okhttp.Response
import org.junit.Assert.assertEquals
import org.junit.Test


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val client = OkHttpClient()

        val url =
            "http://api.openweathermap.org/data/2.5/weather?lat=35.69&lon=139.69&APPID=4e1ceba3f8253ccbc27b056bb52d741a"
        val request: Request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response: Response = client.newCall(request).execute()
        val body = response.body()
        println(body.string())
        assertEquals(4, 2 + 2)
    }
}