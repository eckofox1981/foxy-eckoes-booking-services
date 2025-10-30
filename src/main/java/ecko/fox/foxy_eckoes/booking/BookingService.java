package ecko.fox.foxy_eckoes.booking;

import ecko.fox.foxy_eckoes.event.Event;
import ecko.fox.foxy_eckoes.event.EventService;
import ecko.fox.foxy_eckoes.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository repository;
    private final EventService eventService;

    public Booking bookEvent(User user, UUID eventID, int numberOfTickets) {
        Event event = eventService.getEventById(eventID);
        Booking booking = new Booking(
                UUID.randomUUID(),
                event,
                user,
                numberOfTickets
        );

        eventService.updateSeatsLefts(eventID, numberOfTickets);

        return repository.save(booking);
    }

    public Booking getUserBookingById(User user, UUID bookingId) throws IllegalAccessException, NoSuchElementException {
        Booking booking = repository.findById(bookingId).orElseThrow(()-> new NoSuchElementException("Bolling not found."));

        if (user.getRole().equalsIgnoreCase("admin")) {
            return booking;
        }

        if (!booking.getUser().getUserID().equals(user.getUserID())) {
            throw new IllegalAccessException("You are not allowed to access this booking.");
        }

        return booking;
    }

    public List<Booking> getAllUserBookings(User user) throws NoSuchElementException {
        return repository.findAllByUser(user).orElseThrow(()-> new NoSuchElementException("No bookings found."));
    }

}
