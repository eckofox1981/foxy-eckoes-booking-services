package ecko.fox.foxy_eckoes.event;

import ecko.fox.foxy_eckoes.event.dto.ControllReport;
import ecko.fox.foxy_eckoes.event.dto.EventDTO;
import ecko.fox.foxy_eckoes.event.dto.EventFilterDTO;
import ecko.fox.foxy_eckoes.event.dto.NewEventDTO;
import ecko.fox.foxy_eckoes.user.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.IllegalQueryOperationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repository;


    public Event createEvent(NewEventDTO newEventDTO) {
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

        }

        if (tags.isEmpty()) {
            tags = null;
        }

        List<String> filterTags = tags;

        return events.stream()
                .filter(event -> filterParams.getFromDate() == null ||
                        event.getDate().after(filterParams.getFromDate()))
                .filter(event -> filterParams.getToDate() == null ||
                        event.getDate().before(filterParams.getToDate()))
                .filter(event -> filterParams.getPerformer() == null ||
                        event.getPerformer().toLowerCase().contains(filterParams.getPerformer().toLowerCase()))
                .filter(event -> filterParams.getLocation() == null ||
                        event.getLocation().toLowerCase().contains(filterParams.getLocation().toLowerCase()))
                .filter(event -> tagFilterer(event, filterTags))
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

        event.setNumberOfSeatsLeft(seatsAvailable - seatsBooked);
        return repository.save(event);
    }

    public Event updateEvent(EventDTO eventDTO) {
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
                eventsUpdated++;
                diff += thisDiff;
                updatedEventList.add(event.getPerformer() + ", " + event.getLocation() + ", " + event.getDate() + " => " + thisDiff + " changes made.");
            }
        }

        String text = "Number of updated events: " + eventsUpdated + ". " + diff + " seat discrepancies fixed.";
        return new ControllReport(updatedEventList, text);
    }

    private boolean tagFilterer(Event event, List<String> filterTags) {
        if (filterTags == null) return true;

        List<String> eventTags = event.getTags();
        if (eventTags == null || eventTags.isEmpty()) return true;

        Set<String> eventTagSet = eventTags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());


        return filterTags.stream().anyMatch(eventTagSet::contains);
    }

    //FOLLOWING ADDED ONLY TO MAKE CORRECTION PROCESS EASIER
    /**
     * creates filler events and saves them to database upon first launch.
     */
    @PostConstruct
    public void createFillerEvents() {
        List<Event> eventList = repository.findAll();

        try {
            //if list completely empty assumed to be first time application is running
            if (eventList.isEmpty()) {
                System.out.println(lines + "\nEvent List empty: creating filler events...");
                repository.save(new NewEventDTO(
                        new Date(1732478400000L), // 2025-11-24T20:00:00Z
                        "The Rolling Stones",
                        "The Mosty Not Dead Tour: even though iconic drummer Charlie Watts died four years ago, the rest of the band are still grabbing their walker to deambulate onto a stage near you",
                        "Scandinavium, Gothenburg, Sweden",
                        "https://cdn.artphotolimited.com/images/5a0af3030d0f835b8cacac95/300x300/the-rolling-stones-en-concert-a-paris-2003.jpg",
                        List.of("rockn'roll", "rock", "stones", "Scandinavium", "Gothenburg", "Sweden", "MickJagger"),
                        50000
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1731873600000L), // 2025-11-17T20:00:00Z
                        "Billie Eilish",
                        "The multi-Grammy winner brings her haunting vocals and captivating stage presence on her world tour.",
                        "Royal Arena, Copenhagen, Denmark",
                        "https://img-4.linternaute.com/iplCNGn2m-1dAnFiGI-5tqgAZf0=/1500x/smart/cdef086a7bd645928fc450ec6c0bfc78/ccmcms-linternaute/59007360.jpg",
                        List.of("pop", "alternativepop", "BillieEilish", "Copenhagen", "Denmark"),
                        12500
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1733079000000L), // 2025-12-01T18:30:00Z
                        "Taylor Swift",
                        "The Eras Tour continues! Experience a spectacular journey through Taylor's entire discography with stunning visuals and surprise songs.",
                        "Parken Stadium, Copenhagen, Denmark",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/191125_Taylor_Swift_at_the_2019_American_Music_Awards_%28cropped%29.png/300px-191125_Taylor_Swift_at_the_2019_American_Music_Awards_%28cropped%29.png",
                        List.of("pop", "countrypop", "TaylorSwift", "Copenhagen", "Denmark", "ErasTour"),
                        38000
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1733688000000L), // 2025-12-08T20:00:00Z
                        "Foo Fighters",
                        "Rock royalty Foo Fighters bring their high-energy performance and decades of hits to Scandinavia.",
                        "Hartwall Arena, Helsinki, Finland",
                        "https://media.gettyimages.com/id/75264785/photo/london-dave-grohl-of-foo-fighters-performs-on-stage-during-the-live-earth-concert-at-wembley.jpg?s=612x612&w=0&k=20&c=hovSzNq6xO9OhOrt56LWVGtCclJHbdazS39PA_qeANI=",
                        List.of("rock", "alternativerock", "FooFighters", "Helsinki", "Finland", "DaveGrohl"),
                        13506
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1732474800000L), // 2025-11-24T19:00:00Z
                        "Metallica",
                        "Heavy metal legends deliver an explosive evening of thrash metal classics and new material from their 72 Seasons album.",
                        "Telenor Arena, Oslo, Norway",
                        "https://contentf5.dailynewshungary.com/wp-content/uploads/2025/06/metallica-concert.jpg",
                        List.of("metal", "heavymetal", "thrashmetal", "Metallica", "Oslo", "Norway"),
                        23000
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1734289800000L), // 2025-12-15T19:30:00Z
                        "Ed Sheeran",
                        "The Mathematics Tour brings Ed Sheeran's intimate storytelling and loop pedal mastery to the stage with songs spanning his entire career.",
                        "Scandinavium, Gothenburg, Sweden",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/Ed_Sheeran-6886_%28cropped%29.jpg/300px-Ed_Sheeran-6886_%28cropped%29.jpg",
                        List.of("pop", "folkpop", "Ed Sheeran", "Scandinavium", "Gothenburg", "Sweden"),
                        12044
                ).saveEvent());

                System.out.println("Filler events created \n" + lines);
            } else {
                System.out.println(lines + "\nDatabase used before, no filler events created.\n" + lines);
            }
        } catch (Exception e) {
            System.out.println("Could not create filler users but all is good.");
        }
    }

    private String lines = "==============================================================";
}
