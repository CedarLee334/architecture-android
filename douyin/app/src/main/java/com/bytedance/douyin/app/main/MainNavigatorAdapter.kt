package com.bytedance.douyin.app.main

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.bytedance.core.common.util.asSafe
import com.bytedance.core.common.util.getDataFromThemeAttr
import com.bytedance.douyin.R
import com.bytedance.douyin.core.architecture.app.views.adapter.AppNavigatorAdapter
import com.bytedance.douyin.core.common.interfaces.OnTabClickAgainListener
import com.bytedance.douyin.core.common.interfaces.OnTabClickRefreshFinishListener
import com.bytedance.douyin.core.common.interfaces.OnTabClickRefreshListener
import com.bytedance.douyin.core.login.login.LoginManager
import com.bytedance.douyin.core.model.MainTabType
import com.bytedance.douyin.core.test.AppTestActivity
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.CommonPagerTitleView
import com.bytedance.douyin.databinding.ItemActivityMainTabBinding as ViewBinding
import com.google.android.material.R as materialR

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/4/1 上午9:14
 */

class MainNavigatorAdapter(
    private val activity: FragmentActivity,
    private val viewPager2: ViewPager2,
    private val mainFragmentStateAdapter: MainFragmentStateAdapter,
    private val magicIndicator: MagicIndicator,
) : AppNavigatorAdapter<MainTabsItem>() {
    private var isDarkFont: Boolean = true

    override fun getTitleView(context: Context, index: Int, item: MainTabsItem): IPagerTitleView {
        val pagerTitleView = CommonPagerTitleView(context)
        // set custom layout
        val binding = ViewBinding.inflate(LayoutInflater.from(context))
        if (item.type == MainTabType.CAMERA) {
            // 拍摄
            binding.setCurrentTabLayout(3)
            binding.image.setImageResource(R.mipmap.ic_launcher)
            binding.image.setContentDescription(context.resources.getString(item.titleResId))
            binding.title.setText(item.titleResId)
        } else {
            // 普通
            binding.setCurrentTabLayout(1)
            binding.title.setText(item.titleResId)
        }
        pagerTitleView.setContentView(binding.root)
//        pagerTitleView.onPagerTitleChangeListener
        // 设置颜色，在此设置是为了解决跳转到后台或其它页面，然后再回来，会重新设置Adapter的数据，内部调用了notifyDataSetChanged()，所以此布局会重新创建一次。
        pagerTitleView.setColor(isDarkFont, index == viewPager2.currentItem)
        // 点击
        pagerTitleView.setOnClickListener {
            binding.onTabClickHandle(index, item)
        }
        // -图片点击，以支持无障碍。
        binding.image.setOnClickListener {
            binding.onTabClickHandle(index, item)
        }
        // 长按
        if (item.type == MainTabType.SHOP || item.type == MainTabType.FRIEND) {
            // 商城、好友，长按。
            pagerTitleView.setOnLongClickListener {
                // Item处理
                item.updateMainTabShopOrFriend(item.type == MainTabType.SHOP)
                true
            }
        } else {
            pagerTitleView.setOnLongClickListener(null)
        }

        return pagerTitleView
    }

    override fun getIndicator(context: Context): IPagerIndicator? = null

    private fun ViewBinding.onTabClickHandle(index: Int, item: MainTabsItem) {
        if (viewPager2.currentItem == index) {
            // 在当前页面，做其它操作，如刷新、滚动到未读，实现功能由实现类自己控制。
            onTabClickAgainHandle(index)
        } else {
            // 不在当前页面，切换。
            val type = item.type
            if (type == MainTabType.CAMERA) {
                // 拍摄，直接返回。
                AppTestActivity.startActivity(activity, item.titleResId)
                return
            }
            // 切换
            if (type == MainTabType.HOME) {
                // 首页，不登录。
                viewPager2.setCurrentItem(index, false)
            } else {
                // 其它，需要登录
                LoginManager.checkLogin(activity) {
                    viewPager2.setCurrentItem(index, false)
                }
            }
        }
    }

    private fun ViewBinding.onTabClickAgainHandle(index: Int) {
        // 在当前Tab下单击，实现功能由实现类自己控制。效果如下：
        // -首页、朋友，刷新内容，并底部显示Loading。
        // -消息，滚动到未读，没未读滚动到顶部。
        // -我的，无
        val currentFragment = mainFragmentStateAdapter.getFragmentByIndex(index)
        if (currentFragment is OnTabClickRefreshListener) {
            // 刷新
            // -展示Loading
            setCurrentTabLayout(2)
            currentFragment.onTabClickRefresh(object : OnTabClickRefreshFinishListener {
                override fun onTabClickRefreshFinish() {
                    // 隐藏Loading
                    setCurrentTabLayout(1)
                }
            })
        } else if (currentFragment is OnTabClickAgainListener) {
            // 让子类实现效果，如：消息栏目的效果（滚动到未读，没未读滚动到顶部）。
            currentFragment.onTabClickAgain()
        }
    }

    /**
     * 设置当前Tab布局，[type]：1展示标题、2展示Loading、3展示图片。
     */
    private fun ViewBinding.setCurrentTabLayout(type: Int) {
        title.isVisible = type == 1
        loading.isVisible = type == 2
        image.isVisible = type == 3
    }

    fun setIsDarkFont(isDarkFont: Boolean) {
        this.isDarkFont = isDarkFont
        // 说明：不能使用OnPagerTitleChangeListener来实现设置此颜色变化，因为这个每次通知的都是变化的2个（一个是onSelected、另一个是onDeselected），不能实现这个要控制全部的，
        // -所以只能每次改变都是调用notifyDataSetChanged，然后重新设置新的值，但是这样性能不好，每次布局都是重新绘制的。
        // -所以只能拿到原来的View然后进行设置更新。

        // 设置标题颜色
        setTitleColor(isDarkFont)
        // 设置背景颜色
        setBackgroundColor(isDarkFont)
    }

    private fun setTitleColor(isDarkFont: Boolean) {
        val titleContainer =
            magicIndicator.navigator.asSafe<CommonNavigator>()?.titleContainer ?: return
        for (i in 0 until titleContainer.childCount) {
            // item view
            titleContainer.getChildAt(i).asSafe<CommonPagerTitleView>()
                ?.setColor(isDarkFont, i == viewPager2.currentItem)
        }
    }

    private fun CommonPagerTitleView.setColor(
        isDarkFont: Boolean,
        selected: Boolean,
    ) {
        val title = findViewById<TextView>(R.id.title)
        // 设置值
        title.alpha = if (selected) 1f else 0.7f
        title.setTextColor(context.getDataFromThemeAttr(if (isDarkFont) materialR.attr.colorOnSurface else materialR.attr.colorOnSurfaceInverse))
    }

    private fun setBackgroundColor(
        isDarkFont: Boolean,
    ) {
        val selectedBackgroundColor =
            magicIndicator.context.getDataFromThemeAttr(if (isDarkFont) materialR.attr.colorSurface else materialR.attr.colorSurfaceInverse)
        magicIndicator.setBackgroundColor(selectedBackgroundColor)
    }
}