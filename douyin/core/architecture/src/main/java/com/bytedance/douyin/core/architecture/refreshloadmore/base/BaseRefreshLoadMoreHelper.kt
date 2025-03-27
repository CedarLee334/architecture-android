package com.bytedance.douyin.core.architecture.refreshloadmore.base

import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.core.architecture.message.MessageDisplayManager
import com.bytedance.core.designsystem.widget.interfaces.StateView
import com.bytedance.douyin.core.architecture.refreshloadmore.FixBugHeaderAdapter
import com.bytedance.douyin.core.architecture.refreshloadmore.MyDefaultTrailingLoadStateAdapter
import com.bytedance.douyin.core.data.repository.refreshloadmore.LoadState
import com.bytedance.douyin.core.data.repository.refreshloadmore.interfaces.OnRepositoryLoadMoreListener
import com.bytedance.douyin.core.data.repository.refreshloadmore.interfaces.RefreshRepository
import com.bytedance.douyin.core.data.repository.refreshloadmore.interfaces.RefreshRepositoryOwner
import com.bytedance.douyin.core.designsystem.util.createAppListStateView
import com.bytedance.douyin.core.designsystem.util.toErrorMessage
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.QuickAdapterHelper
import com.chad.library.adapter4.loadState.trailing.TrailingLoadStateAdapter
import kotlinx.coroutines.launch
import com.chad.library.adapter4.loadState.LoadState as AdapterLoadState

/**
 * 描述：定制的刷新加载逻辑，支持刷新（通过控件实现）、StateView、加载（通过连接Adapter实现）。
 *
 * @author zhangrq
 * createTime 2024/9/25 上午10:00
 */
abstract class BaseRefreshLoadMoreHelper<RefreshLayout>(
    private val viewModel: ViewModel,
    private val recyclerView: RecyclerView,
    private val adapter: BaseQuickAdapter<*, *>,
    private val createStateView: ((ViewGroup) -> StateView)? = null,
) {
    private val stateView: StateView by lazy {
        createStateView?.invoke(recyclerView) ?: createAppListStateView(recyclerView)
    }

    /**
     * [lifecycleOwner]，用于观察LoadState。
     * [refreshLayout]，刷新控件。不为null代表支持刷新功能，为null代表不支持刷新功能。
     * [enableLoadMore]，是否支持加载更多功能，默认为true。
     */
    fun init(
        lifecycleOwner: LifecycleOwner,
        refreshLayout: RefreshLayout? = null,
        enableLoadMore: Boolean = true,
    ) {
        // 初始化刷新
        // 说明：刷新、加载，全部是通过该LoadState控制。此设置初始化值即可。
        refreshLayout?.apply {
            // 刷新控件，不为null代表支持刷新功能，设置支持刷新。
            initViewRefreshEnable { refresh() }
        }

        // 初始化加载
        val helper = initEnableLoadMore(enableLoadMore, refreshLayout)

        // 初始化adapter
        adapter.isStateViewEnable = true

        // 初始化收集LoadState
        initObserverLoadState(lifecycleOwner, adapter, refreshLayout, helper)
    }

    // 初始化，支持刷新。
    abstract fun RefreshLayout.initViewRefreshEnable(refresh: () -> Unit)

    // 是否在刷新中
    abstract fun RefreshLayout.isViewRefreshing(): Boolean

    // 设置刷新状态（首部）
    abstract fun RefreshLayout.setViewRefreshState(
        loadState: LoadState?,
        enableRefresh: Boolean,
    )

    private fun initEnableLoadMore(enableLoadMore: Boolean, refreshLayout: RefreshLayout?) =
        if (enableLoadMore) {
            // 支持加载更多功能
            val helper = QuickAdapterHelper.Builder(adapter).setTrailingLoadStateAdapter(
                MyDefaultTrailingLoadStateAdapter().setOnLoadMoreListener(object :
                    TrailingLoadStateAdapter.OnTrailingListener {
                    override fun onLoad() {
                        load()
                    }

                    override fun onFailRetry() {
                        loadRetry()
                    }

                    override fun isAllowLoading(): Boolean {
                        return if (refreshLayout == null) true else !refreshLayout.isViewRefreshing()
                    }

                })
            ).build()

            helper.addBeforeAdapter(FixBugHeaderAdapter())
            // 设置预加载，请调用以下方法
//            helper.trailingLoadStateAdapter?.preloadSize = 1
            recyclerView.adapter = helper.adapter
            helper
        } else {
            // 不支持加载更多功能
            recyclerView.adapter = adapter
            null
        }

    private fun initObserverLoadState(
        lifecycleOwner: LifecycleOwner,
        adapter: BaseQuickAdapter<*, *>,
        refreshLayout: RefreshLayout?,
        helper: QuickAdapterHelper?,
    ) {
        if (viewModel is RefreshRepositoryOwner) {
            lifecycleOwner.lifecycleScope.launch {
                viewModel.onRefreshRepository().loadState.flowWithLifecycle(lifecycleOwner.lifecycle)
                    .collect { loadState ->
                        onLoadStateCollect(adapter, refreshLayout, helper, loadState)
                    }
            }
        }
    }

    private fun onLoadStateCollect(
        adapter: BaseQuickAdapter<*, *>,
        refreshLayout: RefreshLayout?,
        helper: QuickAdapterHelper?,
        loadState: LoadState?,
    ) {
        // 说明：由于是两个Flow，不知道那边setAdapter设置是否完成，所以不能通过adapter获取一些数据，所以有些数据需要传递过来。
        // 需求：刷新状态下，显示loadingView、errorView、emptyView的不能刷新和加载。

        // 设置Adapter的stateView
        setAdapterStateView(adapter, loadState)

        // 设置刷新状态（首部）
        refreshLayout?.apply {
            // 只有支持刷新的，才有此refreshLayout，才需要设置此状态。
            setViewRefreshState(loadState, isEnableRefresh(loadState))
        }

        // 设置加载状态（尾部）
        helper?.let {
            // 只有支持加载更多的，才有此helper，才需要设置此状态。
            setLoadState(helper, loadState)
        }

        // 失败提示
        if (viewModel is MessageDisplayManager && loadState is LoadState.Error) {
            viewModel.showMessage(loadState.error.toErrorMessage())
        }
    }

    // 设置Adapter的stateView
    private fun setAdapterStateView(
        adapter: BaseQuickAdapter<*, *>,
        loadState: LoadState?,
    ) {
        // 空状态，只有刷新下，并且是当前列表无数据时显示，其它不显示。
        adapter.stateView =
            if (loadState?.isRefresh == true && loadState.isCurrentDisplayEmptyList) {
                when (loadState) {
                    is LoadState.Loading -> stateView.showLoading()
                    is LoadState.Error -> stateView.showError(
                        loadState.error
                    ) { refresh() }

                    is LoadState.Success -> stateView.showEmpty()
                }
                stateView.root
            } else null
    }

    private fun setLoadState(
        helper: QuickAdapterHelper,
        loadState: LoadState?,
    ) {
        // 支持加载更多功能
        // 加载控件（不管是刷新、加载都设置），Loading、Error只要是刷新就不显示加载的状态，Success显示stateView的不显示加载的状态。
        val trailingLoadState = when (loadState) {
            is LoadState.Loading -> if (loadState.isRefresh) AdapterLoadState.None else AdapterLoadState.Loading
            is LoadState.Error -> if (loadState.isRefresh) AdapterLoadState.None else AdapterLoadState.Error(
                loadState.error
            )

            is LoadState.Success -> if (loadState.isRefresh && loadState.isCurrentDisplayEmptyList) AdapterLoadState.None else AdapterLoadState.NotLoading(
                loadState.isNoMoreData
            )

            null -> AdapterLoadState.None
        }
        helper.trailingLoadState = trailingLoadState
    }

    // 加载并且是加载中不能刷新（防止加载中能继续刷新）、显示loadingView、errorView的不能刷新（因为前一个属于过度，后两个已经设置了点击刷新了），其它都能。
    private fun isEnableRefresh(loadState: LoadState?) = when (loadState) {
        is LoadState.Loading -> !((loadState.isRefresh && loadState.isCurrentDisplayEmptyList) || !loadState.isRefresh) // 刷新并且空List不能（已显示loadingView）、加载的不能，其它的能。
        is LoadState.Error -> !(loadState.isRefresh && loadState.isCurrentDisplayEmptyList) // 刷新并且空List不能（已显示errorView），其它的能。
        is LoadState.Success -> true // 都能
        null -> true
    }

    private fun refresh() {
        refreshRepositoryFunInvoke(viewModel) { this.refresh() }
    }

    private fun load() {
        refreshRepositoryFunInvoke(viewModel) {
            if (this is OnRepositoryLoadMoreListener) this.load()
            else Log.e(
                "RefreshAndLoadHelper",
                "The $this does not implement the OnRepositoryLoadListener interface"
            )
        }
    }

    private fun loadRetry() {
        refreshRepositoryFunInvoke(viewModel) {
            if (this is OnRepositoryLoadMoreListener) this.loadRetry()
            else Log.e(
                "RefreshAndLoadHelper",
                "The $this does not implement the OnRepositoryLoadListener interface"
            )
        }
    }

    private fun refreshRepositoryFunInvoke(
        viewModel: ViewModel,
        function: suspend RefreshRepository<*>.() -> Unit,
    ) {
        if (viewModel is RefreshRepositoryOwner) {
            viewModel.viewModelScope.launch {
                function(viewModel.onRefreshRepository())
            }
        } else {
            Log.e(
                "RefreshAndLoadHelper",
                "The $viewModel does not implement the RefreshAndLoadRepositoryOwner interface"
            )
        }
    }
}