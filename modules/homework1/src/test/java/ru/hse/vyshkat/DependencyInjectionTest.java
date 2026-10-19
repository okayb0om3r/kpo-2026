package ru.hse.vyshkat;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.hse.vyshkat.config.AppConfig;
import ru.hse.vyshkat.model.Bicycle;
import ru.hse.vyshkat.service.FleetService;
import ru.hse.vyshkat.service.ServiceCenter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DependencyInjectionTest {
    @Test
    void productionContainerWiresActualInspection() {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            var fleet = context.getBean(FleetService.class);
            assertTrue(fleet.admit(new Bicycle("B1", "Исправен", 6, true)));
            assertFalse(fleet.admit(new Bicycle("B2", "Сломан", 6, false)));
        }
    }

    @Test
    void primaryStubReplacesInspectionThroughContainer() {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.registerBean("rejectingInspection", ServiceCenter.class,
                    () -> transport -> false, definition -> definition.setPrimary(true));
            context.register(AppConfig.class);
            context.refresh();
            var fleet = context.getBean(FleetService.class);
            assertFalse(fleet.admit(new Bicycle("B1", "Исправен", 6, true)));
            assertTrue(fleet.inventory().isEmpty());
        }
    }
}
