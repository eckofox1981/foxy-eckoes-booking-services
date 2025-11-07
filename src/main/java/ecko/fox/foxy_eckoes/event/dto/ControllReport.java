package ecko.fox.foxy_eckoes.event.dto;

import lombok.Data;

import java.util.List;

@Data
public class ControllReport {
    private String title = "Seat Availibility Controll";
    private List<String> eventsUpdated;
    private String text;


    public ControllReport(List<String> eventsUpdated, String text) {
        this.eventsUpdated = eventsUpdated;
        this.text = text;
    }
}
