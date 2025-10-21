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
package com.eblan.launcher.domain.model

data class AppWidgetManagerAppWidgetProviderInfo(
    val className: String,
    val packageName: String,
    val serialNumber: Long,
    val componentName: String,
    val configure: String?,
    val targetCellWidth: Int,
    val targetCellHeight: Int,
    val minWidth: Int,
    val minHeight: Int,
    val resizeMode: Int,
    val minResizeWidth: Int,
    val minResizeHeight: Int,
    val maxResizeWidth: Int,
    val maxResizeHeight: Int,
    val preview: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppWidgetManagerAppWidgetProviderInfo

        if (targetCellWidth != other.targetCellWidth) return false
        if (targetCellHeight != other.targetCellHeight) return false
        if (minWidth != other.minWidth) return false
        if (minHeight != other.minHeight) return false
        if (resizeMode != other.resizeMode) return false
        if (minResizeWidth != other.minResizeWidth) return false
        if (minResizeHeight != other.minResizeHeight) return false
        if (maxResizeWidth != other.maxResizeWidth) return false
        if (maxResizeHeight != other.maxResizeHeight) return false
        if (className != other.className) return false
        if (packageName != other.packageName) return false
        if (componentName != other.componentName) return false
        if (configure != other.configure) return false
        if (preview != null) {
            if (other.preview == null) return false
            if (!preview.contentEquals(other.preview)) return false
        } else if (other.preview != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = targetCellWidth
        result = 31 * result + targetCellHeight
        result = 31 * result + minWidth
        result = 31 * result + minHeight
        result = 31 * result + resizeMode
        result = 31 * result + minResizeWidth
        result = 31 * result + minResizeHeight
        result = 31 * result + maxResizeWidth
        result = 31 * result + maxResizeHeight
        result = 31 * result + className.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + componentName.hashCode()
        result = 31 * result + configure.hashCode()
        result = 31 * result + (preview?.contentHashCode() ?: 0)
        return result
    }
}
