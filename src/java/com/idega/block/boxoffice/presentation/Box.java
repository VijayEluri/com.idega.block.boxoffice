package com.idega.block.boxoffice.presentation;

import com.idega.block.IWBlock;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.Image;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.HeaderTable;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWBundle;
import com.idega.core.accesscontrol.business.AccessControl;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.data.ICObjectInstance;
import com.idega.block.boxoffice.data.*;
import com.idega.block.boxoffice.business.*;

public class Box extends Block implements IWBlock {

private int _boxID = -1;
private int _boxCategoryID = -1;
private boolean _isAdmin = false;
private String _attribute;
private int _iLocaleID;
private int _layout = -1;

public final static int BOX_VIEW = 1;
public final static int CATEGORY_VIEW = 2;

private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.boxoffice";
protected IWResourceBundle _iwrb;
protected IWBundle _iwb;
private Image _deleteImage;
private Image _createImage;
private Image _editImage;
private Image _detachImage;

private Table _myTable;
private boolean _newObjInst = false;
private boolean _newWithAttribute = false;

private boolean _styles = true;
private int _numberOfColumns;
private String _headerColor;
private String _borderColor;
private String _inlineColor;
private String _boxWidth;
private String _boxHeight;
private int _boxSpacing;
private int _numberOfDisplayed;
private String _categoryStyle;
private String _linkStyle;
private String _visitedStyle;
private String _activeStyle;
private String _hoverStyle;
private String _name;

private String _target;

public Box(){
  setDefaultValues();
}

public Box(int boxID){
  this();
  _boxID = boxID;
}

public Box(String attribute){
  this();
  _attribute = attribute;
}

  public void main(IWContext iwc) throws Exception {
    _iwrb = getResourceBundle(iwc);
    _iwb = iwc.getApplication().getBundle(IW_CORE_BUNDLE_IDENTIFIER);

    _createImage = _iwb.getImage("shared/create.gif");
    _deleteImage = _iwb.getImage("shared/delete.gif");
    _editImage = _iwb.getImage("shared/edit.gif");
    _detachImage = _iwb.getImage("shared/detach.gif");

    _isAdmin = iwc.hasEditPermission(this);
    //_isAdmin = true;
    _iLocaleID = ICLocaleBusiness.getLocaleId(iwc.getCurrentLocale());

    iwc.removeApplicationAttribute(BoxBusiness.PARAMETER_LINK_ID);
    iwc.removeApplicationAttribute(BoxBusiness.PARAMETER_NEW_OBJECT_INSTANCE);

    BoxEntity box = null;

    _myTable = new Table(1,2);
      _myTable.setCellpadding(0);
      _myTable.setCellspacing(0);
      _myTable.setBorder(0);

    if(_boxID <= 0){
      String sBoxID = iwc.getParameter(BoxBusiness.PARAMETER_BOX_ID);
      if(sBoxID != null)
        _boxID = Integer.parseInt(sBoxID);
      else if(getICObjectInstanceID() > 0){
        _boxID = BoxFinder.getObjectInstanceID(getICObjectInstance());
        if(_boxID <= 0 ){
          BoxBusiness.saveBox(_boxID,getICObjectInstanceID(),null);
          _newObjInst = true;
        }
      }
    }

    if ( _newObjInst ) {
      _boxID = BoxFinder.getObjectInstanceID(new ICObjectInstance(getICObjectInstanceID()));
    }

    if(_boxID > 0) {
      box = BoxFinder.getBox(_boxID);
    }
    else if ( _attribute != null ){
      box = BoxFinder.getBox(_attribute);
      if ( box != null ) {
        _boxID = box.getID();
      }
      else {
        BoxBusiness.saveBox(-1,-1,_attribute);
      }
      _newWithAttribute = true;
    }

    if ( _newWithAttribute ) {
      _boxID = BoxFinder.getBox(_attribute).getID();
    }

    if ( iwc.getParameter(BoxBusiness.PARAMETER_CATEGORY_ID) != null ) {
      try {
        _boxCategoryID = Integer.parseInt(iwc.getParameter(BoxBusiness.PARAMETER_CATEGORY_ID));
      }
      catch (NumberFormatException e) {
        _boxCategoryID = -1;
      }
    }

    int row = 1;
    if(_isAdmin){
      _myTable.add(getAdminPart(),1,row);
      row++;
    }

    _myTable.add(getBox(box),1,row);
    add(_myTable);
  }

  private Table getBox(BoxEntity box) {
    setStyles();

    Table boxTable = new Table();
      boxTable.setCellpadding(0);
      boxTable.setCellspacing(_boxSpacing);

    BoxCategory[] categories = BoxFinder.getCategoriesInBox(box);
    if ( categories != null ) {
      switch (_layout) {
        case BOX_VIEW:
          getBoxView(box,categories,boxTable);
          break;
        case CATEGORY_VIEW:
          getCategoryView(categories,boxTable);
          break;
      }
    }

    return boxTable;
  }

  private void getBoxView(BoxEntity box,BoxCategory[] categories,Table boxTable) {
    int row = 1;
    int column = 1;

    for ( int a = 0; a < categories.length; a++ ) {
      String categoryString = BoxBusiness.getLocalizedString(categories[a],_iLocaleID);
      if ( categoryString == null ) {
        categoryString = "$language$";
      }

      Text categoryText = new Text(categoryString);
        categoryText.setFontStyle(_categoryStyle);

      Table table = new Table();
        table.setCellpadding(3);
        table.setCellspacing(1);
        table.setWidth(_boxWidth);
        table.setHeight(_boxHeight);
        table.setHeight(2,"100%");
        table.setColor(_borderColor);
        table.setColor(1,1,_headerColor);
        table.setColor(1,2,_inlineColor);
        table.setAlignment(1,1,"center");
        table.setVerticalAlignment(1,1,"middle");
        table.setVerticalAlignment(1,2,"top");

      table.add(categoryText,1,1);

      Table linksTable = new Table();
        linksTable.setRows(_numberOfDisplayed+1);
        linksTable.setWidth("100%");
        if ( _isAdmin )
          linksTable.setHeight("100%");
      table.add(linksTable,1,2);

      int linkRow = 1;

      BoxLink[] links = BoxFinder.getLinksInBox(box,categories[a]);
      int linksLength = _numberOfDisplayed;
      if ( links != null ) {
        if ( links.length < linksLength ) {
          linksLength = links.length;
        }

        for ( int b = 0; b < linksLength; b++ ) {
          Link link = getLink(links[b]);
          if ( link != null ) {
            linksTable.add(link,1,linkRow);
            linksTable.setWidth(1,linkRow,"100%");

            if ( _isAdmin ) {
              linksTable.add(getEditLink(links[b].getID()),2,linkRow);
              linksTable.add(getDeleteLink(links[b].getID()),2,linkRow);
            }
            linkRow++;
          }
        }

        if ( _isAdmin ) {
          linksTable.add(getAddLink(categories[a].getID()),1,_numberOfDisplayed+1);
          linksTable.setHeight(1,_numberOfDisplayed+1,"100%");
          linksTable.setVerticalAlignment(1,_numberOfDisplayed+1,"bottom");
        }
      }

      if ( column % _numberOfColumns == 0 ) {
        boxTable.add(table,column,row);
        row++;
        column = 1;
      }
      else {
        boxTable.add(table,column,row);
        column++;
      }
    }
  }

  private void getCategoryView(BoxCategory[] categories,Table boxTable) {
    boxTable.add("Category!!!");
  }

  private Table getAdminPart() {
    Table adminTable = new Table();
      adminTable.setCellpadding(0);
      adminTable.setCellspacing(0);

    Link adminLink = new Link(_createImage);
      adminLink.setWindowToOpen(BoxEditorWindow.class);
      adminLink.addParameter(BoxBusiness.PARAMETER_BOX_ID,_boxID);
      adminLink.addParameter(BoxBusiness.PARAMETER_NEW_OBJECT_INSTANCE,BoxBusiness.PARAMETER_TRUE);
    adminTable.add(adminLink,1,1);

    Link categoryLink = new Link(_editImage);
      categoryLink.setWindowToOpen(BoxCategoryEditor.class);
      categoryLink.addParameter(BoxBusiness.PARAMETER_BOX_ID,_boxID);
    adminTable.add(categoryLink,2,1);

    Link detachLink = new Link(_detachImage);
      detachLink.setWindowToOpen(BoxCategoryChooser.class);
      detachLink.addParameter(BoxBusiness.PARAMETER_BOX_ID,_boxID);
    adminTable.add(detachLink,2,1);

    return adminTable;
  }

  private Link getLink(BoxLink boxLink) {
    String linkString = BoxBusiness.getLocalizedString(boxLink,_iLocaleID);
    if ( linkString != null ) {
      Link link = new Link(linkString);
        if ( _styles ) {
          link.setStyle(_name);
        }
        else {
          link.setFontSize(1);
        }
        link.setOnMouseOver("window.status='"+linkString+"'; return true;");
        link.setOnMouseOut("window.status=''; return true;");

      String URL = boxLink.getURL();
      int fileID = boxLink.getFileID();
      int pageID = boxLink.getPageID();
      String target = boxLink.getTarget();

      if ( URL != null ) {
        if ( URL.indexOf("http://") == -1 ) {
          URL = "http://"+URL;
        }
        link.setURL(URL);
      }
      else if ( fileID != -1 ) {
        link.setFile(fileID);
      }
      else if ( pageID != -1 ) {
        link.setPage(pageID);
      }
      if ( target != null ) {
        link.setTarget(target);
      }
      return link;
    }
    return null;
  }

  private Link getAddLink(int categoryID) {
    Link addLink = new Link(_createImage);
      addLink.setWindowToOpen(BoxEditorWindow.class);
      addLink.addParameter(BoxBusiness.PARAMETER_BOX_ID,_boxID);
      addLink.addParameter(BoxBusiness.PARAMETER_CATEGORY_ID,categoryID);
      addLink.addParameter(BoxBusiness.PARAMETER_NEW_OBJECT_INSTANCE,BoxBusiness.PARAMETER_TRUE);
    return addLink;
  }

  private Link getEditLink(int linkID) {
    Link editLink = new Link(_editImage);
      editLink.setWindowToOpen(BoxEditorWindow.class);
      editLink.addParameter(BoxBusiness.PARAMETER_LINK_ID,linkID);
      editLink.addParameter(BoxBusiness.PARAMETER_BOX_ID,_boxID);
    return editLink;
  }

  private Link getDeleteLink(int linkID) {
    Link deleteLink = new Link(_deleteImage);
      deleteLink.setWindowToOpen(BoxEditorWindow.class);
      deleteLink.addParameter(BoxBusiness.PARAMETER_LINK_ID,linkID);
      deleteLink.addParameter(BoxBusiness.PARAMETER_BOX_ID,_boxID);
      deleteLink.addParameter(BoxBusiness.PARAMETER_DELETE,BoxBusiness.PARAMETER_TRUE);
    return deleteLink;
  }

  private void setDefaultValues() {
    _layout = BOX_VIEW;
    _numberOfColumns = 3;
    _headerColor = "#D8D8D8";
    _borderColor = "#6E6E6E";
    _inlineColor = "#FFFFFF";
    _boxWidth = "120";
    _boxHeight = "120";
    _boxSpacing = 3;
    _numberOfDisplayed = 4;
    _categoryStyle = "font-face: Arial, Helvetica, sans-serif; font-size: 8pt; font-weight: bold";
    _linkStyle = "font-face: Arial, Helvetica,sans-serif; font-size: 8pt; color: #000000; text-decoration: none;";
    _visitedStyle = "font-face: Arial, Helvetica,sans-serif; font-size: 8pt; color: #6E6E6E; text-decoration: none;";
    _activeStyle = "font-face: Arial, Helvetica,sans-serif; font-size: 8pt; color: #D8D8D8; text-decoration: none;";
    _hoverStyle = "font-face: Arial, Helvetica,sans-serif; font-size: 8pt; color: #D8D8D8; text-decoration: underline overline;";
    _target = Link.TARGET_TOP_WINDOW;
  }

  public void setNumberOfColumns(int columns) {
    _numberOfColumns = columns;
  }

  public void setHeaderColor(String color) {
    _headerColor = color;
  }

  public void setBorderColor(String color) {
    _borderColor = color;
  }

  public void setInlineColor(String color) {
    _inlineColor = color;
  }

  /**
   * @deprecated
   */
  public void setBoxWidth(String width) {
    _boxWidth = width;
  }

  /**
   * @deprecated
   */
  public void setBoxHeight(String height) {
    _boxHeight = height;
  }

  public void setWidth(String width) {
    _boxWidth = width;
  }

  public void setWidth(int width) {
    setWidth(Integer.toString(width));
  }

  public void setHeight(String height) {
    _boxHeight = height;
  }

  public void setHeight(int height) {
    setHeight(Integer.toString(height));
  }

  public void setLayout(int layout) {
   _layout = layout;
  }

  public void setBoxSpacing(int spacing) {
    _boxSpacing = spacing;
  }

  public void setNumberOfDisplayed(int number) {
    _numberOfDisplayed = number;
  }

  public void setCategoryStyle(String style) {
    _categoryStyle = style;
  }

  public void setTarget(String target) {
    _target = target;
  }

  public void setLinkStyle(String linkStyle,String activeStyle,String visitedStyle,String hoverStyle) {
    _linkStyle = linkStyle;
    _visitedStyle = linkStyle;
    _activeStyle = visitedStyle;
    _hoverStyle = hoverStyle;
  }

  private void setStyles() {
    if ( _name == null )
      _name = this.getName();
    if ( _name == null ) {
      if ( _attribute == null )
        _name = "boxoffice_"+Integer.toString(_boxID);
      else
        _name = "boxoffice_"+_attribute;
    }

    if ( getParentPage() != null ) {
      getParentPage().setStyleDefinition("A."+_name+":link",_linkStyle);
      getParentPage().setStyleDefinition("A."+_name+":visited",_visitedStyle);
      getParentPage().setStyleDefinition("A."+_name+":active",_activeStyle);
      getParentPage().setStyleDefinition("A."+_name+":hover",_hoverStyle);
    }
    else {
      _styles = false;
    }
  }

  public boolean deleteBlock(int ICObjectInstanceId) {
      return BoxBusiness.deleteBox(ICObjectInstanceId);
  }

  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }

  public Object clone() {
    Box obj = null;
    try {
      obj = (Box) super.clone();

      if ( this._myTable != null ) {
        obj._myTable = (Table) this._myTable.clone();
      }
    }
    catch (Exception ex) {
      ex.printStackTrace(System.err);
    }
    return obj;
  }
}
