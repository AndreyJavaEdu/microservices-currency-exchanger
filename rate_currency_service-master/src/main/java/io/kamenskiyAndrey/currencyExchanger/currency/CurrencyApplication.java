package io.kamenskiyAndrey.currencyExchanger.currency;

import io.kamenskiyAndrey.currencyExchanger.currency.config.CurrencyClientConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan("io.kamenskiyAndrey.currencyExchanger.currency.config")
public class CurrencyApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyApplication.class, args);
	}

}
