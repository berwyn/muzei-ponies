package so.codeweaver.muzei.ponies

import com.google.android.apps.muzei.api.UserCommand
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.MuzeiArtProvider

class PonyArtProvider : MuzeiArtProvider() {
    companion object {
        private const val TAG = "PonyArtProvider"

        private const val COMMAND_ID_VIEW_PAGE = 1
    }

    override fun onLoadRequested(initial: Boolean) {
        DerpibooruWorker.enqueueLoad()
    }

    override fun getCommands(artwork: Artwork) = listOf(
            UserCommand(COMMAND_ID_VIEW_PAGE, context.getString(R.string.pony_view_art_page))
    )

    override fun onCommand(artwork: Artwork, id: Int) {
        when (id) {
            COMMAND_ID_VIEW_PAGE -> {
                TODO("Send them to Derpi and the artwork page")
            }
        }
    }
}