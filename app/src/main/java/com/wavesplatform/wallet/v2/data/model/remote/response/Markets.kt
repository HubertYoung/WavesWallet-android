package com.wavesplatform.wallet.v2.data.model.remote.response

import java.util.*

data class Markets(
        var matcherPublicKey: String? = null,
        var markets: ArrayList<Market> = ArrayList<Market>()
)
