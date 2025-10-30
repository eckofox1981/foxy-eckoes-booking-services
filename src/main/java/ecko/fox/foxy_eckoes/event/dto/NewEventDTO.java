package ecko.fox.foxy_eckoes.event.dto;

import ecko.fox.foxy_eckoes.booking.Booking;
import ecko.fox.foxy_eckoes.event.Event;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class NewEventDTO {
    private Date date;
    private String performer;
    private String description;
    private String Location;
    private String pictureUrl;
    private List<String> tags;
    private int numberOfSeats;

    public Event saveEvent() {
        return new Event(
                UUID.randomUUID(),
                this.date,
                this.performer,
                this.description,
                this.pictureUrl,
                this.tags,
                this.numberOfSeats,
                this.numberOfSeats,
                new ArrayList<Booking>()
        );
    }
}
