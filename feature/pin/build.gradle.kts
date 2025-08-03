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

plugins {
    alias(libs.plugins.com.eblan.launcher.feature)
    alias(libs.plugins.com.eblan.launcher.libraryCompose)
}

android {
    namespace = "com.eblan.launcher.feature.pin"
}

dependencies {
    implementation(projects.common)
    implementation(projects.domain.framework)
    implementation(projects.domain.grid)
    implementation(projects.domain.repository)
    implementation(projects.domain.useCase)
    implementation(projects.framework.launcherApps)
    implementation(projects.framework.widgetManager)
}