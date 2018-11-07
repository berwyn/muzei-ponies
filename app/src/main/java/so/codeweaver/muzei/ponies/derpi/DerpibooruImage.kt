package so.codeweaver.muzei.ponies.derpi

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.apps.muzei.api.provider.Artwork

@Entity
data class DerpibooruImage(
        @PrimaryKey
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