package com.unipay.models;

import com.unipay.constants.Constants;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;

import static com.unipay.constants.Constants.EXPIRATION_MINUTES;


@Getter
@Setter
@Entity
@NoArgsConstructor()
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfirmationToken {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "ID")
    @EqualsAndHashCode.Include
    protected String id;

    @Column(name = "CONFIRMATION_TOKEN")
    private String confirmationToken;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public static ConfirmationToken create(User user) {
        final ConfirmationToken confirmationToken = new ConfirmationToken();

        final String code = confirmationToken.generateFormattedCode();
        confirmationToken.user = user;
        confirmationToken.createdDate = new Date();
        confirmationToken.confirmationToken = code;

        return confirmationToken;
    }
    private String generateFormattedCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            if (i > 0) code.append("-");
            for (int j = 0; j < 3; j++) {
                code.append(Constants.CHARACTERS.charAt(random.nextInt(Constants.CHARACTERS.length())));
            }
        }
        return code.toString();
    }
    public boolean isExpired() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(createdDate);
        calendar.add(Calendar.MINUTE, EXPIRATION_MINUTES);
        return new Date().after(calendar.getTime());
    }
}
