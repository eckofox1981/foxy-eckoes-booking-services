package ecko.fox.foxy_eckoes.event.dto;

import ecko.fox.foxy_eckoes.event.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class EventDTO {
    private UUID eventId;
    private Date date;
    private String performer;
    private String description;
    private String location;
    private String pictureUrl;
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
                event.getLocation(),
                event.getPictureUrl(),
                event.getTags(),
                event.getNumberOfSeats(),
                event.getNumberOfSeatsLeft(),
                event.getBookings().size()
        );
    }

    public Event updateEvent(Event eventToUpdate) {
        eventToUpdate.setDate(this.date);
        eventToUpdate.setPerformer(this.performer);
        eventToUpdate.setDescription(this.description);
        eventToUpdate.setLocation(this.location);
        eventToUpdate.setPictureUrl(this.pictureUrl);
        eventToUpdate.setTags(this.tags);
        eventToUpdate.setNumberOfSeats(this.numberOfSeats);
        eventToUpdate.setNumberOfSeatsLeft(this.numberOfSeatsLeft);

        return eventToUpdate;
    }

}
