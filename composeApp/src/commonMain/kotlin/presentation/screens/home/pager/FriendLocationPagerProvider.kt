package io.github.vrcmteam.vrcm.presentation.screens.home.pager

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vrcmteam.vrcm.core.shared.SharedFlowCentre
import io.github.vrcmteam.vrcm.network.api.attributes.IUser
import io.github.vrcmteam.vrcm.network.api.attributes.LocationType
import io.github.vrcmteam.vrcm.network.api.attributes.UserStatus
import io.github.vrcmteam.vrcm.network.api.friends.date.FriendData
import io.github.vrcmteam.vrcm.presentation.compoments.AImage
import io.github.vrcmteam.vrcm.presentation.compoments.RefreshBox
import io.github.vrcmteam.vrcm.presentation.compoments.UserStateIcon
import io.github.vrcmteam.vrcm.presentation.extensions.currentNavigator
import io.github.vrcmteam.vrcm.presentation.extensions.getInsetPadding
import io.github.vrcmteam.vrcm.presentation.screens.home.data.FriendLocation
import io.github.vrcmteam.vrcm.presentation.screens.home.sheet.FriendLocationBottomSheet
import io.github.vrcmteam.vrcm.presentation.screens.profile.UserProfileScreen
import io.github.vrcmteam.vrcm.presentation.screens.profile.data.UserProfileVO
import io.github.vrcmteam.vrcm.presentation.screens.world.WorldProfileScreen
import io.github.vrcmteam.vrcm.presentation.screens.world.data.WorldProfileVO
import io.github.vrcmteam.vrcm.presentation.supports.ListPagerProvider
import org.koin.compose.koinInject

object FriendLocationPagerProvider : ListPagerProvider {

    override val index: Int
        get() = 0
    override val title: String
        get() = "Location"

    override val icon: Painter
        @Composable  get() = rememberVectorPainter(image = Icons.Rounded.Explore)

    @Composable
    override fun createPager(lazyListState: LazyListState):@Composable () -> Unit {
        val isRefreshing = rememberSaveable { mutableStateOf(true) }
        LaunchedEffect(Unit){
            SharedFlowCentre.error.collect{
                // 如果报错跳转登录，并重制刷新标记
                isRefreshing.value = true
            }
        }
        val friendLocationPagerModel: FriendLocationPagerModel = koinInject()
        LaunchedEffect(Unit){
            // 未clear()的同步刷新一次
            friendLocationPagerModel.doRefreshFriendLocation(removeNotIncluded = true)
        }
        return remember {
            {
                FriendLocationPager(
                    friendLocationMap = friendLocationPagerModel.friendLocationMap,
                    isRefreshing = isRefreshing.value,
                    lazyListState = lazyListState
                ) {
                    friendLocationPagerModel.refreshFriendLocation()
                    isRefreshing.value = false
                }
            }
        }
    }

}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun  FriendLocationPager(
    friendLocationMap:  Map<LocationType, MutableList<FriendLocation>>,
    isRefreshing:Boolean,
    lazyListState: LazyListState = rememberLazyListState(),
    doRefresh: suspend () -> Unit
) {
    var bottomSheetIsVisible by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val currentLocation: MutableState<FriendLocation?> = remember { mutableStateOf(null) }
    val onClickLocation : (FriendLocation) -> Unit ={
        currentLocation.value = it
        bottomSheetIsVisible = true
    }
    RefreshBox(
        isStartRefresh = isRefreshing,
        doRefresh = doRefresh
    ) {
        val currentNavigator = currentNavigator
        val offlineFriendLocation = friendLocationMap[LocationType.Offline]?.get(0)
        val privateFriendLocation = friendLocationMap[LocationType.Private]?.get(0)
        val travelingFriendLocation = friendLocationMap[LocationType.Traveling]?.get(0)
        val instanceFriendLocations = friendLocationMap[LocationType.Instance]
        val onClickUserIcon = { user: IUser ->
            if (currentNavigator.size <= 1) currentNavigator push UserProfileScreen(UserProfileVO(user))
        }
        // 如果没有底部系统手势条，默认12dp
        val bottomPadding = getInsetPadding(12, WindowInsets::getBottom) + 86.dp
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(
                start = 6.dp,
                top = 6.dp,
                end = 6.dp,
                bottom = bottomPadding
            )
        ) {

            item(key = LocationType.Offline) {
                SingleLocationCard(
                    offlineFriendLocation,
                    "Active on the Website",
                    onClickUserIcon
                )
            }

            item(key = LocationType.Private) {
                SingleLocationCard(
                    privateFriendLocation,
                    "Friends in Private Worlds",
                    onClickUserIcon
                )
            }

            item(key = LocationType.Traveling) {
                SingleLocationCard(
                    travelingFriendLocation,
                    "Friends is Traveling",
                    onClickUserIcon
                )
            }

            if (!instanceFriendLocations.isNullOrEmpty()) {
                item(key = LocationType.Instance) {
                    Text(
                        text = "by Location",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                items(instanceFriendLocations, key = { it.location }) { location ->
                    LocationCard(location, { onClickLocation(location) }) {
                        UserIconsRow(location.friendList, onClickUserIcon)
                    }
                }
            }

        }
    }
    FriendLocationBottomSheet(bottomSheetIsVisible, sheetState, currentLocation){
         bottomSheetIsVisible = false
    }
}
@Composable
private fun SingleLocationCard(
    friendLocations: FriendLocation?,
    text: String,
    onClickUserIcon: (IUser) -> Unit
) {
    if (friendLocations == null || friendLocations.friends.isEmpty()) return
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
    )
    Spacer(modifier = Modifier.height(6.dp))
    UserIconsRow(friendLocations.friendList, onClickUserIcon)
}

@Composable
private fun UserIconsRow(
    friends: List<State<FriendData>>,
    onClickUserIcon: (IUser) -> Unit
) {
    if (friends.isEmpty()) return
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(friends, key = { it.value.id }) {
            LocationFriend(
                it.value.iconUrl,
                it.value.displayName,
                it.value.status
            ) { onClickUserIcon(it.value) }
        }
    }
}

@Composable
private fun LocationFriend(
    iconUrl: String,
    name: String,
    userStatus: UserStatus,
    onClickUserIcon: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClickUserIcon),
        verticalArrangement = Arrangement.Center
    ) {
        UserStateIcon(
            modifier = Modifier.fillMaxSize(),
            iconUrl = iconUrl,
            userStatus = userStatus
        )
        Text(
            modifier = Modifier.fillMaxSize(),
            text = name,
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
private fun LocationCard(location: FriendLocation,clickable: () -> Unit, content: @Composable () -> Unit) {
    val instants by location.instants
    val currentNavigator = currentNavigator

    Card(
        modifier = Modifier.clickable(onClick = clickable),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth()
            ) {
                AImage(
                    modifier = Modifier
                        .width(120.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { currentNavigator.push(WorldProfileScreen(WorldProfileVO(
                            worldId = instants.worldId,
                            worldName = instants.worldName,
                            worldImageUrl = instants.worldImageUrl,
                            worldDescription = instants.worldDescription,
                        ))) },
                    imageData = instants.worldImageUrl,
                    contentDescription = "WorldImage"
                )
                Column(
                    modifier = Modifier.padding(horizontal = 6.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = instants.worldName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = instants.worldDescription,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .height(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AImage(
                            modifier = Modifier
                                .size(15.dp)
                                .align(Alignment.CenterVertically)
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape),
                            imageData = instants.regionIconUrl
                        )
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 6.dp),
                            text = instants.accessType,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 6.dp),
                            text = instants.userCount,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Icon(
                            modifier = Modifier
                                .size(15.dp),
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "PersonCount"
                        )
                    }
                }
            }
            content()
        }
    }
}
