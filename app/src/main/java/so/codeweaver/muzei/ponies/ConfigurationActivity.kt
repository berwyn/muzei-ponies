package so.codeweaver.muzei.ponies

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnTextChanged
import com.google.android.apps.muzei.api.provider.Artwork
import com.google.android.apps.muzei.api.provider.ProviderContract
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ConfigurationActivity : AppCompatActivity() {
    @BindView(R.id.view_tags_input)
    lateinit var tagsInput: TextInputEditText

    @BindView(R.id.view_api_key_input)
    lateinit var keyInput: TextInputEditText

    @BindView(R.id.view_preview_list)
    lateinit var previewList: RecyclerView

    @BindView(R.id.view_flex_root)
    lateinit var flexRoot: FlexboxLayout

    @BindView(R.id.view_toolbar)
    lateinit var toolbar: Toolbar

    private val adapter = PreviewListAdapter()

    private val tagSubject = BehaviorSubject.create<String>()
    private val apiKeySubject = BehaviorSubject.create<String?>()

    private val query = Observable.combineLatest(
            tagSubject,
            apiKeySubject,
            BiFunction { tag: String, key: String? -> Pair(tag, key) }
    )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configuration)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)

        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val tagString = prefs.getString(DerpibooruService.PREF_TAGS, DerpibooruService.DEFAULT_TAGS)!!
        tagSubject.onNext(tagString)
        tagsInput.setText(tagString)

        val apiKey = prefs.getString(DerpibooruService.PREF_KEY, null)
        apiKeySubject.onNext(apiKey)
        keyInput.setText(apiKey)

        val layoutManager = when (flexRoot.flexDirection) {
            FlexDirection.COLUMN -> {
                LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
            }
            else -> {
                GridLayoutManager(this, previewList.width / 192)
            }
        }

        previewList.layoutManager = layoutManager
        previewList.adapter = adapter

        compositeDisposable = CompositeDisposable()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_config, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()

        compositeDisposable.add(query
                .debounce(300, TimeUnit.MILLISECONDS)
                .subscribe { (tagString, apiKey) ->
                    adapter.queryImages(tagString, apiKey)
                })
    }

    override fun onPause() {
        compositeDisposable.dispose()
        super.onPause()
    }

    override fun onOptionsItemSelected(item: MenuItem?) = when (item?.itemId) {
        R.id.menu_item_commit -> {
            commitChanges()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @OnTextChanged(R.id.view_tags_input)
    fun onTagsChanged(text: CharSequence?) {
        tagSubject.onNext(text.toString())
    }

    @OnTextChanged(R.id.view_api_key_input)
    fun onApiKeyChanged(text: CharSequence?) {
        apiKeySubject.onNext(text.toString())
    }

    private fun commitChanges() {
        compositeDisposable.add(
                query.firstElement()
                        .subscribe { (tagString, apiKey) ->
                            val tx = PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                            tx.putString(DerpibooruService.PREF_TAGS, tagString)
                            tx.putString(DerpibooruService.PREF_KEY, apiKey)
                            tx.apply()

                            adapter.images.forEachIndexed { index, image ->
                                if (index == 0) {
                                    ProviderContract.Artwork.setArtwork(
                                            this,
                                            PonyArtProvider::class.java,
                                            image.buildArtwork()
                                    )
                                } else {
                                    ProviderContract.Artwork.addArtwork(
                                            this,
                                            PonyArtProvider::class.java,
                                            image.buildArtwork()
                                    )
                                }

                            }
                        }
        )
    }
}

class PreviewListAdapter : RecyclerView.Adapter<PreviewListViewHolder>() {
    internal val images: MutableList<DerpibooruImage> = mutableListOf()
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
