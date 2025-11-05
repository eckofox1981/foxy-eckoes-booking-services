package ecko.fox.foxy_eckoes.event;

import ecko.fox.foxy_eckoes.event.dto.EventDTO;
import ecko.fox.foxy_eckoes.event.dto.EventFilterDTO;
import ecko.fox.foxy_eckoes.event.dto.NewEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {
    private final EventService service;

    //TODO: if security config not OK, add check of userRole = admin
    @PostMapping("/create")
    public ResponseEntity<?> createEvent(@RequestBody NewEventDTO newEvent) {
        try {
            EventDTO eventDTO = EventDTO.fromEvent(service.createEvent(newEvent));
            return ResponseEntity.ok(eventDTO);
        }  catch (Exception e) {
            return ResponseEntity.badRequest().body("Could not create event: " + e.getMessage());
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllEvents() {
        try {
            List<EventDTO> eventDTOs = service.getAllEvents()
                    .stream()
                    .map(EventDTO::fromEvent)
                    .toList();
            return ResponseEntity.ok(eventDTOs);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/filter") //NOTE: GET-method should not have a body
    public ResponseEntity<?> filterEvents(@RequestBody EventFilterDTO eventFilterDTO) {
        try {
            List<EventDTO> eventDTOs = service.filterEvents(eventFilterDTO)
                    .stream()
                    .map(EventDTO::fromEvent)
                    .toList();
            return ResponseEntity.ok(eventDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/id")
    public ResponseEntity<?> getEventByID(@RequestParam UUID eventID) {
        try {
            EventDTO eventDTO = EventDTO.fromEvent(service.getEventById(eventID));
            return ResponseEntity.ok(eventDTO);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/cancel")
    public ResponseEntity<?> cancelEvent(@RequestParam UUID eventID) {
        try {
            return ResponseEntity.status(203).body(service.cancelEvent(eventID));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-seats-left")
    public ResponseEntity<?> updateSeatsLeft(@RequestParam UUID eventID, @RequestParam int seatsBooked) {
        try {
            EventDTO eventDTO = EventDTO.fromEvent(service.updateSeatsLefts(eventID, seatsBooked));
            return ResponseEntity.ok(eventDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateEvent(@RequestBody EventDTO eventToUpdate) {
        try {
            EventDTO eventDTO = EventDTO.fromEvent(service.updateEvent(eventToUpdate));
            return ResponseEntity.ok(eventDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/control-all-event-availability")
    public ResponseEntity<?> seatAvailibilityControlAllEvent() {
        try {
            return ResponseEntity.ok(service.seatAvailabilityControlAllEvent());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
