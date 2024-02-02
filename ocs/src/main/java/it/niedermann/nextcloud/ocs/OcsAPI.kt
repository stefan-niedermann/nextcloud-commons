package it.niedermann.nextcloud.ocs

import com.nextcloud.android.sso.model.ocs.OcsCapabilitiesResponse.OcsCapabilities
import com.nextcloud.android.sso.model.ocs.OcsResponse
import com.nextcloud.android.sso.model.ocs.OcsUser
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

/**
 * @link [OCS REST API](https://docs.nextcloud.com/server/latest/developer_manual/client_apis/OCS/ocs-api-overview.html)
 */
interface OcsAPI {

    @GET("capabilities?format=json")
    fun getCapabilities(@Header("If-None-Match") eTag: String): Call<OcsResponse<OcsCapabilities>>

    @GET("users/{userId}?format=json")
    fun getUser(@Path("userId") userId: String): Call<OcsResponse<OcsUser>>
}
