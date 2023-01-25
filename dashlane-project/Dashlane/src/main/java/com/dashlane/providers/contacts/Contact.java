package com.dashlane.providers.contacts;

import java.util.ArrayList;



public class Contact {
    public String id;
    public String name;
    public ArrayList<ContactEmail> emails;

    public Contact(String id, String name) {
        this.id = id;
        this.name = name;
        this.emails = new ArrayList<ContactEmail>();
    }

    public void addEmail(String address, String type) {
        emails.add(new ContactEmail(address, type));
    }
}
