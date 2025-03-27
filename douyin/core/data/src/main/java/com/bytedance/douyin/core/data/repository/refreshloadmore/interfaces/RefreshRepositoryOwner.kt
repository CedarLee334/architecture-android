package com.bytedance.douyin.core.data.repository.refreshloadmore.interfaces

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/11/7 16:34
 */
interface RefreshRepositoryOwner {
    fun onRefreshRepository(): RefreshRepository<*>
}