package ecko.fox.foxy_eckoes.booking;

import ecko.fox.foxy_eckoes.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.IllegalQueryOperationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/booking")
public class BookingController {
    private final BookingService service;

    @Transactional
    @PostMapping("/book")
    public ResponseEntity<?> bookEvent(@AuthenticationPrincipal User user, @RequestParam UUID eventID, @RequestParam int numberOfTickets) {
        try {
            BookingDTO bookingDTO = BookingDTO.fromBooking(service.bookEvent(user, eventID, numberOfTickets));
            return ResponseEntity.ok(bookingDTO);
        } catch (IllegalQueryOperationException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error while booking: " + e.getMessage());
        }
    }

    @GetMapping("/id")
    public ResponseEntity<?> getUserBookingById(@AuthenticationPrincipal User user, @RequestParam UUID bookingID) {
        try {
            BookingDTO bookingDTO = BookingDTO.fromBooking(service.getUserBookingById(user, bookingID));
            return ResponseEntity.ok(bookingDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body("Error: " + e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("Not found:" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Could not fetch booking:" + e.getMessage());
        }
    }

    @GetMapping("/get-all-user")
    public ResponseEntity<?> getAllUserBookings(@AuthenticationPrincipal User user) {
        try {
            List<BookingDTO> bookings = service.getAllUserBookings(user)
                    .stream()
                    .map(BookingDTO::fromBooking)
                    .toList();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Could not fetch booking:" + e.getMessage());
        }
    }

    @PutMapping("/cancel")
    public ResponseEntity<?> cancelBooking(@AuthenticationPrincipal User user, @RequestParam UUID bookingID) {
        try {
            return ResponseEntity.ok(service.cancelBooking(user, bookingID));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).body("Error cancelling: " + e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("Error cancelling: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error cancelling: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete") //admin only
    public ResponseEntity<?> deleteBooking(@AuthenticationPrincipal User user, @RequestParam UUID bookingID) {
        try {
            return ResponseEntity.status(204).body(service.deleteBooking(user, bookingID));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).body("Error cancelling: " + e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("Error cancelling: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error cancelling: " + e.getMessage());
        }
    }
}
