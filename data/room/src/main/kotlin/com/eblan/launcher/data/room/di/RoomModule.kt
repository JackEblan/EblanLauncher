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

import android.content.Context
import androidx.room.Room
import com.eblan.launcher.data.room.EblanLauncherDatabase
import com.eblan.launcher.data.room.converter.EblanLauncherTypeConverters
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object RoomModule {
    @Singleton
    @Provides
    fun eblanLauncherDatabase(
        @ApplicationContext context: Context,
        eblanLauncherTypeConverters: EblanLauncherTypeConverters,
    ): EblanLauncherDatabase = Room.databaseBuilder(
        context,
        EblanLauncherDatabase::class.java,
        EblanLauncherDatabase.DATABASE_NAME,
    ).addTypeConverter(eblanLauncherTypeConverters).build()
}
