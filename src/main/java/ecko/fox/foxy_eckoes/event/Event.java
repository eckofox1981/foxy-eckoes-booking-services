package ecko.fox.foxy_eckoes.event;

import ecko.fox.foxy_eckoes.booking.Booking;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity(name = "events")
@NoArgsConstructor
@Getter
@Setter
public class Event {
    @Id
    private UUID eventID;
    @Column
    private Date date;
    @Column
    private String performer;
    @Column
    private String description;
    @Column
    private String location;
    @Column
    private String pictureUrl;
    @Column
    private List<String> tags;
    @Column
    private int numberOfSeats;
    @Column
    private int NumberOfSeatsLeft;
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;

    public Event(UUID eventID, Date date, String performer, String description, String location, String pictureUrl, List<String> tags, int numberOfSeats, int numberOfSeatsLeft, List<Booking> bookings) {
        this.eventID = eventID;
        this.date = date;
        this.performer = performer;
        this.description = description;
        this.location = location;
        if (pictureUrl.isEmpty()) {
            this.pictureUrl = "https://images.unsplash.com/photo-1573152958734-1922c188fba3?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NHx8Y29uY2VydCUyMGNyb3dkfGVufDB8fDB8fHww&fm=jpg&q=60&w=3000";
        } else {
            this.pictureUrl = pictureUrl;
        }
        this.tags = tags;
        this.numberOfSeats = numberOfSeats;
        NumberOfSeatsLeft = numberOfSeatsLeft;
        this.bookings = bookings;
    }
}
