package com.bytedance.douyin.feature.login.ui.retrievepassword

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.bytedance.douyin.feature.login.ui.base.BaseLoginViewsFragment
import dagger.hilt.android.AndroidEntryPoint
import com.bytedance.douyin.feature.login.databinding.DouyinFeatureLoginFragmentRetrievePasswordByPhoneNumberAndVerificationCodeBinding as ViewBinding
import com.bytedance.douyin.feature.login.ui.retrievepassword.RetrievePasswordByPhoneNumberAndVerificationCodeFragmentDirections as Directions
import com.bytedance.douyin.feature.login.ui.retrievepassword.RetrievePasswordByPhoneNumberAndVerificationCodeUiState as UiState
import com.bytedance.douyin.feature.login.ui.retrievepassword.RetrievePasswordByPhoneNumberAndVerificationCodeViewModel as ViewModel

/**
 * 描述：找回密码-手机号+验证码-验证码（手机号由上个页面传入）
 *
 * @author zhangrq
 * createTime 2024/4/9 下午4:03
 */
@AndroidEntryPoint
class RetrievePasswordByPhoneNumberAndVerificationCodeFragment :
    BaseLoginViewsFragment<ViewBinding, UiState, ViewModel>() {
    override val viewModel: ViewModel by viewModels()

    override fun inflateViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        ViewBinding.inflate(inflater, container, false)

    override fun ViewBinding.initViews() {
        // 协议
        initAgreement(agreement, false)
    }

    override fun ViewBinding.initListeners() {
        // 验证码
        verificationCode.doAfterTextChanged { viewModel.verificationCodeChanged(it.toString()) }
        // 协议
        agreement.setOnCheckedChangeListener { _, isChecked ->
            viewModel.agreementIsCheckedChanged(isChecked)
        }
        // 完成
        complete.setOnClickListener {
            viewModel.complete { uiStateNavigateInProgress = true }
        }
    }

    override fun ViewBinding.initObservers() {
        // 初始化验证码已发送
        initVerificationCodeSent(subtitle) { it.phoneNumber }
    }

    override fun ViewBinding.onUiStateCollect(uiState: UiState) {
        // 处理通用的
        handleBaseLoginUiState(uiState.baseLoginUiState, error, complete)
        // 验证码（view层原生组件可省略赋值）
        verificationCode.setTextKeepState(uiState.verificationCode ?: "")
        // 协议（view层原生组件可省略赋值）
        agreement.isChecked = uiState.isAgreementChecked

        // 成功
        if (uiState.isCompleted && uiStateNavigateInProgress) {
            uiStateNavigateInProgress = false
            // 下个页面
            navigate(Directions.actionToNewPassword(uiState.phoneNumber ?: ""))
        }
    }
}