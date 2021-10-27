package github.leavesc.compose_chat.ui.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.accompanist.insets.imePadding
import com.google.accompanist.insets.navigationBarsPadding
import github.leavesc.compose_chat.base.model.ActionResult
import github.leavesc.compose_chat.base.model.Conversation
import github.leavesc.compose_chat.base.model.ServerState
import github.leavesc.compose_chat.cache.AccountCache
import github.leavesc.compose_chat.extend.navToC2CChatScreen
import github.leavesc.compose_chat.extend.navToGroupChatScreen
import github.leavesc.compose_chat.extend.navigateWithBack
import github.leavesc.compose_chat.logic.ComposeChat
import github.leavesc.compose_chat.logic.HomeViewModel
import github.leavesc.compose_chat.model.AppTheme
import github.leavesc.compose_chat.model.HomeDrawerViewState
import github.leavesc.compose_chat.model.HomeScreenTab
import github.leavesc.compose_chat.model.Screen
import github.leavesc.compose_chat.ui.conversation.ConversationScreen
import github.leavesc.compose_chat.ui.friend.FriendshipScreen
import github.leavesc.compose_chat.ui.person.PersonProfileScreen
import github.leavesc.compose_chat.ui.theme.BottomSheetShape
import github.leavesc.compose_chat.utils.log
import github.leavesc.compose_chat.utils.showToast
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * @Author: leavesC
 * @Date: 2021/7/4 1:04
 * @Desc:
 * @Github：https://github.com/leavesC
 */
@Composable
fun HomeScreen(
    navController: NavHostController,
    appTheme: AppTheme,
    switchToNextTheme: () -> Unit,
    screenSelected: HomeScreenTab,
    onTabSelected: (HomeScreenTab) -> Unit
) {
    val homeViewModel = viewModel<HomeViewModel>()
    log(log = "homeViewModel: $homeViewModel")
    LaunchedEffect(Unit) {
        launch {
            homeViewModel.serverConnectState.collect {
                when (it) {
                    ServerState.KickedOffline -> {
                        showToast("本账号已在其它客户端登陆，请重新登陆")
                        AccountCache.onUserLogout()
                        navController.navigateWithBack(
                            screen = Screen.LoginScreen
                        )
                    }
                    ServerState.Logout -> {
                        navController.navigateWithBack(
                            screen = Screen.LoginScreen
                        )
                    }
                    else -> {
                        showToast("Connect State Changed : $it")
                    }
                }
            }
        }
        homeViewModel.init()
    }
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scaffoldState = rememberScaffoldState(drawerState = drawerState)
    val sheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val conversationList by homeViewModel.conversationList.collectAsState()
    val totalUnreadCount by homeViewModel.totalUnreadCount.collectAsState()
    val friendList by homeViewModel.fiendList.collectAsState()
    val joinedGroupList by homeViewModel.joinedGroupList.collectAsState()
    val personProfile by homeViewModel.personProfile.collectAsState()

    val conversationListState = rememberLazyListState()
    val friendShipListState = rememberLazyListState()

    fun sheetContentAnimateTo(targetValue: ModalBottomSheetValue) {
        coroutineScope.launch {
            sheetState.animateTo(targetValue = targetValue)
        }
    }

    ModalBottomSheetLayout(
        modifier = Modifier.navigationBarsPadding(),
        sheetState = sheetState,
        sheetShape = BottomSheetShape,
        sheetContent = {
            HomeMoreActionScreen(
                modalBottomSheetState = sheetState,
                onAddFriend = {
                    homeViewModel.addFriend(userId = it)
                })
        }
    ) {
        Scaffold(
            scaffoldState = scaffoldState,
            snackbarHost = {
                SnackbarHost(it) { data ->
                    Snackbar(
                        modifier = Modifier.imePadding(),
                        backgroundColor = MaterialTheme.colors.primary,
                        elevation = 0.dp,
                        snackbarData = data,
                    )
                }
            },
            topBar = {
                HomeScreenTopBar(
                    screenSelected = screenSelected,
                    openDrawer = {
                        coroutineScope.launch {
                            scaffoldState.drawerState.open()
                        }
                    },
                    onAddFriend = {
                        sheetContentAnimateTo(targetValue = ModalBottomSheetValue.Expanded)
                    },
                    onJoinGroup = {
                        coroutineScope.launch {
                            val groupId = ComposeChat.groupId
                            when (val result = homeViewModel.joinGroup(groupId)) {
                                is ActionResult.Success -> {
                                    showToast("加入成功")
                                    navController.navToGroupChatScreen(groupId = groupId)
                                }
                                is ActionResult.Failed -> {
                                    showToast(result.reason)
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                HomeScreenBottomBar(
                    screenList = HomeScreenTab.values().toList(),
                    screenSelected = screenSelected,
                    unreadMessageCount = totalUnreadCount,
                    onTabSelected = onTabSelected
                )
            },
            drawerContent = {
                HomeDrawerScreen(
                    drawerState = drawerState,
                    homeDrawerViewState = HomeDrawerViewState(
                        appTheme = appTheme,
                        userProfile = personProfile,
                        switchToNextTheme = switchToNextTheme,
                        updateProfile = { faceUrl: String, nickname: String, signature: String ->
                            homeViewModel.updateProfile(
                                faceUrl = faceUrl,
                                nickname = nickname,
                                signature = signature
                            )
                        },
                        logout = {
                            homeViewModel.logout()
                        },
                    ),
                )
            },
            drawerShape = RoundedCornerShape(0.dp),
            floatingActionButton = {
                if (screenSelected == HomeScreenTab.Friendship) {
                    FloatingActionButton(
                        backgroundColor = MaterialTheme.colors.primary,
                        onClick = {
                            sheetContentAnimateTo(targetValue = ModalBottomSheetValue.Expanded)
                        }) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            tint = Color.White,
                            contentDescription = null,
                        )
                    }
                }
            },
        ) { paddingValues ->
            when (screenSelected) {
                HomeScreenTab.Conversation -> {
                    ConversationScreen(
                        listState = conversationListState,
                        paddingValues = paddingValues,
                        conversationList = conversationList,
                        onClickConversation = {
                            when (it) {
                                is Conversation.C2CConversation -> {
                                    navController.navToC2CChatScreen(friendId = it.id)
                                }
                                is Conversation.GroupConversation -> {
                                    navController.navToGroupChatScreen(groupId = it.id)
                                }
                            }
                        },
                        onDeleteConversation = {
                            homeViewModel.deleteConversation(it)
                        },
                        onConversationPinnedChanged = { conversation, pin ->
                            homeViewModel.pinConversation(
                                conversation = conversation,
                                pin = pin
                            )
                        }
                    )
                }
                HomeScreenTab.Friendship -> {
                    FriendshipScreen(
                        listState = friendShipListState,
                        paddingValues = paddingValues,
                        joinedGroupList = joinedGroupList,
                        friendList = friendList,
                        onClickGroup = {
                            navController.navToGroupChatScreen(groupId = it.id)
                        },
                        onClickFriend = {
                            navController.navigate(
                                route = Screen.FriendProfileScreen.generateRoute(friendId = it.userId)
                            )
                        },
                    )
                }
                HomeScreenTab.PersonProfile -> {
                    PersonProfileScreen(
                        personProfile = personProfile
                    )
                }
            }
        }
    }
}