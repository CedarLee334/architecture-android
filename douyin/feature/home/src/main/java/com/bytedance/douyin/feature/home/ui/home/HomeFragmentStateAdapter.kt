package com.bytedance.douyin.feature.home.ui.home

import android.content.Context
import androidx.fragment.app.Fragment
import com.bytedance.douyin.core.architecture.app.views.adapter.AppFragmentStateAdapter
import com.bytedance.douyin.core.common.util.setFragmentTopExtraHeight
import com.bytedance.douyin.core.model.HomeTabType
import com.bytedance.douyin.core.router.Router
import com.bytedance.douyin.core.test.AppTestFragment
import com.bytedance.douyin.feature.home.R

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/4/1 上午9:15
 */

class HomeFragmentStateAdapter(fragment: Fragment, val context: Context) :
    AppFragmentStateAdapter<HomeTabsItem>(fragment, true) {

    override fun createFragment(position: Int, item: HomeTabsItem): Fragment {
        // 热点（视频）
        // 直播（视频）
        // 精选（图文，点击跳到新页面）
        // 北京（视频，未获取到位置为：同城）
        // 团购（商城-团购）
        // 关注（视频）
        // 经验（图文）
        // 商城（商城-商城）
        // 推荐（视频、图文切换）
        // 朋友（视频）

        return when (item.type) {
            // 视频
            HomeTabType.HOT, HomeTabType.LIVE, HomeTabType.SAME_CITY, HomeTabType.FOLLOW,
            HomeTabType.RECOMMENDED, HomeTabType.FRIEND,
                -> Router.Video.createVideoFragment()
            // 商城
            HomeTabType.SHOP -> Router.Shop.Shop.createShopFragment()
            // 其它
            HomeTabType.EXPERIENCE, HomeTabType.GROUP_BUYING, HomeTabType.CHOICE ->
                AppTestFragment.newInstance(item.titleResId)
        }
            // 存高度值
            .setFragmentTopExtraHeight(context.resources.getDimensionPixelSize(R.dimen.douyin_feature_home_magic_indicator_height))
    }
}