package ecko.fox.foxy_eckoes.user;

import ecko.fox.foxy_eckoes.booking.Booking;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity(name = "foxy_users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class User implements UserDetails {
    @Id
    private UUID userID;
    @Column(unique = true)
    private String username;
    @Column
    private String passwordHash;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column(unique = true)
    private String email;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<Booking> bookings;
    @Column
    private String role;
    @Column
    private String openId;
    @Column
    private String openIdProvider;

    public User(UUID userID, String username, String passwordHash, String firstName, String lastName, String email, String role) {
        this.userID = userID;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.openId = "";
        this.openIdProvider = "none";
        this.bookings = new ArrayList<>();
    }

    public User(UUID userID, String username, String openId, String openIdProvider) {
        this.userID = userID;
        this.username = username;
        this.openId = openId;
        this.openIdProvider = openIdProvider;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public String getPassword() {
        return "";
    }
}
