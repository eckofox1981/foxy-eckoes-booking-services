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
            .filter(event ->
                fromDateFilter(event, filterParams.getFromDate()) &&
                toDateFilter(event, filterParams.getToDate()) &&
                performerFilter(event, filterParams.getPerformer()) &&
                locationFilter(event, filterParams.getLocation()) &&
                tagFilterer(event, filterTags))
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

    /**
     * ADMIN ONLY functions
     * checks through all bookings and counts the number of seats booked for each event and compares them
     * to the recorded seatsAvailable.
     * If there is a difference (at the time of writing only due to testing though modifying database directly),
     * the event is updated to the number of seats booked.
     * Each difference is recorded and a report is compiled.
     * @return a ControllReport with all the changes made to the event's seatAvailibility
     */
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

    private boolean fromDateFilter(Event event, Date fromDate) {
        if (fromDate == null) {
            return true;
        }

        return event.getDate().after(fromDate);
    }

    private boolean toDateFilter(Event event, Date toDate) {
        if (toDate == null) {
            return true;
        }

        return event.getDate().before(toDate);
    }

    private boolean performerFilter(Event event, String performer) {
        if (performer == null || performer.isEmpty()) {
            return true;
        }

        return event.getPerformer().toLowerCase().contains(performer.toLowerCase());
    }

    private boolean locationFilter(Event event, String location) {
        if (location == null || location.isEmpty()) {
            return true;
        }

        return event.getLocation().toLowerCase().contains(location.toLowerCase());
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
                        new Date(1733280000000L), // 2025-12-01T20:00:00Z
                        "Billie Eilish",
                        "The multi-Grammy winner brings her haunting vocals and captivating stage presence on her world tour.",
                        "Royal Arena, Copenhagen, Denmark",
                        "https://img-4.linternaute.com/iplCNGn2m-1dAnFiGI-5tqgAZf0=/1500x/smart/cdef086a7bd645928fc450ec6c0bfc78/ccmcms-linternaute/59007360.jpg",
                        List.of("pop", "alternativepop", "BillieEilish", "Copenhagen", "Denmark"),
                        12500
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1734375600000L), // 2025-12-15T18:30:00Z
                        "Taylor Swift",
                        "The Eras Tour continues! Experience a spectacular journey through Taylor's entire discography with stunning visuals and surprise songs.",
                        "Parken Stadium, Copenhagen, Denmark",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b5/191125_Taylor_Swift_at_the_2019_American_Music_Awards_%28cropped%29.png/300px-191125_Taylor_Swift_at_the_2019_American_Music_Awards_%28cropped%29.png",
                        List.of("pop", "countrypop", "TaylorSwift", "Copenhagen", "Denmark", "ErasTour"),
                        38000
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1734980400000L), // 2025-12-29T20:00:00Z
                        "Foo Fighters",
                        "Rock royalty Foo Fighters bring their high-energy performance and decades of hits to Scandinavia.",
                        "Hartwall Arena, Helsinki, Finland",
                        "https://media.gettyimages.com/id/75264785/photo/london-dave-grohl-of-foo-fighters-performs-on-stage-during-the-live-earth-concert-at-wembley.jpg?s=612x612&w=0&k=20&c=hovSzNq6xO9OhOrt56LWVGtCclJHbdazS39PA_qeANI=",
                        List.of("rock", "alternativerock", "FooFighters", "Helsinki", "Finland", "DaveGrohl"),
                        13506
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1735861200000L), // 2026-01-24T19:00:00Z
                        "Metallica",
                        "Heavy metal legends deliver an explosive evening of thrash metal classics and new material from their 72 Seasons album.",
                        "Telenor Arena, Oslo, Norway",
                        "https://contentf5.dailynewshungary.com/wp-content/uploads/2025/06/metallica-concert.jpg",
                        List.of("metal", "heavymetal", "thrashmetal", "Metallica", "Oslo", "Norway"),
                        23000
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1737735600000L), // 2026-02-10T19:30:00Z
                        "Ed Sheeran",
                        "The Mathematics Tour brings Ed Sheeran's intimate storytelling and loop pedal mastery to the stage with songs spanning his entire career.",
                        "Scandinavium, Gothenburg, Sweden",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c1/Ed_Sheeran-6886_%28cropped%29.jpg/300px-Ed_Sheeran-6886_%28cropped%29.jpg",
                        List.of("pop", "folkpop", "Ed Sheeran", "Scandinavium", "Gothenburg", "Sweden"),
                        12044
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1740474000000L), // 2026-03-01T20:00:00Z
                        "Coldplay",
                        "Coldplay’s Music of the Spheres Tour is a breathtaking spectacle, with incredible visuals, and a global celebration of music.",
                        "Olympic Stadium, Helsinki, Finland",
                        "https://www.ekathimerini.com/wp-content/uploads/2023/10/326c3879752c10109d46e9664c1f946b_coldplay-AP-960x600.jpg",
                        List.of("rock", "pop", "Coldplay", "Helsinki", "Finland"),
                        50000
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1742036400000L), // 2026-03-25T20:00:00Z
                        "Imagine Dragons",
                        "Experience the electrifying performance of Imagine Dragons as they bring their chart-topping hits to Europe.",
                        "Friends Arena, Stockholm, Sweden",
                        "https://www.udiscovermusic.com/wp-content/uploads/2019/11/Imagine-Dragons-GettyImages-942804922-1000x600.jpg",
                        List.of("poprock", "alternativerock", "ImagineDragons", "Stockholm", "Sweden"),
                        32000
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1745762400000L), // 2026-04-15T19:00:00Z
                        "BTS",
                        "BTS returns for an unforgettable night of music, dance, and performances that have made them global superstars.",
                        "Croke Park, Dublin, Ireland",
                        "https://www.nme.com/wp-content/uploads/2022/10/BTS-_Yet-To-Come_-in-BUSAN_7-1.jpg",
                        List.of("kpop", "bts", "Dublin", "Ireland"),
                        75000
                ).saveEvent());

                repository.save(new NewEventDTO(
                        new Date(1746241200000L), // 2026-04-29T20:00:00Z
                        "Adele",
                        "Adele’s stunning voice and powerful ballads will leave you speechless in her live tour experience.",
                        "Wembley Stadium, London, UK",
                        "https://www.billboard.com/wp-content/uploads/2024/08/adele-munich-2024-billboard-1548.jpg?w=942&h=623&crop=1",
                        List.of("pop", "soul", "Adele", "London", "UK"),
                        90000
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
