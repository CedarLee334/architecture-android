package com.bytedance.douyin.core.data.repository.refreshloadmore

import com.bytedance.douyin.core.data.repository.refreshloadmore.interfaces.RefreshLoadMoreRepository
import com.bytedance.douyin.core.data.repository.refreshloadmore.interfaces.RefreshRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import java.util.UUID

/**
 * 描述：[RefreshRepository]-内存-通用实现
 * 说明：因为是通用功能类，所以实现了全部的刷新、加载的功能。
 * -如果只有刷新功能，也需要实现此，加载功能通过[BaseRefreshLoadMoreHelper]子类控制关闭。
 *
 * @author zhangrq
 * createTime 2024/9/14 下午2:34
 */
@Suppress("KDocUnresolvedReference")
abstract class BaseMemoryRefreshLoadMoreRepository<Key : Any?, Value : Any>(private val initKey: Key) :
    RefreshLoadMoreRepository<Value> {
    // 用于强制更新，当keyFlow值相同时，是不会进行通知的，所以需要强制更新。
    private val forceUpdateFlow = MutableStateFlow("")
    private val keyFlow = MutableStateFlow(initKey)
    private var finish: (() -> Unit)? = null

    val tempListFlow = MutableStateFlow<List<Value>?>(null)

    // 加载的（包含刷新、加载）状态，控制其Ui效果。
    override val loadState = MutableStateFlow<LoadState?>(null)

    private val getDataFlow: Flow<Key?> = combine(keyFlow, forceUpdateFlow) { key, _ ->
        val isRefresh = key == initKey

        // LoadState-Loading
        loadState.update {
            LoadState.Loading(
                isRefresh = isRefresh,
                isCurrentDisplayEmptyList = tempListFlow.value.isNullOrEmpty()
            )
        }
        try {
            // currentKeyList
            val currentKeyList = getListDataByKey(key, pageSize)
            val successList = if (isRefresh) currentKeyList else {
                // 加载，结果不为空才加，否则使用原来的。
                (currentKeyList?.let { (tempListFlow.value ?: emptyList()) + it }
                    ?: tempListFlow.value)
            }
            // Result-Success
            tempListFlow.update {
                successList
            }
            // LoadState-Success
            delay(1) // 延时1毫秒，解决获取数据太快，导致loadState丢失上面设置的LoadState.Loading状态。
            loadState.update {
                LoadState.Success(
                    isRefresh = it!!.isRefresh,
                    isCurrentDisplayEmptyList = successList.isNullOrEmpty(),
                    isNoMoreData = isNoMoreData(currentKeyList),
                )
            }
            // 通知完成
            notifyFinish()

            key
        } catch (e: Exception) {
            e.printStackTrace()
            val errorList = if (isRefresh) null else tempListFlow.value
            // Result-Error
            // 思考，异常要不要在此接收处理
            // -需要，因为
            tempListFlow.update {
                errorList
            }
            // LoadState-Success
            delay(1) // 延时1毫秒，解决获取数据太快，导致loadState丢失上面设置的LoadState.Loading状态。
            loadState.update {
                LoadState.Error(
                    isRefresh = it!!.isRefresh,
                    isCurrentDisplayEmptyList = errorList.isNullOrEmpty(),
                    error = e
                )
            }
            // 通知完成
            notifyFinish()

            key
        }
    }

    override val result: Flow<List<Value>?> = combine(getDataFlow, tempListFlow) { _, oldList ->
        // 每一次数据，都有提交，因为getDataFlow，不管成功与失败，始终有值在发出。
        oldList
    }

    override fun refresh(finish: (() -> Unit)?) {
        this.finish = finish
        forceUpdateKey(initKey)
    }

    override fun load() {
        val nextKey = getNextKey()
        forceUpdateKey(nextKey)
    }

    override fun loadRetry() {
        val currentKey = getCurrentKey()
        forceUpdateKey(currentKey)
    }

    abstract val pageSize: Int

    // 获取列表数据
    abstract suspend fun getListDataByKey(key: Key, pageSize: Int): List<Value>?

    // 获取下个Key
    abstract fun getNextKey(): Key

    // 获取当前Key
    fun getCurrentKey(): Key = keyFlow.value

    // 是否没有更多，子类可重写。
    open fun isNoMoreData(currentKeyList: List<Value>?) = (currentKeyList?.size ?: 0) < pageSize

    private fun forceUpdateKey(key: Key) {
        if (keyFlow.value == key) {
            // 相同，强制更新
            forceUpdateFlow.value = UUID.randomUUID().toString()
        } else {
            // 不同，普通更新
            keyFlow.value = key
        }
    }

    private fun notifyFinish() {
        finish?.invoke()
        finish = null
    }
}