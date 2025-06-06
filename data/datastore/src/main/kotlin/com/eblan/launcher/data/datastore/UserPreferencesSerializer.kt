/*
/ *
/ *   Copyright 2023 Einstein Blanco
/ *
/ *   Licensed under the GNU General Public License v3.0 (the "License");
/ *   you may not use this file except in compliance with the License.
/ *   You may obtain a copy of the License at
/ *
/ *       https://www.gnu.org/licenses/gpl-3.0
/ *
/ *   Unless required by applicable law or agreed to in writing, software
/ *   distributed under the License is distributed on an "AS IS" BASIS,
/ *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
/ *   See the License for the specific language governing permissions and
/ *   limitations under the License.
/ *
*/
package com.eblan.launcher.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.eblan.launcher.data.datastore.proto.TextColor
import com.eblan.launcher.data.datastore.proto.UserPreferences
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * An [androidx.datastore.core.Serializer] for the [UserPreferences] proto.
 */
class UserPreferencesSerializer @Inject constructor() : Serializer<UserPreferences> {
    override val defaultValue: UserPreferences = UserPreferences.newBuilder().apply {
        rows = 5
        columns = 5
        pageCount = 1
        infiniteScroll = true
        dockRows = 1
        dockColumns = 5
        dockHeight = 300
        textColor = TextColor.White
        appDrawerColumns = 5
    }.build()

    override suspend fun readFrom(input: InputStream): UserPreferences = try {
        // readFrom is already called on the data store background thread
        UserPreferences.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Cannot read proto.", exception)
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        // writeTo is already called on the data store background thread
        t.writeTo(output)
    }
}