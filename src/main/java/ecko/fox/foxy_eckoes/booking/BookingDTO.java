package ecko.fox.foxy_eckoes.booking;

import ecko.fox.foxy_eckoes.event.dto.EventDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class BookingDTO {
    private UUID bookingId;
    private EventDTO event;
    private String username;
    private int numberOfTickets;

    public static BookingDTO fromBooking(Booking booking) {
        return new BookingDTO(
                booking.getBookingId(),
                EventDTO.fromEvent(booking.getEvent()),
                booking.getUser().getUsername(),
                booking.getNumberOfTickets()
        );
    }
}
