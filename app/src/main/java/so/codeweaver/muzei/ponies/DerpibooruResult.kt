package so.codeweaver.muzei.ponies

import android.net.Uri
import com.google.android.apps.muzei.api.provider.Artwork
import java.util.*

data class DerpibooruImage(
        val id: String,
        val image: String,
        val uploader: String,
        val tags: String
) {
    val fullImageUri: String
        get() = "https:$image"

    val siteUri: String
        get() = "https://derpibooru.org/$id"

    fun buildArtwork(): Artwork {
        val imageId = id

        return Artwork().apply {
            token = imageId
            title = "#$imageId"
            byline = "Uploaded by $uploader"
            webUri = Uri.parse(siteUri)
            persistentUri = Uri.parse(fullImageUri)
        }
    }
}

data class DerpibooruResult(
        val total: Int,
        val search: Array<DerpibooruImage>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DerpibooruResult

        if (total != other.total) return false
        if (!Arrays.equals(search, other.search)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = total
        result = 31 * result + Arrays.hashCode(search)
        return result
    }
}
