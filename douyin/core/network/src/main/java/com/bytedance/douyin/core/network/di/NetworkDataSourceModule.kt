package com.bytedance.douyin.core.network.di

import com.bytedance.douyin.core.network.datasource.FakeNetworkLoginDataSource
import com.bytedance.douyin.core.network.datasource.interfaces.NetworkLoginDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 描述：
 *
 * @author zhangrq
 * createTime 2024/11/28 10:48
 */
@Module
@InstallIn(SingletonComponent::class)
internal interface NetworkDataSourceModule {

    @Binds
    fun binds(impl: FakeNetworkLoginDataSource): NetworkLoginDataSource
}