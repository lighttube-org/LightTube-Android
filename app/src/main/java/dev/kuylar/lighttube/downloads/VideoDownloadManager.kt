package dev.kuylar.lighttube.downloads

import android.content.Context
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.scheduler.Requirements
import com.google.gson.Gson
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.api.models.Format
import dev.kuylar.lighttube.service.VideoDownloadService
import dev.kuylar.lighttube.ui.activity.MainActivity
import java.io.File
import java.util.concurrent.Executor
import kotlin.io.path.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@UnstableApi
object VideoDownloadManager {
	private val allowedItags = arrayListOf("18", "22")
	private var exoDatabaseProvider: StandaloneDatabaseProvider? = null
	private val gson = Gson()

	private fun getDatabaseProvider(context: Context): StandaloneDatabaseProvider {
		if (exoDatabaseProvider == null)
			exoDatabaseProvider = StandaloneDatabaseProvider(context)
		return exoDatabaseProvider!!
	}

	fun getDownloadManager(context: Context): DownloadManager {
		val databaseProvider = getDatabaseProvider(context)
		val downloadCache = SimpleCache(
			File(context.filesDir.absolutePath, "downloads"),
			NoOpCacheEvictor(),
			databaseProvider
		)
		val dataSourceFactory = DefaultHttpDataSource.Factory()
		val downloadExecutor = Executor(Runnable::run)

		val downloadManager = DownloadManager(
				context,
				databaseProvider,
				downloadCache,
				dataSourceFactory,
				downloadExecutor
			)

		downloadManager.requirements =
			Requirements(Requirements.NETWORK_UNMETERED or Requirements.DEVICE_STORAGE_NOT_LOW)
		downloadManager.maxParallelDownloads = 1

		return downloadManager
	}

	fun startDownload(
		context: Context,
		id: String,
		format: Format,
		info: DownloadInfo,
		proxied: Boolean
	) {
		if (!allowedItags.contains(format.itag)) throw Exception(
			context.getString(
				R.string.error_download_itag_not_supported,
				format.itag
			)
		)

		val url = if (proxied) {
			val api = (context as MainActivity).getApi()
			if (!api.getInstanceInfo().allowsThirdPartyProxyUsage)
				throw Exception(context.getString(R.string.error_download_itag_not_supported))
			"${api.host}/proxy/media/$id/${format.itag}"
		} else format.url
		val downloadDir = Path(context.filesDir.absolutePath, "downloads")
		if (!downloadDir.exists()) downloadDir.createDirectories()

		val downloadRequest = DownloadRequest.Builder(id, Uri.parse(url)).apply {
			this.setData(gson.toJson(info).encodeToByteArray())
		}.build()
		DownloadService.sendAddDownload(context, VideoDownloadService::class.java, downloadRequest, true)
	}

	fun getDownloads(context: Context): List<DownloadInfo> {
		return emptyList()
	}
}