package com.photo.act.photo_act.utils;


//import com.tool.guicomps.*;


import com.photo.act.photo_act.db.RecordService;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class UtilsDouble {

    private char doubleDecimalFormatChar;
    private int decimalNumbers;
    private char thousandsChar;

    //    private Database db;
    //    private ResultSet rs;
    public UtilsDouble() {
        //db= new Database();
    }

    public static void main(String args[]) {
        UtilsDouble ud = new UtilsDouble();
        Object value = 311932.10000000027;
        String strValue = "1.000,25";
        System.out.println("  " + strValue + "  " + ud.getDoubleReading(strValue, true));
        //ud.getSettingsFromDb();
        //System.out.println(" "+ud.doubleDecimalFormatChar+"  "+ud.decimalNumbers);

    }

    // return with . instead of , (the way double is recognised by the  db)
    public String returnDoubleWithDotInsteadOfAComma(String value) {
        String ret = "";
        //  System.out.println("-UtilsDouble.returnDoubleWithDotInsteadOfAComma  value:"+value +"  ret:"+ret );

        if (value != null) {
            if (doubleDecimalFormatChar == ',') {
                int idx = value.indexOf(",");
                if (idx != -1) {
                    ret = value.replace(".", "");  // replace , with nothing
                    ret = ret.replace(",", ".");
                } else {
                    ret = value;
                }
            } else if (doubleDecimalFormatChar == '.') {
                int idx = value.indexOf(".");
                if (idx != -1) {
                    ret = value.replace(",", "");  // replace , with nothing
                    //System.out.println("UtilsDouble.returnDoubleWithDotInsteadOfAComma IF ret:"+ret);
                    //ret = ret.replace("",".");
                } else {
                    ret = value;
                    //System.out.println("UtilsDouble.returnDoubleWithDotInsteadOfAComma ELSE ret:"+ret);
                }
            } else {
                System.out.println("UtilsDouble.returnDoubleWithDotInsteadOfAComma UNKNOWN, perhaps not called getDoubleSettingsFromDb ret:" + ret + "  doubleDecimalFormatChar:" + doubleDecimalFormatChar + " value:" + value);
            }

        } else {

            value = "";
            ret = "";
        }


        return ret;
    }

    // should be called on setEntity in PanelODORD, in PanelODMRD when renderer is setted, panel statistics
    public void getSettingsFromDb() {
        //System.out.println("UtilsDouble.getSettingsFromDb");
     /*try
     {
        Properties props = new Properties(); //properties to get from file
        String dirFile = VariablesGlobal.globalDirConfiguration+System.getProperty("file.separator")+FILE_CONFIG;
       FileInputStream in = new FileInputStream(dirFile);
       props.load(in);

       String dfor = props.getProperty("number.decimalFormat");
       doubleDecimalFormatChar = dfor.charAt(0);

       String decNo =props.getProperty("number.decimalNumbers");

       decimalNumbers = Integer.parseInt(decNo);



      }
     catch (IOException ex)
     {
         System.out.println("UtilsDouble.getSettingsFromDb IOException: Cannot find text file:"+FILE_CONFIG);
         System.out.println(ex.getMessage());
     }*/


         /*System.out.println("UtilsDouble.getSettingsFromDb ------------------------------------------------------");

         try
         {

           String sqlDec =  "SELECT charOfDecimal, lengthOfDecimalPrice FROM dbcompany WHERE dbcompany.dbCompanyId='"+VariablesGlobal.globalCompanyId+"'";
           db.retrieveDBDataFromQuery(sqlDec,"UtilsDouble.getSettingsFromDb  sqlDec");
          rs=db.getRS();
           rs.first();
           String strDec = rs.getString("charOfDecimal");
           doubleDecimalFormatChar = strDec.charAt(0);

           String strLengthOfDecPrice = rs.getString("lengthOfDecimalPrice");
       decimalNumbers = Integer.parseInt(strLengthOfDecPrice);

       if (doubleDecimalFormatChar==',')
       {
           thousandsChar ='.';
           //pattern = "###.###.###";
       }
       else if(doubleDecimalFormatChar=='.')
       {
           thousandsChar =',';
            //pattern = "###,###,###";
       }


         }
         catch(SQLException e)
         {
           System.out.println("error   UtilsDouble.getSettingsFromDb "+e);
           if(VariablesGlobal.globalShowPrintStackTrace)
           {
               e.printStackTrace();
           }
             closeDB();
         }

         closeDB();
         */

    }

    //	return txtDescr;


  /*public void closeDB()
  {
        db.releaseConnectionRs();
         db.releaseConnectionRsmd();

  }*/

    // Object used for tableModelResultSet.hasEmptyRow

    /*
    for  intLengthOfDecPrice   -->   PanelDbProcedures.fetchProcedureList
     */
    public void getDoubleSettingsFromDb(RecordService recordService, String user, int intLengthOfDecPrice) {


/*

      ArrayList<EntityDBFields> dbfields = new ArrayList<EntityDBFields>();
      dbfields.add( new EntityDBFields("dbuser","charOfDecimal","χαρακτήρας",2,"java.lang.String",100,FIELD_NORMAL_NO_PRIMARY_KEY,LOOKUPTYPE_NOLOOKUP,null,FIELD_NOCOMPLETION,FIELD_VALIDATION_NO,FIELD_VISIBLE_AND_EDITABLE,null,""));
      dbfields.add( new EntityDBFields("dbuser","lengthOfDecimalPrice","μήκος",3,"java.lang.String",15,FIELD_NORMAL_NO_PRIMARY_KEY,LOOKUPTYPE_NOLOOKUP,null,FIELD_NOCOMPLETION,FIELD_VALIDATION_NO,FIELD_VISIBLE_AND_EDITABLE,null,""));


      String sqlDec =  "SELECT charOfDecimal AS \"χαρακτήρας\", lengthOfDecimalPrice AS \"μήκος\" FROM dbuser WHERE username LIKE '"+user+"'";//dbcompany.dbCompanyId='"+VariablesGlobal.globalCompanyId+"'";

      System.out.println("UtilsDouble.getDoubleSettingsFromDb -dbuser---------------------"+user+"--------------------------------");

      List<Record> selectedRecordFromPk = recordService.getByPK(sqlDec, dbfields,null);// entityLookupDbF, lstPkColumnValuesPk);



       //Set<Map<String, Object>> rows = (Set<Map<String, Object>>)((SelectionModel) grid.getSelectionModel()).getSelectedItems();
       //for(int r = 0;r<selected.size();r++)
       List<Record> lstAllRecordSetDescr = new ArrayList<>();
       if(!selectedRecordFromPk.isEmpty())
       {
       System.out.println("PanelEntity.getLookUpDescription look up size:"+selectedRecordFromPk.get(0).getSize());
       for (Record rowd : selectedRecordFromPk)
       {
           Record recd = new Record();
           //rec.addColumnData(nameOfField,valueOfField);
           for(int s = 0;s<selectedRecordFromPk.get(0).getSize();s++)
           {


               //System.out.println("selectedRecord for-:  "+entityDbFields.get(s).getCaption()+"="+row.getColumnData(entityDbFields.get(s).getCaption())+" size:"+row.getSize());//+" selectedRecord:"+selectedRecord);//.get(0).getColumnData(strPkColumnNames[s]));//+"  "+row.getColumnData(strPkColumnNames[s])+"");
               //String value =);
               */
/*String lukey = lookupMgt.getLookUpKey(lookupName);
               String lukeyFT = lookupMgt.getLookUpKeyFTKey(lookupName);
               int luIntDescr = lookupMgt.getLookUpFieldIndex(lookupName);
               String luDescr = lookupMgt.getLookUpLabel(lookupName);
               String luDescrCaption = lookupMgt.getLookUpFieldLabel(lookupName);
               String recf= selectedRecordFromPk.get(0).getColumnData(luDescrCaption);*//*



         */


        //String  strLengthOfDecPrice = rowd.getColumnData("μήκος");
        String strLengthOfDecPrice = "2";
        if (intLengthOfDecPrice != -1) {
            decimalNumbers = intLengthOfDecPrice;
        } else {
            decimalNumbers = Integer.parseInt(strLengthOfDecPrice);
        }
        //String strDec = rowd.getColumnData("χαρακτήρας");//"charOfDecimal");
        String strDec = ".";

        //String strDec = rs.getString("charOfDecimal");
        doubleDecimalFormatChar = strDec.charAt(0);
        if (doubleDecimalFormatChar == ',') {
            thousandsChar = '.';
            //pattern = "###.###.###";
        } else if (doubleDecimalFormatChar == '.') {
            thousandsChar = ',';
            //pattern = "###,###,###";
        }


        //System.out.println("PanelEdit.setEntity  sel "+lukey+"  "+lukeyFT+"  "+luIntDescr+" "+luDescr+" "+luDescrCaption+" "+recf);
        //System.out.println("PanelEdit.setEntity  selectedRecord for:"+luDescr+" "+ rowd.getColumnData(luDescr));//+entityLookupDbF.get(s).getCaption()+"    "+rowd.getColumnData(entityLookupDbF.get(s).getCaption()));
        //recd.addColumnData(luDescr+"",rowd.getColumnData(luDescr)+"");
      /*     	txtDescr = rowd.getColumnData(luDescr);
               if(txtDescr.equalsIgnoreCase(""))
               {
                 System.out.println("PanelEdit.getLookUpDescription: ....txtDescr is empty.... lookupName:"+lookupName+"  luDescr:"+luDescr);
               }
           */
/*           }

       }

 */

        //String txtDescr = entityLookupDbF.get(f).getCaption();

        //txtFieldDescription.setValue(txtDescr);

    }// if(lookupMgt.getQuery(lookupName)!=null)

    /**
     * @param value
     * @param showZero if true then return the 0,00 else return nothing. Used in reports and elsewhere.
     * @return
     */
    public String getDoubleReading(Object value, boolean showZero) {
        String ret = "";
        double valu = Double.valueOf("0.00").doubleValue();
        String v = getDoubleReading(valu);
        //System.out.println("UtilsDouble.getDoubleReading "+value+"-"+v+"-"+getDoubleEditing(valu));
        if (value == null || value.equals(null) || value.toString().trim().equals("") || value.toString().trim().equals("0.0") || value.toString().trim().equals("0"))  // 0.0 in h2
        {
            double val = Double.valueOf("0.00").doubleValue();
            //System.out.println("UtilsDouble.getDoubleReading "+value+"-"+val+"-"+getDoubleReading(val));
            if (showZero) {
                ret = getDoubleReading(val);
            } else {
                ret = "";
            }
        } else {
            double val = Double.valueOf(value.toString()).doubleValue();
            //long val = Long.valueOf(value.toString()).longValue();
            //System.out.println("UtilsDouble.getDoubleReading "+value+"-"+val);//+"-"+getDoubleReading(val));
            ret = getDoubleReading(val);
        }

        return ret;
    }

    /**
     * @param value
     * @param showZero if true then return the 0,00 else return nothing. Used in reports and elsewhere.
     * @return
     */
    public String getDoubleReading(String value, boolean showZero) {
        double valu = 0.00;
        String ret = "";

        try {
            if (value.indexOf(",") != -1) {
                String strvalue = value.replace(".", "");
                value = strvalue.replace(",", ".");
                //System.out.println("   value:"+value);
                valu = Double.valueOf(value).doubleValue();
                //System.out.println("   valu:"+valu);
            } else {
                valu = Double.valueOf("0.00").doubleValue();
            }

        } catch (NumberFormatException e) {
            System.out.println("     error     UtilsDouble.getDoubleReading  NumberFormatException   value:" + value + "   valu:" + valu + "  " + e.getMessage());
            // e.printStackTrace();
        }


        String v = getDoubleReading(valu);
        //System.out.println("UtilsDouble.getDoubleReading "+value+"-"+v+"-"+getDoubleEditing(valu));
        if (value == null || value.equals(null) || value.toString().trim().equals("null") || value.toString().trim().equals("") || value.toString().trim().equals("0.0") || value.toString().trim().equals("0")) // 0.0 in h2
        {
            double val = Double.valueOf("0.00").doubleValue();
            //System.out.println("UtilsDouble.getDoubleReading "+value+"-"+val+"-"+getDoubleReading(val));
            if (showZero) {
                ret = getDoubleReading(val);
            } else {
                ret = "";
            }
        } else {
            double val = Double.valueOf(value.toString()).doubleValue();
            ret = getDoubleReading(val);
            //System.out.println("UtilsDouble.getDoubleReading   showZero:"+showZero+"   "+value+"-"+val+"-"+getDoubleReading(val)+"   ret:"+ret);
        }

        return ret;
    }

    public String getDoubleReading(double value) {
        String s = "0.00";

        String pattern = "###,###,###,###,###,##0";
      /* if (doubleDecimalFormatChar==',')
       {
           thousandsChar ='.';
           //pattern = "###.###.###";
       }
       else if(doubleDecimalFormatChar=='.')
       {
           thousandsChar =',';
            //pattern = "###,###,###";
       }*/

      /* String decNo =props.getProperty("number.decimalNumbers");

       int decimalNumbers = Integer.parseInt(decNo);*/


        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator(doubleDecimalFormatChar);
        dfs.setGroupingSeparator(thousandsChar);
        dfs.setZeroDigit('0');

        //System.out.println(pattern+" "+doubleDecimalFormatChar+" "+thousandsChar);
        DecimalFormat df = new DecimalFormat(pattern, dfs);
        df.setGroupingSize(3);
        df.setMaximumFractionDigits(decimalNumbers);
        df.setMinimumFractionDigits(decimalNumbers);
        df.setRoundingMode(RoundingMode.HALF_UP); // if 5.5 then 6
        //System.out.println("UtilsDouble.getDoubleReading "+value);
        s = df.format(value);
        //System.out.println("UtilsDouble.getDoubleReading "+dfs.getGroupingSeparator() );
        //d = Double.valueOf(s).doubleValue();

        //System.out.println("UtilsDouble.getDoubleReading input value:"+value+" ret:"+s);
        return s;

    }

    public String getDoubleEditing(double value) {
        String s = "0.00";
      /* try
     {
        Properties props = new Properties(); //properties to get from file
       FileInputStream in = new FileInputStream(FILE_CONFIG);
       props.load(in);

       String dfor = props.getProperty("number.decimalFormat");
       char doubleFormat = dfor.charAt(0);


       String decNo =props.getProperty("number.decimalNumbers");
       int decimalNumbers = Integer.parseInt(decNo);*/


        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator(doubleDecimalFormatChar);

        DecimalFormat df = new DecimalFormat("0.00", dfs);
        df.setMaximumFractionDigits(decimalNumbers);
        df.setMinimumFractionDigits(decimalNumbers);

        //value=value.trim();
        //System.out.println(value);
        s = df.format(value) + "";
        //s = value;
        //System.out.println(s);
        //d = Double.valueOf(s).doubleValue();


        return s;
    }

    public String getDoubleSaving(String value) {
        String str = "0";


        //System.out.println("UtilsDouble.getDoubleSaving  value:"+value+"  returnDoubleWithDotInsteadOfAComma"+returnDoubleWithDotInsteadOfAComma(value));
     /* String strBeforeDecimalChar =   value.substring(0,value.length()-(decimalNumbers+1));
      String strAfterDecimalChar =   value.substring(value.length()-(decimalNumbers),value.length());
      System.out.println("UtilsDouble 1  "+strBeforeDecimalChar);
      value = strBeforeDecimalChar.replaceAll(".", "");

       System.out.println("UtilsDouble 2  "+value + " strBeforeDecimalChar:"+strBeforeDecimalChar+"."+strAfterDecimalChar);*/


         /* if(doubleDecimalFormatChar==',')
          {
                str = value.replace(',','.');
          }
          else
          {

                 str=value;
          }*/
        //System.out.println("UtilsDouble  str:"+str);
        str = returnDoubleWithDotInsteadOfAComma(value);
        String val = "";
        if (str == null || str.trim().equalsIgnoreCase("") || str.equalsIgnoreCase("null")) {
            val = "";
            str = "";
        } else {
            //System.out.println("UtilsDouble.getDoubleSaving  val:"+val+"  str:"+str);
            val = getDoubleEditing(Double.parseDouble(str));
            str = returnDoubleWithDotInsteadOfAComma(val);
        }


        return str;

    }

}
