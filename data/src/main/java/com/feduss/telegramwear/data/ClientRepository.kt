package com.feduss.telegramwear.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.feduss.telegram.entity.LoadChatResult
import com.feduss.telegram.entity.LoggedUser
import com.feduss.telegram.entity.QrCodeResult
import com.feduss.telegram.entity.consts.ChatHistoryMessageType
import com.feduss.telegram.entity.consts.LastMessageType
import com.feduss.telegram.entity.consts.MessageDeletionOption
import com.feduss.telegram.entity.consts.MessageSendState
import com.feduss.telegram.entity.consts.TdLibParam
import com.feduss.telegram.entity.model.ChatHistoryItemModel
import com.feduss.telegram.entity.model.ChatListItemModel
import com.feduss.telegram.entity.model.MessageAuthor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import org.drinkless.td.libcore.telegram.Client
import org.drinkless.td.libcore.telegram.TdApi
import org.drinkless.td.libcore.telegram.TdApi.SetLogVerbosityLevel
import org.drinkless.td.libcore.telegram.TdApi.UpdateChatTitle
import org.drinkless.td.libcore.telegram.TdApi.UserStatusOnline
import java.io.File
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject


interface ClientRepository {
    fun logout(): Flow<Boolean>
    fun sendOTP(phoneNumber: String): Flow<Boolean>
    fun checkOTP(otp: String): Flow<Boolean>
    fun fetchQRCodeLink(): Flow<QrCodeResult>
    fun requestQrCode(): Flow<QrCodeResult>
    fun checkPassword(password: String): Flow<Boolean>
    fun getAuthStatus(): Flow<Int>
    fun getMe(): Flow<LoggedUser?>
    fun requestChats(limit: Int): Flow<LoadChatResult>
    fun getChatModels(): Flow<ArrayList<ChatListItemModel>>

    fun getChatHistory(chatId: Long, lastMessageId: Long, fetchLimit: Int): Flow<ArrayList<ChatHistoryItemModel>>
}

class ClientRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context
): ClientRepository {
    private lateinit var client: Client
    private var authStatus = MutableStateFlow(-1)
    private var loggedUser: LoggedUser? = null

    private var chats = mapOf<Long, TdApi.Chat>()
    private var chatsFlow = Channel<Map<Long, TdApi.Chat>>(
    )

    private var chatIdForChatPhotoId = HashMap<Int, Long>()

    private var userOnlineStatus = mapOf<Long, Boolean?>()

    private var userOnlineStatusFlow = Channel<Map<Long, Boolean?>>()

    private var userInfo = HashMap<Long, TdApi.User>()

    private var prevHistoryChatId = -1L
    private var prevChatHistory = listOf<TdApi.Message>()


    init {
        val appDir = context.getExternalFilesDir(null).toString()
        val dir = File(appDir + "TelegramWear/tdlib")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        setupHandler(appDir)
    }

    // Init fun

    private fun setupHandler(appDir: String) {
        client = Client.create(
            //updateHandler
            { tdApiObject ->
                handleUpdate(tdApiObject, appDir)
            },
            //updateExceptionHandler
            {
                Log.e("LogTest: ", "updateExceptionHandler: ${it.localizedMessage}")
            },
            //defaultExceptionHandler
            {
                Log.e("LogTest: ", "defaultExceptionHandler: ${it.localizedMessage}")
            }
        )

    }

    private fun handleUpdate(
        tdApiObject: TdApi.Object?,
        appDir: String
    ) {
        //Log.i("Status update", " --> $tdApiObject")
        when (tdApiObject) {
            is TdApi.UpdateAuthorizationState -> {
                val authState = tdApiObject.authorizationState
                authStatus.value = authState.constructor
                if (authState.constructor == TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR) {
                    Log.i("LogTest: ", "Client created")
                    setTdLibParams(appDir)
                }
            }

            is TdApi.UpdateFile -> {

                //TODO: temp impl
                if (tdApiObject.file.local.path.contains("profile_photos")) {
                    updateChats { newChats ->
                        val chatId = chatIdForChatPhotoId[tdApiObject.file.id]
                        newChats[chatId]?.photo?.small = tdApiObject.file
                        newChats
                    }
                }
            }

            is TdApi.UpdateNewChat -> {
                val chatPhotoId = tdApiObject.chat.photo?.small?.id ?: 0

                chatIdForChatPhotoId[chatPhotoId] = tdApiObject.chat.id

                client.send(
                    TdApi.DownloadFile(
                        chatPhotoId,
                        1,
                        0,
                        0,
                        true
                    )
                ) {}

                updateChats { newChats ->
                    newChats[tdApiObject.chat.id] = tdApiObject.chat
                    newChats
                }
            }

            is TdApi.UpdateChatPhoto -> {

                updateChats { newChats ->
                    newChats[tdApiObject.chatId]?.photo = tdApiObject.photo
                    newChats
                }
            }

            is UpdateChatTitle -> {

                updateChats { newChats ->
                    newChats[tdApiObject.chatId]?.title = tdApiObject.title
                    newChats
                }
            }

            is TdApi.UpdateChatLastMessage -> {

                updateChats { newChats ->
                    newChats[tdApiObject.chatId]?.lastMessage = tdApiObject.lastMessage
                    newChats
                }
            }

            is TdApi.UpdateChatPosition -> {

                updateChats { newChats ->
                    val newPosition = tdApiObject.position

                    if (newPosition.order == 0L){
                        newChats.remove(tdApiObject.chatId)
                    } else {
                        val oldPositions = chats[tdApiObject.chatId]?.positions
                        val newSize = (oldPositions?.size ?: 0) + 1
                        val newPositions = Array<TdApi.ChatPosition>(newSize) { newPosition }
                        oldPositions?.iterator()?.withIndex()?.forEach { item ->
                            newPositions[item.index + 1] = item.value
                        }

                        newChats[tdApiObject.chatId]?.positions = newPositions
                    }

                    newChats
                }
            }

            is TdApi.UpdateChatNotificationSettings -> {

                updateChats { newChats ->
                    newChats[tdApiObject.chatId]?.notificationSettings = tdApiObject.notificationSettings
                    newChats
                }
            }

            is TdApi.UpdateNewMessage -> {

                updateChats { newChats ->
                    newChats[tdApiObject.message.chatId]?.lastMessage = tdApiObject.message
                    newChats
                }
            }

            is TdApi.UpdateMessageEdited -> { /*Handle in other state */ }

            is TdApi.UpdateMessageContent -> {
                updateChats { newChats ->
                    newChats[tdApiObject.chatId]?.lastMessage?.content = tdApiObject.newContent
                    newChats
                }
            }

            is TdApi.UpdateDeleteMessages -> { /*Handle in other state */ }

            is TdApi.UpdateUserStatus -> {

                updateUsersStatus { newUsersStatus ->
                    newUsersStatus[tdApiObject.userId] = tdApiObject.status is UserStatusOnline
                    newUsersStatus
                }
            }

            is TdApi.UpdateChatDraftMessage -> {

                updateChats { newChats ->

                    newChats[tdApiObject.chatId]?.draftMessage = tdApiObject.draftMessage
                    newChats
                }
            }

            else -> {
                //Log.i("OtherStatus", " --> $tdApiObject")
            }
        }
    }

    private fun updateChats(updateHandler: (MutableMap<Long, TdApi.Chat>) -> MutableMap<Long, TdApi.Chat>) {

        val updatedChats = updateHandler(chats.toMutableMap())
        chats = updatedChats
        chatsFlow.trySend(updatedChats)
        userOnlineStatusFlow.trySend(userOnlineStatus)


    }

    private fun updateUsersStatus(updateHandler: (MutableMap<Long, Boolean?>) -> MutableMap<Long, Boolean?>) {

        val updatedUsersStatus = updateHandler(userOnlineStatus.toMutableMap())
        userOnlineStatus = updatedUsersStatus
        userOnlineStatusFlow.trySend(updatedUsersStatus)
        chatsFlow.trySend(chats)
    }

    private fun setTdLibParams(appDir: String) {
        val parameters = TdApi.TdlibParameters()
        parameters.useTestDc = false
        parameters.databaseDirectory = "$appDir/TelegramWear"
        parameters.useMessageDatabase = true
        parameters.useSecretChats = true
        parameters.apiId = Integer.parseInt(TdLibParam.ApiId.value)
        parameters.apiHash = TdLibParam.ApiHash.value
        parameters.systemLanguageCode = "it"
        parameters.deviceModel = "Mobile"
        parameters.applicationVersion = "0.1"
        parameters.enableStorageOptimizer = true
        client.send(TdApi.SetTdlibParameters(parameters)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("LogTest: ", "tdlib params set")
                checkDBEncryptionKey()
            } else {
                Log.e("LogTest: ", "Can't set tdlib params")
            }
        }

        client.send(SetLogVerbosityLevel(0)) {}
    }

    private fun checkDBEncryptionKey() {
        client.send(TdApi.CheckDatabaseEncryptionKey()) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("LogTest: ", "Database encryption key checked")
            } else {
                Log.e("LogTest: ", "Can't check database encryption key")
            }
        }
    }


    override fun logout(): Flow<Boolean> = flow {
        val completionDeferred = CompletableDeferred<Boolean>()
        client.send(TdApi.LogOut()) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("LogTest: ", "Logout completed")
                completionDeferred.complete(true)
            } else {
                Log.e("LogTest: ", "Logout error")
                completionDeferred.complete(false)
            }
        }

        emit(completionDeferred.await())
    }

    // OTP

    override fun sendOTP(phoneNumber: String): Flow<Boolean> = flow {
        val completionDeferred = CompletableDeferred<Boolean>()
        client.send(TdApi.SetAuthenticationPhoneNumber(phoneNumber, null)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("LogTest: ", "Otp sent")
                completionDeferred.complete(true)
            } else {
                Log.e("LogTest: ", "Otp error")
                completionDeferred.complete(false)
            }
        }

        emit(completionDeferred.await())
    }

    override fun checkOTP(otp: String): Flow<Boolean> = flow {
        val completionDeferred = CompletableDeferred<Boolean>()

        client.send(TdApi.CheckAuthenticationCode(otp)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                completionDeferred.complete(true)
            } else if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("LogTest: ", "Wrong otp")
                completionDeferred.complete(false)
            }
        }

        emit(completionDeferred.await())
    }

    // Qr code

    override fun fetchQRCodeLink() = flow {
        val completionDeferred = CompletableDeferred<QrCodeResult>()
        client.send(TdApi.GetAuthorizationState()) { tdApiObject ->
            //Pending/prev qr code request
            if (tdApiObject.constructor == TdApi.AuthorizationStateWaitOtherDeviceConfirmation.CONSTRUCTOR) {
                val prevValidQrCode = getQrCodeResponse(tdApiObject)
                if(prevValidQrCode != null) {
                    completionDeferred.complete(QrCodeResult.ValidQrCode(prevValidQrCode))
                } else {
                    completionDeferred.complete(QrCodeResult.Error)
                }
            } else {
                completionDeferred.complete(QrCodeResult.Error)
            }
        }
        emit(completionDeferred.await())
    }

    override fun requestQrCode() = flow {
        val completionDeferred = CompletableDeferred<QrCodeResult>()
        client.send(TdApi.RequestQrCodeAuthentication()) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                val qrCodeLink = getQrCodeResponse(tdApiObject)
                if (qrCodeLink != null) {
                    completionDeferred.complete(
                        QrCodeResult.ValidQrCode(qrCodeLink)
                    )
                } else {
                    completionDeferred.complete(QrCodeResult.Error)
                }
            } else if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("LogTest: ", "Can't generate qr code")
                completionDeferred.complete(QrCodeResult.Error)
            }

        }
        emit(completionDeferred.await())
    }

    private fun getQrCodeResponse(tdApiObject: TdApi.Object?): String? {
        return if(tdApiObject is TdApi.AuthorizationStateWaitOtherDeviceConfirmation) {
            Log.i("LogTest: ", "Qr code generated")
            tdApiObject.link
        } else {
            null
        }
    }

    //2FA

    override fun checkPassword(password: String) = flow {
        val completionDeferred = CompletableDeferred<Boolean>()
        client.send(TdApi.CheckAuthenticationPassword(password)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                completionDeferred.complete(true)
            } else if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                Log.e("LogTest: ", "Wrong password")
                completionDeferred.complete(false)
            }
        }
        emit(completionDeferred.await())
    }

    //

    override fun getAuthStatus(): Flow<Int> {
        return authStatus
    }

    // Me

    override fun getMe() = flow {
        val completionDeferred = CompletableDeferred<LoggedUser?>()
        client.send(TdApi.GetMe()) { tdApiObject ->
            if (tdApiObject is TdApi.User) {
                loggedUser = if (tdApiObject != null) {
                    LoggedUser(
                        id = tdApiObject.id,
                        username = tdApiObject.username
                    )
                } else {
                    null
                }
                completionDeferred.complete(loggedUser)
            } else {
                completionDeferred.complete(null)
            }
        }

        emit(completionDeferred.await())
    }

    // Chat list

    override fun requestChats(limit: Int) = flow {
        val completionDeffered = CompletableDeferred<LoadChatResult>()

        chatsFlow.send(mapOf())
        userOnlineStatusFlow.send(mapOf())

        requestChatOfType(
            TdApi.ChatListMain(),
            limit,
            completionDeffered
        )


        emit(completionDeffered.await())
    }

    private fun requestChatOfType(
        chatList: TdApi.ChatList,
        limit: Int,
        completionDeferred: CompletableDeferred<LoadChatResult>
    ) {
        client.send(TdApi.LoadChats(chatList, limit)) { tdApiObject ->
            if (tdApiObject.constructor == TdApi.Error.CONSTRUCTOR) {
                if (tdApiObject is TdApi.Error && tdApiObject.code == 404) {
                    Log.e("LogTest: ", "retrieveChat error 404 --> ${tdApiObject.message}")
                    completionDeferred.complete(LoadChatResult.NoMoreChat)
                } else {
                    Log.e("LogTest: ", "retrieveChat error other --> $tdApiObject")
                    completionDeferred.complete(LoadChatResult.LoadingError)
                }
            } else if (tdApiObject.constructor == TdApi.Ok.CONSTRUCTOR) {
                Log.i("LogTest: ", "retrieveChat downloading chats")
                requestChatOfType(
                    chatList = chatList,
                    limit = limit,
                    completionDeferred = completionDeferred
                )
                //completionDeferred.complete(LoadChatResponse.ChatUpdated)
            } else {
                Log.e("LogTest: ", "retrieveChat error --> $tdApiObject")
            }
        }
    }

    override fun getChatModels(): Flow<ArrayList<ChatListItemModel>> {

        return chatsFlow.receiveAsFlow().combine(userOnlineStatusFlow.receiveAsFlow()) { chats, usersStatus ->

            val orders = chats.values.associate {
                    chat -> chat.id to chat.positions.firstOrNull() {
                    position -> position.order > 0
                }?.order
            }

            val resultChats = chats.values.map { chat ->

                val isPinned = chat.positions.count { it.isPinned } != 0
                val isMuted = chat.notificationSettings.muteFor != 0
                val unreadMessagesCount = chat.unreadCount

                val lastMessageType: LastMessageType = getChatListLastMessageType(chat)

                val chatListItemModel = ChatListItemModel(
                    id = chat.id,
                    imagePath = chat.photo?.small?.local?.path,
                    personName = chat.title.trim(),
                    lastMessageId = (chat.lastMessage?.id ?: 0).toString(),
                    lastMessageType = lastMessageType,
                    lastMessageDateTimestamp = chat.lastMessage?.date?.toLong(),
                    unreadMessagesCount = unreadMessagesCount,
                    isPinned = isPinned,
                    isMuted = isMuted,
                    isOnline = usersStatus[chat.id] == true,
                    hasOnlineStatus = chat.id > 0L,
                    orderId = orders[chat.id] ?: 0
                )

                chatListItemModel
            }

            ArrayList(resultChats)
        }
    }

    private fun getChatListLastMessageType(chat: TdApi.Chat): LastMessageType {
        val lastMessageType: LastMessageType

        val draftMessage = chat.draftMessage
        if (draftMessage != null) {
            val inputMessageText = draftMessage.inputMessageText as TdApi.InputMessageText
            lastMessageType = LastMessageType.Draft(text = inputMessageText.text.text)
        } else {
            when (val chatContent = chat.lastMessage?.content) {
                is TdApi.MessageText -> {
                    lastMessageType = LastMessageType.Text(text = chatContent.text.text)
                }

                is TdApi.MessageDocument -> {
                    lastMessageType =
                        LastMessageType.Document(filename = chatContent.document.fileName)
                }

                is TdApi.MessagePhoto -> {
                    lastMessageType = LastMessageType.Photo
                }

                is TdApi.MessageVideo -> {
                    lastMessageType = LastMessageType.Video
                }

                is TdApi.MessageVoiceNote -> {
                    lastMessageType = LastMessageType.VoiceNote
                }

                is TdApi.MessageAnimation -> {
                    lastMessageType = LastMessageType.Animation
                }

                is TdApi.MessageSticker -> {
                    val stickerEmoji = chatContent.sticker.emoji
                    lastMessageType = LastMessageType.Sticker(emoji = stickerEmoji)
                }

                is TdApi.MessageAnimatedEmoji -> {
                    val stickerEmoji = chatContent.emoji
                    lastMessageType = LastMessageType.AnimatedEmoji(emoji = stickerEmoji)
                }

                else -> {
                    lastMessageType = LastMessageType.Other
                }
            }
        }
        return lastMessageType
    }

    override fun getChatHistory(chatId: Long, lastMessageId: Long, fetchLimit: Int) = flow {
        val completionDeferred = CompletableDeferred<ArrayList<ChatHistoryItemModel>>()
        client.send(
            TdApi.GetChatHistory(
                chatId,
                lastMessageId,
                0,
                fetchLimit,
                false
            )
        ) { tdApiObject ->

            if (tdApiObject is TdApi.Messages) {
                getUsersInfo(tdApiObject) {
                    populateChatHistory(chatId, tdApiObject, completionDeferred)
                }
            } else {
                completionDeferred.complete(ArrayList())
            }
        }

        emit(completionDeferred.await())
    }

    private fun getUsersInfo(
        tdApiObject: TdApi.Messages,
        completion: () -> Unit
    ) {
        val senderIds = tdApiObject.messages.mapNotNull { message ->
            val messageSenderUser = message.senderId as? TdApi.MessageSenderUser
            val id = messageSenderUser?.userId

            if (!userInfo.values.map { it.id }.contains(id)) {
                return@mapNotNull id
            } else {
                return@mapNotNull null
            }
        }

        val forwardsUserIds = tdApiObject.messages.mapNotNull { message ->
            val forwardOrigin = message.forwardInfo?.origin as? TdApi.MessageForwardOriginUser
            val id = forwardOrigin?.senderUserId

            if (!userInfo.values.map { it.id }.contains(id)) {
                return@mapNotNull id
            } else {
                return@mapNotNull null
            }
        }

        val forwardsChatIds = tdApiObject.messages.mapNotNull { message ->
            val forwardOrigin = message.forwardInfo?.origin as? TdApi.MessageForwardOriginChannel

            if (forwardOrigin != null) {
                val a = 0
            }

            return@mapNotNull forwardOrigin?.chatId
        }

        val userIds = senderIds + forwardsUserIds

        userIds.forEach { userId ->
            retrieveUserById(userId) {
                if (userInfo.values.map { it.id }.containsAll(userIds)) {
                    completion()
                }
            }
        }
    }

    private fun retrieveUserById(id: Long, completion: () -> Unit) {
        client.send(TdApi.GetUser(id)) { userObject ->
            if (userObject is TdApi.User) {
                userInfo[id] = userObject
            }

            completion()
        }
    }

    private fun populateChatHistory(
        chatId: Long,
        tdApiObject: TdApi.Messages,
        completionDeferred: CompletableDeferred<ArrayList<ChatHistoryItemModel>>
    ) {

        val chatHistory = tdApiObject.messages.asList()

        if (chatId == prevHistoryChatId) {
            prevChatHistory += chatHistory
        } else {
            prevChatHistory = chatHistory
            prevHistoryChatId = chatId
        }

        val models = prevChatHistory.map { rawMessage ->

            val chatContent = rawMessage.content
            val messageType: ChatHistoryMessageType =
                getChatHistoryMessageType(chatContent)

            //TODO: to test
            val quotedMessageRaw =
                prevChatHistory.firstOrNull { it.id == rawMessage.replyToMessageId }
            val quotedMessage = if (quotedMessageRaw != null) {
                getChatHistoryMessageType(quotedMessageRaw.content)
            } else {
                null
            }

            val quotedMessageAuthorId = quotedMessageRaw?.senderId
            val quotedMessageAuthor = if (quotedMessageAuthorId is TdApi.MessageSenderUser) {
                val user = userInfo.values.firstOrNull { it.id == quotedMessageAuthorId.userId }
                MessageAuthor(
                    id = user?.id ?: 0L,
                    name = "${user?.firstName} ${user?.lastName}"
                )
            } else {
                null
            }

            val senderId = rawMessage.senderId
            val author = if (senderId is TdApi.MessageSenderUser) {
                val user = userInfo.values.firstOrNull { it.id == senderId.userId }
                MessageAuthor(
                    id = user?.id ?: 0L,
                    name = "${user?.firstName} ${user?.lastName}"
                )
            } else {
                null
            }

            val datetime = Instant.ofEpochSecond(rawMessage.date.toLong())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()

            val sendState = when (rawMessage.sendingState) {

                is TdApi.MessageSendingStatePending -> {
                    MessageSendState.Pending
                }

                is TdApi.MessageSendingStateFailed -> {
                    MessageSendState.Failed
                }

                else -> {
                    null
                }
            }

            var forwardAuthor: String? = null
            val forwardInfoOrigin = rawMessage.forwardInfo?.origin

            if (forwardInfoOrigin is TdApi.MessageForwardOriginUser) {
                forwardAuthor = userInfo[forwardInfoOrigin.senderUserId]?.username
            }

            val messageDeletionOption =
                if (rawMessage.canBeDeletedOnlyForSelf) {
                    MessageDeletionOption.OnlyForMe
                } else if (rawMessage.canBeDeletedForAllUsers) {
                    MessageDeletionOption.ForAll
                } else {
                    MessageDeletionOption.None
                }

            ChatHistoryItemModel(
                id = rawMessage.id,
                type = messageType,
                quotedMessageAuthor = quotedMessageAuthor,
                quotedMessage = quotedMessage,
                author = author,
                datetime = datetime,
                sendState = sendState,
                isPinned = rawMessage.isPinned,
                isEdited = rawMessage.editDate > 0, //TODO: to test
                canBeEdited = rawMessage.canBeEdited,
                isRead = false, //TOOD: to impl
                forwardAuthor = forwardAuthor,
                canBeForward = rawMessage.canBeForwarded,
                messageDeletionOption = messageDeletionOption,
                isChannelPost = rawMessage.isChannelPost
            )
        }
        completionDeferred.complete(
            ArrayList(models)
        )
    }

    private fun getChatHistoryMessageType(chatContent: TdApi.MessageContent?) =
        when (chatContent) {
            is TdApi.MessageText -> {
                ChatHistoryMessageType.Text(
                    message = chatContent.text.text.trim()
                )
            }

            is TdApi.MessageDocument -> {

                ChatHistoryMessageType.Document(
                    fileName = chatContent.document.fileName,
                    filePath = chatContent.document.document.local.path,
                    remoteId = chatContent.document.document.remote.id
                )
            }

            is TdApi.MessagePhoto -> {

                val thumbnailByteArray = chatContent.photo.minithumbnail?.data
                var thumbnail: Bitmap? = null

                if (thumbnailByteArray != null) {
                    thumbnail = BitmapFactory.decodeByteArray(
                        thumbnailByteArray,
                        0, thumbnailByteArray.size
                    )
                }

                val photoPath = chatContent.photo.sizes.first().photo.local.path
                var photo: Bitmap? = null

                if (photoPath != null) {
                    photo = BitmapFactory.decodeFile(photoPath)
                }

                if (thumbnail != null) {
                    ChatHistoryMessageType.Photo(
                        photoBitmap = photo,
                        caption = chatContent.caption.text,
                        filePath = photoPath, //TODO: select correct size
                        remoteId = chatContent.photo.sizes.first().photo.remote.id, //TODO: select correct size,
                        thumbnail = thumbnail
                    )
                } else {
                    ChatHistoryMessageType.Other
                }


            }

            is TdApi.MessageVideo -> {

                val thumbnailByteArray = chatContent.video.minithumbnail?.data
                var thumbnail: Bitmap? = null

                if (thumbnailByteArray != null) {
                    thumbnail = BitmapFactory.decodeByteArray(
                        thumbnailByteArray,
                        0, thumbnailByteArray.size
                    )
                }

                if (thumbnail != null) {
                    ChatHistoryMessageType.Video(
                        caption = "Video: ${chatContent.caption.text}",
                        filePath = chatContent.video.video.local.path,
                        remoteId = chatContent.video.video.remote.id,
                        duration = chatContent.video.duration,
                        thumbnail = thumbnail
                    )
                } else {
                    ChatHistoryMessageType.Other
                }
            }

            is TdApi.MessageVoiceNote -> {

                ChatHistoryMessageType.Audio(
                    caption = "Audio: ${chatContent.caption.text}",
                    filePath = chatContent.voiceNote.voice.local.path,
                    remoteId = chatContent.voiceNote.voice.remote.id,
                    duration = chatContent.voiceNote.duration
                )
            }

            is TdApi.MessageAnimation -> {

                val thumbnailByteArray =
                    chatContent.animation.minithumbnail?.data
                var thumbnail: Bitmap? = null

                if (thumbnailByteArray != null) {
                    thumbnail = BitmapFactory.decodeByteArray(
                        thumbnailByteArray,
                        0, thumbnailByteArray.size
                    )
                }

                if (thumbnail != null) {
                    ChatHistoryMessageType.Video(
                        caption = "GIF: ${chatContent.caption.text}",
                        filePath = chatContent.animation.animation.local.path,
                        remoteId = chatContent.animation.animation.remote.id,
                        duration = chatContent.animation.duration,
                        thumbnail = thumbnail
                    )
                } else {
                    ChatHistoryMessageType.Other
                }
            }

            is TdApi.MessageSticker -> {

                val thumbnailPath =
                    chatContent.sticker.thumbnail?.file?.local?.path
                var thumbnail: Bitmap? = null

                if (thumbnailPath != null) {
                    thumbnail = BitmapFactory.decodeFile(thumbnailPath)
                }

                val photoPath = chatContent.sticker.sticker.local.path
                var photo: Bitmap? = null

                if (photoPath != null) {
                    photo = BitmapFactory.decodeFile(photoPath)
                }

                if (thumbnail != null) {
                    ChatHistoryMessageType.Photo(
                        photoBitmap = photo,
                        caption = "${chatContent.sticker.emoji} Sticker",
                        filePath = photoPath,
                        remoteId = chatContent.sticker.sticker.remote.id,
                        thumbnail = thumbnail
                    )
                } else {
                    ChatHistoryMessageType.Other
                }
            }

            is TdApi.MessageAnimatedEmoji -> {

                val thumbnailPath =
                    chatContent.animatedEmoji.sticker.thumbnail?.file?.local?.path
                var thumbnail: Bitmap? = null

                if (thumbnailPath != null) {
                    thumbnail = BitmapFactory.decodeFile(thumbnailPath)
                }

                if (thumbnail != null) {
                    ChatHistoryMessageType.Video(
                        caption = "${chatContent.emoji} Sticker",
                        filePath = chatContent.animatedEmoji.sticker.sticker.local.path,
                        remoteId = chatContent.animatedEmoji.sticker.sticker.remote.id,
                        duration = -1,
                        thumbnail
                    )
                } else {
                    ChatHistoryMessageType.Other
                }
            }

            else -> {
                ChatHistoryMessageType.Other
            }
        }
}