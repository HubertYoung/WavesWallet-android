package com.wavesplatform.sdk.service

import com.wavesplatform.sdk.Constants
import com.wavesplatform.sdk.model.request.*
import com.wavesplatform.sdk.model.response.*
import io.reactivex.Observable
import okhttp3.OkHttpClient
import ren.yale.android.retrofitcachelibrx2.RetrofitCache
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NodeService {

    @GET("assets/balance/{address}")
    fun assetsBalance(@Path("address") address: String?): Observable<AssetBalances>

    @GET("addresses/balance/{address}")
    fun wavesBalance(@Path("address") address: String?): Observable<WavesBalance>

    @GET("transactions/address/{address}/limit/{limit}")
    fun transactionList(@Path("address") address: String?,
                        @Path("limit") limit: Int): Observable<List<List<Transaction>>>

    @GET("assets/balance/{address}/{assetId}")
    fun addressAssetBalance(@Path("address") address: String?,
                            @Path("assetId") assetId: String?): Observable<AddressAssetBalance>

    @GET("transactions/info/{asset}")
    fun getTransactionsInfo(@Path("asset") asset: String): Observable<TransactionsInfo>

    @POST("assets/broadcast/transfer")
    fun broadcastTransfer(@Body tx: TransferTransactionRequest): Observable<TransferTransactionRequest>

    @POST("assets/broadcast/issue")
    fun broadcastIssue(@Body tx: IssueTransactionRequest): Observable<IssueTransactionRequest>

    @POST("assets/broadcast/reissue")
    fun broadcastReissue(@Body tx: ReissueTransactionRequest): Observable<ReissueTransactionRequest>

    @POST("transactions/broadcast")
    fun createAlias(@Body createAliasRequest: AliasRequest): Observable<Alias>

    @GET("transactions/unconfirmed")
    fun unconfirmedTransactions(): Observable<List<Transaction>>

    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body tx: TransactionsBroadcastRequest): Observable<TransactionsBroadcastRequest>

    @GET("blocks/height")
    fun currentBlocksHeight(): Observable<Height>

    @GET("leasing/active/{address}")
    fun activeLeasing(@Path("address") address: String?): Observable<List<Transaction>>

    @POST("transactions/broadcast")
    fun createLeasing(@Body createLeasingRequest: CreateLeasingRequest): Observable<Transaction>

    @POST("transactions/broadcast")
    fun cancelLeasing(@Body cancelLeasingRequest: CancelLeasingRequest): Observable<Transaction>

    @POST("transactions/broadcast")
    fun burn(@Body burnRequest: BurnRequest): Observable<BurnRequest>

    @GET("addresses/scriptInfo/{address}")
    fun scriptAddressInfo(@Path("address") address: String): Observable<ScriptInfo>

    @GET("/assets/details/{assetId}")
    fun assetDetails(@Path("assetId") assetId: String): Observable<AssetsDetails>
}
