package com.uottawa.thebench.model;/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.26.1-f40f105-3613 modeling language!*/


import android.graphics.Bitmap;

import java.util.*;

// line 44 "model.ump"
// line 83 "model.ump"
public class BasicUser extends User
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public BasicUser(int aId, String aName, Bitmap aAvatar, Household aHousehold, int aPoints)
  {
    super(aId, aName, aAvatar, aHousehold, aPoints);
  }

  //------------------------
  // INTERFACE
  //------------------------

  public void delete()
  {
    super.delete();
  }

}