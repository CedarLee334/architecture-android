package com.bytedance.douyin.core.network.model.base

import com.bytedance.core.network.model.BaseNetworkModel
import kotlinx.serialization.Serializable

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/8/29 下午4:53
 */
@Serializable
data class AppBaseNetworkModel<T>(
    val errorCode: Int,
    val errorMsg: String? = null,
    val data: T? = null,
) : BaseNetworkModel<T> {
    override fun isRuleSuccess() = errorCode == 0

    override fun code() = errorCode
    override fun message() = errorMsg
    override fun data() = data
}