package com.bytedance.douyin.core.architecture.app.views.adapter

import androidx.recyclerview.widget.AdapterListUpdateCallback
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.AsyncListDiffer.ListListener
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.core.architecture.base.views.adapter.BaseDiffItemCallback
import com.bytedance.core.model.BaseDiffItem
import com.chad.library.adapter4.BaseQuickAdapter

/**
 * 描述：
 * 说明：不要调用非[submitList]，其它的修改数据方法。
 *
 * @author zhangrq
 * createTime 2024/2/29 下午5:05
 */
@Suppress("LeakingThis")
abstract class AppDifferAdapter<T : BaseDiffItem, VH : RecyclerView.ViewHolder> :
    BaseQuickAdapter<T, VH>() {

    private val mDiffer: AsyncListDiffer<T> = AsyncListDiffer(
        AdapterListUpdateCallback(this),
        AsyncDifferConfig.Builder(BaseDiffItemCallback<T>()).build()
    )
    private val mListener: ListListener<T> = ListListener<T> { previousList, currentList ->
        val oldDisplayEmptyLayout = displayEmptyView(previousList)
        val newDisplayEmptyLayout = displayEmptyView(currentList)

        if (oldDisplayEmptyLayout && !newDisplayEmptyLayout) {
            notifyItemRemoved(0)
            recyclerView.scrollToPosition(0)
        } else if (newDisplayEmptyLayout && !oldDisplayEmptyLayout) {
            notifyItemInserted(0)
        } else if (oldDisplayEmptyLayout) {
            notifyItemChanged(0, EMPTY_PAYLOAD)
        }

        this@AppDifferAdapter.onCurrentListChanged(previousList, currentList)
    }

    init {
        mDiffer.addListListener(mListener)
    }

    // 父类getItemCount、getItem已经实现了，且没有提供暴露或者是定义了final，所以此必须实现。
    final override var items: List<T>
        get() = mDiffer.currentList
        set(value) {
            mDiffer.submitList(value, null)
        }

    override fun submitList(list: List<T>?) {
        mDiffer.submitList(list)
    }

    open fun submitList(list: List<T>?, commitCallback: Runnable?) {
        mDiffer.submitList(list, commitCallback)
    }

//    protected fun getItem(position: Int): T {
//        return mDiffer.currentList[position]
//    }
//
//    override fun getItemCount(): Int {
//        return mDiffer.currentList.size
//    }

    val currentList: List<T>
        get() = mDiffer.currentList

    @Suppress("EmptyMethod")
    open fun onCurrentListChanged(previousList: List<T>, currentList: List<T>) {
    }

    override fun set(position: Int, data: T) {
    }

    override fun add(data: T) {
    }

    override fun add(position: Int, data: T) {
    }

    override fun addAll(collection: Collection<T>) {
    }

    override fun addAll(position: Int, collection: Collection<T>) {
    }

    override fun removeAt(position: Int) {
    }

    override fun remove(data: T) {
    }

    override fun removeAtRange(range: IntRange) {
    }

    override fun swap(fromPosition: Int, toPosition: Int) {
    }

    override fun move(fromPosition: Int, toPosition: Int) {
    }

    companion object {
        internal const val EMPTY_PAYLOAD = 0
    }
}