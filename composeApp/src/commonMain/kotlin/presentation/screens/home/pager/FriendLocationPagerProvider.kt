package io.github.vrcmteam.vrcm.presentation.screens.home.pager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import io.github.vrcmteam.vrcm.presentation.configs.locale.strings
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
        @Composable
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
@OptIn(ExperimentalMaterial3Api::class)
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
    val onClickLocation : (FriendLocation) -> Unit = {
        currentLocation.value = it
        bottomSheetIsVisible = true
    }
    val topPadding = getInsetPadding(WindowInsets::getTop) + 80.dp
    RefreshBox(
        refreshContainerOffsetY = topPadding,
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
        val bottomPadding = getInsetPadding(12, WindowInsets::getBottom) + 80.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = topPadding,
                end = 16.dp,
                bottom = bottomPadding
            )
        ) {

            SimpleCLocationCard(
                offlineFriendLocation,
                LocationType.Offline,
                onClickUserIcon
            ) { strings.fiendLocationPagerWebsite }

            SimpleCLocationCard(
                privateFriendLocation,
                LocationType.Private,
                onClickUserIcon
            ) { strings.fiendLocationPagerPrivate }

            SimpleCLocationCard(
                travelingFriendLocation,
                LocationType.Traveling,
                onClickUserIcon
            ) { strings.fiendLocationPagerTraveling }

            if (!instanceFriendLocations.isNullOrEmpty()) {
                item(key = LocationType.Instance) {
                    LocationTitle(
                        text = strings.fiendLocationPagerLocation,
                    )
                }
                items(instanceFriendLocations, key = { it.location }) { location ->
                    LocationCard(location, { onClickLocation(location) }) {
                        UserIconsRow(it, onClickUserIcon)
                    }
                }
            }

        }
    }
    FriendLocationBottomSheet(bottomSheetIsVisible, sheetState, currentLocation){
         bottomSheetIsVisible = false
    }
}

private fun LazyListScope.SimpleCLocationCard(
    friendLocation: FriendLocation?,
    locationType: LocationType,
    onClickUserIcon: (IUser) -> Unit,
    text: @Composable () -> String

) {
    friendLocation?.friendList.let {
        if (it.isNullOrEmpty()) return@let
        item(key = locationType) {
            LocationTitle(text())
        }
        item(key = locationType.value) {
            UserIconsRow(it, onClickUserIcon)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.LocationTitle(
    text: String,
) {
    Text(
        modifier = Modifier.animateItemPlacement(),
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun UserIconsRow(
    friends: List<State<FriendData>>,
    onClickUserIcon: (IUser) -> Unit
) {
    if (friends.isEmpty()) return
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.LocationFriend(
    iconUrl: String,
    name: String,
    userStatus: UserStatus,
    onClickUserIcon: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClickUserIcon)
            .animateItemPlacement(),
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
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.LocationCard(
    location: FriendLocation,
    clickable: () -> Unit,
    content: @Composable (List<State<FriendData>>) -> Unit
) {
    val instants by location.instants
    val currentNavigator = currentNavigator
    var showUser by rememberSaveable(location.location) { mutableStateOf(false) }
    val friendList = location.friendList
    Surface(
//        modifier = Modifier.animateItemPlacement(),
        tonalElevation = (-2).dp,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .clickable(onClick = clickable)
                .padding(8.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(112.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AImage(
                    modifier = Modifier
                        .weight(0.5f)
                        .clip(RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 8.dp,
                            bottomStart = 16.dp,
                            bottomEnd = 8.dp
                        ))
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
                    modifier = Modifier
                        .weight(0.5f),
                ) {
                    Text(
                        text = instants.worldName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Row(
                        modifier = Modifier
                            .height(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AImage(
                            modifier = Modifier
                                .size(15.dp)
                                .align(Alignment.CenterVertically)
                                .clip(CircleShape)
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.surfaceContainerHighest,
                                    CircleShape
                                ),
                            imageData = instants.regionIconUrl
                        )
                        Text(
                            text = instants.accessType,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )

                    }
                    Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = instants.worldDescription,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp),
                    ) {
                        friendList.take(5).forEachIndexed { index, state ->
                            val friend by state
                            UserStateIcon(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .align(Alignment.CenterVertically)
                                    .offset(x = (-8 * index).dp)
                                    .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                iconUrl = friend.iconUrl,
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier.fillMaxHeight()
                                .background(
                                    MaterialTheme.colorScheme.inverseOnSurface,
                                    MaterialTheme.shapes.medium
                                )
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { showUser = !showUser }
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = instants.userCount,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
            AnimatedVisibility(showUser){
                content(friendList)
            }
        }
    }
}

