package ecko.fox.foxy_eckoes.event.dto;

import java.util.Date;
import java.util.List;

public class EventFilterDTO {
    private Date fromDate;
    private Date toDate;
    private String performer;
    private String location;
    private List<String> tags;
}
