package ecko.fox.foxy_eckoes.booking;

import ecko.fox.foxy_eckoes.event.Event;
import ecko.fox.foxy_eckoes.event.EventRepository;
import ecko.fox.foxy_eckoes.event.EventService;
import ecko.fox.foxy_eckoes.user.User;
import ecko.fox.foxy_eckoes.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.IllegalQueryOperationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.MethodNotAllowedException;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository repository;
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public Booking bookEvent(User user, UUID eventID, int numberOfTickets) throws IllegalQueryOperationException{
        Event event = eventService.getEventById(eventID);
        Booking booking = new Booking(
                UUID.randomUUID(),
                event,
                user,
                numberOfTickets,
                Status.PENDING, //pending is never actually never saved
                new Date()

        );

        eventService.updateSeatsLefts(eventID, numberOfTickets); //throws exception

        booking.setStatus(Status.CONFIRMED); //fom a user POV we could have set CONFIRMED in constructor above

        return repository.save(booking);
    }

    public Booking getUserBookingById(User user, UUID bookingId) throws IllegalAccessException, NoSuchElementException {
        Booking booking = repository.findById(bookingId).orElseThrow(()-> new NoSuchElementException("Booking not found."));

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

    public String cancelBooking(User user, UUID bookingID) throws IllegalAccessException, NoSuchElementException {
        Booking booking = repository.findById(bookingID).orElseThrow(()-> new NoSuchElementException("Booking not found."));
        Event event = eventService.getEventById(booking.getEvent().getEventID());
        String response = "Booking for " + booking.getEvent().getPerformer()
                + " in " + booking.getEvent().getLocation() + " has been cancelled.";

        if (user.getRole().equalsIgnoreCase("admin")) {
            event.setNumberOfSeatsLeft(event.getNumberOfSeatsLeft() + booking.getNumberOfTickets());
            booking.setStatus(Status.CANCELLED);
            repository.save(booking);
            eventRepository.save(event);
            return response;
        }

        if (!booking.getUser().getUserID().equals(user.getUserID())) {
            throw new IllegalAccessException("You are not allowed to delete this booking.");
        }

        event.setNumberOfSeatsLeft(event.getNumberOfSeatsLeft() + booking.getNumberOfTickets());
        booking.setStatus(Status.CANCELLED);
        repository.save(booking);
        eventRepository.save(event);

        return response;
    }

    public String deleteBooking(User user, UUID bookingID) throws IllegalAccessException, NoSuchElementException {
        Booking booking = repository.findById(bookingID).orElseThrow(()-> new NoSuchElementException("Booking not found."));
        Event event = eventService.getEventById(booking.getEvent().getEventID());
        String response = "Booking for " + booking.getEvent().getPerformer()
                + " in " + booking.getEvent().getLocation() + " has been cancelled.";

        if (!user.getRole().equalsIgnoreCase("admin")) {
            throw new IllegalAccessException("You are not allowed to delete this booking.");
        }

        if (booking.getStatus().equals(Status.CONFIRMED)) {
            event.setNumberOfSeatsLeft(event.getNumberOfSeatsLeft() + booking.getNumberOfTickets());
            eventRepository.save(event);
        }

        repository.delete(booking);

        return response;
    }

    public List<Booking> getAllBookingForEvent(Event event) throws NoSuchElementException {
        return repository.findAllByEvent(event).orElseThrow(()-> new NoSuchElementException("No booking founds."));
    }

    public Booking updateBooking(User user, UUID bookingID, int newNumberOfTickets)
            throws NoSuchElementException, IllegalAccessException, IllegalArgumentException {
        Booking booking = repository.findById(bookingID).orElseThrow(() -> new NoSuchElementException("Booking not found."));
        Event event = eventService.getEventById(booking.getEvent().getEventID());

        if (!booking.getUser().getUserID().equals(user.getUserID())) {
            throw new IllegalAccessException("You are not allowed to update this booking.");
        }

        if (booking.getStatus().equals(Status.CANCELLED)) {
            throw new IllegalArgumentException("You cannot update cancelled events");
        }

        int diffInBookedTickets = booking.getNumberOfTickets() - newNumberOfTickets;
        eventService.updateSeatsLefts(event.getEventID(), diffInBookedTickets);
        booking.setNumberOfTickets(newNumberOfTickets);

        return repository.save(booking);
    }

    public List<Booking> getBookingsByUserId(@RequestParam UUID userID) throws NoSuchElementException{
        User user = userRepository.findById(userID).orElseThrow(() -> new NoSuchElementException("User not found"));

        return repository.findAllByUser(user).orElseThrow(() -> new NoSuchElementException("Could not find bookings."));
    }

}
