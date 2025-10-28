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
@AllArgsConstructor
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
    private List<String> tags;
    @Column
    private int numberOfSeats;
    @Column
    private int NumberOfSeatsLeft;
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings;
}
