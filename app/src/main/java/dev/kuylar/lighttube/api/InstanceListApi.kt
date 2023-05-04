package dev.kuylar.lighttube.api

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request

class InstanceListApi {
	companion object {
		fun getInstances(): ArrayList<LightTubeInstance> {
			val request: Request = Request.Builder()
				.url("https://raw.githubusercontent.com/kuylar/lighttube/master/public_instances.json")
				.build()

			OkHttpClient().newCall(request).execute().use { response ->
				return Gson().fromJson(
					response.body!!.string(),
					object : TypeToken<ArrayList<LightTubeInstance>>() {}.type
				)
			}
		}
	}
}

class LightTubeInstance(
	val host: String,
	val api: Boolean,
	val accounts: Boolean
)
