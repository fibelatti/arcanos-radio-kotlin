package de.developercity.arcanosradio.core.network

import de.developercity.arcanosradio.BuildConfig
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class HeadersInterceptor @Inject constructor() : Interceptor {

    private val headers: MutableMap<String, String> = hashMapOf(
        "X-Parse-Application-Id" to BuildConfig.API_APPLICATION_ID,
        "Content-Type" to "application/json"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        val requestBuilder: Request.Builder = request.newBuilder()

        headers.forEach { (headerName, headerValue) ->
            requestBuilder.addHeader(headerName, headerValue)
        }

        return chain.proceed(requestBuilder.build())
    }
}
