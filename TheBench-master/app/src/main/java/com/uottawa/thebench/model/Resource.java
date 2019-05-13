package com.uottawa.thebench.model;/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.26.1-f40f105-3613 modeling language!*/



// line 38 "model.ump"
// line 78 "model.ump"
public class Resource
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //Resource Attributes
  private int id;
  private String name;
  private boolean checked;

  //Resource Associations
  private Chore chore;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public Resource(int aId, String aName, boolean aChecked, Chore aChore)
  {
    id = aId;
    name = aName;
    checked = aChecked;
    boolean didAddChore = setChore(aChore);
    if (!didAddChore)
    {
      throw new RuntimeException("Unable to create resource due to chore");
    }
  }

  //------------------------
  // INTERFACE
  //------------------------

  public boolean setId(int aId)
  {
    boolean wasSet = false;
    id = aId;
    wasSet = true;
    return wasSet;
  }

  public boolean setName(String aName)
  {
    boolean wasSet = false;
    name = aName;
    wasSet = true;
    return wasSet;
  }

  public boolean setChecked(boolean aChecked)
  {
    boolean wasSet = false;
    checked = aChecked;
    wasSet = true;
    return wasSet;
  }

  public int getId()
  {
    return id;
  }

  public String getName()
  {
    return name;
  }

  public boolean isChecked()
  {
    return checked;
  }

  public Chore getChore()
  {
    return chore;
  }

  public boolean setChore(Chore aChore)
  {
    boolean wasSet = false;
    if (aChore == null)
    {
      return wasSet;
    }

    Chore existingChore = chore;
    chore = aChore;
    if (existingChore != null && !existingChore.equals(aChore))
    {
      existingChore.removeResource(this);
    }
    chore.addResource(this);
    wasSet = true;
    return wasSet;
  }

  public void delete()
  {
    Chore placeholderChore = chore;
    this.chore = null;
    placeholderChore.removeResource(this);
  }


  public String toString()
  {
    return super.toString() + "["+
            "id" + ":" + getId()+ "," +
            "name" + ":" + getName()+ "]" + System.getProperties().getProperty("line.separator") +
            "  " + "chore = "+(getChore()!=null?Integer.toHexString(System.identityHashCode(getChore())):"null");
  }
}