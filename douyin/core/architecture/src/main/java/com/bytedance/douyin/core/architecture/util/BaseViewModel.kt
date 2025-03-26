package com.bytedance.douyin.core.architecture.util

import android.view.View
import androidx.lifecycle.viewModelScope
import com.bytedance.core.architecture.base.BaseViewModel
import com.bytedance.core.architecture.stateview.LoadStateUiState
import com.bytedance.douyin.core.designsystem.util.toErrorMessage
import kotlinx.coroutines.launch

/**
 * 描述：App内所有的请求StateView样式
 *
 * @author zhangrq
 * createTime 2024/12/18 10:17
 */
/**
 * 请求异步-通用
 */
private fun <Data> BaseViewModel<*>.requestAsyncBase(
    // 默认值，用于统一修改下面状态的默认值。
    isShowContentLayoutAllStateDefault: Boolean?,
    isShowStateViewAllStateDefault: Boolean,
    // loading
    isShowContentLayoutLoading: Boolean? = isShowContentLayoutAllStateDefault,
    isShowStateViewLoading: Boolean = isShowStateViewAllStateDefault,
    isStateViewLoadingDialog: Boolean = false,
    loadingCallback: (LoadStateUiState.Loading.() -> Unit)? = null,
    // error
    isShowContentLayoutError: Boolean? = isShowContentLayoutAllStateDefault,
    isShowStateViewError: Boolean = isShowStateViewAllStateDefault,
    errorCallback: (LoadStateUiState.Error.() -> Unit)? = null,
    // empty
    isShowContentLayoutEmpty: Boolean? = isShowContentLayoutAllStateDefault,
    isSuccessEmpty: Data.() -> Boolean = { false }, // 判断Success下什么情况下为空，默认不支持为空。
    isShowStateViewEmpty: Boolean = isShowStateViewAllStateDefault,
    emptyCallback: (LoadStateUiState.Empty.() -> Unit)? = null,
    // success
    successCallback: (LoadStateUiState.Success.() -> Unit)? = null,

    // asyncFun
    asyncFun: suspend () -> Data,
) {
    viewModelScope.launch {
        // Loading
        val loading =
            LoadStateUiState.Loading(
                isShowContentLayoutLoading,
                isShowStateViewLoading,
                isStateViewLoadingDialog
            )
        loadStateUiState.value = loading
        loadingCallback?.invoke(loading)
        try {
            val data = asyncFun()
            if (isSuccessEmpty(data)) {
                // Empty
                val empty = LoadStateUiState.Empty(isShowContentLayoutEmpty, isShowStateViewEmpty)
                loadStateUiState.value = empty
                emptyCallback?.invoke(empty)
            } else {
                // Success
                // 加载成功，一定要显示内容布局、显示StateView-Success，防止其它操控loadStateUiState的设置了隐藏内容布局。
                val success =
                    LoadStateUiState.Success(isShowContentLayout = true, isShowStateView = true)
                loadStateUiState.value = success
                successCallback?.invoke(success)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Error
            // 重试
            val retry: (v: View) -> Unit = {
                requestAsyncBase(
                    isShowContentLayoutAllStateDefault,
                    isShowStateViewAllStateDefault,
                    // loading
                    isShowContentLayoutLoading,
                    isShowStateViewLoading,
                    isStateViewLoadingDialog,
                    loadingCallback,
                    // error
                    isShowContentLayoutError,
                    isShowStateViewError,
                    errorCallback,
                    // empty
                    isShowContentLayoutEmpty,
                    isSuccessEmpty,
                    isShowStateViewEmpty,
                    emptyCallback,
                    // success
                    successCallback,
                    // asyncFun
                    asyncFun
                )
            }
            val error =
                LoadStateUiState.Error(isShowContentLayoutError, isShowStateViewError, e, retry)
            loadStateUiState.value = error
            errorCallback?.invoke(error)
        }
    }
}

/**
 * 请求异步-单网络-仅提示。即：内容布局全部显示，StateView仅展示加载中，加载失败默认改为展示消息提示。
 */
fun <Data> BaseViewModel<*>.requestAsyncSingleOnlyHint(
    errorCallback: (LoadStateUiState.Error.() -> Unit)? = null,
    asyncFun: suspend () -> Data,
) {
    if (loadStateUiState.value is LoadStateUiState.Loading) {
        // 网络请求中，直接返回。
        return
    }
    // 效果说明：不隐藏内容，仅展示加载中，加载失败改为展示消息提示。
    requestAsyncBase(
        // 所有状态默认显示内容布局
        isShowContentLayoutAllStateDefault = true,
        // 所有状态默认不显示StateView（Loading除外，下面设置）
        isShowStateViewAllStateDefault = false,
        // 设置显示StateView-Loading
        isShowStateViewLoading = true,
        // 设置显示StateView-Error，改为定制（先用参数的，然后再用自己的消息提示）
        errorCallback = errorCallback ?: { showMessage(error.toErrorMessage()) },
        // 请求
        asyncFun = asyncFun
    )
}

/**
 * 请求异步-单网络-展示所有状态。即：StateView全部显示，内容布局仅加载中、成功显示。
 */
fun <Data> BaseViewModel<*>.requestAsyncSingleShowAllState(
    asyncFun: suspend () -> Data,
) {
    if (loadStateUiState.value is LoadStateUiState.Loading) {
        // 网络请求中，直接返回。
        return
    }
    requestAsyncBase(
        // 所有状态默认不显示内容布局（Loading除外，下面设置）
        isShowContentLayoutAllStateDefault = false,
        // 所有状态默认显示StateView
        isShowStateViewAllStateDefault = true,
        // Loading，不操作内容（防止设置为true后，强制显示内容布局，导致显示闪烁）。
        isShowContentLayoutLoading = null,
        // 请求
        asyncFun = asyncFun
    )
}