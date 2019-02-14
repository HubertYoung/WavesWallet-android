package com.wavesplatform.sdk.utils

import com.google.common.primitives.Bytes
import com.google.common.primitives.Shorts
import com.wavesplatform.sdk.Constants
import com.wavesplatform.sdk.model.response.*
import org.spongycastle.util.encoders.Hex
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.SecureRandom


fun String.isWaves(): Boolean {
    return this.toLowerCase() == Constants.wavesAssetInfo.name.toLowerCase()
}

fun getWavesDexFee(fee: Long): BigDecimal {
    return MoneyUtil.getScaledText(fee, Constants.wavesAssetInfo.precision).clearBalance().toBigDecimal()
}

fun String.isWavesId(): Boolean {
    return this.toLowerCase() == Constants.wavesAssetInfo.id
}

fun ByteArray.arrayWithSize(): ByteArray {
    return Bytes.concat(Shorts.toByteArray(size.toShort()), this)
}

fun String.clearBalance(): String {
    return this.stripZeros().replace(",", "")
}

fun Transaction.transactionType(): TransactionType {
    return TransactionType.getTypeById(this.transactionTypeId)
}

fun String.stripZeros(): String {
    if (this == "0.0") return this
    return if (!this.contains(".")) this else this.replace("0*$".toRegex(), "").replace("\\.$".toRegex(), "")
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

fun findMyOrder(first: Order, second: Order, address: String?): Order {
    return if (first.sender == second.sender) {
        if (first.timestamp > second.timestamp) {
            first
        } else {
            second
        }
    } else if (first.sender == address) {
        first
    } else if (second.sender == address) {
        second
    } else {
        if (first.timestamp > second.timestamp) {
            first
        } else {
            second
        }
    }
}

fun ErrorResponse.isSmartError(): Boolean {
    return this.error in 305..307
}

fun AssetInfo.getTicker(): String {

    if (this.id.isWavesId()) {
        return Constants.wavesAssetInfo.name
    }

    return this.ticker ?: this.name
}

fun getScaledAmount(amount: Long, decimals: Int): String {
    val absAmount = Math.abs(amount)
    val value = BigDecimal.valueOf(absAmount, decimals)
    if (amount == 0L) {
        return "0"
    }

    val sign = if (amount < 0) "-" else ""

    return sign + when {
        value >= MoneyUtil.ONE_M -> value.divide(MoneyUtil.ONE_M, 1, RoundingMode.HALF_EVEN)
                .toPlainString().stripZeros() + "M"
        value >= MoneyUtil.ONE_K -> value.divide(MoneyUtil.ONE_K, 1, RoundingMode.HALF_EVEN)
                .toPlainString().stripZeros() + "k"
        else -> MoneyUtil.createFormatter(decimals).format(BigDecimal.valueOf(absAmount, decimals))
                .stripZeros() + ""
    }
}

fun randomString(): String {
    val bytes = ByteArray(16)
    val random = SecureRandom()
    random.nextBytes(bytes)
    return String(Hex.encode(bytes), charset("UTF-8"))
}

fun isShowTicker(assetId: String?): Boolean {
    return Constants.defaultAssets.any {
        it.assetId == assetId || assetId.isNullOrEmpty()
    }
}