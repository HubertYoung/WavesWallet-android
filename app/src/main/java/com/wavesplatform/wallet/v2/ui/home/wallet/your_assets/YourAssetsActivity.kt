package com.wavesplatform.wallet.v2.ui.home.wallet.your_assets

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.EditDrawableText
import com.mindorks.editdrawabletext.onDrawableClickListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_your_assets.*
import kotlinx.android.synthetic.main.layout_empty_data.view.*
import pers.victor.ext.inflate
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class YourAssetsActivity : BaseActivity(), YourAssetsView {

    @Inject
    @InjectPresenter
    lateinit var presenter: YourAssetsPresenter


    @Inject
    lateinit var adapter: YourAssetsAdapter

    @ProvidePresenter
    fun providePresenter(): YourAssetsPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_your_assets

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.white)

        setupToolbar(toolbar_view, true, getString(R.string.your_assets_toolbar_title), R.drawable.ic_toolbar_back_black)

        if (intent.hasExtra(BUNDLE_ASSET_ID)) {
            val assetId = intent.getStringExtra(BUNDLE_ASSET_ID)
            adapter.currentAssetId = assetId
        }

        val searchView = layoutInflater
                .inflate(R.layout.search_view, null, false)
        val search = searchView.findViewById<EditDrawableText>(R.id.editText_search)

        eventSubscriptions.add(RxTextView.textChanges(search)
                .skipInitialValue()
                .map {
                    if (it.isNotEmpty()) {
                        search.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_search_24_basic_500, 0,
                                R.drawable.ic_clear_24_basic_500, 0)
                    } else {
                        search.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_search_24_basic_500, 0, 0, 0)
                    }
                    return@map it.toString()
                }
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    adapter.filter(it)
                })

        search.setDrawableClickListener(object : onDrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawablePosition.RIGHT -> {
                        search.text = null
                    }
                }
            }
        })

        recycle_assets.layoutManager = LinearLayoutManager(this)
        recycle_assets.adapter = adapter
        adapter.bindToRecyclerView(recycle_assets)

        if (intent.hasExtra(CRYPTO_CURRENCY)) {
            presenter.loadCryptoAssets(presenter.greaterZeroBalance)
        } else {
            presenter.loadAssets(presenter.greaterZeroBalance)
        }

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as AssetBalance

            adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, _ ->
                // disable click for next items, which user click before activity will finish
            }

            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(BUNDLE_ASSET_ITEM, item)
            })
            finish()
        }

        adapter.setHeaderView(searchView)
        adapter.emptyView = getEmptyView()

        recycle_assets.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                enableElevation(recyclerView.computeVerticalScrollOffset() == 0)
            }
        })
    }

    override fun showAssets(assets: ArrayList<AssetBalance>) {
        adapter.allData = ArrayList(assets)
        adapter.setNewData(assets)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_your_assets, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_sorting -> {
                if (intent.hasExtra(CRYPTO_CURRENCY)) {
                    presenter.loadCryptoAssets(!presenter.greaterZeroBalance)
                } else {
                    presenter.loadAssets(!presenter.greaterZeroBalance)
                }
                item.title = if (presenter.greaterZeroBalance) {
                    getString(R.string.your_asset_activity_all)
                } else {
                    getString(R.string.your_asset_activity_greater_zero)
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun enableElevation(enable: Boolean) {
        if (enable) {
            ViewCompat.setElevation(appbar_layout, 0F)
        } else {
            ViewCompat.setElevation(appbar_layout, 8F)
        }
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.layout_empty_data)
        view.text_empty.text = getString(R.string.your_assets_empty_state)
        return view
    }

    companion object {
        const val BUNDLE_ASSET_ITEM = "asset"
        const val BUNDLE_ADDRESS = "address"
        const val CRYPTO_CURRENCY = "crypto_currency"
        const val BUNDLE_ASSET_ID = "asset_id"
    }
}