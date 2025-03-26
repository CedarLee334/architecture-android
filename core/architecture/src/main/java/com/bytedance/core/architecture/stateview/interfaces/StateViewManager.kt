package com.bytedance.core.architecture.stateview.interfaces

import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import com.bytedance.core.designsystem.widget.interfaces.LoadingDialog
import com.bytedance.core.designsystem.widget.interfaces.StateView

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/7/22 上午10:36
 */
interface StateViewManager {
    val stateView: Lazy<StateView>
    val loadingDialog: Lazy<LoadingDialog>

    // ==========================初始化相关===========================
    @Suppress("EmptyMethod")
    fun initStateView(stateView: StateView)
    fun initLoadingDialog(loadingDialog: LoadingDialog)

    // StateView要替换的View
    // 1、用于实现显示StateView时，隐藏此View。
    // 2、用于获取StateView要添加的父布局（DefaultStateViewManager用）。
    fun getStateViewReplaceView(): View

    // ======================创建StateView相关=======================
    fun getStateViewParent(): ViewGroup // StateView的显示区域
    fun createStateView(stateViewParent: ViewGroup): StateView // StateView的样式
    fun createStateViewLayoutParams(): LayoutParams? // 可选，StateView位置的调整

    // ===========================显示相关===========================
    fun showLoading(isLoadingDialog: Boolean)
    fun showErrorView(error: Throwable, retry: View.OnClickListener?)
    fun showEmptyView()
    fun showSuccess()
    fun hideLoading()
    fun hideAll()
}