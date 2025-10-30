package ecko.fox.foxy_eckoes.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository repository;

    public Event createEvent(){
        return new Event();
    }
}
