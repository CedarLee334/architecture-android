package com.bytedance.douyin.feature.login.ui.login.verificationcode

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.bytedance.douyin.core.designsystem.util.NoUnderlineClickableSpan
import com.bytedance.douyin.core.webview.WebViewRouter
import com.bytedance.douyin.feature.login.R
import com.bytedance.douyin.feature.login.ui.base.BaseLoginViewsFragment
import com.zrq.spanbuilder.Spans
import dagger.hilt.android.AndroidEntryPoint
import com.bytedance.douyin.feature.login.databinding.DouyinFeatureLoginFragmentLoginByPhoneNumberAndVerificationCodeBinding as ViewBinding
import com.bytedance.douyin.feature.login.ui.login.verificationcode.LoginByPhoneNumberAndVerificationCodeFragmentDirections as Directions
import com.bytedance.douyin.feature.login.ui.login.verificationcode.LoginByPhoneNumberAndVerificationCodeUiState as UiState
import com.bytedance.douyin.feature.login.ui.login.verificationcode.LoginByPhoneNumberAndVerificationCodeViewModel as ViewModel


/**
 * 描述：登录-手机号+验证码
 *
 * @author zhangrq
 * createTime 2024/4/9 下午4:03
 */
@AndroidEntryPoint
class LoginByPhoneNumberAndVerificationCodeFragment :
    BaseLoginViewsFragment<ViewBinding, UiState, ViewModel>() {
    override val viewModel: ViewModel by viewModels()

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ViewBinding.inflate(inflater, container, false)

    override fun ViewBinding.initViews() {
        // 协议
        initAgreement(agreement, true)
        // 找回帐号
        retrieveAccount.text = Spans.builder()
            .text(resources.getText(R.string.douyin_feature_login_retrieve_account_1))
            .text(resources.getText(R.string.douyin_feature_login_retrieve_account_2))
            .click(retrieveAccount, NoUnderlineClickableSpan {
                WebViewRouter.startRetrieveAccount(
                    requireActivity(),
                    R.string.douyin_feature_login_retrieve_account_2
                )
            }).build()
    }

    override fun ViewBinding.initListeners() {
        // 帮助与设置
        titleBar.setRightClick { startHelpAndSetup() }
        // 协议
        agreement.setOnCheckedChangeListener { _, isChecked ->
            viewModel.agreementIsCheckedChanged(isChecked)
        }
        // 手机号
        phoneNumber.doAfterTextChanged { viewModel.phoneNumberChanged(it.toString()) }
        // 验证并登录
        verificationAndLogin.setOnClickListener {
            viewModel.verificationAndLogin { uiStateNavigateInProgress = true }
        }
        // 密码登录
        loginPassword.setOnClickListener { navigate(Directions.actionToLoginByPhoneNumberAndPassword()) }
        // 其他方式登录
        loginOther.setOnClickListener { viewModel.showMessage(R.string.douyin_feature_login_login_other) }
    }

    override fun ViewBinding.initObservers() {
    }

    override fun ViewBinding.onUiStateCollect(uiState: UiState) {
        // 处理通用的
        handleBaseLoginUiState(uiState.baseLoginUiState, error, verificationAndLogin)
        // 协议（view层原生组件可省略赋值）
        agreement.isChecked = uiState.isAgreementChecked
        // 手机号（view层原生组件可省略赋值）
        phoneNumber.setTextKeepState(uiState.phoneNumber ?: "")

        // 成功
        if (uiState.isVerificationSucceed && uiStateNavigateInProgress) {
            uiStateNavigateInProgress = false
            // 下个页面
            navigate(Directions.actionToNext(phoneNumber.text.toString()))
        }
    }
}