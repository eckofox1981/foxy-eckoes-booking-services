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
            return ResponseEntity.internalServerError().body("Error while booking: " + e.getMessage());
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
            return ResponseEntity.internalServerError().body("Could not fetch booking:" + e.getMessage());
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
            return ResponseEntity.internalServerError().body("Could not fetch booking:" + e.getMessage());
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
            return ResponseEntity.internalServerError().body("Error cancelling: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete") //admin only
    public ResponseEntity<?> deleteBooking(@AuthenticationPrincipal User user, @RequestParam UUID bookingID) {
        try {
            return ResponseEntity.ok(service.deleteBooking(user, bookingID));
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).body("Error cancelling: " + e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("Error cancelling: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error cancelling: " + e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateBooking(@AuthenticationPrincipal User user, @RequestParam UUID bookingID, @RequestParam int numberOfTickets) {
        try {
            BookingDTO bookingDTO = BookingDTO.fromBooking(service.updateBooking(user, bookingID, numberOfTickets));
            return ResponseEntity.ok(bookingDTO);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("Update error: " + e.getMessage());
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(401).body("Update error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(405).body("Update error: " + e.getMessage());
        }
    }

    @GetMapping("/get-bookings-by-userId")
    public ResponseEntity<?> getBookingsByUserId(@RequestParam UUID userID){
        try {
            List<BookingDTO> bookings = service.getBookingsByUserId(userID)
                    .stream()
                    .map(BookingDTO::fromBooking)
                    .toList();
            return ResponseEntity.ok(bookings);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body("Error finding bookings:" + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error finding bookings:" + e.getMessage());
        }
    }
}
