package io.github.kamo.vrcm.data.api.file

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.request

internal const val FILE_API_SUFFIX = "file"

class FileAPI(private val client: HttpClient) {

    companion object {
        private val regex = Regex("file_[\\w-]+")
        private val versionRegex = Regex("/[0-9]+/")
        fun findFileId(fileUrl: String): String {
            val match = regex.find(fileUrl)
            return match?.groupValues?.get(0) ?: ""
        }

        fun findFileVersion(fileUrl: String): String {
            val versionMatch = versionRegex.find(fileUrl, 30)
            return versionMatch?.groupValues?.last()?.replace("/", "") ?: "1"
        }
    }

    suspend fun findImageFileLocal(fileUrl: String, fileSize: Int = 128) =
        client.get {
            val findFileId = findFileId(fileUrl)
            val findFileVersion = findFileVersion(fileUrl)
            url("https://api.vrchat.cloud/api/1/image/$findFileId/$findFileVersion/$fileSize")
        }.request.url.toString()

}