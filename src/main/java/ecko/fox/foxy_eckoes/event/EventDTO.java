package ecko.fox.foxy_eckoes.event;

import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class EventDTO {
    private UUID eventId;
    private Date date;
    private String performer;
    private String description;
    private List<String> tags;
    private int numberOfSeats;
    private int numberOfSeatsLeft;
    private int numberOfBookings;

    public static EventDTO fromEvent(Event event) {
        return new EventDTO(
                event.getEventID(),
                event.getDate(),
                event.getPerformer(),
                event.getDescription(),
                event.getTags(),
                event.getNumberOfSeats(),
                event.getNumberOfSeatsLeft(),
                event.getBookings().size()
        );
    }

}
