package com.photo.act.photo_act.db;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

public class Record implements Serializable {


    @Serial
    private static final long serialVersionUID = 1L;

    //Field[] listFields;
    private ArrayList<String> lstColumnName;
    private Map row;
    private ArrayList<Object> lstColumnData;

   /* private Long id;
    private String firstName, lastName;
    
    public Record(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Long getId() {
        return id;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setFirstName(String firstName) {
    	this.firstName=firstName;
    }
    public void setLastName(String lastName) {
    	this.lastName=lastName;
    }
    */
    
    
    /*public Record(Object[] arrColumnNameIn,Map rowIn, ArrayList lstColumnDataIn)
    {
    	arrColumnName=arrColumnNameIn;
    	row = rowIn;
    	lstColumnData=lstColumnDataIn;
    }*/
  
    /*public void setColumnData(String strField, Object strValue)
    {
    	for(int c=0;c<lstColumnName.size();c++)
    	{
    		if(lstColumnName.get(c).toString().equalsIgnoreCase(strField))
    		{
    			System.out.println("Record.setColumnData   c:"+c+"  "+strField+"="+strValue);
    			lstColumnData.set(c, strValue);
    		}
    	}
    	//lstColumnName.add(strField);
    	//lstColumnData.add(strValue);
    	
    }*/

    public Record() {
        lstColumnName = new ArrayList<String>();
        lstColumnData = new ArrayList<Object>();
        //addEntitiesDBField();
    }

    public void addColumnData(String strField, Object strValue) {
        lstColumnName.add(strField);
        lstColumnData.add(strValue);

    }

    public String getColumnNameFromIndex(int i) {
        String ret = "";
        for (int c = 0; c < lstColumnName.size(); c++) {
            if (c == i) {
                ret = lstColumnName.get(c).toString();
            }
        }
        return ret;
    }

    public String getColumnData(String columnName) {
        String ret = "";
        for (int c = 0; c < lstColumnName.size(); c++) {
            if (columnName.equalsIgnoreCase(lstColumnName.get(c).toString())) {
                //System.out.println("Record  c:"+c+"  "+lstColumnData.get(c).toString());

                if (lstColumnData.get(c) == null) {
                    ret = "";
                } else {
                    ret = lstColumnData.get(c) + "";//[c].toString();
                }
            }
        }

        return ret;
    }

    public void setColumnData(String columnName, Object strValue) {
        String ret = "";
        for (int c = 0; c < lstColumnName.size(); c++) {
            System.out.println("Record  c:" + c + "   columnName:" + columnName + " =  " + lstColumnName.get(c).toString());
            if (columnName.equalsIgnoreCase(lstColumnName.get(c).toString())) {


                if (lstColumnData.get(c) == null) {
                    //ret="";
                } else {
                    lstColumnData.set(c, strValue.toString());//[c].toString();
                }
            }
        }

        //return ret;
    }

    public int getSize() {
        return lstColumnName.size();
    }
	   
	  /* public void addEntitiesField(Field[] edbf)
	   { 
		   listFields = edbf;
	   }
	   
	   public Object getFieldValueFromName(String strField)
	   {
	   	  for(int i =0; i<listFields.length; i++)
	   	  {  

	   	     if (listFields[i].getName().toUpperCase().equalsIgnoreCase(strField.toUpperCase()))// non case sensitive search
	   	     {

	   	    	return listFields[i].getValue();
	   	     }
	   	  }
	      return null;
	   }*/
	   
	   
	  /* public void setFieldValueFromName(String strField, Object strValue)
	   {
		   	  for(int i =0; i<listFields.length; i++)
		   	  {  

		   	     if (listFields[i].getName().toUpperCase().equalsIgnoreCase(strField.toUpperCase()))// non case sensitive search
		   	     {

		   	    	 listFields[i].setValue(strValue);
		   	     }
		   	  }
	   }*/

}
