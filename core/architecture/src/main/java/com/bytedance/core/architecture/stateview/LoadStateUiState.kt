package com.bytedance.core.architecture.stateview

import android.view.View
import com.bytedance.core.common.exception.RuleException

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/12/8 下午5:22
 */
sealed class LoadStateUiState(
    // 是否展示内容布局（如：Error状态下，展示Error布局，不展示内容布局），为null代表不操作。
    val isShowContentLayout: Boolean?,
    // 是否展示StateView（如：Error状态下，不展示StateView-Error布局，展示提示）
    val isShowStateView: Boolean,
) {
    class Loading(
        isShowContentLayout: Boolean?,
        isShowStateView: Boolean,
        val isLoadingDialog: Boolean,
    ) : LoadStateUiState(isShowContentLayout, isShowStateView)

    class Error(
        isShowContentLayout: Boolean?,
        isShowStateView: Boolean,
        val error: Throwable,
        val retry: View.OnClickListener?,
    ) : LoadStateUiState(isShowContentLayout, isShowStateView)

    class Empty(
        isShowContentLayout: Boolean?,
        isShowStateView: Boolean,
    ) : LoadStateUiState(isShowContentLayout, isShowStateView)

    class Success(
        isShowContentLayout: Boolean?,
        isShowStateView: Boolean,
    ) : LoadStateUiState(isShowContentLayout, isShowStateView)
}

// 判断
fun LoadStateUiState?.isLoading() = this is LoadStateUiState.Loading
fun LoadStateUiState?.isError() = this is LoadStateUiState.Error
fun LoadStateUiState?.isErrorRule() = this is LoadStateUiState.Error && error.isRuleException()
fun LoadStateUiState?.isErrorNormal() = this is LoadStateUiState.Error && error.isNormalException()
fun LoadStateUiState?.isSuccess() = this is LoadStateUiState.Success

fun Throwable?.isRuleException() = this is RuleException
fun Throwable?.isNormalException() = this !is RuleException

// 获取异常
fun LoadStateUiState?.getErrorException() = if (this is LoadStateUiState.Error) error else null
fun LoadStateUiState?.getErrorExceptionRule() =
    if (this is LoadStateUiState.Error && error.isRuleException()) error else null

fun LoadStateUiState?.getErrorExceptionNormal() =
    if (this is LoadStateUiState.Error && error.isNormalException()) error else null

// 获取消息
fun LoadStateUiState?.getErrorMessage() = getErrorException()?.message
fun LoadStateUiState?.getErrorMessageRule() = getErrorExceptionRule()?.message
fun LoadStateUiState?.getErrorMessageNormal() = getErrorExceptionNormal()?.message