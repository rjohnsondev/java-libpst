/**
 * Copyright 2010 Richard Johnson & Orin Eman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ---
 *
 * This file is part of java-libpst.
 *
 * java-libpst is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * java-libpst is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with java-libpst. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.pff;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 * Class for Contacts
 * 
 * @author Richard Johnson
 */
public class PSTContact extends PSTMessage implements IContact {

    /**
     * @param theFile
     * @param descriptorIndexNode
     * @throws PSTException
     * @throws IOException
     */
    public PSTContact(final PSTFile theFile, final DescriptorIndexNode descriptorIndexNode)
        throws PSTException, IOException {
        super(theFile, descriptorIndexNode);
    }

    /**
     * @param theFile
     * @param folderIndexNode
     * @param table
     * @param localDescriptorItems
     */
    public PSTContact(final PSTFile theFile, final DescriptorIndexNode folderIndexNode, final PSTTableBC table,
        final HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
        super(theFile, folderIndexNode, table, localDescriptorItems);
    }

    /**
     * Contact's Account name
     */
    @Override
    public String getAccount() {
        return this.getStringItem(0x3a00);
    }

    /**
     * Callback telephone number
     */
    @Override
    public String getCallbackTelephoneNumber() {
        return this.getStringItem(0x3a02);
    }

    /**
     * Contact's generational abbreviation FTK: Name suffix
     */
    @Override
    public String getGeneration() {
        return this.getStringItem(0x3a05);
    }

    /**
     * Contacts given name
     */
    @Override
    public String getGivenName() {
        return this.getStringItem(0x3a06);
    }

    /**
     * Contacts Government ID Number
     */
    @Override
    public String getGovernmentIdNumber() {
        return this.getStringItem(0x3a07);
    }

    /**
     * Business/Office Telephone Number
     */
    @Override
    public String getBusinessTelephoneNumber() {
        return this.getStringItem(0x3a08);
    }

    /**
     * Home Telephone Number
     */
    @Override
    public String getHomeTelephoneNumber() {
        return this.getStringItem(0x3a09);
    }

    /**
     * Contacts initials
     */
    @Override
    public String getInitials() {
        return this.getStringItem(0x3a0a);
    }

    /**
     * Keyword
     */
    @Override
    public String getKeyword() {
        return this.getStringItem(0x3a0b);
    }

    /**
     * Contact's language
     */
    @Override
    public String getLanguage() {
        return this.getStringItem(0x3a0c);
    }

    /**
     * Contact's location
     */
    @Override
    public String getLocation() {
        return this.getStringItem(0x3a0d);
    }

    /**
     * MHS Common Name
     */
    @Override
    public String getMhsCommonName() {
        return this.getStringItem(0x3a0f);
    }

    /**
     * Organizational identification number
     */
    @Override
    public String getOrganizationalIdNumber() {
        return this.getStringItem(0x3a10);
    }

    /**
     * Contact's surname FTK: Last name
     */
    @Override
    public String getSurname() {
        return this.getStringItem(0x3a11);
    }

    /**
     * Original display name
     */
    @Override
    public String getOriginalDisplayName() {
        return this.getStringItem(0x3a13);
    }

    /**
     * Default Postal Address
     */
    @Override
    public String getPostalAddress() {
        return this.getStringItem(0x3a15);
    }

    /**
     * Contact's company name
     */
    @Override
    public String getCompanyName() {
        return this.getStringItem(0x3a16);
    }

    /**
     * Contact's job title FTK: Profession
     */
    @Override
    public String getTitle() {
        return this.getStringItem(0x3a17);
    }

    /**
     * Contact's department name Used in contact item
     */
    @Override
    public String getDepartmentName() {
        return this.getStringItem(0x3a18);
    }

    /**
     * Contact's office location
     */
    @Override
    public String getOfficeLocation() {
        return this.getStringItem(0x3a19);
    }

    /**
     * Primary Telephone
     */
    @Override
    public String getPrimaryTelephoneNumber() {
        return this.getStringItem(0x3a1a);
    }

    /**
     * Contact's secondary office (business) phone number
     */
    @Override
    public String getBusiness2TelephoneNumber() {
        return this.getStringItem(0x3a1b);
    }

    /**
     * Mobile Phone Number
     */
    @Override
    public String getMobileTelephoneNumber() {
        return this.getStringItem(0x3a1c);
    }

    /**
     * Radio Phone Number
     */
    @Override
    public String getRadioTelephoneNumber() {
        return this.getStringItem(0x3a1d);
    }

    /**
     * Car Phone Number
     */
    @Override
    public String getCarTelephoneNumber() {
        return this.getStringItem(0x3a1e);
    }

    /**
     * Other Phone Number
     */
    @Override
    public String getOtherTelephoneNumber() {
        return this.getStringItem(0x3a1f);
    }

    /**
     * Transmittable display name
     */
    @Override
    public String getTransmittableDisplayName() {
        return this.getStringItem(0x3a20);
    }

    /**
     * Pager Phone Number
     */
    @Override
    public String getPagerTelephoneNumber() {
        return this.getStringItem(0x3a21);
    }

    /**
     * Primary Fax Number
     */
    @Override
    public String getPrimaryFaxNumber() {
        return this.getStringItem(0x3a23);
    }

    /**
     * Contact's office (business) fax number
     */
    @Override
    public String getBusinessFaxNumber() {
        return this.getStringItem(0x3a24);
    }

    /**
     * Contact's home fax number
     */
    @Override
    public String getHomeFaxNumber() {
        return this.getStringItem(0x3a25);
    }

    /**
     * Business Address Country
     */
    @Override
    public String getBusinessAddressCountry() {
        return this.getStringItem(0x3a26);
    }

    /**
     * Business Address City
     */
    @Override
    public String getBusinessAddressCity() {
        return this.getStringItem(0x3a27);
    }

    /**
     * Business Address State
     */
    @Override
    public String getBusinessAddressStateOrProvince() {
        return this.getStringItem(0x3a28);
    }

    /**
     * Business Address Street
     */
    @Override
    public String getBusinessAddressStreet() {
        return this.getStringItem(0x3a29);
    }

    /**
     * Business Postal Code
     */
    @Override
    public String getBusinessPostalCode() {
        return this.getStringItem(0x3a2a);
    }

    /**
     * Business PO Box
     */
    @Override
    public String getBusinessPoBox() {
        return this.getStringItem(0x3a2b);
    }

    /**
     * Telex Number
     */
    @Override
    public String getTelexNumber() {
        return this.getStringItem(0x3a2c);
    }

    /**
     * ISDN Number
     */
    @Override
    public String getIsdnNumber() {
        return this.getStringItem(0x3a2d);
    }

    /**
     * Assistant Phone Number
     */
    @Override
    public String getAssistantTelephoneNumber() {
        return this.getStringItem(0x3a2e);
    }

    /**
     * Home Phone 2
     */
    @Override
    public String getHome2TelephoneNumber() {
        return this.getStringItem(0x3a2f);
    }

    /**
     * Assistant�s Name
     */
    @Override
    public String getAssistant() {
        return this.getStringItem(0x3a30);
    }

    /**
     * Hobbies
     */
    @Override
    public String getHobbies() {
        return this.getStringItem(0x3a43);
    }

    /**
     * Middle Name
     */
    @Override
    public String getMiddleName() {
        return this.getStringItem(0x3a44);
    }

    /**
     * Display Name Prefix (Contact Title)
     */
    @Override
    public String getDisplayNamePrefix() {
        return this.getStringItem(0x3a45);
    }

    /**
     * Profession
     */
    @Override
    public String getProfession() {
        return this.getStringItem(0x3a46);
    }

    /**
     * Preferred By Name
     */
    @Override
    public String getPreferredByName() {
        return this.getStringItem(0x3a47);
    }

    /**
     * Spouse�s Name
     */
    @Override
    public String getSpouseName() {
        return this.getStringItem(0x3a48);
    }

    /**
     * Computer Network Name
     */
    @Override
    public String getComputerNetworkName() {
        return this.getStringItem(0x3a49);
    }

    /**
     * Customer ID
     */
    @Override
    public String getCustomerId() {
        return this.getStringItem(0x3a4a);
    }

    /**
     * TTY/TDD Phone
     */
    @Override
    public String getTtytddPhoneNumber() {
        return this.getStringItem(0x3a4b);
    }

    /**
     * Ftp Site
     */
    @Override
    public String getFtpSite() {
        return this.getStringItem(0x3a4c);
    }

    /**
     * Manager�s Name
     */
    @Override
    public String getManagerName() {
        return this.getStringItem(0x3a4e);
    }

    /**
     * Nickname
     */
    @Override
    public String getNickname() {
        return this.getStringItem(0x3a4f);
    }

    /**
     * Personal Home Page
     */
    @Override
    public String getPersonalHomePage() {
        return this.getStringItem(0x3a50);
    }

    /**
     * Business Home Page
     */
    @Override
    public String getBusinessHomePage() {
        return this.getStringItem(0x3a51);
    }

    /**
     * Note
     */
    @Override
    public String getNote() {
        return this.getStringItem(0x6619);
    }

    String getNamedStringItem(final int key) {
        final int id = this.pstFile.getNameToIdMapItem(key, PSTFile.PSETID_Address);
        if (id != -1) {
            return this.getStringItem(id);
        }
        return "";
    }

    @Override
    public String getSMTPAddress() {
        return this.getNamedStringItem(0x00008084);
    }

    /**
     * Company Main Phone
     */
    @Override
    public String getCompanyMainPhoneNumber() {
        return this.getStringItem(0x3a57);
    }

    /**
     * Children's names
     */
    @Override
    public String getChildrensNames() {
        return this.getStringItem(0x3a58);
    }

    /**
     * Home Address City
     */
    @Override
    public String getHomeAddressCity() {
        return this.getStringItem(0x3a59);
    }

    /**
     * Home Address Country
     */
    @Override
    public String getHomeAddressCountry() {
        return this.getStringItem(0x3a5a);
    }

    /**
     * Home Address Postal Code
     */
    @Override
    public String getHomeAddressPostalCode() {
        return this.getStringItem(0x3a5b);
    }

    /**
     * Home Address State or Province
     */
    @Override
    public String getHomeAddressStateOrProvince() {
        return this.getStringItem(0x3a5c);
    }

    /**
     * Home Address Street
     */
    @Override
    public String getHomeAddressStreet() {
        return this.getStringItem(0x3a5d);
    }

    /**
     * Home Address Post Office Box
     */
    @Override
    public String getHomeAddressPostOfficeBox() {
        return this.getStringItem(0x3a5e);
    }

    /**
     * Other Address City
     */
    @Override
    public String getOtherAddressCity() {
        return this.getStringItem(0x3a5f);
    }

    /**
     * Other Address Country
     */
    @Override
    public String getOtherAddressCountry() {
        return this.getStringItem(0x3a60);
    }

    /**
     * Other Address Postal Code
     */
    @Override
    public String getOtherAddressPostalCode() {
        return this.getStringItem(0x3a61);
    }

    /**
     * Other Address State
     */
    @Override
    public String getOtherAddressStateOrProvince() {
        return this.getStringItem(0x3a62);
    }

    /**
     * Other Address Street
     */
    @Override
    public String getOtherAddressStreet() {
        return this.getStringItem(0x3a63);
    }

    /**
     * Other Address Post Office box
     */
    @Override
    public String getOtherAddressPostOfficeBox() {
        return this.getStringItem(0x3a64);
    }

    ///////////////////////////////////////////////////
    // Below are the values from the name to id map...
    ///////////////////////////////////////////////////

    /**
     * File under FTK: File as
     */
    @Override
    public String getFileUnder() {
        return this.getNamedStringItem(0x00008005);
    }

    /**
     * Home Address
     */
    @Override
    public String getHomeAddress() {
        return this.getNamedStringItem(0x0000801a);
    }

    /**
     * Business Address
     */
    @Override
    public String getWorkAddress() {
        return this.getNamedStringItem(0x0000801b);
    }

    /**
     * Other Address
     */
    @Override
    public String getOtherAddress() {
        return this.getNamedStringItem(0x0000801c);
    }

    /**
     * Selected Mailing Address
     */
    @Override
    public int getPostalAddressId() {
        return this.getIntItem(this.pstFile.getNameToIdMapItem(0x00008022, PSTFile.PSETID_Address));
    }

    /**
     * Webpage
     */
    @Override
    public String getHtml() {
        return this.getNamedStringItem(0x0000802b);
    }

    /**
     * Business Address City
     */
    @Override
    public String getWorkAddressStreet() {
        return this.getNamedStringItem(0x00008045);
    }

    /**
     * Business Address Street
     */
    @Override
    public String getWorkAddressCity() {
        return this.getNamedStringItem(0x00008046);
    }

    /**
     * Business Address State
     */
    @Override
    public String getWorkAddressState() {
        return this.getNamedStringItem(0x00008047);
    }

    /**
     * Business Address Postal Code
     */
    @Override
    public String getWorkAddressPostalCode() {
        return this.getNamedStringItem(0x00008048);
    }

    /**
     * Business Address Country
     */
    @Override
    public String getWorkAddressCountry() {
        return this.getNamedStringItem(0x00008049);
    }

    /**
     * Business Address Country
     */
    @Override
    public String getWorkAddressPostOfficeBox() {
        return this.getNamedStringItem(0x0000804A);
    }

    /**
     * IM Address
     */
    @Override
    public String getInstantMessagingAddress() {
        return this.getNamedStringItem(0x00008062);
    }

    /**
     * E-mail1 Display Name
     */
    @Override
    public String getEmail1DisplayName() {
        return this.getNamedStringItem(0x00008080);
    }

    /**
     * E-mail1 Address Type
     */
    @Override
    public String getEmail1AddressType() {
        return this.getNamedStringItem(0x00008082);
    }

    /**
     * E-mail1 Address
     */
    @Override
    public String getEmail1EmailAddress() {
        return this.getNamedStringItem(0x00008083);
    }

    /**
     * E-mail1 Display Name
     */
    @Override
    public String getEmail1OriginalDisplayName() {
        return this.getNamedStringItem(0x00008084);
    }

    /**
     * E-mail1 type
     */
    @Override
    public String getEmail1EmailType() {
        return this.getNamedStringItem(0x00008087);
    }

    /**
     * E-mail2 display name
     */
    @Override
    public String getEmail2DisplayName() {
        return this.getNamedStringItem(0x00008090);
    }

    /**
     * E-mail2 address type
     */
    @Override
    public String getEmail2AddressType() {
        return this.getNamedStringItem(0x00008092);
    }

    /**
     * E-mail2 e-mail address
     */
    @Override
    public String getEmail2EmailAddress() {
        return this.getNamedStringItem(0x00008093);
    }

    /**
     * E-mail2 original display name
     */
    @Override
    public String getEmail2OriginalDisplayName() {
        return this.getNamedStringItem(0x00008094);
    }

    /**
     * E-mail3 display name
     */
    @Override
    public String getEmail3DisplayName() {
        return this.getNamedStringItem(0x000080a0);
    }

    /**
     * E-mail3 address type
     */
    @Override
    public String getEmail3AddressType() {
        return this.getNamedStringItem(0x000080a2);
    }

    /**
     * E-mail3 e-mail address
     */
    @Override
    public String getEmail3EmailAddress() {
        return this.getNamedStringItem(0x000080a3);
    }

    /**
     * E-mail3 original display name
     */
    @Override
    public String getEmail3OriginalDisplayName() {
        return this.getNamedStringItem(0x000080a4);
    }

    /**
     * Fax1 Address Type
     */
    @Override
    public String getFax1AddressType() {
        return this.getNamedStringItem(0x000080b2);
    }

    /**
     * Fax1 Email Address
     */
    @Override
    public String getFax1EmailAddress() {
        return this.getNamedStringItem(0x000080b3);
    }

    /**
     * Fax1 Original Display Name
     */
    @Override
    public String getFax1OriginalDisplayName() {
        return this.getNamedStringItem(0x000080b4);
    }

    /**
     * Fax2 Address Type
     */
    @Override
    public String getFax2AddressType() {
        return this.getNamedStringItem(0x000080c2);
    }

    /**
     * Fax2 Email Address
     */
    @Override
    public String getFax2EmailAddress() {
        return this.getNamedStringItem(0x000080c3);
    }

    /**
     * Fax2 Original Display Name
     */
    @Override
    public String getFax2OriginalDisplayName() {
        return this.getNamedStringItem(0x000080c4);
    }

    /**
     * Fax3 Address Type
     */
    @Override
    public String getFax3AddressType() {
        return this.getNamedStringItem(0x000080d2);
    }

    /**
     * Fax3 Email Address
     */
    @Override
    public String getFax3EmailAddress() {
        return this.getNamedStringItem(0x000080d3);
    }

    /**
     * Fax3 Original Display Name
     */
    @Override
    public String getFax3OriginalDisplayName() {
        return this.getNamedStringItem(0x000080d4);
    }

    /**
     * Free/Busy Location (URL)
     */
    @Override
    public String getFreeBusyLocation() {
        return this.getNamedStringItem(0x000080d8);
    }

    /**
     * Birthday
     */
    @Override
    public Date getBirthday() {
        return this.getDateItem(0x3a42);
    }

    /**
     * (Wedding) Anniversary
     */
    @Override
    public Date getAnniversary() {
        return this.getDateItem(0x3a41);
    }

    @Override
    public String toString() {

        return "Contact's Account name: " + this.getAccount() + "\n" + "Display Name: " + this.getGivenName() + " "
            + this.getSurname() + " (" + this.getSMTPAddress() + ")\n" + "Email1 Address Type: "
            + this.getEmail1AddressType() + "\n" + "Email1 Address: " + this.getEmail1EmailAddress() + "\n"
            + "Callback telephone number: " + this.getCallbackTelephoneNumber() + "\n"
            + "Contact's generational abbreviation (name suffix): " + this.getGeneration() + "\n"
            + "Contacts given name: " + this.getGivenName() + "\n" + "Contacts Government ID Number: "
            + this.getGovernmentIdNumber() + "\n" + "Business/Office Telephone Number: "
            + this.getBusinessTelephoneNumber() + "\n" + "Home Telephone Number: " + this.getHomeTelephoneNumber()
            + "\n" + "Contacts initials: " + this.getInitials() + "\n" + "Keyword: " + this.getKeyword() + "\n"
            + "Contact's language: " + this.getLanguage() + "\n" + "Contact's location: " + this.getLocation() + "\n"
            + "MHS Common Name: " + this.getMhsCommonName() + "\n" + "Organizational identification number: "
            + this.getOrganizationalIdNumber() + "\n" + "Contact's surname  (Last name): " + this.getSurname() + "\n"
            + "Original display name: " + this.getOriginalDisplayName() + "\n" + "Default Postal Address: "
            + this.getPostalAddress() + "\n" + "Contact's company name: " + this.getCompanyName() + "\n"
            + "Contact's job title (Profession): " + this.getTitle() + "\n"
            + "Contact's department name  Used in contact ite: " + this.getDepartmentName() + "\n"
            + "Contact's office location: " + this.getOfficeLocation() + "\n" + "Primary Telephone: "
            + this.getPrimaryTelephoneNumber() + "\n" + "Contact's secondary office (business) phone number: "
            + this.getBusiness2TelephoneNumber() + "\n" + "Mobile Phone Number: " + this.getMobileTelephoneNumber()
            + "\n" + "Radio Phone Number: " + this.getRadioTelephoneNumber() + "\n" + "Car Phone Number: "
            + this.getCarTelephoneNumber() + "\n" + "Other Phone Number: " + this.getOtherTelephoneNumber() + "\n"
            + "Transmittable display name: " + this.getTransmittableDisplayName() + "\n" + "Pager Phone Number: "
            + this.getPagerTelephoneNumber() + "\n" + "Primary Fax Number: " + this.getPrimaryFaxNumber() + "\n"
            + "Contact's office (business) fax numbe: " + this.getBusinessFaxNumber() + "\n"
            + "Contact's home fax number: " + this.getHomeFaxNumber() + "\n" + "Business Address Country: "
            + this.getBusinessAddressCountry() + "\n" + "Business Address City: " + this.getBusinessAddressCity() + "\n"
            + "Business Address State: " + this.getBusinessAddressStateOrProvince() + "\n" + "Business Address Street: "
            + this.getBusinessAddressStreet() + "\n" + "Business Postal Code: " + this.getBusinessPostalCode() + "\n"
            + "Business PO Box: " + this.getBusinessPoBox() + "\n" + "Telex Number: " + this.getTelexNumber() + "\n"
            + "ISDN Number: " + this.getIsdnNumber() + "\n" + "Assistant Phone Number: "
            + this.getAssistantTelephoneNumber() + "\n" + "Home Phone 2: " + this.getHome2TelephoneNumber() + "\n"
            + "Assistant's Name: " + this.getAssistant() + "\n" + "Hobbies: " + this.getHobbies() + "\n"
            + "Middle Name: " + this.getMiddleName() + "\n" + "Display Name Prefix (Contact Title): "
            + this.getDisplayNamePrefix() + "\n" + "Profession: " + this.getProfession() + "\n" + "Preferred By Name: "
            + this.getPreferredByName() + "\n" + "Spouse's Name: " + this.getSpouseName() + "\n"
            + "Computer Network Name: " + this.getComputerNetworkName() + "\n" + "Customer ID: " + this.getCustomerId()
            + "\n" + "TTY/TDD Phone: " + this.getTtytddPhoneNumber() + "\n" + "Ftp Site: " + this.getFtpSite() + "\n"
            + "Manager's Name: " + this.getManagerName() + "\n" + "Nickname: " + this.getNickname() + "\n"
            + "Personal Home Page: " + this.getPersonalHomePage() + "\n" + "Business Home Page: "
            + this.getBusinessHomePage() + "\n" + "Company Main Phone: " + this.getCompanyMainPhoneNumber() + "\n"
            + "Childrens names: " + this.getChildrensNames() + "\n" + "Home Address City: " + this.getHomeAddressCity()
            + "\n" + "Home Address Country: " + this.getHomeAddressCountry() + "\n" + "Home Address Postal Code: "
            + this.getHomeAddressPostalCode() + "\n" + "Home Address State or Province: "
            + this.getHomeAddressStateOrProvince() + "\n" + "Home Address Street: " + this.getHomeAddressStreet() + "\n"
            + "Home Address Post Office Box: " + this.getHomeAddressPostOfficeBox() + "\n" + "Other Address City: "
            + this.getOtherAddressCity() + "\n" + "Other Address Country: " + this.getOtherAddressCountry() + "\n"
            + "Other Address Postal Code: " + this.getOtherAddressPostalCode() + "\n" + "Other Address State: "
            + this.getOtherAddressStateOrProvince() + "\n" + "Other Address Street: " + this.getOtherAddressStreet()
            + "\n" + "Other Address Post Office box: " + this.getOtherAddressPostOfficeBox() + "\n" + "\n"
            + this.getBody();
    }
}
