package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.support.annotation.NonNull
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.AppCompatImageView
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import com.sdsmdg.harjot.vectormaster.VectorMasterDrawable
import com.wavesplatform.sdk.model.response.AssetBalance
import com.wavesplatform.sdk.model.response.AssetInfo
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import pers.victor.ext.findColor
import pers.victor.ext.resize
import pers.victor.ext.sp
import pyxis.uzuki.live.richutilskt.utils.drawableToBitmap


class AssetAvatarView : AppCompatImageView {

    /*
    * Place holder drawable (with background color and initials)
    * */
    internal lateinit var drawable: Drawable

    /*
    * Contains initials of the asset
    * */
    internal var text: String? = null

    /*
    * Used to set size and color of the asset initials
    * */
    internal lateinit var textPaint: TextPaint

    /*
    * Used as background of the initials with asset specific color
    * */
    internal lateinit var paint: Paint

    /*
    * Text size for asset letter
    * */
    var textSize: Float = 0f

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs)
    }

    /*
    * Initialize fields
    * */
    protected fun init(attrs: AttributeSet? = null) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.AssetAvatarView, 0, 0)

        textSize = attr.getDimensionPixelSize(R.styleable.AssetAvatarView_avatar_text_size, 24.sp).toFloat()

        paint = Paint(Paint.ANTI_ALIAS_FLAG)

        textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        textPaint.textSize = textSize
        textPaint.color = Color.WHITE
        textPaint.typeface = try {
            ResourcesCompat.getFont(context, R.font.roboto)
        } catch (e: Exception) {
            Typeface.DEFAULT
        }

        attr.recycle()
    }

    /*
    * Set asset object to get initials for drawable
    * */
    fun setAsset(asset: AssetBalance?) {
        val avatar = Constants.defaultAssetsAvatar[asset?.assetId]
        if (avatar != null) {
            setImageResource(avatar)
        } else {
            setValues(asset?.getName() ?: "", asset?.isSponsored() == true)
        }
    }

    /*
   * Set asset info object to get initials for drawable
   * */
    fun setAssetInfo(asset: AssetInfo) {
        val avatar = Constants.defaultAssetsAvatar[asset.id]
        if (avatar != null) {
            setImageResource(avatar)
        } else {
            setValues(asset.name, false) // TODO: check if need to show sponsor asset icon here
        }
    }

    /*
    * Set asset info object to get initials for drawable
    * */
    private fun getInitialsFromAssetName(text: String?): String {
        if (text.isNullOrEmpty()) {
            return context.getString(R.string.common_persist)
        }

        val letter = text.trim().substring(0, 1)
        val letterColor = Constants.alphabetColor[letter.toLowerCase()]

        return if (letterColor != null) {
            letter.toUpperCase()
        } else {
            context.getString(R.string.common_persist)
        }
    }

    private fun getPlaceholderColorFromAssetName(text: String?): Int {
        if (TextUtils.isEmpty(text)) {
            findColor(R.color.persist)
        }
        val letterColor = Constants.alphabetColor[text!!.trim().substring(0, 1).toLowerCase()]

        return if (letterColor != null) {
            findColor(letterColor)
        } else {
            findColor(R.color.persist)
        }
    }

    /*
    * Setup view with values
    * */
    private fun setValues(name: String, isSponsoredAsset: Boolean) {
        val color = getPlaceholderColorFromAssetName(name)
        paint.color = color
        text = getInitialsFromAssetName(name)
        setDrawable(isSponsoredAsset)
    }


    /*
    * Create placeholder drawable
    * */
    private fun setDrawable(sponsoredAsset: Boolean) {
        drawable = object : Drawable() {
            override fun draw(@NonNull canvas: Canvas) {

                val centerX = Math.round(canvas.width * 0.5f)
                val centerY = Math.round(canvas.height * 0.5f)
                val sponsorIconSize = canvas.width.toFloat() * SPONSOR_ICON_SCALE_FACTOR

                if (text != null) {
                    val textWidth = textPaint.measureText(text) * 0.5f
                    val textBaseLineHeight = textPaint.fontMetrics.ascent * -0.4f

                    /*
                    * Draw the background color before drawing initials text
                    * */
                    canvas.drawCircle(centerX.toFloat(),
                            centerY.toFloat(),
                            Math.max((canvas.height / 2).toFloat(), textWidth / 2),
                            paint)
                    /*
                    * Draw the text above the background color
                    * */
                    canvas.drawText(text!!, centerX - textWidth, centerY + textBaseLineHeight, textPaint)


                    /*
                    * Draw sponsor icon
                    * */
                    if (sponsoredAsset) {
                        val vectorMasterDrawable = VectorMasterDrawable(context, R.drawable.ic_sponsoritem_18_color)
                        val pathModel = vectorMasterDrawable.getPathModelByName("background")
                        pathModel.fillColor = paint.color

                        canvas.drawBitmap(drawableToBitmap(vectorMasterDrawable).resize(sponsorIconSize, sponsorIconSize),
                                (canvas.width - sponsorIconSize).toFloat(),
                                (canvas.width - sponsorIconSize).toFloat(),
                                paint)
                    }

                }
            }

            override fun setAlpha(alpha: Int) {

            }

            override fun setColorFilter(colorFilter: ColorFilter?) {

            }

            override fun getOpacity(): Int {
                return PixelFormat.UNKNOWN
            }
        }

        setImageDrawable(drawable)
        invalidate()
    }

    /*
    * Set initials text size of placeholder
    * */
    fun setTextSizeValue(value: Float) {
        textPaint.textSize = value
    }

    companion object {
        const val SPONSOR_ICON_SCALE_FACTOR = 0.375
    }
}