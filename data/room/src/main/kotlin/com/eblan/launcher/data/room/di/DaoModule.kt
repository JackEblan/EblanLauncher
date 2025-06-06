/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.data.room.di

import com.eblan.launcher.data.room.EblanDatabase
import com.eblan.launcher.data.room.dao.EblanAppWidgetProviderInfoDao
import com.eblan.launcher.data.room.dao.EblanApplicationInfoDao
import com.eblan.launcher.data.room.dao.GridDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DaoModule {

    @Provides
    @Singleton
    fun gridDao(eblanDatabase: EblanDatabase): GridDao = eblanDatabase.gridDao()

    @Provides
    @Singleton
    fun eblanApplicationInfoDao(eblanDatabase: EblanDatabase): EblanApplicationInfoDao =
        eblanDatabase.eblanApplicationInfoDao()

    @Provides
    @Singleton
    fun eblanAppWidgetProviderInfoDao(eblanDatabase: EblanDatabase): EblanAppWidgetProviderInfoDao =
        eblanDatabase.eblanAppWidgetProviderInfoDao()
}
