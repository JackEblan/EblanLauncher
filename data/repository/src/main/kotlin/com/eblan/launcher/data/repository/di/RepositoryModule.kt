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

import com.eblan.launcher.data.repository.DefaultApplicationInfoRepository
import com.eblan.launcher.data.repository.DefaultGridRepository
import com.eblan.launcher.data.repository.DefaultInMemoryApplicationInfoRepository
import com.eblan.launcher.data.repository.DefaultUserDataRepository
import com.eblan.launcher.domain.repository.ApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.InMemoryApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun gridRepository(impl: DefaultGridRepository): GridRepository

    @Binds
    @Singleton
    fun userDataRepository(impl: DefaultUserDataRepository): UserDataRepository

    @Binds
    @Singleton
    fun applicationInfoRepository(impl: DefaultApplicationInfoRepository): ApplicationInfoRepository


    @Binds
    @Singleton
    fun inMemoryApplicationInfoRepository(impl: DefaultInMemoryApplicationInfoRepository): InMemoryApplicationInfoRepository
}
