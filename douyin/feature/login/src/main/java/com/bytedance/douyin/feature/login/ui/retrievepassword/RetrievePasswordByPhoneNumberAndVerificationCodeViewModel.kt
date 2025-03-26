package com.bytedance.douyin.feature.login.ui.retrievepassword

import androidx.lifecycle.SavedStateHandle
import com.bytedance.core.architecture.stateview.isSuccess
import com.bytedance.douyin.core.data.repository.interfaces.LoginRepository
import com.bytedance.douyin.feature.login.ui.base.BaseLoginUiState
import com.bytedance.douyin.feature.login.ui.base.BaseLoginViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import com.bytedance.douyin.feature.login.ui.retrievepassword.RetrievePasswordByPhoneNumberAndVerificationCodeUiState as UiState

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/9/2 下午3:13
 */
@HiltViewModel
class RetrievePasswordByPhoneNumberAndVerificationCodeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loginRepository: LoginRepository,
) : BaseLoginViewModel<UiState>(savedStateHandle) {

    // UiState初始化值
    override val uiStateInitialValue: UiState =
        UiState(phoneNumber = argsPhoneNumber, verificationCode = verificationCode.value)

    // UiState的Flow
    override val uiStateFlow: Flow<UiState> = combine(
        isAgreementChecked,
        verificationCode,
        loadStateUiState
    ) { isAgreementChecked, verificationCode, loadStateUiState ->
        // 转UiState
        UiState(
            // base
            baseLoginUiState = createBaseLoginUiState(
                isAgreementChecked && isVerificationCodeValid(),
                loadStateUiState
            ),
            // other
            phoneNumber = argsPhoneNumber,
            verificationCode = verificationCode,
            isAgreementChecked = isAgreementChecked,
            isCompleted = loadStateUiState.isSuccess()
        )
    }

    // 完成
    fun complete(requestStart: () -> Unit) {
        if (checkVerificationCodeValid() && checkAgreementChecked()) {
            // 检查成功，请求异步获取数据。
            requestStart()
            requestAsync {
                // 找回密码
                loginRepository.verifyRetrievePasswordByPhoneNumberAndVerificationCode(
                    argsPhoneNumber.toString(),
                    verificationCode.value.toString()
                )
            }
        }
    }
}

data class RetrievePasswordByPhoneNumberAndVerificationCodeUiState(
    val baseLoginUiState: BaseLoginUiState = BaseLoginUiState(),
    val phoneNumber: String? = null,
    val verificationCode: String? = null,
    val isAgreementChecked: Boolean = false,
    val isCompleted: Boolean = false,
)