package it.niedermann.nextcloud.ocs

import android.content.Context
import com.google.gson.GsonBuilder
import com.nextcloud.android.sso.AccountImporter
import com.nextcloud.android.sso.api.NextcloudAPI
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException
import com.nextcloud.android.sso.model.SingleSignOnAccount
import retrofit2.NextcloudRetrofitApiBuilder

class ApiProvider<T> private constructor(
    context: Context,
    ssoAccount: SingleSignOnAccount,
    clazz: Class<T>,
    endpoint: String
) : AutoCloseable {
    private val nextcloudAPI: NextcloudAPI
    private val api: T

    init {
        nextcloudAPI = NextcloudAPI(
            context,
            AccountImporter.getSingleSignOnAccount(context, ssoAccount.name),
            GsonBuilder().create()
        ) { obj: Throwable -> obj.printStackTrace() }
        api = NextcloudRetrofitApiBuilder(nextcloudAPI, endpoint).create(clazz)
    }

    fun getApi(): T {
        return api
    }

    override fun close() {
        nextcloudAPI.close()
    }

    class Factory() {
        @Throws(NextcloudFilesAppAccountNotFoundException::class)
        fun <T> createApiProvider(
            context: Context,
            ssoAccount: SingleSignOnAccount,
            clazz: Class<T>,
            endpoint: String
        ): ApiProvider<T> {
            return ApiProvider(context, ssoAccount, clazz, endpoint)
        }
    }
}
