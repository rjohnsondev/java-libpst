/**
 * 
 */
package com.pff;

import java.io.IOException;
import java.util.HashMap;

/**
 * @author toweruser
 *
 */
public class PSTContact extends PSTMessage {

	/**
	 * @param theFile
	 * @param descriptorIndexNode
	 * @throws PSTException
	 * @throws IOException
	 */
	public PSTContact(PSTFile theFile, DescriptorIndexNode descriptorIndexNode)
		throws PSTException, IOException {
		super(theFile, descriptorIndexNode);
	}

	/**
	 * @param theFile
	 * @param folderIndexNode
	 * @param table
	 * @param localDescriptorItems
	 */
	public PSTContact(PSTFile theFile, DescriptorIndexNode folderIndexNode, PSTTableBC table, HashMap<Integer, PSTDescriptorItem> localDescriptorItems) {
		super(theFile, folderIndexNode, table, localDescriptorItems);
	}
	

	/**
	 * Contact's Account name 
	 */
	public String getAccount() {
		return this.getStringItem(0x3a00);
	}
	/**
	 * Callback telephone number 
	 */
	public String getCallbackTelephoneNumber() {
		 return this.getStringItem(0x3a02);
	}
	/**
	 * Contact's generational abbreviation  FTK: Name suffix
	 */
	 public String getGeneration() {
		 return this.getStringItem(0x3a05);
	 }
	/**
	 * Contacts given name 
	 */
	 public String getGivenName() {
		 return this.getStringItem(0x3a06);
	 }
	/**
	 * Contacts Government ID Number 
	 */
	 public String getGovernmentIdNumber() {
		 return this.getStringItem(0x3a07);
	 }

	/**
	 * Business/Office Telephone Number 
	 */
	 public String getBusinessTelephoneNumber() {
		return this.getStringItem(0x3a08);
	 }
	/**
	 * Home Telephone Number 
	 */
	 public String getHomeTelephoneNumber() {
		 return this.getStringItem(0x3a09);
	 }
	/**
	 * Contacts initials 
	 */
	 public String getInitials() {
		 return this.getStringItem(0x3a0a);
	 }
	/**
	 * Keyword 
	 */
	 public String getKeyword() {
		 return this.getStringItem(0x3a0b);
	 }
	/**
	 * Contact’s language 
	 */
	 public String getLanguage() {
		 return this.getStringItem(0x3a0c);
	 }
	/**
	 * Contact’s location 
	 */
	 public String getLocation() {
		 return this.getStringItem(0x3a0d);
	 }
	/**
	 * MHS Common Name 
	 */
	 public String getMhsCommonName() {
		 return this.getStringItem(0x3a0f);
	 }
	/**
	 * Organizational identification number 
	 */
	 public String getOrganizationalIdNumber() {
		 return this.getStringItem(0x3a10);
	 }
	/**
	 * Contact's surname  FTK: Last name
	 */
	 public String getSurname() {
		 return this.getStringItem(0x3a11);
	 }
	/**
	 * Original display name 
	 */
	 public String getOriginalDisplayName() {
		 return this.getStringItem(0x3a13);
	 }
	/**
	 * Default Postal Address 
	 */
	 public String getPostalAddress() {
		 return this.getStringItem(0x3a15);
	 }
	/**
	 * Contact's company name 
	 */
	 public String getCompanyName() {
		 return this.getStringItem(0x3a16);
	 }
	/**
	 * Contact's job title  FTK: Profession
	 */
	 public String getTitle() {
		 return this.getStringItem(0x3a17);
	 }
	/**
	 * Contact's department name  Used in contact item
	 */
	 public String getDepartmentName() {
		 return this.getStringItem(0x3a18);
	 }
	/**
	 * Contact's office location 
	 */
	 public String getOfficeLocation() {
		 return this.getStringItem(0x3a19);
	 }
	/**
	 * Primary Telephone 
	 */
	 public String getPrimaryTelephoneNumber() {
		 return this.getStringItem(0x3a1a);
	 }

	/**
	 * Contact's secondary office (business) phone number 
	 */
	 public String getBusiness2TelephoneNumber() {
		return this.getStringItem(0x3a1b);
	 }

	/**
	 * Mobile Phone Number 
	 */
	 public String getMobileTelephoneNumber() {
		 return this.getStringItem(0x3a1c);
	 }
	/**
	 * Radio Phone Number 
	 */
	 public String getRadioTelephoneNumber() {
		 return this.getStringItem(0x3a1d);
	 }
	/**
	 * Car Phone Number 
	 */
	 public String getCarTelephoneNumber() {
		 return this.getStringItem(0x3a1e);
	 }
	/**
	 * Other Phone Number 
	 */
	 public String getOtherTelephoneNumber() {
		 return this.getStringItem(0x3a1f);
	 }
	/**
	 * Transmittable display name 
	 */
	 public String getTransmittableDisplayName() {
		 return this.getStringItem(0x3a20);
	 }
	/**
	 * Pager Phone Number 
	 */
	 public String getPagerTelephoneNumber() {
		 return this.getStringItem(0x3a21);
	 }
	/**
	 * Primary Fax Number 
	 */
	 public String getPrimaryFaxNumber() {
		 return this.getStringItem(0x3a23);
	 }

	/**
	 * Contact's office (business) fax number
	 */
	 public String getBusinessFaxNumber() {
		 return this.getStringItem(0x3a24);
	 }
	/**
	 * Contact's home fax number 
	 */
	 public String getHomeFaxNumber() {
		 return this.getStringItem(0x3a25);
	 }
	 
	/**
	 * Business Address Country 
	 */
	 public String getBusinessAddressCountry() {
		 return this.getStringItem(0x3a26);
	 }
	/**
	 * Business Address City 
	 */
	 public String getBusinessAddressCity() {
		 return this.getStringItem(0x3a27);
	 }
	/**
	 * Business Address State 
	 */
	 public String getBusinessAddressStateOrProvince () {
		 return this.getStringItem(0x3a28);
	 }
	/**
	 * Business Address Street 
	 */
	 public String getBusinessAddressStreet() {
		 return this.getStringItem(0x3a29);
	 }
	/**
	 * Business Postal Code 
	 */
	 public String getBusinessPostalCode() {
		 return this.getStringItem(0x3a2a);
	 }
	/**
	 * Business PO Box 
	 */
	 public String getBusinessPoBox() {
		 return this.getStringItem(0x3a2b);
	 }
	/**
	 * Telex Number 
	 */
	 public String getTelexNumber() {
		 return this.getStringItem(0x3a2c);
	 }
	/**
	 * ISDN Number 
	 */
	 public String getIsdnNumber() {
		 return this.getStringItem(0x3a2d);
	 }
	/**
	 * Assistant Phone Number 
	 */
	 public String getAssistantTelephoneNumber() {
		 return this.getStringItem(0x3a2e);
	 }
	/**
	 * Home Phone 2 
	 */
	 public String getHome2TelephoneNumber() {
		 return this.getStringItem(0x3a2f);
	 }
	/**
	 * Assistant’s Name 
	 */
	 public String getAssistant() {
		 return this.getStringItem(0x3a30);
	 }
	/**
	 * Hobbies 
	 */
	 public String getHobbies() {
		 return this.getStringItem(0x3a43);
	 }
	/**
	 * Middle Name 
	 */
	 public String getMiddleName() {
		 return this.getStringItem(0x3a44);
	 }
	/**
	 * Display Name Prefix (Contact Title) 
	 */
	 public String getDisplayNamePrefix() {
		 return this.getStringItem(0x3a45);
	 }
	/**
	 * Profession 
	 */
	 public String getProfession() {
		 return this.getStringItem(0x3a46);
	 }
	/**
	 * Preferred By Name 
	 */
	 public String getPreferredByName() {
		 return this.getStringItem(0x3a47);
	 }
	/**
	 * Spouse’s Name 
	 */
	 public String getSpouseName() {
		 return this.getStringItem(0x3a48);
	 }
	/**
	 * Computer Network Name 
	 */
	 public String getComputerNetworkName() {
		 return this.getStringItem(0x3a49);
	 }
	/**
	 * Customer ID 
	 */
	 public String getCustomerId() {
		 return this.getStringItem(0x3a4a);
	 }
	/**
	 * TTY/TDD Phone 
	 */
	 public String getTtytddPhoneNumber() {
		 return this.getStringItem(0x3a4b);
	 }
	/**
	 * Ftp Site 
	 */
	 public String getFtpSite() {
		 return this.getStringItem(0x3a4c);
	 }
	/**
	 * Manager’s Name 
	 */
	 public String getManagerName() {
		 return this.getStringItem(0x3a4e);
	 }
	/**
	 * Nickname 
	 */
	 public String getNickname() {
		 return this.getStringItem(0x3a4f);
	 }
	/**
	 * Personal Home Page 
	 */
	 public String getPersonalHomePage() {
		 return this.getStringItem(0x3a50);
	 }
	/**
	 * Business Home Page 
	 */
	 public String getBusinessHomePage() {
		 return this.getStringItem(0x3a51);
	 }
	/**
	 * Company Main Phone 
	 */
	 public String getCompanyMainPhoneNumber() {
		 return this.getStringItem(0x3a57);
	 }
	/**
	 * Children's names 
	 */
	 public String getChildrensNames() {
		 return this.getStringItem(0x3a58);
	 }
	/**
	 * Home Address City 
	 */
	 public String getHomeAddressCity() {
		 return this.getStringItem(0x3a59);
	 }
	/**
	 * Home Address Country 
	 */
	 public String getHomeAddressCountry() {
		 return this.getStringItem(0x3a5a);
	 }
	/**
	 * Home Address Postal Code 
	 */
	 public String getHomeAddressPostalCode() {
		 return this.getStringItem(0x3a5b);
	 }
	/**
	 * Home Address State or Province 
	 */
	 public String getHomeAddressStateOrProvince () {
		 return this.getStringItem(0x3a5c);
	 }
	/**
	 * Home Address Street 
	 */
	 public String getHomeAddressStreet() {
		 return this.getStringItem(0x3a5d);
	 }
	/**
	 * Home Address Post Office Box 
	 */
	 public String getHomeAddressPostOfficeBox () {
		 return this.getStringItem(0x3a5e);
	 }
	/**
	 * Other Address City 
	 */
	 public String getOtherAddressCity() {
		 return this.getStringItem(0x3a5f);
	 }
	/**
	 * Other Address Country 
	 */
	 public String getOtherAddressCountry() {
		 return this.getStringItem(0x3a60);
	 }
	/**
	 * Other Address Postal Code 
	 */
	 public String getOtherAddressPostalCode() {
		 return this.getStringItem(0x3a61);
	 }
	/**
	 * Other Address State 
	 */
	 public String getOtherAddressStateOrProvince () {
		 return this.getStringItem(0x3a62);
	 }
	/**
	 * Other Address Street 
	 */
	 public String getOtherAddressStreet() {
		 return this.getStringItem(0x3a63);
	 }
	/**
	 * Other Address Post Office box 
	 */
	 public String getOtherAddressPostOfficeBox() {
		 return this.getStringItem(0x3a64);
	 }
	 
	 /**
	  * Business address
	  */
	 public String getBusinessAddress() {
		 return this.getStringItem(pstFile.getNameToIdMapItem(0x0000801b));
	 }
	 
	 public String getEmail() {
		 int emailKey = this.pstFile.getNameToIdMapItem(0x8083);
		 return this.getStringItem(emailKey);
	 }
	
	public String toString() {
		return 
			"Contact's Account name: "+getAccount()+"\n"+
			"Email Address: "+getEmail()+"\n"+
			"Callback telephone number: "+getCallbackTelephoneNumber()+"\n"+
			"Contact's generational abbreviation (name suffix): "+getGeneration()+"\n"+
			"Contacts given name: "+getGivenName()+"\n"+
			"Contacts Government ID Number: "+getGovernmentIdNumber()+"\n"+
			"Business/Office Telephone Number: "+getBusinessTelephoneNumber()+"\n"+
			"Home Telephone Number: "+getHomeTelephoneNumber()+"\n"+
			"Contacts initials: "+getInitials()+"\n"+
			"Keyword: "+getKeyword()+"\n"+
			"Contact's language: "+getLanguage()+"\n"+
			"Contact's location: "+getLocation()+"\n"+
			"MHS Common Name: "+getMhsCommonName()+"\n"+
			"Organizational identification number: "+getOrganizationalIdNumber()+"\n"+
			"Contact's surname  (Last name): "+getSurname()+"\n"+
			"Original display name: "+getOriginalDisplayName()+"\n"+
			"Default Postal Address: "+getPostalAddress()+"\n"+
			"Contact's company name: "+getCompanyName()+"\n"+
			"Contact's job title (Profession): "+getTitle()+"\n"+
			"Contact's department name  Used in contact ite: "+getDepartmentName()+"\n"+
			"Contact's office location: "+getOfficeLocation()+"\n"+
			"Primary Telephone: "+getPrimaryTelephoneNumber()+"\n"+
			"Contact's secondary office (business) phone number: "+getBusiness2TelephoneNumber()+"\n"+
			"Mobile Phone Number: "+getMobileTelephoneNumber()+"\n"+
			"Radio Phone Number: "+getRadioTelephoneNumber()+"\n"+
			"Car Phone Number: "+getCarTelephoneNumber()+"\n"+
			"Other Phone Number: "+getOtherTelephoneNumber()+"\n"+
			"Transmittable display name: "+getTransmittableDisplayName()+"\n"+
			"Pager Phone Number: "+getPagerTelephoneNumber()+"\n"+
			"Primary Fax Number: "+getPrimaryFaxNumber()+"\n"+
			"Contact's office (business) fax numbe: "+getBusinessFaxNumber()+"\n"+
			"Contact's home fax number: "+getHomeFaxNumber()+"\n"+
			"Business Address Full: " + getBusinessAddress()+"\n"+
			"Business Address Country: "+getBusinessAddressCountry()+"\n"+
			"Business Address City: "+getBusinessAddressCity()+"\n"+
			"Business Address State: "+getBusinessAddressStateOrProvince ()+"\n"+
			"Business Address Street: "+getBusinessAddressStreet()+"\n"+
			"Business Postal Code: "+getBusinessPostalCode()+"\n"+
			"Business PO Box: "+getBusinessPoBox()+"\n"+
			"Telex Number: "+getTelexNumber()+"\n"+
			"ISDN Number: "+getIsdnNumber()+"\n"+
			"Assistant Phone Number: "+getAssistantTelephoneNumber()+"\n"+
			"Home Phone 2: "+getHome2TelephoneNumber()+"\n"+
			"Assistant's Name: "+getAssistant()+"\n"+
			"Hobbies: "+getHobbies()+"\n"+
			"Middle Name: "+getMiddleName()+"\n"+
			"Display Name Prefix (Contact Title): "+getDisplayNamePrefix()+"\n"+
			"Profession: "+getProfession()+"\n"+
			"Preferred By Name: "+getPreferredByName()+"\n"+
			"Spouse’s Name: "+getSpouseName()+"\n"+
			"Computer Network Name: "+getComputerNetworkName()+"\n"+
			"Customer ID: "+getCustomerId()+"\n"+
			"TTY/TDD Phone: "+getTtytddPhoneNumber()+"\n"+
			"Ftp Site: "+getFtpSite()+"\n"+
			"Manager's Name: "+getManagerName()+"\n"+
			"Nickname: "+getNickname()+"\n"+
			"Personal Home Page: "+getPersonalHomePage()+"\n"+
			"Business Home Page: "+getBusinessHomePage()+"\n"+
			"Company Main Phone: "+getCompanyMainPhoneNumber()+"\n"+
			"Childrens names: "+getChildrensNames()+"\n"+
			"Home Address City: "+getHomeAddressCity()+"\n"+
			"Home Address Country: "+getHomeAddressCountry()+"\n"+
			"Home Address Postal Code: "+getHomeAddressPostalCode()+"\n"+
			"Home Address State or Province: "+getHomeAddressStateOrProvince ()+"\n"+
			"Home Address Street: "+getHomeAddressStreet()+"\n"+
			"Home Address Post Office Box: "+getHomeAddressPostOfficeBox ()+"\n"+
			"Other Address City: "+getOtherAddressCity()+"\n"+
			"Other Address Country: "+getOtherAddressCountry()+"\n"+
			"Other Address Postal Code: "+getOtherAddressPostalCode()+"\n"+
			"Other Address State: "+getOtherAddressStateOrProvince ()+"\n"+
			"Other Address Street: "+getOtherAddressStreet()+"\n"+
			"Other Address Post Office box: "+getOtherAddressPostOfficeBox()+"\n" +
			"\n"+
			this.getPlainText();
	}
}
