package com.idega.block.boxoffice.business;

import com.idega.data.EntityFinder;
import com.idega.block.boxoffice.data.*;
import com.idega.core.data.ICFile;
import com.idega.builder.data.IBPage;
import java.sql.SQLException;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.data.ICObjectInstance;
import com.idega.core.business.ICObjectBusiness;
import java.util.List;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class BoxFinder {

  public static IBPage getPage(int pageID) {
    try {
      return new IBPage(pageID);
    }
    catch (SQLException e) {
      return new IBPage();
    }
  }

  public static ICFile getFile(int fileID) {
    try {
      return new ICFile(fileID);
    }
    catch (SQLException e) {
      return new ICFile();
    }
  }

  public static BoxEntity getBox(String attribute){
    try {
      List L = EntityFinder.findAllByColumn(BoxEntity.getStaticInstance(BoxEntity.class),BoxEntity.getColumnNameAttribute(),attribute);
      if(L!= null) {
        return (BoxEntity) L.get(0);
      }
      return null;
    }
    catch (SQLException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public static BoxEntity getBox(int boxID){
    try {
      return new BoxEntity(boxID);
    }
    catch (SQLException ex) {
      return null;
    }
  }

  public static BoxCategory getCategory(int boxCategoryID) {
    try {
      return new BoxCategory(boxCategoryID);
    }
    catch (SQLException e) {
      return null;
    }
  }

  public static BoxLink getLink(int boxLinkID) {
    try {
      BoxLink link = new BoxLink(boxLinkID);
      return link;
    }
    catch (SQLException e) {
      return null;
    }
  }

  public static List getCategoriesInBox(BoxEntity box,int userID) {
    try {
      List list = null;
      if ( box != null )
        list = EntityFinder.findRelated(box,BoxCategory.getStaticInstance(BoxCategory.class));
      List userList = EntityFinder.findAllByColumn(BoxCategory.getStaticInstance(BoxCategory.class),BoxCategory.getColumnNameUserID(),userID);
      if ( userList != null ) {
        if ( list != null ) {
          for ( int a = 0; a < list.size(); a++ ) {
            if ( !userList.contains(list.get(a)) )
              userList.add(list.get(a));
          }
        }
        return userList;
      }
      else {
        if ( list != null ) {
          return list;
        }
      }
      return null;
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
      return null;
    }
  }

  public static List getCategoriesNotInBox(int boxID) {
    try {
      BoxEntity box = BoxFinder.getBox(boxID);
      if ( box != null )
        return EntityFinder.findNonRelated(box,BoxCategory.getStaticInstance(BoxCategory.class));
      return null;
    }
    catch (Exception e) {
      e.printStackTrace(System.err);
      return null;
    }
  }

  public static List getCategoriesInBox(int boxID) {
    try {
      BoxEntity box = BoxFinder.getBox(boxID);
      if ( box != null )
        return EntityFinder.findRelated(box,BoxCategory.getStaticInstance(BoxCategory.class));
      return null;
    }
    catch (Exception e) {
      return null;
    }
  }

  public static BoxCategory[] getCategoriesInBox(BoxEntity box) {
    try {
      BoxCategory[] categories = (BoxCategory[]) box.findRelated(BoxCategory.getStaticInstance(BoxCategory.class));
      if ( categories != null ) {
        return categories;
      }
      return null;
    }
    catch (Exception e) {
      return null;
    }
  }

  public static BoxLink[] getLinksInBox(BoxEntity box,BoxCategory boxCategory) {
    try {
      BoxLink[] links = (BoxLink[]) BoxLink.getStaticInstance(BoxLink.class).findAllByColumnOrdered(box.getColumnNameBoxID(),Integer.toString(box.getID()),boxCategory.getColumnNameBoxCategoryID(),Integer.toString(boxCategory.getID()),BoxLink.getColumnNameCreationDate()+" desc","=","=");
      if ( links != null ) {
        return links;
      }
      return null;
    }
    catch (Exception e) {
      return null;
    }
  }

  public static BoxLink[] getLinksInCategory(BoxCategory boxCategory) {
    try {
      BoxLink[] links = (BoxLink[]) BoxLink.getStaticInstance(BoxLink.class).findAllByColumnOrdered(boxCategory.getColumnNameBoxCategoryID(),Integer.toString(boxCategory.getID()),BoxLink.getColumnNameCreationDate()+" desc","=");
      if ( links != null ) {
        return links;
      }
      return null;
    }
    catch (Exception e) {
      return null;
    }
  }

  // BEGIN COPY PASTE CRAP

  /**@todo make some sence into this crap**/
  public static BoxEntity getObjectInstanceFromID(int ICObjectInstanceID){
    try {
      ICObjectBusiness icob = ICObjectBusiness.getInstance();
      ICObjectInstance ICObjInst = icob.getICObjectInstance(ICObjectInstanceID);
      return (BoxEntity)icob.getRelatedEntity(ICObjInst,BoxEntity.class);
    }
    catch (com.idega.data.IDOFinderException ex) {
      ex.printStackTrace();
      return null;
    }
  }

  public static int getRelatedEntityId(ICObjectInstance eObjectInstance){
    ICObjectBusiness bis = ICObjectBusiness.getInstance();
    return bis.getRelatedEntityId(eObjectInstance,BoxEntity.class);
  }

  public static int getObjectInstanceIdFromID(int boxID){
    try {
      BoxEntity box = new BoxEntity(boxID);
      List L = EntityFinder.findRelated(box,new ICObjectInstance());
      if(L!= null){
        return ((ICObjectInstance) L.get(0)).getID();
      }
      else
        return -1;
    }
    catch (SQLException ex) {
      ex.printStackTrace();
      return -1;

    }
  }

}