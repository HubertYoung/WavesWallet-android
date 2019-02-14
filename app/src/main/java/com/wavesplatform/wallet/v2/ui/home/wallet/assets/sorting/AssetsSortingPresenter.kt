package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.data.model.local.AssetSortingItem
import com.wavesplatform.sdk.model.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class AssetsSortingPresenter @Inject constructor() : BasePresenter<AssetsSortingView>() {
    var needToUpdate: Boolean = false
    var visibilityConfigurationActive = false

    fun loadAssets() {
        runAsync {
            addSubscription(queryAllAsSingle<AssetBalanceDb>().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        val result = mutableListOf<AssetSortingItem>()

                        val favoriteList = it.filter { it.isFavorite }
                                .sortedBy { it.position }
                                .sortedByDescending { it.isFavorite }
                                .mapTo(mutableListOf()) {
                                    AssetSortingItem(AssetSortingItem.TYPE_FAVORITE, it.convertFromDb())
                                }
                        val notFavoriteList = it.filter({ !it.isFavorite && !it.isSpam })
                                .sortedBy { it.position }
                                .mapTo(mutableListOf()) {
                                    AssetSortingItem(AssetSortingItem.TYPE_NOT_FAVORITE, it.convertFromDb())
                                }

                        result.addAll(favoriteList)
                        if (favoriteList.isNotEmpty() && notFavoriteList.isNotEmpty()) {
                            result.add(AssetSortingItem(AssetSortingItem.TYPE_LINE))
                        }
                        result.addAll(notFavoriteList)

                        viewState.showAssets(result)
                    }, {
                        it.printStackTrace()
                    }))
        }
    }

    fun saveSortedPositions(data: MutableList<AssetSortingItem>) {
        val list = data
                .mapTo(mutableListOf()) { it.asset }
                .filter { !it.isWaves() }
        list.forEachIndexed { index, assetBalance ->
            assetBalance.position = index
        }
        AssetBalanceDb.convertToDb(list).saveAll()
    }

}
