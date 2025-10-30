package ecko.fox.foxy_eckoes.event;

import ecko.fox.foxy_eckoes.event.dto.EventDTO;
import ecko.fox.foxy_eckoes.event.dto.EventFilterDTO;
import ecko.fox.foxy_eckoes.event.dto.NewEventDTO;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.IllegalQueryOperationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repository;

    public Event createEvent(NewEventDTO newEventDTO){
        Event event = newEventDTO.saveEvent();
        return repository.save(event);
    }

    public List<Event> getAllEvents() {
        return repository.findAll();
    }

    @SuppressWarnings("SlowListContainsAll") //removes warning for tags filtering since 'new HashSet'
                                            // unnecessary for so few expected tags
    public List<Event> filterEvent(EventFilterDTO filterParams) {
        //TODO: if time create specific repository function for filtering
        List<Event> events = getAllEvents();
        return events.stream()
                .filter(event -> filterParams.getFromDate() == null ||
                        event.getDate().after(filterParams.getFromDate()))
                .filter(event -> filterParams.getToDate() == null ||
                        event.getDate().before(filterParams.getToDate()))
                .filter(event -> filterParams.getPerformer() == null ||
                        event.getPerformer().equals(filterParams.getPerformer()))
                .filter(event -> filterParams.getLocation() == null ||
                        event.getLocation().equalsIgnoreCase(filterParams.getLocation()))
                .filter(event -> filterParams.getTags() == null ||
                        filterParams.getTags().isEmpty() ||
                        event.getTags().containsAll(filterParams.getTags()))
                .collect(Collectors.toList());
    }

    public Event getEventById(UUID eventId) throws NoSuchElementException {
        return repository.findById(eventId).orElseThrow(() -> new NoSuchElementException("Event not found"));
    }

    public String cancelEvent(UUID eventId) {
        Event event = repository.findById(eventId).orElseThrow(() -> new NoSuchElementException("Event not found"));
        String response = event.getPerformer() + " in " + event.getLocation();
        repository.delete(event);
        return "Event: " + response + ", was deleted.";
    }

    public Event updateSeatsLefts(EventDTO eventDTO, int seatsBooked) throws IllegalQueryOperationException {
        Event event = getEventById(eventDTO.getEventId());

        int seatsAvailable = event.getNumberOfSeatsLeft();

        if (seatsAvailable < seatsBooked) {
            throw new IllegalQueryOperationException("There are not enough seats left for your booking.");
        }

        event.setNumberOfSeatsLeft(seatsAvailable-seatsBooked);
        return repository.save(event);
    }

    public Event updateEvent (EventDTO eventDTO) {
        Event event = getEventById(eventDTO.getEventId());
        eventDTO.updateEvent(event);
        return repository.save(event);
    }
}
