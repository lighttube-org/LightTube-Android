package dev.kuylar.lighttube.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.google.gson.Gson
import dev.kuylar.lighttube.R
import dev.kuylar.lighttube.database.models.DownloadInfo

class DownloadDatabase(private val context: Context) :
	SQLiteOpenHelper(context, "downloads", null, DATABASE_VERSION) {
	private val gson = Gson()

	override fun onCreate(db: SQLiteDatabase?) {
		db?.execSQL(DownloadEntry.SQL_CREATE_DOWNLOADS)
	}

	override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
		TODO("Not yet implemented")
	}

	fun addDownload(id: String, itag: Int, info: DownloadInfo) {
		val db = writableDatabase
		val countQuery = db?.query(
			DownloadEntry.TABLE_NAME,
			arrayOf(DownloadEntry.COLUMN_NAME_ID),
			"${DownloadEntry.COLUMN_NAME_ID} = ?",
			arrayOf(id),
			null,
			null,
			"${DownloadEntry.COLUMN_NAME_ID} ASC"
		)
		val count = countQuery?.count ?: 0
		countQuery?.close()

		val values = ContentValues().apply {
			put(DownloadEntry.COLUMN_NAME_ID, id)
			put(DownloadEntry.COLUMN_NAME_ITAG, itag)
			put(DownloadEntry.COLUMN_NAME_INFO_JSON, gson.toJson(info))
		}

		if (count > 0) {
			throw Exception(context.getString(R.string.error_download_already_exists))
		} else {
			db?.insert(DownloadEntry.TABLE_NAME, null, values)
		}
		db.close()
	}

	@SuppressLint("Range")
	fun getAllDownloads(): List<DownloadInfo> {
		val db = readableDatabase
		val cursor = db.query(
			DownloadEntry.TABLE_NAME, arrayOf(
				DownloadEntry.COLUMN_NAME_ID,
				DownloadEntry.COLUMN_NAME_ITAG,
				DownloadEntry.COLUMN_NAME_INFO_JSON
			), "", arrayOf(), "", "", ""
		)
		val list = arrayListOf<DownloadInfo>()
		while (cursor.moveToNext()) {
			list.add(gson.fromJson(
				cursor.getString(cursor.getColumnIndex(DownloadEntry.COLUMN_NAME_INFO_JSON)),
				DownloadInfo::class.java
			))
		}
		cursor.close()
		db.close()
		return list
	}

	companion object {
		const val DATABASE_VERSION = 1

		object DownloadEntry : BaseColumns {
			const val TABLE_NAME = "downloads"
			const val COLUMN_NAME_ID = "id"
			const val COLUMN_NAME_ITAG = "itag"
			const val COLUMN_NAME_INFO_JSON = "info_json"

			const val SQL_CREATE_DOWNLOADS =
				"CREATE TABLE $TABLE_NAME (" +
						"$COLUMN_NAME_ID TEXT PRIMARY KEY NOT NULL, " +
						"$COLUMN_NAME_ITAG UNSIGNED TINYINT NOT NULL, " +
						"$COLUMN_NAME_INFO_JSON TEXT NOT NULL);"
		}
	}
}