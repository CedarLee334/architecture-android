package com.bytedance.core.network.util

import com.bytedance.core.common.exception.RuleException
import com.bytedance.core.network.model.BaseNetworkModel

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/11/20 17:44
 */
/**
 * 网络成功-规则成功-内部数据-不可空
 */
fun <T> BaseNetworkModel<T>.toRuleSuccessData(): T {
    if (!isRuleSuccess()) {
        throw RuleException(code(), message())
    }
    return data()!!
}

/**
 * 网络成功-规则成功-内部数据-可空
 */
fun <T> BaseNetworkModel<T>.toRuleSuccessDataNullable(): T? {
    if (!isRuleSuccess()) {
        throw RuleException(code(), message())
    }
    return data()
}

/**
 * 网络成功-规则成功-全部数据
 */
fun <T> BaseNetworkModel<T>.toRuleSuccess(): BaseNetworkModel<T> {
    if (!isRuleSuccess()) {
        throw RuleException(code(), message())
    }
    return this
}
// 网络成功-全部数据。则不需要调用此转换方法，直接返回即可。