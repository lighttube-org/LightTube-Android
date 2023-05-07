package dev.kuylar.lighttube.api.models

import java.util.Date

class SubscriptionFeedItem(
	val id: String,
	val title: String,
	val description: String,
	val viewCount: Long,
	val thumbnail: String,
	val channelName: String,
	val channelId: String,
	val publishedDate: Date
)