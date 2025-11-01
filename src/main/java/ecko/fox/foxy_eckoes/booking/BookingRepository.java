package ecko.fox.foxy_eckoes.booking;

import ecko.fox.foxy_eckoes.event.Event;
import ecko.fox.foxy_eckoes.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    Optional<List<Booking>> findAllByUser(User user);

    Optional<List<Booking>> findAllByEvent(Event event);
}
