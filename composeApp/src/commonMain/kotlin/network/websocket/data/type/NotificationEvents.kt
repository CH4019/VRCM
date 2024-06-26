package io.github.vrcmteam.vrcm.network.websocket.data.type

object NotificationEvents {
    /**
     * 通知
     * 携带一个Notification对象，并被邀请、好友请求和其他游戏内通知使用
     */
    data object Notification: WebSocketEventType("notification",Unit::class)

    /**
     * 响应通知
     * 用于响应先前发送的事件
     */
    data object ResponseNotification: WebSocketEventType("response-notification",Unit::class)

    /**
     * 查看通知
     * 当客户端将特定通知标记为已看到时，将发送此事件
     */
    data object SeeNotification: WebSocketEventType("see-notification",Unit::class)

    /**
     * 隐藏通知
     * 当客户端隐藏通知时，将发送此事件
     */
    data object HideNotification : WebSocketEventType("hide-notification",Unit::class)

    /**
     * 清除通知
     * 当客户端清除所有通知时，将发送此事件
     */
    data object ClearNotification : WebSocketEventType("clear-notification",Unit::class)
}