package com.pff;

public interface IRecipient {
    String getDisplayName();

    int getRecipientType();

    String getEmailAddressType();

    String getEmailAddress();

    int getRecipientFlags();

    int getRecipientOrder();

    String getSmtpAddress();
}
