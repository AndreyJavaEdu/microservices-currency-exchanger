package io.kamenskiyAndrey.currencyExchanger.notification

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
//import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
//@EnableDiscoveryClient
class NotificationBotApplication

fun main(args: Array<String>) {
	runApplication<NotificationBotApplication>(*args)
}
