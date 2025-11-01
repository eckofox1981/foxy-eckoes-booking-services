package ecko.fox.foxy_eckoes.booking;

import ecko.fox.foxy_eckoes.event.Event;
import ecko.fox.foxy_eckoes.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Entity(name = "bookings")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Booking {
    @Id
    private UUID bookingId;
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id")
    private Event event;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
    @Column
    private int numberOfTickets;
    @Column
    private Status status;
    @Column
    private Date dateCreated;
}
