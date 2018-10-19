package so.codeweaver.muzei.ponies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ConfigurationActivity : AppCompatActivity() {
    @BindView(R.id.view_tags_input)
    lateinit var tagsInput: TextInputEditText

    @BindView(R.id.view_api_key_input)
    lateinit var keyInput: TextInputEditText

    var gridParallel: Int = 1

    @BindView(R.id.view_preview_list)
    lateinit var previewList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)
        ButterKnife.bind(this)

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val tagString = prefs.getString(DerpibooruService.PREF_TAGS, "safe,wallpaper,score.gte:300,width.gte:1920,height.gte:1080")
        tagsInput.setText(tagString)

        val keyString = prefs.getString(DerpibooruService.PREF_KEY, null)
        keyInput.setText(keyString)

        val layoutManager = when (gridParallel) {
            1 -> {
                LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
            }
            else -> {
                GridLayoutManager(this, gridParallel)
            }
        }

        val adapter = PreviewListAdapter()
        adapter.queryImages(tagString, keyString)

        previewList.layoutManager = layoutManager
        previewList.adapter = adapter
    }
}

class PreviewListAdapter : RecyclerView.Adapter<PreviewListViewHolder>() {
    private var images: MutableList<DerpibooruResult.Image> = mutableListOf()
    private val service: DerpibooruService

    init {
        val client = OkHttpClient.Builder().build()
        service = Retrofit.Builder()
                .baseUrl("https://derpibooru.org")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(DerpibooruService::class.java)
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: PreviewListViewHolder, position: Int) {
        val picasso = Picasso.get()
        if (BuildConfig.DEBUG) {
            picasso.setIndicatorsEnabled(true)
        }

        picasso.load(images[position].fullImageUri)
                .resize(192, 192)
                .centerCrop()
                .into(holder.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PreviewListViewHolder {
        var v = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_preview_list_item, parent, false) as ImageView

        return PreviewListViewHolder(v)
    }

    fun queryImages(tags: String, apiKey: String?) {
        service
                .search(tags, DerpibooruService.SEARCH_FILTER_RANDOM, DerpibooruService.SEARCH_ORDER_DESC, apiKey)
                .enqueue(object : Callback<DerpibooruResult> {
                    override fun onFailure(call: Call<DerpibooruResult>, t: Throwable) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onResponse(call: Call<DerpibooruResult>, response: Response<DerpibooruResult>) {
                        images.clear()
                        images.addAll(response.body()?.search ?: arrayOf())
                        if (images.isNotEmpty()) {
                            notifyDataSetChanged()
                        }
                    }

                })
    }
}

data class PreviewListViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
