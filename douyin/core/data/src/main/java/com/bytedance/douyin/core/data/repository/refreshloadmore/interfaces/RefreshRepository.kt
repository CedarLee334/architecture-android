package com.bytedance.douyin.core.data.repository.refreshloadmore.interfaces

import com.bytedance.douyin.core.data.repository.refreshloadmore.LoadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * 描述：支持使用[BaseRefreshLoadMoreHelper]子类的，Repository层接口。
 * 1、如果UI层使用[BaseRefreshLoadMoreHelper]子类的，则Repository层【必须】实现此接口。
 * 2、[OnRepositoryLoadMoreListener]，为Repository层【可选】实现。
 *
 * 说明：此接口实现[OnRepositoryRefreshListener]的原因：因为即使UI层没刷新、加载功能，也会有网络错误等失败情况，其点击会调用【刷新】，所以需要必须实现此刷新接口。
 *
 * @author zhangrq
 * createTime 2024/11/8 09:32
 */
@Suppress("KDocUnresolvedReference")
// 刷新仓库
interface RefreshRepository<Value : Any> : OnRepositoryRefreshListener {
    val loadState: MutableStateFlow<LoadState?>
    val result: Flow<List<Value>?>
}

// 刷新、加载仓库
interface RefreshLoadMoreRepository<Value : Any> : RefreshRepository<Value>,
    OnRepositoryLoadMoreListener