package com.example.instach

class NotificationChannel(var strategy: NotificationTypeStrategy) {
    fun update(strategy: NotificationTypeStrategy) {
        this.strategy = strategy
    }

    fun howToNotifiy():String{
        return "${strategy.notificationMode()}"
    }
}