package com.wavesplatform.wallet.v2.ui.home.profile.network

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.EqualRule
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import io.github.anderscheow.validator.rules.common.NotEqualRule
import kotlinx.android.synthetic.main.activity_network.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.app
import pers.victor.ext.click
import javax.inject.Inject


class NetworkActivity : BaseActivity(), NetworkView {

    @Inject
    @InjectPresenter
    lateinit var presenter: NetworkPresenter

    var validator = Validator.with(app).setMode(Mode.CONTINUOUS)

    @ProvidePresenter
    fun providePresenter(): NetworkPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_network


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.network_toolbar_title), R.drawable.ic_toolbar_back_black)

        edit_spam_filter.setText(prefsUtil.getValue(PrefsUtil.KEY_SPAM_URL, Constants.URL_SPAM))

        switch_spam_filter.isChecked = prefsUtil.getValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, false)
        switch_spam_filter.setOnCheckedChangeListener { buttonView, isChecked ->
            presenter.spamFilterEnableValid = prefsUtil.getValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, false) != isChecked
            isFieldsValid()
        }

        edit_spam_filter.addTextChangedListener {
            on { s, start, before, count ->
                val spamUrlValidation = Validation(til_spam_filter)
                        .and(NotEmptyRule(R.string.new_account_account_name_validation_required_error))
                        .and(NotEqualRule((prefsUtil.getValue(PrefsUtil.KEY_SPAM_URL, Constants.URL_SPAM)), " "))
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                presenter.spamUrlFieldValid = true
                                isFieldsValid()
                            }

                            override fun onValidateFailed() {
                                presenter.spamUrlFieldValid = false
                                isFieldsValid()
                            }
                        }, spamUrlValidation
                        )
            }
        }

        button_set_default.click {
            edit_spam_filter.setText(Constants.URL_SPAM)
            switch_spam_filter.isChecked = false
        }

        button_save.click {
            if (presenter.spamFilterEnableValid) {
                mRxEventBus.post(Events.SpamFilterStateChanged())
            }

            prefsUtil.setValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, switch_spam_filter.isChecked)
            prefsUtil.setValue(PrefsUtil.KEY_SPAM_URL, edit_spam_filter.text.toString())
            finish()
        }
    }

    private fun isFieldsValid() {
        button_save.isEnabled = presenter.isAllFieldsValid()
    }

}
