package com.uottawa.thebench.model;/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.26.1-f40f105-3613 modeling language!*/


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// line 23 "model.ump"
// line 67 "model.ump"
public class Chore {

    //------------------------
    // ENUMERATIONS
    //------------------------

    public enum Status {ACTIVE, POSTPONED, COMPLETED}

    public enum RecurrencePattern {NONE, DAILY, WEEKLY, MONTHLY}

    //------------------------
    // MEMBER VARIABLES
    //------------------------

    //Chore Attributes
    private int id;
    private String name;
    private String description;
    private Date deadline;
    private boolean isAllDay;
    private int points;
    private Status status;
    private RecurrencePattern recurrencePattern;
    private Date recurrenceEndDate;

    //Chore Associations
    private User creator;
    private User assignee;
    private List<Resource> resources;

    //------------------------
    // CONSTRUCTOR
    //------------------------

    public Chore(int aId, String aName, String aDescription, Date aDeadline, boolean aIsAllDay, int aPoints, Status aStatus, RecurrencePattern aRecurrencePattern, Date aRecurrenceEndDate, User aCreator) {
        id = aId;
        name = aName;
        description = aDescription;
        deadline = aDeadline;
        isAllDay = aIsAllDay;
        points = aPoints;
        status = aStatus;
        recurrencePattern = aRecurrencePattern;
        recurrenceEndDate = aRecurrenceEndDate;
        boolean didAddCreator = setCreator(aCreator);
        if (!didAddCreator) {
//            throw new RuntimeException("Unable to create createdChore due to creator");
        }
        resources = new ArrayList<Resource>();
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

    public boolean setDescription(String aDescription) {
        boolean wasSet = false;
        description = aDescription;
        wasSet = true;
        return wasSet;
    }

    public boolean setDeadline(Date aDeadline) {
        boolean wasSet = false;
        deadline = aDeadline;
        wasSet = true;
        return wasSet;
    }

    public boolean setIsAllDay(boolean aIsAllDay) {
        boolean wasSet = false;
        isAllDay = aIsAllDay;
        wasSet = true;
        return wasSet;
    }

    public boolean setPoints(int aPoints) {
        boolean wasSet = false;
        points = aPoints;
        wasSet = true;
        return wasSet;
    }

    public boolean setStatus(Status aStatus) {
        boolean wasSet = false;
        status = aStatus;
        wasSet = true;
        return wasSet;
    }

    public boolean setRecurrencePattern(RecurrencePattern aRecurrencePattern) {
        boolean wasSet = false;
        recurrencePattern = aRecurrencePattern;
        wasSet = true;
        return wasSet;
    }

    public boolean setRecurrenceEndDate(Date aRecurrenceEndDate) {
        boolean wasSet = false;
        recurrenceEndDate = aRecurrenceEndDate;
        wasSet = true;
        return wasSet;
    }

    public boolean setResources(List<Resource> aResources) {
        boolean wasSet = false;
        resources = aResources;
        wasSet = true;
        return wasSet;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getDeadline() {
        return deadline;
    }

    public boolean isAllDay() {
        return isAllDay;
    }

    public int getPoints() {
        return points;
    }

    public Status getStatus() {
        return status;
    }

    public RecurrencePattern getRecurrencePattern() {
        return recurrencePattern;
    }

    public Date getRecurrenceEndDate() {
        return recurrenceEndDate;
    }

    public User getCreator() {
        return creator;
    }

    public User getAssignee() {
        return assignee;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public boolean hasCreator() {
        boolean has = creator != null;
        return has;
    }

    public boolean hasAssignee() {
        boolean has = assignee != null;
        return has;
    }

    public Resource getResource(int index) {
        Resource aResource = resources.get(index);
        return aResource;
    }

    public int numberOfResources() {
        int number = resources.size();
        return number;
    }

    public boolean hasResources() {
        boolean has = resources.size() > 0;
        return has;
    }

    public int indexOfResource(Resource aResource) {
        int index = resources.indexOf(aResource);
        return index;
    }

    public boolean setCreator(User aCreator) {
        boolean wasSet = false;
        if (aCreator == null) {
            return wasSet;
        }

        User existingCreator = creator;
        creator = aCreator;
        if (existingCreator != null && !existingCreator.equals(aCreator)) {
            existingCreator.removeCreatedChore(this);
        }
        creator.addCreatedChore(this);
        wasSet = true;
        return wasSet;
    }

    public boolean setAssignee(User aAssignee) {
        boolean wasSet = false;
        User existingAssignee = assignee;
        assignee = aAssignee;
        if (existingAssignee != null && !existingAssignee.equals(aAssignee)) {
            existingAssignee.removeAssignedChore(this);
        }
        if (aAssignee != null) {
            aAssignee.addAssignedChore(this);
        }
        wasSet = true;
        return wasSet;
    }

    public static int minimumNumberOfResources() {
        return 0;
    }

    public Resource addResource(int aId, String aName, boolean aChecked) {
        return new Resource(aId, aName, aChecked, this);
    }

    public boolean addResource(Resource aResource) {
        boolean wasAdded = false;
        if (resources.contains(aResource)) {
            return false;
        }
        Chore existingChore = aResource.getChore();
        boolean isNewChore = existingChore != null && !this.equals(existingChore);
        if (isNewChore) {
            aResource.setChore(this);
        } else {
            resources.add(aResource);
        }
        wasAdded = true;
        return wasAdded;
    }

    public boolean removeResource(Resource aResource) {
        boolean wasRemoved = false;
        //Unable to remove aResource, as it must always have a chore
        if (!this.equals(aResource.getChore())) {
            resources.remove(aResource);
            wasRemoved = true;
        }
        return wasRemoved;
    }

    //Never used
    public boolean addResourceAt(Resource aResource, int index) {
        boolean wasAdded = false;
        if (addResource(aResource)) {
            if (index < 0) {
                index = 0;
            }
            if (index > numberOfResources()) {
                index = numberOfResources() - 1;
            }
            resources.remove(aResource);
            resources.add(index, aResource);
            wasAdded = true;
        }
        return wasAdded;
    }

    public boolean addOrMoveResourceAt(Resource aResource, int index) {
        boolean wasAdded = false;
        if (resources.contains(aResource)) {
            if (index < 0) {
                index = 0;
            }
            if (index > numberOfResources()) {
                index = numberOfResources() - 1;
            }
            resources.remove(aResource);
            resources.add(index, aResource);
            wasAdded = true;
        } else {
            wasAdded = addResourceAt(aResource, index);
        }
        return wasAdded;
    }


    public void delete() {
        User placeholderCreator = creator;
        this.creator = null;
        placeholderCreator.removeCreatedChore(this);
        if (assignee != null) {
            User placeholderAssignee = assignee;
            this.assignee = null;
            placeholderAssignee.removeAssignedChore(this);
        }
        for (int i = resources.size(); i > 0; i--) {
            Resource aResource = resources.get(i - 1);
            aResource.delete();
        }
    }


    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "name" + ":" + getName() + "," +
                "description" + ":" + getDescription() + "," +
                "points" + ":" + getPoints() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "deadline" + "=" + (getDeadline() != null ? !getDeadline().equals(this) ? getDeadline().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "status" + "=" + (getStatus() != null ? !getStatus().equals(this) ? getStatus().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "recurrencePattern" + "=" + (getRecurrencePattern() != null ? !getRecurrencePattern().equals(this) ? getRecurrencePattern().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "recurrenceEndDate" + "=" + (getRecurrenceEndDate() != null ? !getRecurrenceEndDate().equals(this) ? getRecurrenceEndDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "creator = " + (getCreator() != null ? Integer.toHexString(System.identityHashCode(getCreator())) : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "assignee = " + (getAssignee() != null ? Integer.toHexString(System.identityHashCode(getAssignee())) : "null");
    }
}