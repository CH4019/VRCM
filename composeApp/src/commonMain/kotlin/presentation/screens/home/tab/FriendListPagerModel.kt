package io.github.vrcmteam.vrcm.presentation.screens.home.tab

import androidx.compose.runtime.mutableStateListOf
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.vrcmteam.vrcm.core.shared.SharedFlowCentre
import io.github.vrcmteam.vrcm.network.api.friends.FriendsApi
import io.github.vrcmteam.vrcm.network.api.friends.date.FriendData
import io.github.vrcmteam.vrcm.network.supports.VRCApiException
import io.github.vrcmteam.vrcm.presentation.supports.AuthSupporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FriendListPagerModel(
    private val friendsApi: FriendsApi,
    private val authSupporter: AuthSupporter,
): ScreenModel {

    val friendList: MutableList<FriendData> = mutableStateListOf()

    private val updateMutex = Mutex()

    suspend fun refreshFriendList() {
        friendList.clear()
        // 多次更新时加把锁
        // 防止再次更新时拉取到的与上次相同的instanceId导致item的key冲突
        screenModelScope.launch(Dispatchers.IO) {
            friendsApi.allFriendsFlow()
                .retry(1) {
                    if (it is VRCApiException) authSupporter.doReTryAuth() else false
                }.catch {
                    SharedFlowCentre.error.emit(it.message.toString())
                }.collect { friends ->
                    updateMutex.withLock(friendList) { update(friends) }
                }
        }.join()

    }

    private fun update(
        friends: List<FriendData>,
    ) = screenModelScope.launch(Dispatchers.Default) {
        runCatching {
            friendList.removeAll { old -> friends.any { old.id == it.id } }
            val temp = friendList + friends
            friendList.clear()
            friendList.addAll(temp)
        }.onFailure {
            SharedFlowCentre.error.emit(it.message.toString())}
    }

}