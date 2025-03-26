package com.bytedance.douyin.feature.login.ui.login.verificationcode

import androidx.lifecycle.SavedStateHandle
import com.bytedance.core.architecture.stateview.isSuccess
import com.bytedance.douyin.core.data.repository.interfaces.LoginRepository
import com.bytedance.douyin.feature.login.ui.base.BaseLoginUiState
import com.bytedance.douyin.feature.login.ui.base.BaseLoginViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import com.bytedance.douyin.feature.login.ui.login.verificationcode.LoginByPhoneNumberAndVerificationCodeUiState as UiState

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/7/3 上午11:15
 */
@HiltViewModel
class LoginByPhoneNumberAndVerificationCodeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loginRepository: LoginRepository,
) : BaseLoginViewModel<UiState>(savedStateHandle) {

    // UiState初始化值，需要给UiState内的isAgreementChecked、phoneNumber赋值，否则由于进程被杀死导致ViewModel被重新创建，如果没在savedStateHandle里取值，导致UiState初始化值的之前的状态会丢失。
    override val uiStateInitialValue: UiState =
        UiState(isAgreementChecked = isAgreementChecked.value, phoneNumber = phoneNumber.value)

    // UiState的Flow
    override val uiStateFlow: Flow<UiState> = combine(
        isAgreementChecked, phoneNumber, loadStateUiState
    ) { isAgreementChecked, phoneNumber, loadStateUiState ->
        // 转UiState
        UiState(
            // base
            baseLoginUiState = createBaseLoginUiState(
                isAgreementChecked && isPhoneNumberValid(),
                loadStateUiState
            ),
            // other
            isAgreementChecked = isAgreementChecked,
            phoneNumber = phoneNumber,
            isVerificationSucceed = loadStateUiState.isSuccess()
        )
    }

    fun verificationAndLogin(requestStart: () -> Unit) {
        if (checkPhoneNumberValid() && checkAgreementChecked()) {
            // 检查成功
            requestStart()
            requestAsync {
                // 发送验证码
                loginRepository.sendLoginPhoneNumberVerificationCode(phoneNumber.value.toString())
            }
        }
    }
}

data class LoginByPhoneNumberAndVerificationCodeUiState(
    val baseLoginUiState: BaseLoginUiState = BaseLoginUiState(),
    val isAgreementChecked: Boolean = false,
    val phoneNumber: String? = null,
    val isVerificationSucceed: Boolean = false,
)