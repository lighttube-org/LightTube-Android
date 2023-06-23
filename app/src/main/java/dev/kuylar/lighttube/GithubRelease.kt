package dev.kuylar.lighttube

class GithubRelease(
	val url: String,
	val assetsUrl: String,
	val uploadUrl: String,
	val htmlUrl: String,
	val id: Long,
	val author: Author,
	val nodeID: String,
	val tagName: String,
	val targetCommitish: String,
	val name: String,
	val draft: Boolean,
	val prerelease: Boolean,
	val createdAt: String,
	val publishedAt: String,
	val assets: List<Asset>,
	val tarballUrl: String,
	val zipballUrl: String,
	val body: String
) {
	class Asset(
		val url: String,
		val id: Long,
		val nodeID: String,
		val name: String,
		val label: String,
		val uploader: Author,
		val contentType: String,
		val state: String,
		val size: Long,
		val downloadCount: Long,
		val createdAt: String,
		val updatedAt: String,
		val browserDownloadUrl: String
	)

	class Author(
		val login: String,
		val id: Long,
		val nodeID: String,
		val avatarUrl: String,
		val gravatarID: String,
		val url: String,
		val htmlUrl: String,
		val followersUrl: String,
		val followingUrl: String,
		val gistsUrl: String,
		val starredUrl: String,
		val subscriptionsUrl: String,
		val organizationsUrl: String,
		val reposUrl: String,
		val eventsUrl: String,
		val receivedEventsUrl: String,
		val type: String,
		val siteAdmin: Boolean
	)
}