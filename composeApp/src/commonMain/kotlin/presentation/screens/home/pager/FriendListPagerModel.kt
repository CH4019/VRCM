package io.github.vrcmteam.vrcm.presentation.screens.home.pager

import androidx.compose.runtime.mutableStateMapOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.vrcmteam.vrcm.core.shared.SharedFlowCentre
import io.github.vrcmteam.vrcm.network.api.attributes.UserStatus
import io.github.vrcmteam.vrcm.network.api.friends.FriendsApi
import io.github.vrcmteam.vrcm.network.api.friends.date.FriendData
import io.github.vrcmteam.vrcm.network.supports.VRCApiException
import io.github.vrcmteam.vrcm.network.websocket.data.type.FriendEvents
import io.github.vrcmteam.vrcm.presentation.supports.AuthSupporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class FriendListPagerModel(
    private val friendsApi: FriendsApi,
    private val authSupporter: AuthSupporter,
    private val json: Json
): ScreenModel {

    private val friendMap: MutableMap<String,FriendData> = mutableStateMapOf()

    /**
     * 获取好友列表按在线状态最后登录时间与id排序
     * 使用sortedByDescending是因为匹配最后登录时间倒序
     */
    val friendList: List<FriendData>
        get() = friendMap.values.sortedByDescending {
            (if (it.status == UserStatus.Offline) "0" else "1" )+ it.lastLogin + it.displayName
        }

    init {
        screenModelScope.launch {
            SharedFlowCentre.webSocket.collect { socketEvent ->
                when (socketEvent.type) {
                    FriendEvents.FriendActive.typeName,
                    FriendEvents.FriendOffline.typeName,
                    FriendEvents.FriendOnline.typeName-> {}
                    else -> return@collect
                }
                doRefreshFriendList()
            }
        }
    }

    suspend fun refreshFriendList() {
        friendMap.clear()
        doRefreshFriendList()
    }

    private suspend fun doRefreshFriendList(){
        // 多次更新时加把锁
        // 防止再次更新时拉取到的与上次相同的instanceId导致item的key冲突
        screenModelScope.launch(Dispatchers.IO) {
            friendsApi.allFriendsFlow()
                .retry(1) {
                    if (it is VRCApiException) authSupporter.doReTryAuth() else false
                }.catch {
                    SharedFlowCentre.error.emit(it.message.toString())
                }.collect { friends ->
                    update(friends)
                }
        }.join()
    }

    private fun update(
        friends: List<FriendData>,
    ) = screenModelScope.launch(Dispatchers.Default) {
        runCatching {
            friendMap.putAll(friends.associateBy { it.id })
        }.onFailure {
            SharedFlowCentre.error.emit(it.message.toString())}
    }

}