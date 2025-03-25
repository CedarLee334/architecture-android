package com.bytedance.demo.app.main

import com.bytedance.douyin.core.architecture.app.AppViewModel
import com.bytedance.douyin.core.data.repository.interfaces.MainRepository
import com.bytedance.douyin.core.model.MainTabType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import com.bytedance.demo.app.main.MainUiState as UiState

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2025/3/24 11:14
 */
@HiltViewModel
class MainViewModel @Inject constructor(mainRepository: MainRepository) : AppViewModel<UiState>() {

    override val uiStateInitialValue: UiState = UiState() // UiState-初始化值

    // 从MainRepository获取的本地流，本地数据改，UI改。
    override val uiStateFlow: Flow<UiState> = mainRepository.getMainTabsStream().map {
        // UiState-页面值
        UiState(tabs = it)
    }
}

// Main-UiState
data class MainUiState(
    val tabs: List<MainTabType>? = null,
)