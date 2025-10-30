package ecko.fox.foxy_eckoes.booking;

import ecko.fox.foxy_eckoes.event.Event;
import ecko.fox.foxy_eckoes.event.EventService;
import ecko.fox.foxy_eckoes.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.print.Book;
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

}
