package com.pff;

import java.util.Date;

public interface IContact extends IMessage {
    /**
     * Contact's Account name
     */
    String getAccount();

    /**
     * Callback telephone number
     */
    String getCallbackTelephoneNumber();

    /**
     * Contact's generational abbreviation FTK: Name suffix
     */
    String getGeneration();

    /**
     * Contacts given name
     */
    String getGivenName();

    /**
     * Contacts Government ID Number
     */
    String getGovernmentIdNumber();

    /**
     * Business/Office Telephone Number
     */
    String getBusinessTelephoneNumber();

    /**
     * Home Telephone Number
     */
    String getHomeTelephoneNumber();

    /**
     * Contacts initials
     */
    String getInitials();

    /**
     * Keyword
     */
    String getKeyword();

    /**
     * Contact's language
     */
    String getLanguage();

    /**
     * Contact's location
     */
    String getLocation();

    /**
     * MHS Common Name
     */
    String getMhsCommonName();

    /**
     * Organizational identification number
     */
    String getOrganizationalIdNumber();

    /**
     * Contact's surname FTK: Last name
     */
    String getSurname();

    /**
     * Original display name
     */
    String getOriginalDisplayName();

    /**
     * Default Postal Address
     */
    String getPostalAddress();

    /**
     * Contact's company name
     */
    String getCompanyName();

    /**
     * Contact's job title FTK: Profession
     */
    String getTitle();

    /**
     * Contact's department name Used in contact item
     */
    String getDepartmentName();

    /**
     * Contact's office location
     */
    String getOfficeLocation();

    /**
     * Primary Telephone
     */
    String getPrimaryTelephoneNumber();

    /**
     * Contact's secondary office (business) phone number
     */
    String getBusiness2TelephoneNumber();

    /**
     * Mobile Phone Number
     */
    String getMobileTelephoneNumber();

    /**
     * Radio Phone Number
     */
    String getRadioTelephoneNumber();

    /**
     * Car Phone Number
     */
    String getCarTelephoneNumber();

    /**
     * Other Phone Number
     */
    String getOtherTelephoneNumber();

    /**
     * Transmittable display name
     */
    String getTransmittableDisplayName();

    /**
     * Pager Phone Number
     */
    String getPagerTelephoneNumber();

    /**
     * Primary Fax Number
     */
    String getPrimaryFaxNumber();

    /**
     * Contact's office (business) fax number
     */
    String getBusinessFaxNumber();

    /**
     * Contact's home fax number
     */
    String getHomeFaxNumber();

    /**
     * Business Address Country
     */
    String getBusinessAddressCountry();

    /**
     * Business Address City
     */
    String getBusinessAddressCity();

    /**
     * Business Address State
     */
    String getBusinessAddressStateOrProvince();

    /**
     * Business Address Street
     */
    String getBusinessAddressStreet();

    /**
     * Business Postal Code
     */
    String getBusinessPostalCode();

    /**
     * Business PO Box
     */
    String getBusinessPoBox();

    /**
     * Telex Number
     */
    String getTelexNumber();

    /**
     * ISDN Number
     */
    String getIsdnNumber();

    /**
     * Assistant Phone Number
     */
    String getAssistantTelephoneNumber();

    /**
     * Home Phone 2
     */
    String getHome2TelephoneNumber();

    /**
     * Assistant�s Name
     */
    String getAssistant();

    /**
     * Hobbies
     */
    String getHobbies();

    /**
     * Middle Name
     */
    String getMiddleName();

    /**
     * Display Name Prefix (Contact Title)
     */
    String getDisplayNamePrefix();

    /**
     * Profession
     */
    String getProfession();

    /**
     * Preferred By Name
     */
    String getPreferredByName();

    /**
     * Spouse�s Name
     */
    String getSpouseName();

    /**
     * Computer Network Name
     */
    String getComputerNetworkName();

    /**
     * Customer ID
     */
    String getCustomerId();

    /**
     * TTY/TDD Phone
     */
    String getTtytddPhoneNumber();

    /**
     * Ftp Site
     */
    String getFtpSite();

    /**
     * Manager�s Name
     */
    String getManagerName();

    /**
     * Nickname
     */
    String getNickname();

    /**
     * Personal Home Page
     */
    String getPersonalHomePage();

    /**
     * Business Home Page
     */
    String getBusinessHomePage();

    /**
     * Note
     */
    String getNote();

    String getSMTPAddress();

    /**
     * Company Main Phone
     */
    String getCompanyMainPhoneNumber();

    /**
     * Children's names
     */
    String getChildrensNames();

    /**
     * Home Address City
     */
    String getHomeAddressCity();

    /**
     * Home Address Country
     */
    String getHomeAddressCountry();

    /**
     * Home Address Postal Code
     */
    String getHomeAddressPostalCode();

    /**
     * Home Address State or Province
     */
    String getHomeAddressStateOrProvince();

    /**
     * Home Address Street
     */
    String getHomeAddressStreet();

    /**
     * Home Address Post Office Box
     */
    String getHomeAddressPostOfficeBox();

    /**
     * Other Address City
     */
    String getOtherAddressCity();

    /**
     * Other Address Country
     */
    String getOtherAddressCountry();

    /**
     * Other Address Postal Code
     */
    String getOtherAddressPostalCode();

    /**
     * Other Address State
     */
    String getOtherAddressStateOrProvince();

    /**
     * Other Address Street
     */
    String getOtherAddressStreet();

    /**
     * Other Address Post Office box
     */
    String getOtherAddressPostOfficeBox();

    /**
     * File under FTK: File as
     */
    String getFileUnder();

    /**
     * Home Address
     */
    String getHomeAddress();

    /**
     * Business Address
     */
    String getWorkAddress();

    /**
     * Other Address
     */
    String getOtherAddress();

    /**
     * Selected Mailing Address
     */
    int getPostalAddressId();

    /**
     * Webpage
     */
    String getHtml();

    /**
     * Business Address City
     */
    String getWorkAddressStreet();

    /**
     * Business Address Street
     */
    String getWorkAddressCity();

    /**
     * Business Address State
     */
    String getWorkAddressState();

    /**
     * Business Address Postal Code
     */
    String getWorkAddressPostalCode();

    /**
     * Business Address Country
     */
    String getWorkAddressCountry();

    /**
     * Business Address Country
     */
    String getWorkAddressPostOfficeBox();

    /**
     * IM Address
     */
    String getInstantMessagingAddress();

    /**
     * E-mail1 Display Name
     */
    String getEmail1DisplayName();

    /**
     * E-mail1 Address Type
     */
    String getEmail1AddressType();

    /**
     * E-mail1 Address
     */
    String getEmail1EmailAddress();

    /**
     * E-mail1 Display Name
     */
    String getEmail1OriginalDisplayName();

    /**
     * E-mail1 type
     */
    String getEmail1EmailType();

    /**
     * E-mail2 display name
     */
    String getEmail2DisplayName();

    /**
     * E-mail2 address type
     */
    String getEmail2AddressType();

    /**
     * E-mail2 e-mail address
     */
    String getEmail2EmailAddress();

    /**
     * E-mail2 original display name
     */
    String getEmail2OriginalDisplayName();

    /**
     * E-mail3 display name
     */
    String getEmail3DisplayName();

    /**
     * E-mail3 address type
     */
    String getEmail3AddressType();

    /**
     * E-mail3 e-mail address
     */
    String getEmail3EmailAddress();

    /**
     * E-mail3 original display name
     */
    String getEmail3OriginalDisplayName();

    /**
     * Fax1 Address Type
     */
    String getFax1AddressType();

    /**
     * Fax1 Email Address
     */
    String getFax1EmailAddress();

    /**
     * Fax1 Original Display Name
     */
    String getFax1OriginalDisplayName();

    /**
     * Fax2 Address Type
     */
    String getFax2AddressType();

    /**
     * Fax2 Email Address
     */
    String getFax2EmailAddress();

    /**
     * Fax2 Original Display Name
     */
    String getFax2OriginalDisplayName();

    /**
     * Fax3 Address Type
     */
    String getFax3AddressType();

    /**
     * Fax3 Email Address
     */
    String getFax3EmailAddress();

    /**
     * Fax3 Original Display Name
     */
    String getFax3OriginalDisplayName();

    /**
     * Free/Busy Location (URL)
     */
    String getFreeBusyLocation();

    /**
     * Birthday
     */
    Date getBirthday();

    /**
     * (Wedding) Anniversary
     */
    Date getAnniversary();
}
