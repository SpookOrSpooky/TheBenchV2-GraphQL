package com.uottawa.thebench.model;/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.26.1-f40f105-3613 modeling language!*/


import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

// line 17 "model.ump"
// line 61 "model.ump"
public class Household {

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Household Attributes
    private int id;
    private String name;

    //Household Associations
    private List<User> users;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public Household(int aId, String aName) {
        name = aName;
        users = new ArrayList<User>();
    }

    //------------------------
    // INTERFACE
    //------------------------

    public boolean setId(int aId) {
        boolean wasSet = false;
        id = aId;
        wasSet = true;
        return wasSet;
    }

    public boolean setName(String aName) {
        boolean wasSet = false;
        name = aName;
        wasSet = true;
        return wasSet;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setUsers(List<User> aUsers) {
        users = aUsers;
    }

    public User getUser(int index) {
        User aUser = users.get(index);
        return aUser;
    }

    public List<User> getUsers() {
        return users;
    }

    public int numberOfUsers() {
        int number = users.size();
        return number;
    }

    public boolean hasUsers() {
        boolean has = users.size() > 0;
        return has;
    }

    public int indexOfUser(User aUser) {
        int index = users.indexOf(aUser);
        return index;
    }

    public static int minimumNumberOfUsers() {
        return 0;
    }

    public User addUser(int aId, String aName, Bitmap aAvatar, int aPoints) {
        return new User(aId, aName, aAvatar, this, aPoints);
    }

    public boolean addUser(User aUser) {
        boolean wasAdded = false;
        if (users.contains(aUser)) {
            return false;
        }
        Household existingHousehold = aUser.getHousehold();
        boolean isNewHousehold = existingHousehold != null && !this.equals(existingHousehold);
        if (isNewHousehold) {
            aUser.setHousehold(this);
        } else {
            users.add(aUser);
        }
        wasAdded = true;
        return wasAdded;
    }

    public boolean removeUser(User aUser) {
        boolean wasRemoved = false;
        //Unable to remove aUser, as it must always have a household
        if (!this.equals(aUser.getHousehold())) {
            users.remove(aUser);
            wasRemoved = true;
        }
        return wasRemoved;
    }

    public boolean addUserAt(User aUser, int index) {
        boolean wasAdded = false;
        if (addUser(aUser)) {
            if (index < 0) {
                index = 0;
            }
            if (index > numberOfUsers()) {
                index = numberOfUsers() - 1;
            }
            users.remove(aUser);
            users.add(index, aUser);
            wasAdded = true;
        }
        return wasAdded;
    }

    public boolean addOrMoveUserAt(User aUser, int index) {
        boolean wasAdded = false;
        if (users.contains(aUser)) {
            if (index < 0) {
                index = 0;
            }
            if (index > numberOfUsers()) {
                index = numberOfUsers() - 1;
            }
            users.remove(aUser);
            users.add(index, aUser);
            wasAdded = true;
        } else {
            wasAdded = addUserAt(aUser, index);
        }
        return wasAdded;
    }

    public void delete() {
        for (int i = users.size(); i > 0; i--) {
            User aUser = users.get(i - 1);
            aUser.delete();
        }
    }


    public String toString() {
        return super.toString() + "[" +
                "name" + ":" + getName() + "]";
    }
}