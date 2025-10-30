package ecko.fox.foxy_eckoes.booking;

import ecko.fox.foxy_eckoes.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error while booking: " + e.getMessage());
        }
    }
}
