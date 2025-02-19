package github.leavesczy.compose_chat.ui.friendship.logic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.leavesczy.compose_chat.common.model.ActionResult
import github.leavesczy.compose_chat.common.model.PersonProfile
import github.leavesczy.compose_chat.common.model.PrivateChat
import github.leavesczy.compose_chat.model.FriendProfilePageAction
import github.leavesczy.compose_chat.model.FriendProfilePageViewState
import github.leavesczy.compose_chat.model.SetFriendRemarkDialogViewState
import github.leavesczy.compose_chat.ui.chat.ChatActivity
import github.leavesczy.compose_chat.ui.main.logic.ComposeChat
import github.leavesczy.compose_chat.utils.ContextHolder
import github.leavesczy.compose_chat.utils.showToast
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * @Author: leavesCZY
 * @Date: 2021/7/3 19:54
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
class FriendProfileViewModel(private val friendId: String) : ViewModel() {

    private val _setFriendRemarkDialogViewState = MutableStateFlow(
        SetFriendRemarkDialogViewState(
            visible = false,
            personProfile = PersonProfile.Empty,
            onDismissRequest = ::dismissSetFriendRemarkDialog,
            setRemark = ::setFriendRemark
        )
    )

    private val _friendProfilePageState = MutableStateFlow(
        FriendProfilePageViewState(
            personProfile = PersonProfile.Empty,
            showAlterBtb = false,
            showAddBtn = false,
            navToChat = {
                ChatActivity.navTo(
                    context = ContextHolder.context,
                    chat = PrivateChat(id = friendId)
                )
                finishActivity()
            },
            addFriend = ::addFriend,
            deleteFriend = {
                deleteFriend()
            },
            showSetFriendRemarkPanel = ::showSetFriendRemarkPanel
        )
    )

    private val _friendProfilePageAction = MutableSharedFlow<FriendProfilePageAction>()

    val friendProfilePageState: StateFlow<FriendProfilePageViewState> = _friendProfilePageState

    val setFriendRemarkDialogViewState: StateFlow<SetFriendRemarkDialogViewState> =
        _setFriendRemarkDialogViewState

    val friendProfilePageAction: SharedFlow<FriendProfilePageAction> = _friendProfilePageAction

    fun getFriendProfile() {
        viewModelScope.launch {
            val profile =
                ComposeChat.friendshipProvider.getFriendProfile(friendId) ?: PersonProfile.Empty
            val showAlterBtb = profile.isFriend
            val showAddBtn = if (showAlterBtb) {
                false
            } else {
                val selfId = ComposeChat.accountProvider.personProfile.value.id
                selfId.isNotBlank() && selfId != friendId
            }
            _friendProfilePageState.emit(
                _friendProfilePageState.value.copy(
                    personProfile = profile,
                    showAlterBtb = showAlterBtb,
                    showAddBtn = showAddBtn
                )
            )
        }
    }

    private fun addFriend() {
        viewModelScope.launch {
            when (val result = ComposeChat.friendshipProvider.addFriend(friendId = friendId)) {
                is ActionResult.Success -> {
                    delay(timeMillis = 400)
                    showToast("添加成功")
                    getFriendProfile()
                }
                is ActionResult.Failed -> {
                    showToast(result.reason)
                }
            }
        }
    }

    private fun deleteFriend() {
        viewModelScope.launch {
            when (val result = ComposeChat.friendshipProvider.deleteFriend(friendId = friendId)) {
                is ActionResult.Success -> {
                    showToast("已删除好友")
                    finishActivity()
                }
                is ActionResult.Failed -> {
                    showToast(result.reason)
                }
            }
        }
    }

    private fun setFriendRemark(remark: String) {
        viewModelScope.launch {
            when (val result = ComposeChat.friendshipProvider.setFriendRemark(
                friendId = friendId,
                remark = remark
            )) {
                is ActionResult.Success -> {
                    showToast("设置成功")
                    getFriendProfile()
                    ComposeChat.friendshipProvider.getFriendList()
                    ComposeChat.conversationProvider.getConversationList()
                    dismissSetFriendRemarkDialog()
                }
                is ActionResult.Failed -> {
                    showToast(result.reason)
                }
            }
        }
    }

    private fun showSetFriendRemarkPanel() {
        viewModelScope.launch {
            _setFriendRemarkDialogViewState.emit(
                value = _setFriendRemarkDialogViewState.value.copy(
                    visible = true,
                    personProfile = friendProfilePageState.value.personProfile
                )
            )
        }
    }

    private fun dismissSetFriendRemarkDialog() {
        viewModelScope.launch {
            _setFriendRemarkDialogViewState.emit(
                value = _setFriendRemarkDialogViewState.value.copy(
                    visible = false
                )
            )
        }
    }

    private fun finishActivity() {
        viewModelScope.launch {
            _friendProfilePageAction.emit(value = FriendProfilePageAction.FinishActivity)
        }
    }

}