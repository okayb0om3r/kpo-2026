package ru.hse.vyshkat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.hse.vyshkat.service.BasicServiceCenter;
import ru.hse.vyshkat.service.FleetService;
import ru.hse.vyshkat.service.ServiceCenter;

@Configuration(proxyBeanMethods = false)
public class AppConfig {
    @Bean
    public ServiceCenter serviceCenter() {
        return new BasicServiceCenter();
    }

    @Bean
    public FleetService fleetService(ServiceCenter serviceCenter) {
        return new FleetService(serviceCenter);
    }
}
