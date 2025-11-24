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
package com.eblan.launcher.data.repository.di

import com.eblan.launcher.data.repository.DefaultApplicationInfoGridItemRepository
import com.eblan.launcher.data.repository.DefaultEblanAppWidgetProviderInfoRepository
import com.eblan.launcher.data.repository.DefaultEblanApplicationInfoRepository
import com.eblan.launcher.data.repository.DefaultEblanIconPackInfoRepository
import com.eblan.launcher.data.repository.DefaultEblanShortcutConfigActivityRepository
import com.eblan.launcher.data.repository.DefaultEblanShortcutInfoRepository
import com.eblan.launcher.data.repository.DefaultFolderGridCacheRepository
import com.eblan.launcher.data.repository.DefaultFolderGridItemRepository
import com.eblan.launcher.data.repository.DefaultGridCacheRepository
import com.eblan.launcher.data.repository.DefaultShortcutConfigActivityGridItemRepository
import com.eblan.launcher.data.repository.DefaultShortcutInfoGridItemRepository
import com.eblan.launcher.data.repository.DefaultUserDataRepository
import com.eblan.launcher.data.repository.DefaultWidgetGridItemRepository
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigActivityRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.FolderGridCacheRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridCacheRepository
import com.eblan.launcher.domain.repository.ShortcutConfigActivityGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface RepositoryModule {
    @Binds
    @Singleton
    fun userDataRepository(impl: DefaultUserDataRepository): UserDataRepository

    @Binds
    @Singleton
    fun eblanApplicationInfoRepository(impl: DefaultEblanApplicationInfoRepository): EblanApplicationInfoRepository

    @Binds
    @Singleton
    fun gridCacheRepository(impl: DefaultGridCacheRepository): GridCacheRepository

    @Binds
    @Singleton
    fun folderGridCacheRepository(impl: DefaultFolderGridCacheRepository): FolderGridCacheRepository

    @Binds
    @Singleton
    fun eblanAppWidgetProviderInfoRepository(impl: DefaultEblanAppWidgetProviderInfoRepository): EblanAppWidgetProviderInfoRepository

    @Binds
    @Singleton
    fun eblanShortcutInfoRepository(impl: DefaultEblanShortcutInfoRepository): EblanShortcutInfoRepository

    @Binds
    @Singleton
    fun applicationInfoGridItemRepository(impl: DefaultApplicationInfoGridItemRepository): ApplicationInfoGridItemRepository

    @Binds
    @Singleton
    fun widgetGridItemRepository(impl: DefaultWidgetGridItemRepository): WidgetGridItemRepository

    @Binds
    @Singleton
    fun shortcutInfoGridItemRepository(impl: DefaultShortcutInfoGridItemRepository): ShortcutInfoGridItemRepository

    @Binds
    @Singleton
    fun folderGridItemRepository(impl: DefaultFolderGridItemRepository): FolderGridItemRepository

    @Binds
    @Singleton
    fun iconPackRepository(impl: DefaultEblanIconPackInfoRepository): EblanIconPackInfoRepository

    @Binds
    @Singleton
    fun eblanShortcutConfigActivityRepository(impl: DefaultEblanShortcutConfigActivityRepository): EblanShortcutConfigActivityRepository

    @Binds
    @Singleton
    fun shortcutConfigActivityGridItemRepository(impl: DefaultShortcutConfigActivityGridItemRepository): ShortcutConfigActivityGridItemRepository
}
