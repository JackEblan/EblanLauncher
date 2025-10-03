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

import com.eblan.launcher.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.com.eblan.launcher.library.get().pluginId)
                apply(libs.plugins.com.eblan.launcher.hilt.get().pluginId)
                apply(libs.plugins.kotlin.serialization.get().pluginId)
            }

            dependencies {
                add("implementation", project(":design-system"))
                add("implementation", project(":ui"))

                add("implementation", libs.androidx.hilt.navigation.compose)
                add("implementation", libs.androidx.activity.compose)
                add("implementation", libs.androidx.activity.ktx)
                add("implementation", libs.androidx.lifecycle.runtime.compose)
                add("implementation", libs.androidx.lifecycle.viewmodel.compose)
                add("implementation", libs.androidx.navigation.compose)
                add("implementation", libs.kotlinx.serialization.json)
            }
        }
    }
}