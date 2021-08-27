package com.fitmap.function.config;

import com.sendgrid.SendGrid;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SendGridConfig {

    public static final SendGrid SEND_GRID_CLIENT;
    public static final int ACCOUNT_ALERTS_UNSUBSCRIBE_GROUP_ID;
    public static final String SENDGRID_FROM_NAME;
    public static final String SENDGRID_FROM_EMAIL;

    public static final String EMAIL_VERIFY_TEMPLATE_ID;
    public static final String RESET_PASSWORD_TEMPLATE_ID;

    static {

        SEND_GRID_CLIENT = new SendGrid(System.getenv("SENDGRID_API_KEY"));
        EMAIL_VERIFY_TEMPLATE_ID = System.getenv("EMAIL_VERIFY_TEMPLATE_ID");
        RESET_PASSWORD_TEMPLATE_ID = System.getenv("RESET_PASSWORD_TEMPLATE_ID");
        SENDGRID_FROM_NAME = System.getenv("SENDGRID_FROM_NAME");
        SENDGRID_FROM_EMAIL = System.getenv("SENDGRID_FROM_EMAIL");
        ACCOUNT_ALERTS_UNSUBSCRIBE_GROUP_ID = Integer.parseInt(System.getenv("ACCOUNT_ALERTS_UNSUBSCRIBE_GROUP_ID"));
    }

}