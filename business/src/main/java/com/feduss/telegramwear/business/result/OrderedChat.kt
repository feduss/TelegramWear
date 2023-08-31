package com.feduss.telegramwear.data.response

import org.drinkless.td.libcore.telegram.TdApi.ChatPosition


class OrderedChat constructor(
    val chatId: Long, val position: ChatPosition
): Comparable<OrderedChat?> {

    override fun compareTo(other: OrderedChat?): Int {
        if (other == null) {
            return 1
        }

        if (position.order != other.position.order) {
            return if (other.position.order < position.order) -1 else 1
        }
        return if (chatId != other.chatId) {
            if (other.chatId < chatId) -1 else 1
        } else 0
    }

    override fun equals(other: Any?): Boolean {
        return if (other is OrderedChat) {
            chatId == other.chatId && position.order == other.position.order
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return chatId.hashCode()
    }
}