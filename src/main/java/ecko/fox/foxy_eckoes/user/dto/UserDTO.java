package ecko.fox.foxy_eckoes.user.dto;

import ecko.fox.foxy_eckoes.booking.Booking;
import ecko.fox.foxy_eckoes.booking.BookingDTO;
import ecko.fox.foxy_eckoes.event.Event;
import ecko.fox.foxy_eckoes.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class UserDTO {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private List<BookingDTO> bookings;
    private String role;

    public static UserDTO fromUser(User user) {
        return new UserDTO(
                user.getUserID(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getBookings()
                        .stream()
                        .map((BookingDTO::fromBooking))
                        .sorted(Comparator.comparing(booking -> booking.getEvent().getDate()))
                        .toList(),
                user.getRole()
        );

    }
}

