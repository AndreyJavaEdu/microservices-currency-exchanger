package io.kamenskiyAndrey.history

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient

@SpringBootApplication
@EnableDiscoveryClient
class HistoryServiceApplication

fun main(args: Array<String>) {
	runApplication<HistoryServiceApplication>(*args)
}
