package com.vehiclerental;

import com.vehiclerental.models.Car;
import com.vehiclerental.models.Vehicle;
import com.vehiclerental.services.AuthService;
import com.vehiclerental.services.NotificationService;
import com.vehiclerental.services.VehicleManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests 4 and 5 (UT-4 and UT-5) for NotificationService (overdue & upcoming return logic).
 */
class NotificationServiceTest {

    //getOverdueNotifications() – must list vehicles whose end time has already passed and which are still marked rented.
     
    @Test
    void overdueNotifications_onlyWhenPastAndRented() {

        //Arrange 
        VehicleManager vm = Mockito.mock(VehicleManager.class);
        AuthService    au = Mockito.mock(AuthService.class);

        Vehicle overdue = new Car("X1", "Ford", "Fiesta", 30.0);
        overdue.setRented(true);
        overdue.setRentEndDateTime(LocalDateTime.now().minusHours(3));  // overdue

        Vehicle notOverdue = new Car("X2", "BMW", "i3", 40.0);
        notOverdue.setRented(true);
        notOverdue.setRentEndDateTime(LocalDateTime.now().plusHours(2)); // still on time

        Mockito.when(vm.getAllVehicles()).thenReturn(List.of(overdue, notOverdue));

        NotificationService ns = new NotificationService(vm, au);

        //Act 
        List<String> list = ns.getOverdueNotifications();

        //Assert 
        assertEquals(1, list.size(), "Only the overdue vehicle should be listed");
    }

    //getUpcomingReturnWarnings() – must warn when end time is within 24 h and vehicle is still rented.
    @Test
    void upcomingWarnings_onlyWithinNext24Hours() {

        VehicleManager vm = Mockito.mock(VehicleManager.class);
        AuthService    au = Mockito.mock(AuthService.class);

        Vehicle dueSoon = new Car("A1", "VW", "Golf", 25.0);
        dueSoon.setRented(true);
        dueSoon.setRentEndDateTime(LocalDateTime.now().plusHours(6));   // < 24 h

        Vehicle farAway = new Car("A2", "VW", "ID3", 35.0);
        farAway.setRented(true);
        farAway.setRentEndDateTime(LocalDateTime.now().plusHours(48));  // > 24 h

        Mockito.when(vm.getAllVehicles()).thenReturn(List.of(dueSoon, farAway));

        NotificationService ns = new NotificationService(vm, au);

        List<String> list = ns.getUpcomingReturnWarnings();

        assertEquals(1, list.size(), "Only the 6-hour vehicle should trigger a warning");
    }
}
