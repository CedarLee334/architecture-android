package com.bytedance.core.architecture.stateview.interfaces

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.bytedance.core.architecture.stateview.util.addStateView
import com.bytedance.core.designsystem.widget.DefaultLoadingDialog
import com.bytedance.core.designsystem.widget.interfaces.LoadingDialog
import com.bytedance.core.designsystem.widget.interfaces.StateView

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/7/22 上午10:36
 */
interface DefaultStateViewManager : StateViewManager {
    // ==========================初始化相关===========================
    override fun initStateView(stateView: StateView) {}
    override fun initLoadingDialog(loadingDialog: LoadingDialog) {}

    // ======================创建StateView相关=======================
    // -说明：因为所有操作StateView的都是在initStateUiStateCollect内操作，即在Lifecycle.State.STARTED时执行的，
    // --所以创建StateView相关的方法都是在onStart时执行的，
    // --所以getStateViewReplaceView()即使为Fragment根布局，它也已经被添加，所以他的parent一定不为空且为ViewGroup。
    override fun getStateViewParent(): ViewGroup = getStateViewReplaceView().parent as ViewGroup
    override fun createStateView(stateViewParent: ViewGroup): StateView
    override fun createStateViewLayoutParams(): LayoutParams? {
        val stateViewReplaceView = getStateViewReplaceView()
        return if (getStateViewParent() is ConstraintLayout) {
            // 约束布局，自定义，避免stateViewReplaceView控件显示被影响（替换的布局宽高不够，会撑开）。
            val layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            )

            val stateViewReplaceViewId = stateViewReplaceView.id
            layoutParams.topToTop = stateViewReplaceViewId
            layoutParams.bottomToBottom = stateViewReplaceViewId
            layoutParams.startToStart = stateViewReplaceViewId
            layoutParams.endToEnd = stateViewReplaceViewId
            layoutParams
        } else {
            // 其它，使用要替换的View。
            stateViewReplaceView.layoutParams
        }
    }

    override fun showLoading(isLoadingDialog: Boolean) {
        // 隐藏原来的Loading
        hideLoading()
        // 显示新的-Loading
        if (isLoadingDialog) {
            loadingDialog.value.showLoading()
        } else {
            setStateViewVisible(true)
            stateView.value.showLoading()
        }
    }

    override fun showErrorView(error: Throwable, retry: View.OnClickListener?) {
        // 隐藏原来的Loading
        hideLoading()
        // 显示新的-Error
        setStateViewVisible(true)
        stateView.value.showError(error, retry)
    }

    override fun showEmptyView() {
        // 隐藏原来的Loading
        hideLoading()
        // 显示新的-Empty
        setStateViewVisible(true)
        stateView.value.showEmpty()
    }

    override fun showSuccess() {
        // 结束，隐藏Loading。
        hideLoading()
    }

    /**
     * 隐藏Loading（LoadingView、LoadingDialog）。
     */
    override fun hideLoading() {
        // 隐藏LoadingView（如果之前用过）
        if (stateView.isInitialized()) {
            setStateViewVisible(false)
        }

        // 隐藏loadingDialog（如果之前用过）
        if (loadingDialog.isInitialized()) {
            loadingDialog.value.hideLoading()
        }
    }

    /**
     * 隐藏所有（StateView、LoadingDialog）。
     */
    override fun hideAll() {
        // 隐藏StateView（如果之前用过）
        if (stateView.isInitialized()) {
            setStateViewVisible(false)
        }

        // 隐藏loadingDialog（如果之前用过）
        if (loadingDialog.isInitialized()) {
            loadingDialog.value.hideLoading()
        }
    }

    fun createStateViewImpl(): StateView {
        // StateView-Parent
        val stateViewParent = getStateViewParent()
        // StateView-创建
        val createStateView = createStateView(stateViewParent)
        // StateView-初始化
        initStateView(createStateView)
        // StateView-添加
        val layoutParams = createStateViewLayoutParams()
        return stateViewParent.addStateView(createStateView, layoutParams)
    }

    fun createLoadingDialogImpl(activity: Activity): LoadingDialog {
        return DefaultLoadingDialog.create(activity).apply { initLoadingDialog(this) }
    }

    private fun setStateViewVisible(isVisible: Boolean) {
        stateView.value.root.isVisible = isVisible
    }
}