package com.feduss.telegramwear

import android.util.Log
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class ClientRepository constructor(val appDir: String) {
    private var phoneNumber: String = ""
    lateinit var client: Client
    private var authorizationState: AuthorizationState? = null
    @Volatile private var haveAuthorization = false
    @Volatile private var needQuit = false
    @Volatile private var canQuit = false
    private val authorizationLock: Lock = ReentrantLock()
    private val gotAuthorization: Condition = authorizationLock.newCondition()

    internal val users: ConcurrentMap<Long, User> = ConcurrentHashMap()
    internal val basicGroups: ConcurrentMap<Long, BasicGroup> = ConcurrentHashMap()
    internal val supergroups: ConcurrentMap<Long, Supergroup> = ConcurrentHashMap()
    internal val secretChats: ConcurrentMap<Int, SecretChat> = ConcurrentHashMap()

    internal val chats: ConcurrentMap<Long, Chat> = ConcurrentHashMap()
    private val mainChatList: NavigableSet<OrderedChat> = TreeSet()

    internal val usersFullInfo: ConcurrentMap<Long, UserFullInfo> = ConcurrentHashMap()
    internal val basicGroupsFullInfo: ConcurrentMap<Long, BasicGroupFullInfo> = ConcurrentHashMap()
    internal val supergroupsFullInfo: ConcurrentMap<Long, SupergroupFullInfo> = ConcurrentHashMap()

    internal fun setClient(phoneNumner: String) {
        this.client = Client.create(UpdateHandler(this), null, null)
        this.phoneNumber = phoneNumner
    }

    internal fun onAuthorizationStateUpdated(authorizationState: AuthorizationState?, placeholder: String = "") {
        if (authorizationState != null) {
            this.authorizationState = authorizationState
        }
        when (this.authorizationState?.constructor) {
            AuthorizationStateWaitTdlibParameters.CONSTRUCTOR -> {
                val parameters = TdlibParameters()
                parameters.databaseDirectory = appDir + "/TelegramWear"
                parameters.useMessageDatabase = true
                parameters.useSecretChats = true
                parameters.apiId = 94575  //TODO: to update
                parameters.apiHash = "a3406de8d171bb422bb6ddf3bbd800e2" //TODO: to update
                parameters.systemLanguageCode = "it"
                parameters.deviceModel = "Mobile"
                parameters.applicationVersion = "0.1"
                parameters.enableStorageOptimizer = true
                client.send(SetTdlibParameters(parameters), AuthorizationRequestHandler(this))
            }
            AuthorizationStateWaitEncryptionKey.CONSTRUCTOR -> client.send(
                CheckDatabaseEncryptionKey(),
                AuthorizationRequestHandler(this)
            )
            AuthorizationStateWaitPhoneNumber.CONSTRUCTOR -> {
                client.send(
                    SetAuthenticationPhoneNumber(this.phoneNumber, null),
                    AuthorizationRequestHandler(this)
                )
            }
            AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR -> {
                val link =
                    (this.authorizationState as AuthorizationStateWaitOtherDeviceConfirmation).link
                println("Please confirm this login link on another device: $link")
            }
            AuthorizationStateWaitCode.CONSTRUCTOR -> {
                val code: String = placeholder //promptString("Please enter authentication code: ")
                client.send(CheckAuthenticationCode(code), AuthorizationRequestHandler(this))
            }
            AuthorizationStateWaitRegistration.CONSTRUCTOR -> {
                val firstName: String = placeholder //promptString("Please enter your first name: ")
                val lastName: String = placeholder //promptString("Please enter your last name: ")
                client.send(RegisterUser(firstName, lastName), AuthorizationRequestHandler(
                    this
                )
                )
            }
            AuthorizationStateWaitPassword.CONSTRUCTOR -> {
                val password: String = placeholder //promptString("Please enter password: ")
                client.send(CheckAuthenticationPassword(password), AuthorizationRequestHandler(
                    this
                )
                )
            }
            AuthorizationStateReady.CONSTRUCTOR -> {
                haveAuthorization = true
                authorizationLock.lock()
                try {
                    gotAuthorization.signal()
                } finally {
                    authorizationLock.unlock()
                }
            }
            AuthorizationStateLoggingOut.CONSTRUCTOR -> {
                haveAuthorization = false
                print("Logging out")
            }
            AuthorizationStateClosing.CONSTRUCTOR -> {
                haveAuthorization = false
                print("Closing")
            }
            AuthorizationStateClosed.CONSTRUCTOR -> {
                print("Closed")
                if (!needQuit) {
                    client = Client.create(
                        UpdateHandler(this),
                        null,
                        null
                    ) // recreate client after previous has closed
                } else {
                    canQuit = true
                }
            }
            else -> System.err.println("Unsupported authorization state: \n" + this.authorizationState)
        }
    }

    internal fun setChatPositions(chat: Chat, positions: Array<ChatPosition?>) {
        synchronized(mainChatList) {
            synchronized(chat) {
                for (position in chat.positions) {
                    if (position.list.constructor == ChatListMain.CONSTRUCTOR) {
                        val isRemoved: Boolean =
                            mainChatList.remove(OrderedChat(chat.id, position))
                        assert(isRemoved)
                    }
                }
                chat.positions = positions
                for (position in chat.positions) {
                    if (position.list.constructor == ChatListMain.CONSTRUCTOR) {
                        val isAdded: Boolean = mainChatList.add(OrderedChat(chat.id, position))
                        assert(isAdded)
                    }
                }
            }
        }
    }
}

internal class AuthorizationRequestHandler(private val clientRepository: ClientRepository) : Client.ResultHandler {

    override fun onResult(tdApiObject: Object) {
        when (tdApiObject.constructor) {
            Error.CONSTRUCTOR -> {
                System.err.println("Receive an error:\n" + tdApiObject)
                this.clientRepository.onAuthorizationStateUpdated(null ) // repeat last action
            }
            Ok.CONSTRUCTOR -> {}
            else -> System.err.println("Receive wrong response from TDLib:\n" + tdApiObject)
        }
    }
}

private class UpdateHandler(private val clientRepository: ClientRepository) : Client.ResultHandler {

    override fun onResult(tdApiObject: Object) {
        Log.e("CONS123:", tdApiObject.constructor.toString())
        when (tdApiObject.constructor) {
            UpdateAuthorizationState.CONSTRUCTOR -> clientRepository.onAuthorizationStateUpdated((tdApiObject as UpdateAuthorizationState).authorizationState)
            UpdateUser.CONSTRUCTOR -> {
                val updateUser = tdApiObject as UpdateUser
                clientRepository.users[updateUser.user.id] = updateUser.user
            }
            UpdateUserStatus.CONSTRUCTOR -> {
                val updateUserStatus = tdApiObject as UpdateUserStatus
                val user: User? = clientRepository.users[updateUserStatus.userId]
                if (user != null) {
                    synchronized(user) { user.status = updateUserStatus.status }
                }
            }
            UpdateBasicGroup.CONSTRUCTOR -> {
                val updateBasicGroup = tdApiObject as UpdateBasicGroup
                clientRepository.basicGroups[updateBasicGroup.basicGroup.id] = updateBasicGroup.basicGroup
            }
            UpdateSupergroup.CONSTRUCTOR -> {
                val updateSupergroup = tdApiObject as UpdateSupergroup
                clientRepository.supergroups[updateSupergroup.supergroup.id] = updateSupergroup.supergroup
            }
            UpdateSecretChat.CONSTRUCTOR -> {
                val updateSecretChat = tdApiObject as UpdateSecretChat
                clientRepository.secretChats[updateSecretChat.secretChat.id] = updateSecretChat.secretChat
            }
            UpdateNewChat.CONSTRUCTOR -> {
                val updateNewChat = tdApiObject as UpdateNewChat
                val chat = updateNewChat.chat
                synchronized(chat) {
                    clientRepository.chats[chat.id] = chat
                    val positions = chat.positions
                    chat.positions = arrayOfNulls(0)
                    clientRepository.setChatPositions(chat, positions)
                }
            }
            UpdateChatTitle.CONSTRUCTOR -> {
                val updateChat = tdApiObject as UpdateChatTitle
                val chat: Chat? = clientRepository.chats[updateChat.chatId]
                if (chat != null) {
                    synchronized(chat) { chat.title = updateChat.title }
                }
            }
            UpdateChatPhoto.CONSTRUCTOR -> {
                val updateChat = tdApiObject as UpdateChatPhoto
                val chat: Chat? = clientRepository.chats[updateChat.chatId]
                if (chat != null) {
                    synchronized(chat) { chat.photo = updateChat.photo }
                }
            }
            UpdateChatLastMessage.CONSTRUCTOR -> {
                val updateChat = tdApiObject as UpdateChatLastMessage
                val chat: Chat? = clientRepository.chats[updateChat.chatId]
                if (chat != null) {
                    synchronized(chat) {
                        chat.lastMessage = updateChat.lastMessage
                        clientRepository.setChatPositions(chat, updateChat.positions)
                    }
                }
            }
            UpdateChatPosition.CONSTRUCTOR -> {
                val updateChat = tdApiObject as UpdateChatPosition
                if (updateChat.position.list.constructor != ChatListMain.CONSTRUCTOR) {
                    return //break
                }
                val chat: Chat? = clientRepository.chats[updateChat.chatId]
                if (chat != null) {
                    synchronized(chat) {
                        var i: Int
                        i = 0
                        while (i < chat.positions.size) {
                            if (chat.positions[i].list.constructor == ChatListMain.CONSTRUCTOR) {
                                break
                            }
                            i++
                        }
                        val new_positions =
                            arrayOfNulls<ChatPosition>(chat.positions.size + (if (updateChat.position.order == 0L) 0 else 1) - if (i < chat.positions.size) 1 else 0)
                        var pos = 0
                        if (updateChat.position.order != 0L) {
                            new_positions[pos++] = updateChat.position
                        }
                        var j = 0
                        while (j < chat.positions.size) {
                            if (j != i) {
                                new_positions[pos++] = chat.positions[j]
                            }
                            j++
                        }
                        assert(pos == new_positions.size)
                        clientRepository.setChatPositions(chat, new_positions)
                    }
                }
            }
            UpdateChatReadInbox.CONSTRUCTOR -> {
                val updateChat = tdApiObject as UpdateChatReadInbox
                val chat: Chat? = clientRepository.chats[updateChat.chatId]
                if (chat != null) {
                    synchronized(chat) {
                        chat.lastReadInboxMessageId = updateChat.lastReadInboxMessageId
                        chat.unreadCount = updateChat.unreadCount
                    }
                }
            }
            UpdateChatReadOutbox.CONSTRUCTOR -> {
                val updateChat = tdApiObject as UpdateChatReadOutbox
                val chat: Chat? = clientRepository.chats[updateChat.chatId]
                if (chat != null) {
                    synchronized(chat) {
                        chat.lastReadOutboxMessageId = updateChat.lastReadOutboxMessageId
                    }
                }
            }
            UpdateChatUnreadMentionCount.CONSTRUCTOR -> {
                val updateChat = tdApiObject as UpdateChatUnreadMentionCount
                val chat: Chat? = clientRepository.chats[updateChat.chatId]
                if (chat != null) {
                    synchronized(chat) { chat.unreadMentionCount = updateChat.unreadMentionCount }
                }
            }
            UpdateMessageMentionRead.CONSTRUCTOR -> {
                val updateChat = tdApiObject as UpdateMessageMentionRead
                val chat: Chat? = clientRepository.chats[updateChat.chatId]
                if (chat != null) {
                    synchronized(chat) { chat.unreadMentionCount = updateChat.unreadMentionCount }
                }
            }
            UpdateChatReplyMarkup.CONSTRUCTOR -> {
                val updateChat = tdApiObject as UpdateChatReplyMarkup
                val chat: Chat? = clientRepository.chats[updateChat.chatId]
                if (chat != null) {
                    synchronized(chat) { chat.replyMarkupMessageId = updateChat.replyMarkupMessageId }
                }
            }
            UpdateChatDraftMessage.CONSTRUCTOR -> {
                val updateChat = tdApiObject as UpdateChatDraftMessage
                val chat: Chat? = clientRepository.chats[updateChat.chatId]
                if (chat != null) {
                    synchronized(chat) {
                        chat.draftMessage = updateChat.draftMessage
                        clientRepository.setChatPositions(chat, updateChat.positions)
                    }
                }
            }
            UpdateChatPermissions.CONSTRUCTOR -> {
                val update = tdApiObject as UpdateChatPermissions
                val chat: Chat? = clientRepository.chats[update.chatId]
                if (chat != null) {
                    synchronized(chat) { chat.permissions = update.permissions }
                }
            }
            UpdateChatNotificationSettings.CONSTRUCTOR -> {
                val update = tdApiObject as UpdateChatNotificationSettings
                val chat: Chat? = clientRepository.chats[update.chatId]
                if (chat != null) {
                    synchronized(chat) { chat.notificationSettings = update.notificationSettings }
                }
            }
            UpdateChatDefaultDisableNotification.CONSTRUCTOR -> {
                val update = tdApiObject as UpdateChatDefaultDisableNotification
                val chat: Chat? = clientRepository.chats[update.chatId]
                if (chat != null) {
                    synchronized(chat) {
                        chat.defaultDisableNotification = update.defaultDisableNotification
                    }
                }
            }
            UpdateChatIsMarkedAsUnread.CONSTRUCTOR -> {
                val update = tdApiObject as UpdateChatIsMarkedAsUnread
                val chat: Chat? = clientRepository.chats[update.chatId]
                if (chat != null) {
                    synchronized(chat) { chat.isMarkedAsUnread = update.isMarkedAsUnread }
                }
            }
            UpdateChatIsBlocked.CONSTRUCTOR -> {
                val update = tdApiObject as UpdateChatIsBlocked
                val chat: Chat? = clientRepository.chats[update.chatId]
                if (chat != null) {
                    synchronized(chat) { chat.isBlocked = update.isBlocked }
                }
            }
            UpdateChatHasScheduledMessages.CONSTRUCTOR -> {
                val update = tdApiObject as UpdateChatHasScheduledMessages
                val chat: Chat? = clientRepository.chats[update.chatId]
                if (chat != null) {
                    synchronized(chat) { chat.hasScheduledMessages = update.hasScheduledMessages }
                }
            }
            UpdateUserFullInfo.CONSTRUCTOR -> {
                val updateUserFullInfo = tdApiObject as UpdateUserFullInfo
                clientRepository.usersFullInfo[updateUserFullInfo.userId] = updateUserFullInfo.userFullInfo
            }
            UpdateBasicGroupFullInfo.CONSTRUCTOR -> {
                val updateBasicGroupFullInfo = tdApiObject as UpdateBasicGroupFullInfo
                clientRepository.basicGroupsFullInfo[updateBasicGroupFullInfo.basicGroupId] =
                    updateBasicGroupFullInfo.basicGroupFullInfo
            }
            UpdateSupergroupFullInfo.CONSTRUCTOR -> {
                val updateSupergroupFullInfo = tdApiObject as UpdateSupergroupFullInfo
                clientRepository.supergroupsFullInfo[updateSupergroupFullInfo.supergroupId] =
                    updateSupergroupFullInfo.supergroupFullInfo
            }
            else -> {}
        }
    }
}

private class OrderedChat constructor(val chatId: Long, val position: ChatPosition) :
    Comparable<OrderedChat?> {
    override fun compareTo(other: OrderedChat?): Int {
        if (other != null) {
            if (position.order != other.position.order) {
                return if (other.position.order < position.order) -1 else 1
            }

            return if (chatId != other.chatId) {
                if (other.chatId < chatId) -1 else 1
            } else 0
        }
        return 0 //TODO: to check
    }

    override fun equals(obj: Any?): Boolean {
        val o = obj as OrderedChat?
        return chatId == o!!.chatId && position.order == o.position.order
    }


}
