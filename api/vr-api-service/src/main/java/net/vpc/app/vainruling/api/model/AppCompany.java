/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.api.model;

import net.vpc.app.vainruling.api.ui.UIConstants;
import net.vpc.upa.UserFieldModifier;
import net.vpc.upa.config.Entity;
import net.vpc.upa.config.Field;
import net.vpc.upa.config.Id;
import net.vpc.upa.config.Path;
import net.vpc.upa.config.Property;
import net.vpc.upa.config.Sequence;

/**
 *
 * @author vpc
 */
@Entity(listOrder = "name")
@Path("Contact")
public class AppCompany {

    @Id
    @Sequence

    private int id;
    @Field(modifiers = UserFieldModifier.MAIN)
    private String name;
    @Property(name = UIConstants.FIELD_FORM_SPAN, value = "MAX_VALUE")
    private String name2;
    @Property(name = UIConstants.FIELD_FORM_CONTROL, value = "textarea")
    private String address;
    @Field(modifiers = UserFieldModifier.SUMMARY)
    private AppCountry country;
    @Field(modifiers = UserFieldModifier.SUMMARY)
    private AppGovernorate governorate;
    @Field(modifiers = UserFieldModifier.SUMMARY)
    private AppSettlement settlement;
    private AppIndustry industry;
    private String postalCode;
    private String phone;
    private String fax;
    private String mainContact;
    private String mainContactAddress;
    private String mainWebSite;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public AppGovernorate getGovernorate() {
        return governorate;
    }

    public void setGovernorate(AppGovernorate governorate) {
        this.governorate = governorate;
    }

    public AppIndustry getIndustry() {
        return industry;
    }

    public void setIndustry(AppIndustry industry) {
        this.industry = industry;
    }

    public AppCountry getCountry() {
        return country;
    }

    public void setCountry(AppCountry country) {
        this.country = country;
    }

    public AppSettlement getSettlement() {
        return settlement;
    }

    public void setSettlement(AppSettlement settlement) {
        this.settlement = settlement;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getMainContact() {
        return mainContact;
    }

    public void setMainContact(String mainContact) {
        this.mainContact = mainContact;
    }

    public String getMainContactAddress() {
        return mainContactAddress;
    }

    public void setMainContactAddress(String mainContactAddress) {
        this.mainContactAddress = mainContactAddress;
    }

    public String getMainWebSite() {
        return mainWebSite;
    }

    public void setMainWebSite(String mainWebSite) {
        this.mainWebSite = mainWebSite;
    }
    
}
