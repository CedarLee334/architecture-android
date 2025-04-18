package com.bytedance.douyin.core.architecture.app.views

import androidx.fragment.app.viewModels
import androidx.viewbinding.ViewBinding
import com.bytedance.core.architecture.empty.EmptyUiState
import com.bytedance.core.architecture.empty.EmptyViewModel

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/2/26 下午2:45
 */
abstract class AppViewsEmptyViewModelFragment<Binding : ViewBinding> :
    AppViewsFragment<Binding, EmptyUiState, EmptyViewModel>() {
    override val viewModel: EmptyViewModel by viewModels()

    override fun Binding.onUiStateCollect(uiState: EmptyUiState) {
    }
}