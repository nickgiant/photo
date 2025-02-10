// https://docs.spring.io/spring/docs/3.0.0.M3/reference/html/ch13s02.html
// https://mkyong.com/spring/spring-jdbctemplate-querying-examples/

package com.photo.act.photo_act.db;


import com.photo.act.photo_act.utils.UtilsDouble;
import com.photo.act.photo_act.utils.UtilsString;
import com.photo.act.photo_act.views.components.DialogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.photo.act.photo_act.views.MainLayout.APP_VERSION;


//@Component
//@ComponentScan("com.tool.jdbc")

@Service
public class RecordService {


    private static final Logger logger = LoggerFactory.getLogger(RecordService.class);
    //https://stackoverflow.com/questions/31983352/commit-on-jdbctemplate-or-datasource
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PlatformTransactionManager platformTransactionManager;
    private TransactionTemplate transactionTemplate;
    private UtilsDouble utilsDouble;

    private String hostname;
//    private String function;
    private int userId;
    private String username;
    private String ip;
    private String sessionId;
//    private String info;

    public void setGlobalInfo(String hostname, int userId, String username, String ip, String sessionId) {
       this.hostname= hostname;
//       this.function = function;
       this.userId = userId;
       this.username = username;
       this.ip=ip;
       this.sessionId = sessionId;
//       this.info = info;

    }

    public int massRecordInsert(ArrayList<String> lstInsertQueries, ArrayList<Object[]> listInsertValues, ArrayList<String[]> listInsertTypes) {
        int insertRecordsCount = 0;

        DefaultTransactionDefinition paramTransactionDefinition = new DefaultTransactionDefinition();

        TransactionStatus status = platformTransactionManager.getTransaction(paramTransactionDefinition);
        try {

            for (int q = 0; q < lstInsertQueries.size(); q++) {
                if (listInsertValues == null && listInsertTypes == null || (listInsertValues.get(q) == null && listInsertTypes.get(q) == null)) {
                    insertRecordsCount = insertRecordsCount + insertOneRecordWithQuery(lstInsertQueries.get(q), null, null);
                } else {
                    insertRecordsCount = insertRecordsCount + insertOneRecordWithQuery(lstInsertQueries.get(q), listInsertValues.get(q), listInsertTypes.get(q));
                }
            }
            platformTransactionManager.commit(status);
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            String function = "RecordService.massRecordInsert";
            logger.error("Error: ------------------- " + function + " : " + e.getMessage() + "       cause: " + e.getCause());
            logErrorInDb(e,hostname,function,userId,username,ip,sessionId,"");
            displayDialogError(e);
        }
        return insertRecordsCount;
    }

    public int insertOneRecordWithQuery(String query, Object[] fieldValue, String[] strFieldValueType) {
        try {
            utilsDouble = new UtilsDouble();

   		/*for(int f=0;f<fieldNames.length;f++)
   		{
   			System.out.println(" updateRecord: "+fieldNames[f]+"  "+fieldValues[f]);
   		}*/
		/*	String querySet="";
			for(int f=0;f<fieldNames.length;f++)
			{
				querySet=querySet+" "+fieldNames[f]+" = ? ";

				if(f!=fieldNames.length-1)
				{
					querySet=querySet+", ";
				}
			}

			String queryWhere="";
			for(int f=0;f<fieldNamesWhere.length;f++)
			{
				queryWhere=queryWhere+" "+fieldNamesWhere[f]+" = ? ";

				if(f!=fieldNamesWhere.length-1)
				{
					queryWhere=queryWhere+" AND ";
				}
			}*/

            Object[] fieldValuesAll = null;
            String[] fieldClassAll = null;
            int[] intFieldValueTypeAll = null;
            if (fieldValue != null && strFieldValueType != null) {
                System.out.println("RecordService.insertOneRecordWithQuery  fieldValuesWhere:" + fieldValue.length + "  strFieldValueTypeWhere:" + strFieldValueType.length);

                //https://stackoverflow.com/questions/80476/how-can-i-concatenate-two-arrays-in-java

                fieldValuesAll = fieldValue;
                fieldClassAll = strFieldValueType;
                String allvalues = "";
                for (int a = 0; a < fieldValuesAll.length; a++) {

                    String strClass = fieldClassAll[a] + "";//.toString();

                    if (strClass.equalsIgnoreCase("java.lang.String")) {
                        allvalues = allvalues + " '" + fieldValuesAll[a] + "' (String)";
                    } else if (strClass.equalsIgnoreCase("java.lang.Double")) {
                        if (fieldValuesAll[a] == null || fieldValuesAll[a].toString().trim().equalsIgnoreCase("")) {
                            allvalues = allvalues + utilsDouble.getDoubleSaving(" " + fieldValuesAll[a].toString()) + " (null) (Double)";
                            fieldValuesAll[a] = null;
                        } else {
                            fieldValuesAll[a] = utilsDouble.getDoubleSaving(" " + fieldValuesAll[a].toString());
                            allvalues = allvalues + utilsDouble.getDoubleSaving(" " + fieldValuesAll[a].toString()) + " (Double)";
                        }
                    } else if (strClass.equalsIgnoreCase("java.lang.Integer")) {
                        if (fieldValuesAll[a] == null || fieldValuesAll[a].toString().trim().equalsIgnoreCase("")) {
                            allvalues = allvalues + " " + fieldValuesAll[a] + " (null) (Integer)";
                            fieldValuesAll[a] = null;
                        } else {
                            allvalues = allvalues + " " + fieldValuesAll[a] + " (Integer)";
                        }
                    } else if (strClass.equalsIgnoreCase("java.sql.Date")) {
                        if (fieldValuesAll[a] == null || fieldValuesAll[a].toString().trim().equalsIgnoreCase("")) {
                            allvalues = allvalues + " (null) (Date)";
                            fieldValuesAll[a] = null;
                        } else {
                            allvalues = allvalues + " " + fieldValuesAll[a] + " (Date)";
                        }
                    } else if (strClass.equalsIgnoreCase("java.lang.Boolean")) {
                        allvalues = allvalues + " " + fieldValuesAll[a] + " (Boolean)";
                    } else {
                        allvalues = allvalues + " " + fieldValuesAll[a] + " (else)";
                    }
                }

                intFieldValueTypeAll = getTypeIntFromObject(fieldClassAll);

                //String query = "UPDATE "+table+" SET "+querySet+" WHERE "+queryWhere+" ";

                System.out.println("insertOneRecordWithQuery  " + fieldValuesAll.length + " =  " + fieldClassAll.length + " --- allvalues:" + allvalues);
                //			System.out.println("updateRecord2 "+(intFieldValueType.length+intFieldValueTypeWhere.length)+" = "+intFieldValueTypeAll.length);

            }
            logger.info("query: " + query);


            int res = jdbcTemplate.update(query, fieldValuesAll, intFieldValueTypeAll);
 		  /* if(VariablesGlobal.globalShowNotification)
 		   {
					//Notification.show(" insertOneRecordWithQuery res:"+res+"   "+query);
 		   }*/
            return res;
            //rec.getColumnData("lastname"), employee.getFirstName(), employee.getId());
        } catch (Exception e) {
            String function = "RecordService.insertOneRecordWithQuery";
            logger.error("Error: " + function + " : " + e.getMessage() + "       cause: " + e.getCause());
            //      logErrorInDb(e, function);

            displayDialogError(e);
            return 0;
        }
    }

/*
    public void logErrorInDb(Exception e, String function) {
        String strCause = "";
        if(e.getCause()!=null){
            strCause = e.getCause().toString().replaceAll("'", "").replaceAll("\"", "").substring(0, Math.min(e.getCause().toString().trim().length(), 480)).trim();
        }
        String sqlError = "INSERT INTO dberror (errorId, userId, username, ip, sessionid, javaFunctionOrigin, errorMessage, errorCause, appVersion, loginCompany) " +
                "VALUES (0, " + VariablesGlobal.globalUserId + ", '" + VariablesGlobal.globalUserName + "', '" + VariablesGlobal.ip + "', '" + VariablesGlobal.sessionid + "', '" + function + "', '"
                + e.getMessage().replaceAll("'", "").replaceAll("\"", "").substring(0, Math.min(e.getMessage().trim().length(), 480)).trim() + "', '"
                + strCause
                + "', '" + VariablesGlobal.appLeadVersion + "." + VariablesGlobal.appSubVersion + "', '" + VariablesGlobal.globalCompanyName + "')";

        this.insertOneRecordWithQuery(sqlError, null, null);

    }

 */

    public List<Record> findAll(String sqlReadOnly, String[] strSelectColumnNames) {

        UtilsString utilsString = new UtilsString();

        String sql = sqlReadOnly;//"SELECT id, firstname, lastname FROM customer";
        //ArrayList lstColumnNames = new ArrayList();
       // String[] strSelectColumnNames = utilsString.getQuerySelectFieldsAsArray(sql);//{"id","firstname","lastname"};


        List<Record> lstRecordSet = new ArrayList<>();

        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);


        for (Map row : rows) {
            Record rec = new Record();
            for (int s = 0; s < strSelectColumnNames.length; s++) {
                // System.out.println("RecordService.findAll " + strSelectColumnNames[s] + "   " + row.get(strSelectColumnNames[s]));
                rec.addColumnData(strSelectColumnNames[s], row.get(strSelectColumnNames[s]) + "");
	        	/*Long.parseLong(row.get("id")+""),
	        			row.get("firstname")+"",
	        			row.get("lastname")+"");*/
            }

            lstRecordSet.add(rec);
        }


        return lstRecordSet;
    }

    public List<Record> findAll(String sqlReadOnly, String[] strSelectColumnNames, Object[] sqlParValue, String[] sqlParType) {

        UtilsString utilsString = new UtilsString();

        String sql = sqlReadOnly;//"SELECT id, firstname, lastname FROM customer";
        //ArrayList lstColumnNames = new ArrayList();
      //  String[] strSelectColumnNames = utilsString.getQuerySelectFieldsAsArray(sql);//{"id","firstname","lastname"};

        for (int v = 0; v < sqlParValue.length; v++) {
            logger.info(v + "  " + sqlParValue[v] + "  " + sqlParType[v]);
        }

        List<Record> lstRecordSet = new ArrayList<>();


        Object[] fieldValuesAll = null;
        String[] fieldClassAll = null;
        int[] intFieldValueTypeAll = null;
        if (sqlParValue != null && sqlParType != null) {
            System.out.println("RecordService.findAll  sqlParValue:" + sqlParValue.length + "  sqlParType:" + sqlParType.length);

            //https://stackoverflow.com/questions/80476/how-can-i-concatenate-two-arrays-in-java


            fieldValuesAll = sqlParValue;
            fieldClassAll = sqlParType;
            String allvalues = "";
            for (int a = 0; a < fieldValuesAll.length; a++) {


                String strClass = fieldClassAll[a] + "";//.toString();


                if (strClass.equalsIgnoreCase("java.lang.String")) {
                    allvalues = allvalues + " '" + fieldValuesAll[a] + "' (String)";
                } else if (strClass.equalsIgnoreCase("java.lang.Double")) {
                    if (fieldValuesAll[a] == null || fieldValuesAll[a].toString().trim().equalsIgnoreCase("")) {
                        allvalues = allvalues + utilsDouble.getDoubleSaving(" " + fieldValuesAll[a].toString()) + " (null) (Double)";
                        fieldValuesAll[a] = null;
                    } else {
                        fieldValuesAll[a] = utilsDouble.getDoubleSaving(" " + fieldValuesAll[a].toString());
                        allvalues = allvalues + utilsDouble.getDoubleSaving(" " + fieldValuesAll[a].toString()) + " (Double)";
                    }
                } else if (strClass.equalsIgnoreCase("java.lang.Integer")) {
                    if (fieldValuesAll[a] == null || fieldValuesAll[a].toString().trim().equalsIgnoreCase("")) {
                        allvalues = allvalues + " " + fieldValuesAll[a] + " (null) (Integer)";
                        fieldValuesAll[a] = null;
                    } else {
                        allvalues = allvalues + " " + fieldValuesAll[a] + " (Integer)";
                    }
                } else if (strClass.equalsIgnoreCase("java.sql.Date")) {
                    if (fieldValuesAll[a] == null || fieldValuesAll[a].toString().trim().equalsIgnoreCase("")) {
                        allvalues = allvalues + " (null) (Date)";
                        fieldValuesAll[a] = null;
                    } else {
                        allvalues = allvalues + " " + fieldValuesAll[a] + " (Date)";
                    }
                } else if (strClass.equalsIgnoreCase("java.lang.Boolean")) {
                    allvalues = allvalues + " " + fieldValuesAll[a] + " (Boolean)";
                } else {
                    allvalues = allvalues + " " + fieldValuesAll[a] + " (else)";
                }
            }
            //		int[] intFieldValueTypeAll = combineInt(intFieldValueType,intFieldValueTypeWhere);


            intFieldValueTypeAll = getTypeIntFromObject(fieldClassAll);

            //String query = "UPDATE "+table+" SET "+querySet+" WHERE "+queryWhere+" ";

            System.out.println("findAll  " + fieldValuesAll.length + " =  " + fieldClassAll.length + " --- allvalues:" + allvalues);
            //			System.out.println("updateRecord2 "+(intFieldValueType.length+intFieldValueTypeWhere.length)+" = "+intFieldValueTypeAll.length);

        }
        System.out.println("insertOneRecordWithQuery sqlReadOny:" + sql);


        // int res = jdbcTemplate.update(sql, fieldValuesAll, intFieldValueTypeAll);


        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, sqlParValue, intFieldValueTypeAll);


        for (Map row : rows) {
            Record rec = new Record();
            for (int s = 0; s < strSelectColumnNames.length; s++) {
                // System.out.println("RecordService.findAll " + strSelectColumnNames[s] + "   " + row.get(strSelectColumnNames[s]));
                rec.addColumnData(strSelectColumnNames[s], row.get(strSelectColumnNames[s]) + "");
	        	/*Long.parseLong(row.get("id")+""),
	        			row.get("firstname")+"",
	        			row.get("lastname")+"");*/
            }

            lstRecordSet.add(rec);
        }


        return lstRecordSet;
    }

    private void displayDialogError(Exception e) {

        DialogMessage dialogMessage = new DialogMessage("Database Error");
        //dialogMessage.setTitle("Database Error");//, VaadinIcon.EDIT.create());
        dialogMessage.setMessageWithHiddenArea(e.getMessage(), e.getCause() + " \n " + e.getStackTrace());
        dialogMessage.addButtonToMiddle().text("Close").onClick(ev -> {
            dialogMessage.close();
        });
        dialogMessage.open();

    }


    private int[] getTypeIntFromObject(Object[] fieldClassAll) {
        int[] intFieldValueTypeAll = new int[fieldClassAll.length];
        for (int a = 0; a < fieldClassAll.length; a++) {
            if (fieldClassAll[a] != null) {
                String strClass = fieldClassAll[a].toString();
                intFieldValueTypeAll[a] = getTypeIntFromObjectSpecific(strClass);
            } else {
                System.out.println("=-=-=-=-=- RecordService.getTypeIntFromObject is NULL   a:" + a + "-=-=-=-=-= :" + fieldClassAll[a]);
            }
        }
        return intFieldValueTypeAll;
    }

    public int getTypeIntFromObjectSpecific(String strClass) {
        int intRet = -1;
        if (strClass.equalsIgnoreCase("java.lang.String")) {
            intRet = Types.VARCHAR;
        } else if (strClass.equalsIgnoreCase("java.lang.Double")) {
            intRet = Types.DOUBLE;
        } else if (strClass.equalsIgnoreCase("java.sql.Date")) {
            intRet = Types.DATE;
        } else if (strClass.equalsIgnoreCase("java.lang.Integer")) {
            intRet = Types.INTEGER;
        } else if (strClass.equalsIgnoreCase("java.lang.BigInteger")) {
            intRet = Types.BIGINT;
        } else if (strClass.equalsIgnoreCase("java.lang.Boolean")) {
            intRet = Types.BOOLEAN;
        } else {
            intRet = Types.VARCHAR;
        }

        return intRet;
    }


    public void logErrorInDb(Exception e, String hostname, String function, int userId, String username, String ip, String sessionid, String info) {
        String strCause = "";

        if (e.getCause() != null) {
            strCause = e.getCause().toString().replaceAll("'", "").replaceAll("\"", "").substring(0, Math.min(e.getCause().toString().trim().length(), 480)).trim();
        }
        String sqlError = "INSERT INTO dberror (errorId, hostname, userId, username, ip, sessionid, javaFunctionOrigin, errorMessage, errorCause, appVersion, info) " +
                "VALUES (0,  '" + hostname + "' , " + userId + " ,'" + username + "', '" + ip + "', '" + sessionid + "', '" + function + "', '"
                + e.getMessage().replaceAll("'", "").replaceAll("\"", "").substring(0, Math.min(e.getMessage().trim().length(), 480)).trim() + "', '"
                + strCause
                + "', '" + APP_VERSION + "', '" + info + "')";

        this.insertOneRecordWithQuery(sqlError, null, null);

    }

}

/*

}
*/
