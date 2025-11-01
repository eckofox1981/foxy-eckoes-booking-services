package ecko.fox.foxy_eckoes.booking;

import ecko.fox.foxy_eckoes.event.dto.EventDTO;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@AllArgsConstructor
@Data
public class BookingDTO {
    private UUID bookingId;
    private EventDTO event;
    private String username;
    private int numberOfTickets;
    private Status status;
    private Date dateCreated;

    public static BookingDTO fromBooking(Booking booking) {
        return new BookingDTO(
                booking.getBookingId(),
                EventDTO.fromEvent(booking.getEvent()),
                booking.getUser().getUsername(),
                booking.getNumberOfTickets(),
                booking.getStatus(),
                booking.getDateCreated()
        );
    }
}
