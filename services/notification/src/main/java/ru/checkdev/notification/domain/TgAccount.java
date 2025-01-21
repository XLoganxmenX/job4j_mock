package ru.checkdev.notification.domain;

import lombok.*;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder
@Entity
@Table(name = "cd_telegram_accounts")
public class TgAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "tg_user_id")
    private int tgUserId;

    @Column(name = "tg_username")
    private String tgUsername;

    @Column(name = "tg_first_name")
    private String tgFirstName;

    @Column(name = "tg_last_name")
    private String tgLastName;

    @Column(name = "email")
    private String email;

    @Column(name = "tg_chat_id")
    private int tgChatId;
}
