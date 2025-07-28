package com.eblan.launcher.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.eblan.launcher.data.datastore.mapper.toGestureActionProto
import com.eblan.launcher.data.datastore.proto.UserDataProto
import com.eblan.launcher.data.datastore.proto.appdrawer.AppDrawerSettingsProto
import com.eblan.launcher.data.datastore.proto.gesture.GestureSettingsProto
import com.eblan.launcher.data.datastore.proto.home.HomeSettingsProto
import com.eblan.launcher.data.datastore.proto.home.TextColorProto
import com.eblan.launcher.domain.model.GestureAction
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * An [androidx.datastore.core.Serializer] for the [UserDataProto] proto.
 */
class UserDataSerializer @Inject constructor() : Serializer<UserDataProto> {
    private val defaultHomeSettingsProto = HomeSettingsProto.newBuilder().apply {
        rows = 5
        columns = 5
        pageCount = 1
        infiniteScroll = true
        dockRows = 1
        dockColumns = 5
        dockHeight = 300
        textColorProto = TextColorProto.System
        initialPage = 0
        wallpaperScroll = true
    }.build()

    private val defaultAppDrawerSettingsProto = AppDrawerSettingsProto.newBuilder().apply {
        appDrawerColumns = 5
        appDrawerRowsHeight = 500
    }.build()

    private val defaultGestureSettingsProto = GestureSettingsProto.newBuilder().apply {
        doubleTapProto = GestureAction.None.toGestureActionProto()

        swipeUpProto = GestureAction.OpenAppDrawer.toGestureActionProto()

        swipeDownProto = GestureAction.None.toGestureActionProto()
    }.build()

    override val defaultValue: UserDataProto = UserDataProto.newBuilder().apply {
        homeSettingsProto = defaultHomeSettingsProto

        appDrawerSettingsProto = defaultAppDrawerSettingsProto

        gestureSettingsProto = defaultGestureSettingsProto
    }.build()

    override suspend fun readFrom(input: InputStream): UserDataProto = try {
        // readFrom is already called on the data store background thread
        UserDataProto.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Cannot read proto.", exception)
    }

    override suspend fun writeTo(t: UserDataProto, output: OutputStream) {
        // writeTo is already called on the data store background thread
        t.writeTo(output)
    }
}