package home.jwt.springApp.domain;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    private Long id;

    private String userId;
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private String email;
    private String profileImage;
    private Date lastLogInDate;
    private Date lastLogInDateDisplay;
    private Date joinDate;
    private String[] roles; //ROLE_USER = {read, edit}, ROLE_ADMIN = {delete}
    private String[] authorities;
    private boolean isActive;
    private boolean isNotLocked;
}
