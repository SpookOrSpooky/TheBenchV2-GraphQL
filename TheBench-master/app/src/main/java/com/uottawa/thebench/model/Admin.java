package com.uottawa.thebench.model;/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.26.1-f40f105-3613 modeling language!*/


import android.graphics.Bitmap;

import com.uottawa.thebench.model.Household;
import com.uottawa.thebench.model.User;

import java.util.*;

// line 11 "model.ump"
// line 56 "model.ump"
public class Admin extends User
{

  //------------------------
  // MEMBER VARIABLES
  //------------------------

  //Admin Attributes
  private String password;

  //------------------------
  // CONSTRUCTOR
  //------------------------

  public Admin(int aId, String aName, Bitmap aAvatar, Household aHousehold, String aPassword, int aPoints)
  {
    super(aId, aName, aAvatar, aHousehold, aPoints);
    password = aPassword;
  }

  //------------------------
  // INTERFACE
  //------------------------

  public boolean setPassword(String aPassword)
  {
    boolean wasSet = false;
    password = aPassword;
    wasSet = true;
    return wasSet;
  }

  public String getPassword()
  {
    return password;
  }

  public void delete()
  {
    super.delete();
  }


  public String toString()
  {
    return super.toString() + "["+
            "password" + ":" + getPassword()+ "]";
  }

  public boolean validatePassword(String passwordInput) {
    return password.equals(passwordInput);
  }
}