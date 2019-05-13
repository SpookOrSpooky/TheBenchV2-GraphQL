package com.uottawa.thebench.model;/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.26.1-f40f105-3613 modeling language!*/


import android.graphics.Bitmap;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// line 5 "model.ump"
// line 51 "model.ump"
public class User {

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //User Attributes
    private int id;
    private String name;
    private Bitmap avatar;
    private int points;

    //User Associations
    private Household household;
    private List<Chore> createdChore;
    private List<Chore> assignedChore;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public User(int aId, String aName, Bitmap aAvatar, Household aHousehold, int aPoints) {
        id = aId;
        name = aName;
        avatar = aAvatar;
        points = aPoints;
        boolean didAddHousehold = setHousehold(aHousehold);
        if (!didAddHousehold) {
            //throw new RuntimeException("Unable to create user due to household");
        }
        createdChore = new ArrayList<Chore>();
        assignedChore = new ArrayList<Chore>();
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

    public boolean setAvatar(Bitmap aAvatar) {
        boolean wasSet = false;
        avatar = aAvatar;
        wasSet = true;
        return wasSet;
    }

    public boolean setPoints(int aPoints) {
        boolean wasSet = false;
        points = aPoints;
        wasSet = true;
        return wasSet;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public int getPoints() {
        return points;
    }

    public Household getHousehold() {
        return household;
    }

    public Chore getCreatedChore(int index) {
        Chore aCreatedChore = createdChore.get(index);
        return aCreatedChore;
    }

    public List<Chore> getCreatedChore() {
        List<Chore> newCreatedChore = Collections.unmodifiableList(createdChore);
        return newCreatedChore;
    }

    public int numberOfCreatedChore() {
        int number = createdChore.size();
        return number;
    }

    public boolean hasCreatedChore() {
        boolean has = createdChore.size() > 0;
        return has;
    }

    public int indexOfCreatedChore(Chore aCreatedChore) {
        int index = createdChore.indexOf(aCreatedChore);
        return index;
    }

    public Chore getAssignedChore(int index) {
        Chore aAssignedChore = assignedChore.get(index);
        return aAssignedChore;
    }

    public List<Chore> getAssignedChore() {
        List<Chore> newAssignedChore = Collections.unmodifiableList(assignedChore);
        return newAssignedChore;
    }

    public int numberOfAssignedChore() {
        int number = assignedChore.size();
        return number;
    }

    public boolean hasAssignedChore() {
        boolean has = assignedChore.size() > 0;
        return has;
    }

    public int indexOfAssignedChore(Chore aAssignedChore) {
        int index = assignedChore.indexOf(aAssignedChore);
        return index;
    }

    public boolean setHousehold(Household aHousehold) {
        boolean wasSet = false;
        if (aHousehold == null) {
            return wasSet;
        }

        Household existingHousehold = household;
        household = aHousehold;
        if (existingHousehold != null && !existingHousehold.equals(aHousehold)) {
            existingHousehold.removeUser(this);
        }
        household.addUser(this);
        wasSet = true;
        return wasSet;
    }

    public static int minimumNumberOfCreatedChore() {
        return 0;
    }

    public Chore addCreatedChore(int aId, String aName, String aDescription, Date aDeadline, boolean aIsAllDay, int aPoints, Chore.Status aStatus, Chore.RecurrencePattern aRecurrencePattern, Date aRecurrenceEndDate) {
        return new Chore(aId, aName, aDescription, aDeadline, aIsAllDay, aPoints, aStatus, aRecurrencePattern, aRecurrenceEndDate, this);
    }

    public boolean addCreatedChore(Chore aCreatedChore) {
        boolean wasAdded = false;
        if (createdChore.contains(aCreatedChore)) {
            return false;
        }
        User existingCreator = aCreatedChore.getCreator();
        boolean isNewCreator = existingCreator != null && !this.equals(existingCreator);
        if (isNewCreator) {
            aCreatedChore.setCreator(this);
        } else {
            createdChore.add(aCreatedChore);
        }
        wasAdded = true;
        return wasAdded;
    }

    public boolean removeCreatedChore(Chore aCreatedChore) {
        boolean wasRemoved = false;
        //Unable to remove aCreatedChore, as it must always have a creator
        if (!this.equals(aCreatedChore.getCreator())) {
            createdChore.remove(aCreatedChore);
            wasRemoved = true;
        }
        return wasRemoved;
    }

    public boolean addCreatedChoreAt(Chore aCreatedChore, int index) {
        boolean wasAdded = false;
        if (addCreatedChore(aCreatedChore)) {
            if (index < 0) {
                index = 0;
            }
            if (index > numberOfCreatedChore()) {
                index = numberOfCreatedChore() - 1;
            }
            createdChore.remove(aCreatedChore);
            createdChore.add(index, aCreatedChore);
            wasAdded = true;
        }
        return wasAdded;
    }

    public boolean addOrMoveCreatedChoreAt(Chore aCreatedChore, int index) {
        boolean wasAdded = false;
        if (createdChore.contains(aCreatedChore)) {
            if (index < 0) {
                index = 0;
            }
            if (index > numberOfCreatedChore()) {
                index = numberOfCreatedChore() - 1;
            }
            createdChore.remove(aCreatedChore);
            createdChore.add(index, aCreatedChore);
            wasAdded = true;
        } else {
            wasAdded = addCreatedChoreAt(aCreatedChore, index);
        }
        return wasAdded;
    }

    public static int minimumNumberOfAssignedChore() {
        return 0;
    }

    public boolean addAssignedChore(Chore aAssignedChore) {
        boolean wasAdded = false;
        if (assignedChore.contains(aAssignedChore)) {
            return false;
        }
        User existingAssignee = aAssignedChore.getAssignee();
        if (existingAssignee == null) {
            aAssignedChore.setAssignee(this);
        } else if (!this.equals(existingAssignee)) {
            existingAssignee.removeAssignedChore(aAssignedChore);
            addAssignedChore(aAssignedChore);
        } else {
            assignedChore.add(aAssignedChore);
        }
        wasAdded = true;
        return wasAdded;
    }

    public boolean removeAssignedChore(Chore aAssignedChore) {
        boolean wasRemoved = false;
        if (assignedChore.contains(aAssignedChore)) {
            assignedChore.remove(aAssignedChore);
            aAssignedChore.setAssignee(null);
            wasRemoved = true;
        }
        return wasRemoved;
    }

    public boolean addAssignedChoreAt(Chore aAssignedChore, int index) {
        boolean wasAdded = false;
        if (addAssignedChore(aAssignedChore)) {
            if (index < 0) {
                index = 0;
            }
            if (index > numberOfAssignedChore()) {
                index = numberOfAssignedChore() - 1;
            }
            assignedChore.remove(aAssignedChore);
            assignedChore.add(index, aAssignedChore);
            wasAdded = true;
        }
        return wasAdded;
    }

    public boolean addOrMoveAssignedChoreAt(Chore aAssignedChore, int index) {
        boolean wasAdded = false;
        if (assignedChore.contains(aAssignedChore)) {
            if (index < 0) {
                index = 0;
            }
            if (index > numberOfAssignedChore()) {
                index = numberOfAssignedChore() - 1;
            }
            assignedChore.remove(aAssignedChore);
            assignedChore.add(index, aAssignedChore);
            wasAdded = true;
        } else {
            wasAdded = addAssignedChoreAt(aAssignedChore, index);
        }
        return wasAdded;
    }

    public void delete() {
        Household placeholderHousehold = household;
        this.household = null;
        placeholderHousehold.removeUser(this);
        for (int i = createdChore.size(); i > 0; i--) {
            Chore aCreatedChore = createdChore.get(i - 1);
            aCreatedChore.delete();
        }
        while (!assignedChore.isEmpty()) {
            assignedChore.get(0).setAssignee(null);
        }
    }

    public boolean isAdmin() {
        return this instanceof Admin;
    }

    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "name" + ":" + getName() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "household = " + (getHousehold() != null ? Integer.toHexString(System.identityHashCode(getHousehold())) : "null");
    }
}