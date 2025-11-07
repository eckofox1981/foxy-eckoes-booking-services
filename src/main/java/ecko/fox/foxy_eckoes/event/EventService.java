package ecko.fox.foxy_eckoes.event;

import ecko.fox.foxy_eckoes.event.dto.ControllReport;
import ecko.fox.foxy_eckoes.event.dto.EventDTO;
import ecko.fox.foxy_eckoes.event.dto.EventFilterDTO;
import ecko.fox.foxy_eckoes.event.dto.NewEventDTO;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.IllegalQueryOperationException;
import org.springframework.stereotype.Service;

import java.util.*;
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
        List<Event> events = repository.findAll();
        if (events.size() == 0) {
            throw new NoSuchElementException("No events listed.");
        }



        return events.stream()
                .sorted(Comparator.comparing(Event::getDate))
                .toList();
    }

    @SuppressWarnings("SlowListContainsAll") //removes warning for tags filtering since 'new HashSet'
                                            // unnecessary for so few expected tags
    public List<Event> filterEvents(EventFilterDTO filterParams) {
        //TODO: if time create specific repository function for filtering
        List<Event> events = getAllEvents();

        //following makes sure empty string are not recognized as strings (obviously wouldn't math)
        if (filterParams.getLocation() != null && filterParams.getLocation().trim().isEmpty()) {
            filterParams.setLocation(null);
        }
        if (filterParams.getPerformer() != null && filterParams.getPerformer().trim().isEmpty()) {
            filterParams.setPerformer(null);
        }

        //quality checks each of the tags before submitting them to the stream-filtering of events
        List<String> tags = null;
        if (filterParams.getTags() != null) {
            tags = filterParams.getTags().stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
            if (tags.isEmpty()) {
                tags = null;
            }
        }

        List<String> filterTags = tags;

        return events.stream()
                .filter(event -> filterParams.getFromDate() == null ||
                        event.getDate().after(filterParams.getFromDate()))
                .filter(event -> filterParams.getToDate() == null ||
                        event.getDate().before(filterParams.getToDate()))
                .filter(event -> filterParams.getPerformer() == null ||
                        event.getPerformer().equalsIgnoreCase(filterParams.getPerformer()))
                .filter(event -> filterParams.getLocation() == null ||
                        event.getLocation().equalsIgnoreCase(filterParams.getLocation()))
                // inside the stream, replace the tag filter with:
                .filter(event -> {
                    if (filterTags == null) return true;
                    List<String> eventTags = event.getTags();
                    if (eventTags == null) return false;
                    Set<String> eventTagSet = eventTags.stream()
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(String::toLowerCase)
                            .collect(Collectors.toSet());
                    return filterTags.stream().anyMatch(eventTagSet::contains);
                })

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

    public Event updateSeatsLefts(UUID eventID, int seatsBooked) throws IllegalQueryOperationException {
        Event event = getEventById(eventID);

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

    public ControllReport seatAvailabilityControlAllEvent() {
        List<Event> allEvents = repository.findAll();
        List<String> updatedEventList = new ArrayList<>();
        int eventsUpdated = 0;
        int diff = 0;
        for (Event event : allEvents) {
            int thisDiff = event.seatAvailabilityControl();
            if (thisDiff > 0) {
                repository.save(event);
                eventsUpdated ++;
                diff += thisDiff;
                updatedEventList.add(event.getPerformer() + ", " + event.getLocation() + ", " + event.getDate() + " => " + thisDiff + " changes made.");
            }
        }

        String text = "Number of updated events: " + eventsUpdated + ". " + diff + " seat discrepancies fixed.";
        return new ControllReport(updatedEventList, text);
    }
}
