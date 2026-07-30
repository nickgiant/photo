package com.photo.act.photo_act.utils;


import org.springframework.web.util.UriUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class UtilsString {

    private static String secretKey = "s!!!n!g!rg1651651gr";
    private static String salt = "pdisghpi!hgopihoph!bholo";
    private static String[] m = {"", "ΕΝΑ ", "ΔΥΟ ", "ΤΡΙΑ ", "ΤΕΣΣΕΡΑ ", "ΠΕΝΤΕ ", "ΕΞΙ ", "ΕΠΤΑ ", "ΟΚΤΩ ", "ΕΝΝΕΑ "};
    private static String[] mF = {"", "ΜΙΑ ", "", "ΤΡΕΙΣ ", "ΤΕΣΣΕΡΙΣ "};
    private static String[] d1 = {"ΔΕΚΑ ", "ΕΝΤΕΚΑ ", "ΔΩΔΕΚΑ "};
    private static String[] d = {"", "ΔΕΚΑ", "ΕΙΚΟΣΙ ", "ΤΡΙΑΝΤΑ ", "ΣΑΡΑΝΤΑ ", "ΠΕΝΗΝΤΑ ", "ΕΞΗΝΤΑ ", "ΕΒΔΟΜΗΝΤΑ ", "ΟΓΔΟΝΤΑ ", "ΕΝΕΝΗΝΤΑ "};
    private static String[] e = {"", "ΕΚΑΤΟ", "ΔΙΑΚΟΣΙ", "ΤΡΙΑΚΟΣΙ", "ΤΕΤΡΑΚΟΣΙ", "ΠΕΝΤΑΚΟΣΙ", "ΕΞΑΚΟΣΙ", "ΕΠΤΑΚΟΣΙ", "ΟΚΤΑΚΟΣΙ", "ΕΝΝΙΑΚΟΣΙ"};
    private static String[] idx = {"ΛΕΠΤΑ", "ΕΥΡΩ ", "ΧΙΛΙΑΔΕΣ ", "ΕΚΑΤΟΜΜΥΡΙ", "ΔΙΣ", "ΤΡΙΣ", "ΤΕΤΡΑΚΙΣ ", "ΠΕΝΤΑΚΙΣ "};
    private static String STR_AND = "ΚΑΙ ";
    //private static ResourceBundle res = ResourceBundle.getBundle("org.isqlviewer.resource.ResourceStrings");
    ArrayList listQueryFieldToGetTheValueFrom;
    ArrayList listQueryValue;

    public UtilsString() {
        listQueryFieldToGetTheValueFrom = new ArrayList();
        listQueryValue = new ArrayList();
    }

    public boolean isEmailSysntaxValid(String email) {
        boolean isValid = false;

        //Initialize reg ex for email.
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        //Make the comparison case-insensitive.

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;


    }

    public String generateRandomString(int length) {
        // You can customize the characters that you want to add into
        // the random strings
        String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
        String CHAR_UPPER = CHAR_LOWER.toUpperCase();
        String NUMBER = "0123456789";

        String DATA_FOR_RANDOM_STRING = CHAR_LOWER + CHAR_UPPER + NUMBER;
        SecureRandom random = new SecureRandom();

        if (length < 1) throw new IllegalArgumentException();

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            // 0-62 (exclusive), random returns 0-61
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            sb.append(rndChar);
        }

        return sb.toString();
    }

    public static void main(String args[]) {
        UtilsString a = new UtilsString();

        System.out.println(a.getVerbal(123.22));

//System.out.println("checkafm "+a.checkGreekAFM("EL034171743")); //sample from kvs book
//System.out.println("checkafm "+a.checkGreekAFM("EL0120315679"));
//System.out.println("checkafm "+a.checkGreekAFM("121315679"));
//System.out.println("checkafm "+a.checkGreekAFM("120315679"));
        String data = " 'and' ";
        String d = data.replaceAll("'", "\\\\'");
        System.out.println(d);
//System.out.println(" query "+a.queryReplaceWildcardWithStarInWhereClause("SELECT * from buyer where afm like'*123*'"));

        System.out.println(" query " + a.replaceWhereWithAnd("WHERE dbcompanyid like 1"));
        a.removeCaptionsFromQuerySubStringSelect("SELECT deliveryId AS \"Νο αποστολής\", description AS \"περιγραφή\" FROM dbDelivery");
        a.hasQuerySelectColumnName("SELECT deliveryId , description FROM dbDelivery WHERE dbcompanyid like 1", "dbcompanyid");

        String query1 = "SELECT deliveryId , description FROM dbDelivery WHERE dbcompanyid like 1 ORDER BY aa AND dbcompanyid like 2 AND dbYearId like 2";
        String query2 = "SELECT deliveryId , description FROM dbDelivery WHERE dbcompanyid like 1 AND dbcompanyid like 1 ORDER BY aa AND dbcompanyid like 2 AND dbYearId like 2";
        String query3 = "SELECT service.serviceId , service.serviceDescr , service.serviceCode , vatCat.vatDescr, service.serviceCatId , service.active, priceWhole ,  sum(saleline.quantity) , sum(saleline.priceBeforeVat) , sum(saleline.vatValue) , sum(saleline.valueWithVat)  FROM service LEFT JOIN vatcat ON vatcat.vatCatId = service.vatCatId LEFT JOIN saleline ON saleline.serviceId = service.serviceId    WHERE service.dbCompanyId LIKE 1 AND service.dbcompanyid like 1  GROUP BY service.serviceId    ORDER BY  2 ASC AND service.serviceId LIKE '4' AND service.dbCompanyId LIKE '1' GROUP BY service.serviceId    ORDER BY  2 ASC  GROUP BY service.serviceId     ORDER BY  2 ASC";
        String query4 = "SELECT dbcompany.dbCompanyId , dbcompany.title , dbcompany.companyVatNo , activitycat.activityDescr , geoCat.geoCatName , dbcompany.doyId  ,doy.doyname , active,  bank.bankBranch ,dbcompany.bankAccount ,dbcompany.bankAccountIBAN  FROM dbcompany LEFT JOIN doy ON dbcompany.doyId=doy.doyId LEFT JOIN geoCat ON dbcompany.geoCatId=geoCat.geoCatId LEFT JOIN bank ON dbcompany.bankId=bank.bankId  LEFT JOIN activitycat ON activitycat.activityCatId = dbcompany.activityCatId     ORDER BY  2 ASC WHERE dbcompany.dbCompanyId LIKE '2' AND service.dbCompanyId LIKE '1' ORDER BY  2 ASC";


        String queryWithoutOrderby1 = a.getQueryWithoutOrderby(query1);
        String queryOrderby1 = a.getOrderbySubQuery(query1);

        System.out.println("   queryWithoutOrderby1:" + queryWithoutOrderby1 + "     queryOrderby1:" + queryOrderby1);

        String queryWithoutOrderby2 = a.getQueryWithoutOrderby(query2);
        String queryOrderby2 = a.getOrderbySubQuery(query2);
        System.out.println("   queryWithoutOrderby2:" + queryWithoutOrderby2 + "     queryOrderby2:" + queryOrderby2);


        String queryOfWhere = a.getQueryWhere(query3);
        String queryBeforeWhere = a.getQueryBeforeWhere(query3);//   query with only select and from
        //String queryWithoutOrderby = utilsString.getQueryWithoutOrderby(queryReadOnly);
        String queryWithOutGroupByAndOrderBy = a.getQueryWithoutGroupByOrOrderBy(query3);
        String queryOrderby = a.getOrderbySubQuery(query3);
        String queryGroupby = a.getQueryGroupby(query3);


        System.out.println(" -- UtilsString.setEntity   3  queryWithoutWhere:" + queryBeforeWhere + "     queryOfWhere:" + queryOfWhere + "      queryGroupby:" + queryGroupby + "      queryOrderby:" + queryOrderby);


        String queryOfWhere4 = a.getQueryWhere(query4);
        String queryBeforeWhere4 = a.getQueryBeforeWhere(query4);//   query with only select and from
        //String queryWithoutOrderby4 = utilsString.getQueryWithoutOrderby(queryReadOnly);
        String queryWithOutGroupByAndOrderBy4 = a.getQueryWithoutGroupByOrOrderBy(query4);
        String queryOrderby4 = a.getOrderbySubQuery(query4);
        String queryGroupby4 = a.getQueryGroupby(query4);


        System.out.println(" --  UtilsString.setEntity  4   queryWithoutWhere4:" + queryBeforeWhere4 + "     queryOfWhere4:" + queryOfWhere4 + "      queryGroupby4:" + queryGroupby4 + "      queryOrderby4:" + queryOrderby4);

        String query1b = "SELECT deliveryId , description FROM dbDelivery WHERE dbcompanyid='1' AND dbYearId='2' ORDER BY aa";
        System.out.println("  query1b == " + a.replaceFromQueryValuesWithQuestionmarks(query1b));


        System.out.println(" gg == " + a.replaceFromQueryValuesWithQuestionmarks("SELECT dbyear FROM dbyear WHERE dbCompanyId ='1' ORDER BY dbyear"));


        System.out.println(" gg == " + a.replaceFromQueryValuesWithQuestionmarks("SELECT userId, username, password FROM dbuser WHERE username='nik' AND password='1' ORDER BY userId"));

        System.out.println(" gk == " + a.replaceFromQueryValuesWithQuestionmarks("SELECT sxesoexoheader.*,sxactionType.* FROM sxesoexoheader, sxactionType WHERE sxesoexoheader.dbCompanyId = sxactionType.dbCompanyId AND  sxesoexoheader.dbCompanyId = sxesoexoline.dbCompanyId AND sxaccount.accountId = sxesoexoline.accountId AND sxesoexoheader.isTemplate='0' AND sxesoexoheader.dbCompanyId LIKE 1"));


        String originalString = "nickg";

        String encryptedString = a.encrypt(originalString);
        String decryptedString = a.decrypt(encryptedString);

        System.out.println(originalString);
        System.out.println(encryptedString);
        System.out.println(decryptedString);

        a.getQuerySelectFieldsAsArray(query3);

    }


    //Use this when you expect human-readable place names like "Thessaloniki, Greece".

    // Vaadin route parameters are handed over as raw, percent-encoded path segments
    // (e.g. "Mountains%20&%20Villages" for "Mountains & Villages"), so callers must
    // decode them before using them in comparisons, SQL LIKE clauses, or lookups.
    public static String decodeRouteParam(String value) {
        if (value == null) return null;
        return UriUtils.decode(value, StandardCharsets.UTF_8);
    }

    public static String sanitizeLocation(String input) {
        if (input == null) return null;

        // Trim whitespace
        String sanitized = input.trim();

        // Normalize Unicode (remove weird encodings)
        sanitized = java.text.Normalizer.normalize(sanitized, java.text.Normalizer.Form.NFKC);

        // Allow letters, numbers, spaces, commas, hyphens
        sanitized = sanitized.replaceAll("[^\\p{L}\\p{N}\\s,\\-]", "");

        // Collapse multiple spaces
        sanitized = sanitized.replaceAll("\\s+", " ");

        return sanitized;
    }

    public static String escapeSqlInjection(String s) {
        int length = s.length();
        int newLength = length;
        // first check for characters that might
        // be dangerous and calculate a length
        // of the string that has escapes.
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                case '\"':
                case '\'':
                case '\0': {
                    newLength += 1;
                }
                break;
            }
        }
        if (length == newLength) {
            // nothing to escape in the string
            return s;
        }
        StringBuffer sb = new StringBuffer(newLength);
        for (int i = 0; i < length; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': {
                    sb.append("\\\\");
                }
                break;
                case '\"': {
                    sb.append("\\\"");
                }
                break;
                case '\'': {
                    sb.append("\\\'");
                }
                break;
                case '\0': {
                    sb.append("\\0");
                }
                break;
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public ArrayList getIndicesOf(String textToLookAt, String textToLookFor) {
        ArrayList ret = new ArrayList();
        int idx = 0;
        int pointFrom = 0;
        for (int t = 0; t < textToLookAt.length(); t++) {
            idx = textToLookAt.indexOf(textToLookFor, pointFrom);
            if (t == idx) {
                ret.add(idx); // + length because point is after index
                pointFrom = t + textToLookFor.length();
                //System.out.println("UtilsString.getIndicesOf found ="+textToLookAt.substring(idx+textToLookFor.length(), idx+textToLookFor.length()+4)+"from "+(idx+textToLookFor.length()));
                //t=idx;
            }

        }


        return ret;
    }

    public String removeSubString(String allText, int from, int to) {
        String retTextA = "";
        String retTextB = "";
        retTextA = allText.substring(0, from);
        retTextB = allText.substring(to, allText.length());
        return retTextA + retTextB;
    }

    public String insertSubString(String allText, int from, String insertedText) {
        String start = allText.substring(0, from);
        String finish = allText.substring(from, allText.length());
        allText = start + insertedText + finish;
        //allText=allText.substring(0, from)+allText.substring(to, allText.length());
        return allText;
    }

    public String removeCaptionsFromQuerySubStringSelect(String queryIn) {
        String retString = "";
        if (queryIn == null || queryIn.equalsIgnoreCase(""))  //   queryIn.toUpperCase().startsWith("SELECT * FROM"))
        {
            System.out.println(" - UtilsString.removeCaptionsFromQuerySubStringSelect  queryIn:" + queryIn);
        } else {
            int indexFrom = queryIn.toUpperCase().indexOf("FROM".toUpperCase());

            System.out.println("UtilsString.removeCaptionsFromQuerySubStringSelect   queryIn:" + queryIn + "    indexFrom:" + indexFrom);

            String strAfterFrom = queryIn.substring(indexFrom, queryIn.length());
            String strSelect = queryIn.substring(7, indexFrom); // 7 is the "select" end index plus a space

            //System.out.println(" UtilsString.removeCaptionsFromQuerySubStringSelect  queryIn:"+queryIn);

       /*int indexCaptionStart = strSelect.toUpperCase().indexOf("AS \"".toUpperCase());
       //System.out.println(" UtilsString.removeCaptionsFromQuerySubStringSelect  indexCaptionStart:"+indexCaptionStart+"    "+(indexCaptionStart+3));
       String strCaptionStart = removeSubString(strSelect,indexCaptionStart,indexCaptionStart+4); //  no is the length of AS "
       int indexCaptionEnd = strCaptionStart.toUpperCase().indexOf("\"".toUpperCase());
       String strCaption = removeSubString(strCaptionStart,0,indexCaptionEnd);


       //String strCaption = removeSubString(strCaptionStart,0,indexCaptionEnd);*/


            strSelect = strSelect.replaceAll("AS \".*?\\\"", "");   //    "AS \".*\""      /\\*.*?\\*/
            strSelect = strSelect.replaceAll("AS\".*?\\\"", "");   //    "AS \".*\""      /\\*.*?\\*/
            retString = "SELECT " + strSelect + strAfterFrom;

            //System.out.println(" UtilsString.removeCaptionsFromQuerySubStringSelect strSelect: "+strSelect);

            //strSelect =  removeStringFromString(strSelect);
        }


        //System.out.println("  UtilsString.removeCaptionsFromQuerySubStringSelect  SELECT "+strSelect+strAfterFrom);
        return retString;
    }

    public String[] getQuerySelectFieldsAsArray(String queryIn) {

        return getQuerySelectFieldsAsArray(queryIn, false);
    }

    //  https://www.baeldung.com/java-string-with-separator-to-list,   isAlsoTableReturned: true when in the field also table.column is needed
    public String[] getQuerySelectFieldsAsArray(String queryIn, boolean isAlsoTableReturned) {
        String[] retString = null;
        if (queryIn == null || queryIn.equalsIgnoreCase(""))  //   queryIn.toUpperCase().startsWith("SELECT * FROM"))
        {
            System.out.println(" - error  UtilsString.getQuerySelectFieldsAsArray  queryIn:" + queryIn);
        } else {
            int indexFrom = queryIn.toUpperCase().indexOf("FROM ");    // prefer not to hava as field names the word 'from'

            if (indexFrom == -1) {
                int IndexSelect = queryIn.toUpperCase().indexOf("SELECT");
                int IndexAt = queryIn.toUpperCase().indexOf("@");
                retString = new String[1];
                if (IndexSelect != -1 && IndexAt != -1) {
                    retString[0] = queryIn.substring(IndexAt, queryIn.toUpperCase().indexOf(";")).trim();
                }
            } else {
                //      System.out.println("UtilsString.getQuerySelectFieldsAsArray   queryIn:" + queryIn + "    indexFrom:" + indexFrom + "    queryIn" + queryIn);
                //String strAfterFrom = queryIn.substring(indexFrom,queryIn.length());
                String strSelect = queryIn.substring(7, indexFrom); // 7 is the "select" end index plus a space

                String strQCount = "COUNT";
                String strQSum = "SUM";
                String strQAs = "AS";
                String subQWithoutCount = "";
                String subQWithoutSum = "";

                if (strSelect.indexOf(strQCount) != -1) {
                    subQWithoutCount = (String) strSelect.subSequence(strSelect.indexOf(strQCount), strSelect.indexOf(strQAs, strSelect.indexOf(strQCount)) - 1);
                }

                strSelect = strSelect.replace(subQWithoutCount, "");

                if (strSelect.indexOf(strQSum) != -1) {
                    subQWithoutSum = (String) strSelect.subSequence(strSelect.indexOf(strQSum), strSelect.indexOf(strQAs, strSelect.indexOf(strQSum)) - 1);
                }
                strSelect = strSelect.replace(subQWithoutSum, "");

                // remove column with 'if' sub query. like if(employmentPeriodIsDefined=1,'αορίστου','ορισμένου') AS \"σχέση εργ.\"
                String ifSubQStart = "if(";
                String ifSubQEnd = ")";
                String ifSubQuery = "";

                if (strSelect.indexOf(ifSubQStart) != -1 && strSelect.indexOf(ifSubQEnd) != -1) {
                    ifSubQuery = (String) strSelect.subSequence(strSelect.indexOf(ifSubQStart), strSelect.indexOf(ifSubQEnd) + 1); // +1: the include the character

                }

                strSelect = strSelect.replace(ifSubQuery, "");


                String caseSubQStart = "CASE WHEN";
                String caseifSubQEnd = "END";
                String caseSubQuery = "";

                if (strSelect.indexOf(caseSubQStart) != -1 && strSelect.indexOf(caseifSubQEnd) != -1) {
                    caseSubQuery = (String) strSelect.subSequence(strSelect.indexOf(caseSubQStart), strSelect.indexOf(caseifSubQEnd) + 3); // +1: the include the character

                }

                strSelect = strSelect.replace(caseSubQuery, "");

                //System.out.println("UtilsString.getQuerySelectFieldsAsArray    ifSubQuery:"+ifSubQuery+"    strSelect:"+strSelect);

                List<String> lstFields = Arrays.asList(strSelect.split(",", -1));

                retString = new String[lstFields.size()];
                for (int f = 0; f < lstFields.size(); f++) {

                    String field = lstFields.get(f).trim();
                    String fieldName = "";
                    if (field.toLowerCase().contains("AS".toLowerCase())) {
                        //System.out.println("UtilsString.getQuerySelectFieldsAsArray   field:"+field);
                        fieldName = field.substring(field.indexOf("AS") + 2, field.length());
                        fieldName = fieldName.replace("\"", "");
                        fieldName = fieldName.replace("'", "");
                        //System.out.println("UtilsString.getQuerySelectFieldsAsArray f:"+f+"  fieldName:"+fieldName);

                    } else {
                        fieldName = field;
                        if (fieldName.contains(".") && !isAlsoTableReturned) {
                            fieldName = fieldName.substring(fieldName.indexOf(".") + 1, fieldName.length());
                        } else if (fieldName.contains(".") && isAlsoTableReturned) {
                            //fieldName = fieldName;
                        } else {
                            fieldName = field;
                        }
                    }


                    //String caption
                    retString[f] = fieldName.trim();
                    //System.out.println("UtilsString.getQuerySelectFieldsAsArray "+f+" "+retString[f]);//lstFields.get(f).trim());
                }

            }

        }
        //ArrayList<String> lstFields = new ArrayList<String>();
       /*int beginIndex =0;
       while(strSelect.indexOf(",")>-1)
       {

           int commaIndex = strSelect.indexOf(",",beginIndex);//,strSelect.length());
           String field = strSelect.substring(0, commaIndex);
           System.out.println(field+"  beginIndex:"+beginIndex+"  commaIndex:"+commaIndex);
           lstFields.add(field);

           beginIndex = commaIndex;
           strSelect = strSelect.substring(commaIndex, strSelect.length());

       }

       }*/

        return retString;
    }

    public int indexOfQueryWhere(String queryIn) {
        int ret = -1;
        if (queryIn == null || queryIn.equals("")) {
            System.out.println("error  UtilsString.indexOfQueryWhere queryIn:" + queryIn);
        }

        int index = -1;
        if (hasQueryWhere(queryIn)) {
            index = queryIn.toUpperCase().indexOf("WHERE".toUpperCase());

        }


        //System.out.println("UtilsString "+index+"    "+retStr);

        if (index != -1) {

            ret = index;
        }


        return ret;
    }

    //disabled
    public String queryReplaceWildcardWithStarInWhereClause(String query) {
        String ret = "";
        // SELECT (SELECT customer.paymentTypeId FROM customer WHERE  customer.dbCompanyId LIKE 1 AND customer.customerId LIKE 1 ) * 2
        int indexWhere = indexOfQueryWhere(query);
        String queryWhere = "";
        String queryBeforeWhere = "";
        ArrayList listSelects = getCountOfSelects(query);
        if (indexWhere == -1) {
            ret = query;
        } else if (indexWhere != -1 && listSelects.size() > 2) {
            ret = query;
        } else {
            queryWhere = query.substring(indexWhere, query.length());
            queryBeforeWhere = query.substring(0, indexWhere);
            //    queryWhere = queryWhere.replace('*','%');  //    disabled because there might be a subclause with another 'select'

            ret = queryBeforeWhere + queryWhere;
        }

        return ret;
    }

    private ArrayList getCountOfSelects(String queryIn) {
        ArrayList listCountOfSelects = new ArrayList();

        int index1 = queryIn.toUpperCase().indexOf("SELECT".toUpperCase(), 0);
        listCountOfSelects.add(index1);
        if (index1 != -1) {
            int index2 = queryIn.toUpperCase().indexOf("SELECT".toUpperCase(), index1 + 6);
            listCountOfSelects.add(index2);
            if (index2 != -1) {
                int index3 = queryIn.toUpperCase().indexOf("SELECT".toUpperCase(), index2 + 6);
                listCountOfSelects.add(index3);
                if (index3 != -1) {
                    int index4 = queryIn.toUpperCase().indexOf("SELECT".toUpperCase(), index3 + 6);
                    listCountOfSelects.add(index4);
                    if (index4 != -1) {
                        int index5 = queryIn.toUpperCase().indexOf("SELECT".toUpperCase(), index4 + 6);
                        listCountOfSelects.add(index5);
                        if (index5 != -1) {
                            int index6 = queryIn.toUpperCase().indexOf("SELECT".toUpperCase(), index5 + 6);
                            listCountOfSelects.add(index6);
                            if (index6 != -1) {
                                int index7 = queryIn.toUpperCase().indexOf("SELECT".toUpperCase(), index6 + 6);
                                if (index7 != -1) {
                                    System.out.println("error  UtilsString.getCountOfSelects MORE THAN 6, queryIn:" + queryIn);
                                }
                            }
                        }
                    }
                }
            }
        }//  1
        return listCountOfSelects;
    }

    public boolean hasQueryWhere(String queryIn) {
        boolean retBoolean = false;
        if (queryIn == null || queryIn.equalsIgnoreCase("")) { // if called from PanelDataFilter perhaps a checkboxTable -> entityFilterSettings[s].dbTable is not found (perhaps a lookup entityFilterSettings is wrong)

            //    System.out.println("error  UtilsString.hasQueryWhere queryIn:"+queryIn);
            retBoolean = false;
        } else {
            int index = -1;
            index = queryIn.toUpperCase().indexOf("WHERE".toUpperCase());

            //System.out.println("UtilsString "+index+"    "+retStr);

            if (index != -1) {

                retBoolean = true;
            }
        }


        return retBoolean;
    }

    public boolean hasQueryGroupby(String queryIn) {
        boolean retBoolean = false;
        if (queryIn == null || queryIn.equals("")) {
            System.out.println("error  UtilsString.hasQueryGroupby queryIn: " + queryIn);
        } else {

            int index = -1;

            index = queryIn.toUpperCase().indexOf("GROUP BY".toUpperCase());

            if (index != -1) {
                retBoolean = true;
            }

        }


        //System.out.println("UtilsString "+index+"    "+retStr);

        return retBoolean;
    }

    public boolean hasQueryOrderby(String queryIn) {
        boolean retBoolean = false;
        if (queryIn == null || queryIn.equals("")) {
            System.out.println("error  UtilsString.hasQueryOrderby queryIn:" + queryIn);
        } else {

            int index = -1;

            index = queryIn.toUpperCase().indexOf("ORDER BY".toUpperCase());

            if (index != -1) {
                retBoolean = true;
            }
            //System.out.println("UtilsString "+index+"    "+retStr);

        }


        return retBoolean;
    }

    public String getQueryGroupby(String queryIn) {
        String retString = "";
        if (queryIn == null || queryIn.equals("")) {
            System.out.println("error  UtilsString.getQueryGroupby queryIn: " + queryIn);
        } else {

            int indexGroupby = queryIn.toUpperCase().indexOf("GROUP BY".toUpperCase());
            int indexOrderBy = queryIn.toUpperCase().indexOf("ORDER BY".toUpperCase());
            int indexWhere = queryIn.toUpperCase().indexOf("WHERE".toUpperCase());

            if (indexGroupby != -1 && indexOrderBy != -1) {
                if (indexOrderBy == -1) {
                    retString = queryIn.substring(indexGroupby, queryIn.length());
                } else {
                    retString = queryIn.substring(indexGroupby, indexOrderBy);
                }
            } else {
                System.out.println("   error   -   UtilsString.getQueryGroupby    indexGroupby:" + indexGroupby + "  indexOrderBy:" + indexOrderBy + "  indexWhere:" + indexWhere + "           queryIn: " + queryIn);
            }


        }


        //System.out.println("UtilsString "+index+"    "+retStr);

        return retString;
    }

    public String getQueryWithoutOrderby(String queryIn) {
        String retStr = queryIn;
        if (queryIn == null || queryIn.equals("")) {
            System.out.println("error  UtilsString.getQueryWithoutOrderby queryIn:" + queryIn);
        } else {


            retStr = queryIn.replace(getOrderbySubQuery(queryIn), "");
            // System.out.println(" ooooo A  UtilsString.getQueryWithoutOrderby    queryIn:"+queryIn+"     retStr:"+retStr);
            int indexOrderByIfStillExists = queryIn.toUpperCase().indexOf("ORDER BY".toUpperCase());

            if (indexOrderByIfStillExists != -1) {
                retStr = retStr.replace(getOrderbySubQuery(retStr), "");
            }

            //  System.out.println(" ooooo B UtilsString.getQueryWithoutOrderby        retStr:"+retStr);

           /*int indexOrderBy = queryIn.toUpperCase().indexOf("ORDER BY".toUpperCase());
          int indexWhere = queryIn.toUpperCase().indexOf("WHERE".toUpperCase());


          //System.out.println("UtilsString "+index+"    "+retStr);

          if(indexOrderBy!=-1)
          {
              if(indexWhere!=-1 && indexWhere>indexOrderBy)// when WHERE is after ORDER BY
              {
                  retStr=queryIn.substring(0,indexOrderBy)+" "+queryIn.substring(indexWhere,queryIn.length());
              }
              else if(indexWhere!=-1 && indexWhere<indexOrderBy)// when WHERE is before ORDER BY
              {
                  int indexWhereAnd = queryIn.toUpperCase().indexOf("AND".toUpperCase());

                   if(indexWhereAnd!=-1 && indexWhereAnd>indexOrderBy) // when AND is after ORDER BY
                   {
                       System.out.println("getQueryWithoutOrderby when AND is after ORDER BY ");
                       retStr=queryIn.substring(0,indexOrderBy)+" "+queryIn.substring(indexWhereAnd,queryIn.length());
                   }
                   else if(indexWhereAnd!=-1 && indexWhereAnd<indexOrderBy) // when AND is before ORDER BY
                   {
                       System.out.println("getQueryWithoutOrderby when AND is before ORDER BY ");
                       retStr=queryIn.substring(0,indexOrderBy);
                   }
                   else if(indexWhereAnd==-1) // when there is no AND
                   {
                       retStr=queryIn.substring(0,indexOrderBy);
                   }
              }
              else if(indexWhere==-1) // when there is no WHERE
              {
                  retStr=queryIn.substring(0,indexOrderBy);
              }
          }  */
        }
        return retStr;
    }

    public String getQueryBeforeWhere(String queryIn) {
        String retStr = queryIn;
        if (queryIn == null || queryIn.equals("")) {
            System.out.println("error  UtilsString.getQueryBeforeWhere queryIn:" + queryIn);
        } else {
            int indexWhere = queryIn.toUpperCase().indexOf("WHERE".toUpperCase());
            int indexOrderBy = queryIn.toUpperCase().indexOf("ORDER BY".toUpperCase());
            if (indexWhere == -1 && indexOrderBy == -1)// when there is no where
            {
                retStr = queryIn;
            } else if (indexWhere != -1 && indexOrderBy == -1) {
                retStr = queryIn.substring(0, indexWhere);
            } else if (indexWhere == -1 && indexOrderBy != -1) {
                retStr = queryIn.substring(0, indexOrderBy);
            } else if (indexWhere != -1 && indexOrderBy != -1) {
                if (indexOrderBy < indexWhere) {
                    String str = this.replaceStringFromString(indexOrderBy, indexWhere, "", queryIn);
                    int indexOrderByStr = str.toUpperCase().indexOf("ORDER BY".toUpperCase());
                    int indexWhereStr = str.toUpperCase().indexOf("WHERE".toUpperCase());
                    retStr = str.substring(0, indexWhereStr);
                } else if (indexOrderBy > indexWhere) {
                    retStr = queryIn.substring(0, indexWhere);
                } else {
                    System.out.println("error  else A UtilsString.getQueryBeforeWhere queryIn:" + queryIn);
                }
            } else {
                System.out.println("error  else B UtilsString.getQueryBeforeWhere queryIn:" + queryIn);
            }

        }
        return retStr;

    }

    public String getQueryAfterWhere(String queryIn) {
        String retStr = queryIn;
        if (queryIn == null || queryIn.equals("")) {
            System.out.println("error  UtilsString.getQueryWhere queryIn:" + queryIn);
        } else {
            int indexWhere = queryIn.toUpperCase().indexOf("WHERE".toUpperCase());
            int indexOrderBy = queryIn.toUpperCase().indexOf("ORDER BY".toUpperCase());
            int indexGroupBy = queryIn.toUpperCase().indexOf("GROUP BY".toUpperCase());

            //int indexOfAND = queryIn.toUpperCase().indexOf("AND".toUpperCase());


            if (indexOrderBy == -1)// when there is no where
            {
                if (indexGroupBy == -1) {
                    retStr = "";
                } else  // changed also in jar desktop
                {
                    retStr = queryIn.substring(indexGroupBy, queryIn.length());
                }
            } else // when orderby exists
            {
                if (indexGroupBy == -1) {
                    retStr = queryIn.substring(indexOrderBy, queryIn.length());
                    int indexOfAND = retStr.toUpperCase().indexOf("AND".toUpperCase());
                    if (indexOfAND == -1) {
                        retStr = retStr.substring(0, retStr.length());
                    } else {
                        retStr = retStr.substring(0, indexOfAND);
                    }


                } else // when groupby exists
                {
                    retStr = queryIn.substring(indexGroupBy, queryIn.length());


                    int indexOfAND = retStr.toUpperCase().indexOf("AND".toUpperCase());
                    if (indexOfAND == -1) {
                        retStr = retStr.substring(0, retStr.length());
                    } else {
                        retStr = retStr.substring(0, indexOfAND);
                    }
                }

            }


            // System.out.println("UtilsString.getQueryAfterWhere       -------------------------------   indexGroupBy:"+indexGroupBy+"   retStr:"+retStr);


        }
        return " " + retStr;

    }

    /*
     *   works with two previous procedures
     */
    public String getQueryWithoutWhere(String queryIn) {
        String retStr = this.getQueryBeforeWhere(queryIn) + " " + this.getQueryAfterWhere(queryIn);


        return retStr;
    }

    public String getQueryWhere(String queryIn) {
        String retStr = "";
        int indexWhere = queryIn.toUpperCase().indexOf("WHERE".toUpperCase());// if has more than one where it is not calculated
        int indexWhereAnd = queryIn.toUpperCase().indexOf("AND".toUpperCase());
        int indexLeftJoin = queryIn.toUpperCase().indexOf("LEFT JOIN".toUpperCase());
        //int indexAfterWhere = queryIn.toUpperCase().indexOf(this.getQueryAfterWhere(queryIn));
        int indexOrderBy = queryIn.toUpperCase().indexOf("ORDER BY".toUpperCase());
        int indexGroupBy = queryIn.toUpperCase().indexOf("GROUP BY".toUpperCase());
        if (indexWhere == -1 || indexOrderBy == -1) {
            //System.out.println("error missing  UtilsString.getQueryWhere   indexWhere:"+indexWhere+"   indexOrderBy:"+indexOrderBy +"       indexGroupBy:"+indexGroupBy+"     indexLeftJoin:"+indexLeftJoin+"    queryIn:"+queryIn  );
        } else if (indexGroupBy == -1) {
            //System.out.println("error missing 'indexGroupBy' UtilsString.getQueryWhere   indexWhere:"+indexWhere+"   indexOrderBy:"+indexOrderBy +"       indexGroupBy:"+indexGroupBy+"     indexLeftJoin:"+indexLeftJoin+"    queryIn:"+queryIn  );
        } else if (indexLeftJoin == -1) {
            //System.out.println("error missing 'indexLeftJoin' UtilsString.getQueryWhere   indexWhere:"+indexWhere+"   indexOrderBy:"+indexOrderBy +"       indexGroupBy:"+indexGroupBy+"     indexLeftJoin:"+indexLeftJoin+"    queryIn:"+queryIn  );
        }
        //System.out.println("   UtilsString.getQueryWhere   OOOOOOO  indexWhere:"+indexWhere+"   indexOrderBy:"+indexOrderBy+"    indexGroupBy:"+indexGroupBy +"    indexLeftJoin:"+indexLeftJoin+"     queryIn:"+queryIn  );
        if (indexWhere == -1) {

            //        retStr= queryIn.substring(indexLeftJoinFirst,indexAfterWhere);
            System.out.println("       UtilsString.getQueryWhere indexWhere:" + indexWhere + "        queryIn:" + queryIn);
        } else //if(indexAfterWhere==-1)
        {
            if (indexOrderBy == -1 && indexGroupBy == -1) {

                retStr = queryIn.substring(indexWhere, queryIn.length());
            } else if (indexOrderBy != -1 && indexGroupBy == -1) {

                //System.out.println("   UtilsString.getQueryWhere   indexWhere:"+indexWhere+"   indexOrderBy:"+indexOrderBy +"     queryIn:"+queryIn);

                if (indexWhere > indexOrderBy) //    means that query has first the clause orderby and then where
                {
                    String str = this.replaceStringFromString(indexOrderBy, indexWhere, "", queryIn);
                    int indexOrderByStr = str.toUpperCase().indexOf("ORDER BY".toUpperCase());
                    int indexWhereStr = str.toUpperCase().indexOf("WHERE".toUpperCase());
                    retStr = str.substring(indexWhereStr, indexOrderByStr);
                } else {
                    retStr = queryIn.substring(indexWhere, indexOrderBy);
                }
            } else if (indexOrderBy == -1 && indexGroupBy != -1) {

                retStr = queryIn.substring(indexWhere, indexGroupBy);

            } else if (indexOrderBy != -1 && indexGroupBy != -1) {

                retStr = queryIn.substring(indexWhere, indexGroupBy);
                int indexSubOrderBy = retStr.toUpperCase().indexOf("ORDER BY".toUpperCase());
                int indexSubGroupBy = retStr.toUpperCase().indexOf("GROUP BY".toUpperCase());
                if (indexSubOrderBy != -1 && indexSubGroupBy == -1) {
                    retStr = retStr.substring(0, indexSubOrderBy);
                } else if (indexSubOrderBy == -1 && indexSubGroupBy != -1) // exists in wrong position(before)
                {

                    retStr = retStr.substring(0, indexSubGroupBy);

                } else if (indexSubOrderBy == -1 && indexSubGroupBy == -1) {
                    retStr = retStr.substring(0, retStr.length());
                    //System.out.println("  else A    indexSubOrderBy:"+indexSubOrderBy+"    indexSubGroupBy:"+indexSubGroupBy+"   retStr:"+retStr);
                } else {
                    System.out.println("  UtilsString.getQueryWhere  else A  retStr:" + retStr);
                }

            } else {
                System.out.println("   UtilsString.getQueryWhere  else B    indexOrderBy:" + indexOrderBy + "    indexGroupBy:" + indexGroupBy + "    retStr:" + retStr);
            }
        }


        return retStr;
    }

    public String getOrderbySubQuery(String queryIn) {
        String retStr = queryIn;
        if (queryIn == null || queryIn.equals("")) {
            System.out.println("error  UtilsString.getOrderbySubQuery queryIn:" + queryIn);
        } else {

            int indexOrderBy = queryIn.toUpperCase().indexOf("ORDER BY".toUpperCase());
            int indexWhere = queryIn.toUpperCase().indexOf("WHERE".toUpperCase());


            //System.out.println("UtilsString "+index+"    "+retStr);

            if (indexOrderBy != -1) {
                if (indexWhere != -1 && indexWhere > indexOrderBy)// when WHERE is after ORDER BY
                {
                    retStr = queryIn.substring(indexOrderBy, indexWhere);
                } else if (indexWhere != -1 && indexWhere < indexOrderBy)// when WHERE is before ORDER BY
                {
                    int indexWhereAnd = queryIn.toUpperCase().indexOf("AND".toUpperCase());
                    if (indexWhereAnd != -1 && indexWhereAnd > indexOrderBy) // when AND is after ORDER BY
                    {
                        //System.out.println("  when AND is after ORDER BY ");
                        retStr = queryIn.substring(indexOrderBy, indexWhereAnd);
                    } else if (indexWhereAnd != -1 && indexWhereAnd < indexOrderBy) // when AND is before ORDER BY
                    {

                        retStr = queryIn.substring(indexOrderBy, queryIn.length());

                        int indexWhereAnd2 = retStr.toUpperCase().indexOf("AND".toUpperCase());// when there is more than one AND and after ORDER BY
                        //System.out.println("  when AND is before ORDER BY  indexWhereAnd2:"+indexWhereAnd2+"  ");
                        if (indexWhereAnd2 != -1 && indexWhereAnd2 > 0) {
                            retStr = retStr.substring(0, indexWhereAnd2);
                        }

                    } else if (indexWhereAnd == -1) // when there is no AND
                    {
                        retStr = queryIn.substring(indexOrderBy, queryIn.length());
                    }

                } else if (indexWhere == -1) // when there is no WHERE
                {
                    retStr = queryIn.substring(indexOrderBy, queryIn.length());
                }

            } else {
                retStr = "";
            }
        }


        return retStr;
    }

    public String getQueryWithoutGroupByOrOrderBy(String queryIn) {
        String retStr = queryIn;
        if (queryIn == null || queryIn.equals("")) {
            System.out.println("error  UtilsString.getQueryWithoutGroupbyAndOrderby queryIn:" + queryIn);
        } else {

            int index = queryIn.toUpperCase().indexOf("GROUP BY".toUpperCase());

            //System.out.println("UtilsString "+index+"    "+retStr);

            if (index != -1) {

                retStr = queryIn.substring(0, index);
            } else {
                retStr = "";
                //System.out.println("error  UtilsString.getQueryWithoutGroupbyAndOrderby  NO GROUP BY : index:"+index+"    queryIn:"+queryIn);
            }

            if (retStr.equalsIgnoreCase("")) {
                retStr = getQueryWithoutOrderby(queryIn);
                //System.out.println("error  UtilsString.getQueryWithoutGroupbyAndOrderby  NO GROUP BY        queryIn:"+queryIn+"          retStr:"+retStr);
                //if(retStr.equalsIgnoreCase(""))
                //{

                //}
            }

        }


        return retStr;
    }

    public String getGroupbyAndOrderbySubQuery(String queryIn) {
        String retStr = queryIn;
        if (queryIn == null || queryIn.equals("")) {
            System.out.println("error  UtilsString.getGroupbyAndOrderbySubQuery queryIn:" + queryIn);
        } else {

            int index = queryIn.toUpperCase().indexOf("GROUP BY".toUpperCase());

            //System.out.println("UtilsString "+index+"    "+retStr);

            if (index != -1) {

                retStr = queryIn.substring(index, queryIn.length());
            } else {
                retStr = "";
                //System.out.println("error  UtilsString.getGroupbyAndOrderbySubQuery    NO GROUP BY : index:"+index+"  queryIn:"+queryIn);
            }

        }


        return retStr;
    }

    public String removeStringFromString(String strToRemove, String fromString) {
        String retBefore = "";
        String retAfter = "";
        //ret //= fromString.replaceAll(strToremove, "");
        //System.out.println("UtilsString.removeStringFromString fromString "+fromString);
        //System.out.println("UtilsString.removeStringFromString strToRemove "+strToRemove);
        retBefore = fromString.substring(0, fromString.indexOf(strToRemove));
        retAfter = fromString.substring(fromString.indexOf(strToRemove) + strToRemove.length(), fromString.length());


        return retBefore + retAfter;
    }

    /*
     * for prepared statements, called from replaceFromQueryValuesWithQuestionmarks
     */
    private void findValuesInQueryWhere(String queryWhere, String strEqualsValueQuotesStart, String strEqualsValueQuotesEnd, String strStartOfWhere) {

        while (queryWhere.indexOf(" ") >= 0) {
            //idxofEqualsValueQuotesStart = queryWhere.indexOf("='");
            //idxofEqualsValueQuotesEnd = queryWhere.indexOf("'");
            //spaceBeforeField = queryWhere.indexOf(" ");
            int idxofEqualsValueQuotesStart = queryWhere.indexOf(strEqualsValueQuotesStart) + strEqualsValueQuotesStart.length();
            int idxofEqualsValueQuotesEnd = queryWhere.indexOf(strEqualsValueQuotesEnd);
            int idxStartOfWhere = queryWhere.indexOf(strStartOfWhere);
            String fieldValue = "";
            System.out.println("  oAo   findValuesInQueryWhere    (fieldValue:" + fieldValue + ")  [" + strEqualsValueQuotesStart + " " + strEqualsValueQuotesEnd + "]   " + idxofEqualsValueQuotesStart + " . " + idxofEqualsValueQuotesEnd + "    queryWhere:" + queryWhere);
            if (idxofEqualsValueQuotesStart == -1 || idxofEqualsValueQuotesEnd == -1 || idxofEqualsValueQuotesStart >= idxofEqualsValueQuotesEnd) {
            } else {
                //System.out.println("    substring     idxofEqualsValueQuotesStart:"+idxofEqualsValueQuotesStart+"    idxofEqualsValueQuotesEnd:"+idxofEqualsValueQuotesEnd);
                fieldValue = queryWhere.substring(idxofEqualsValueQuotesStart, idxofEqualsValueQuotesEnd);

////////////////////////////////////////////////////////////


                listQueryValue.add(fieldValue);
                System.out.println("  oBo   findValuesInQueryWhere    (fieldValue:" + fieldValue + ")  [" + strEqualsValueQuotesStart + " " + strEqualsValueQuotesEnd + "]  queryWhere:" + queryWhere);
            }
            //int idxStartOfWhere =  queryWhere.indexOf("AND");
            if (idxStartOfWhere < queryWhere.length() && idxStartOfWhere > 0) {
                queryWhere = queryWhere.substring(idxStartOfWhere, queryWhere.length());
                idxStartOfWhere = idxofEqualsValueQuotesEnd;

            } else {
                break;
            }

        }


    }

    /*
     * for prepared statements, called from replaceFromQueryValuesWithQuestionmarks
     */
    private void findFieldsInQueryWhere(String queryWhere, String strSpaceBeforeField, String strEqualsFieldsEnd, String strStartOfWhere) {

        String fieldToGetTheValueFrom = "";//queryWhere.substring(spaceBeforeField, idxofEqualsFieldsEnd);
        //String fieldToGetTheValueFromPrev="";

        //String queryWhereWithoutFilters = "";// the fields of one table that are equal with the fields of an other table. Like
        while (queryWhere.indexOf(" ") >= 0) {
            //idxofEqualsValueQuotesStart = queryWhere.indexOf("='");
            //idxofEqualsValueQuotesEnd = queryWhere.indexOf("'");
            int idxSpaceBeforeField = queryWhere.indexOf(strSpaceBeforeField);
            int idxofEqualsFieldsEnd = queryWhere.indexOf(strEqualsFieldsEnd);
            int idxStartOfWhere = queryWhere.indexOf(strStartOfWhere);


            if (idxSpaceBeforeField == -1 || idxofEqualsFieldsEnd == -1 || idxSpaceBeforeField >= idxofEqualsFieldsEnd) {
            } else {
                fieldToGetTheValueFrom = queryWhere.substring(idxSpaceBeforeField, idxofEqualsFieldsEnd);

                //queryWhereWithoutFilters = fieldToGetTheValueFrom;
                System.out.println("  --  findFieldsInQueryWhere   (fieldToGetTheValueFrom:" + fieldToGetTheValueFrom + ")    from " + idxSpaceBeforeField + " to " + idxofEqualsFieldsEnd + "     queryWhere:" + queryWhere);
                int ff = 0;
                int aa = 0;
                String txtToLookWhenFieldEqualsWithAnOtherField = " = ";
                String txtWhere = " WHERE ";
                String txtAnd = " AND ";


                  /*while(fieldToGetTheValueFrom.indexOf(txtWhere)!=-1)
                  {
                  fieldToGetTheValueFrom = fieldToGetTheValueFrom.substring(ff+txtWhere.length(), fieldToGetTheValueFrom.length());
                  ff= fieldToGetTheValueFrom.indexOf(txtWhere);
                  //System.out.println("  +o--o+  fieldToGetTheValueFrom:"+fieldToGetTheValueFrom);
                  }*/

                //System.out.println("  +++where+  findFieldsInQueryWhere   (fieldToGetTheValueFrom:"+fieldToGetTheValueFrom+")  ["+strSpaceBeforeField+"  "+strEqualsFieldsEnd+"]  queryWhere:"+queryWhere);

                //  listQueryFieldToGetTheValueFrom.add(fieldToGetTheValueFrom);

                while (fieldToGetTheValueFrom.indexOf(txtAnd) != -1) {
                    fieldToGetTheValueFrom = fieldToGetTheValueFrom.substring(aa + txtAnd.length(), fieldToGetTheValueFrom.length());
                    aa = fieldToGetTheValueFrom.indexOf(txtAnd);
                    //System.out.println("  +o--o+  fieldToGetTheValueFrom:"+fieldToGetTheValueFrom);
                }

                System.out.println("  +++and+  findFieldsInQueryWhere   (fieldToGetTheValueFrom:" + fieldToGetTheValueFrom + ")  [" + strSpaceBeforeField + "  " + strEqualsFieldsEnd + "]  queryWhere:" + queryWhere);
                listQueryFieldToGetTheValueFrom.add(fieldToGetTheValueFrom);


            }
            //fieldValue = queryWhere.substring(idxofEqualsValueQuotesStart, idxofEqualsValueQuotesEnd);
            //int idxStartOfWhere =  queryWhere.indexOf("AND");
            if (idxStartOfWhere < queryWhere.length() && idxStartOfWhere > 0) {
                queryWhere = queryWhere.substring(idxStartOfWhere, queryWhere.length());
                idxStartOfWhere = idxofEqualsFieldsEnd;

            } else {
                break;
            }
            //spaceBeforeField=idxofEqualsValueQuotesStart;
            //idxofEqualsValueQuotesStart =


        }


    }

    /*
     * for prepared statements
     */
    public String replaceFromQueryValuesWithQuestionmarks(String query)// for prepared statements
    {
        String strRet = "";
        String queryWhere = "";
        String queryWhereWithoutFilters = "";// the fields of one table that are equal with the fields of an other table. Like
        if (this.hasQueryWhere(query)) {
            //String queryBeforeWhere = this.getQueryWithoutWhere(query);

            queryWhere = this.getQueryWhere(query);
            if (queryWhere.substring(queryWhere.length() - 1, queryWhere.length()).equalsIgnoreCase("'")) {
                queryWhere = queryWhere + " ";
            }
            //System.out.println("  a   queryWhere:"+queryWhere);
            // remove double spaces
            while (queryWhere.indexOf("  ") >= 0) {
                queryWhere = queryWhere.replaceAll("  ", " ");
            }

            // System.out.println(" b query:"+query);
        /* int spaceBeforeField = queryWhere.indexOf(" ") ;
         int idxofEqualsValue = queryWhere.indexOf("=");
         int idxofEqualsFieldsEnd = queryWhere.indexOf("='");
         // int idxofEqualsValueQuotesEnd = queryWhere.indexOf("'");
         int idxStartOfWhere =  queryWhere.indexOf("AND");*/


            //listQueryFieldToGetTheValueFrom = new ArrayList();
            //listQueryValue = new ArrayList();
            listQueryFieldToGetTheValueFrom.clear();
            listQueryValue.clear();
            // System.out.println("    fieldToGetTheValueFrom:"+fieldToGetTheValueFrom);
            // String valueToBeReplaced = queryWhere.substring(idxofEqualsValueQuotesStart+2, idxofEqualsValueQuotesEnd);
            findFieldsInQueryWhere(queryWhere, " ", "='", "AND");
            findFieldsInQueryWhere(queryWhere, " ", " LIKE ", "AND");

            queryWhere = this.getQueryWhere(query);
            if (queryWhere.substring(queryWhere.length() - 1, queryWhere.length()).equalsIgnoreCase("'")) {
                queryWhere = queryWhere + " ";
            }

         /* int idxofEqualsValueQuotesStart = queryWhere.indexOf("='");
          int idxofEqualsValueQuotesEnd = queryWhere.indexOf("' ");
          idxStartOfWhere =  queryWhere.indexOf("AND");*/
            //String fieldValue = "";//queryWhere.substring(idxofEqualsValueQuotesStart, idxofEqualsValueQuotesEnd);
            findValuesInQueryWhere(queryWhere, "='", "' ", "AND");
            findValuesInQueryWhere(queryWhere, " LIKE ", " ", "AND");

            // int idxSpaceBeforeField = queryWhere.indexOf(strSpaceBeforeField);
            // int idxofEqualsFieldsEnd = queryWhere.indexOf(strEqualsFieldsEnd);
            // queryWhereWithoutFilters = queryWhere.substring(idxSpaceBeforeField, idxofEqualsFieldsEnd);
            queryWhereWithoutFilters = queryWhere;
            //int ff =0;
            //int aa=0;
            String txtToLookWhenFieldEqualsWithAnOtherField = " = ";
            String txtAnd = " AND ";
            //while(queryWhereWithoutFilters.indexOf(txtToLookWhenFieldEqualsWithAnOtherField)!=-1)
            // {

            queryWhereWithoutFilters = queryWhereWithoutFilters.substring("WHERE".length(), queryWhereWithoutFilters.length());
             /*     ff= queryWhereWithoutFilters.indexOf(txtToLookWhenFieldEqualsWithAnOtherField);
                  System.out.println("  ====  queryWhereWithoutFilters:"+queryWhereWithoutFilters);

                  while(queryWhereWithoutFilters.indexOf(txtAnd)!=-1)
                  {
                  queryWhereWithoutFilters = queryWhereWithoutFilters.substring(aa+txtAnd.length(), queryWhereWithoutFilters.length());
                  aa= queryWhereWithoutFilters.indexOf(txtAnd);
                  System.out.println("  ==o==  fieldToGetTheValueFrom:"+queryWhereWithoutFilters);
                  }
              } */

        }

        System.out.println("  UtilsString.replaceFromQueryValuesWithQuestionmarks  listQueryFieldToGetTheValueFrom size:" + listQueryFieldToGetTheValueFrom.size() + "   listQueryValue size:" + listQueryValue.size() + "  queryWhereWithoutFilters:" + queryWhereWithoutFilters);

        if (listQueryFieldToGetTheValueFrom.size() == listQueryValue.size()) {

            String sqlWhereClause = "";
            for (int f = 0; f < listQueryFieldToGetTheValueFrom.size(); f++) {


                if (f > 0 && listQueryFieldToGetTheValueFrom.size() > 0) {
                    sqlWhereClause = sqlWhereClause + " AND ";

                }
                sqlWhereClause = sqlWhereClause + " " + listQueryFieldToGetTheValueFrom.get(f) + " = ?";
                //              System.out.println(" o.o.o.o "+f+" "+listQueryFieldToGetTheValueFrom.get(f)+" "+listQueryValue.get(f));
                // is needed             queryWhereWithoutFilters = queryWhereWithoutFilters.replaceAll(listQueryFieldToGetTheValueFrom.get(f)+" LIKE "+listQueryValue.get(f), "");
                queryWhereWithoutFilters = queryWhereWithoutFilters.replaceAll(listQueryFieldToGetTheValueFrom.get(f) + " ='" + listQueryValue.get(f), "");
                queryWhereWithoutFilters = queryWhereWithoutFilters.replaceAll(listQueryFieldToGetTheValueFrom.get(f) + "='" + listQueryValue.get(f), "");
                queryWhereWithoutFilters = queryWhereWithoutFilters.replaceAll(listQueryFieldToGetTheValueFrom.get(f) + "=" + listQueryValue.get(f), "");
                queryWhereWithoutFilters = queryWhereWithoutFilters.replaceAll(listQueryFieldToGetTheValueFrom.get(f) + " = " + listQueryValue.get(f), "");
                queryWhereWithoutFilters = queryWhereWithoutFilters.replaceAll(listQueryFieldToGetTheValueFrom.get(f) + " =" + listQueryValue.get(f), "");
                queryWhereWithoutFilters = queryWhereWithoutFilters.replaceAll(listQueryFieldToGetTheValueFrom.get(f) + "= " + listQueryValue.get(f), "");
                queryWhereWithoutFilters = queryWhereWithoutFilters.replaceAll("'", "");
                queryWhereWithoutFilters = queryWhereWithoutFilters.replaceAll(" '", "");

                if (queryWhereWithoutFilters != null & !queryWhereWithoutFilters.equalsIgnoreCase("") && queryWhereWithoutFilters.trim().length() <= 3) {
                    queryWhereWithoutFilters = queryWhereWithoutFilters.replaceAll("AND", "");
                }

            }

            queryWhereWithoutFilters = queryWhereWithoutFilters.replaceAll("   ", "");
       /* String strAnd = "AND";
         if(queryWhereWithoutFilters.trim().equalsIgnoreCase(strAnd))
         {
             queryWhereWithoutFilters.replaceAll(strAnd, "");
         }*/


            if (listQueryFieldToGetTheValueFrom.size() == 0) {
                strRet = getQueryBeforeWhere(query) + getQueryAfterWhere(query);
            } else {
                System.out.println("  -<><>-  queryWhereWithoutFilters:" + queryWhereWithoutFilters + "     sqlWhereClause:" + sqlWhereClause);
                strRet = getQueryBeforeWhere(query) + " WHERE " + queryWhereWithoutFilters + " " + sqlWhereClause + "  " + getQueryAfterWhere(query);
                while (strRet.indexOf("  ") >= 0) {
                    strRet = strRet.replaceAll("  ", " ");
                }
            }

        } else// count of fields are not equal with count of values, possible sql injection
        {
            System.out.println("--------------count of fields are not equal with count of values, possible sql injection-------------");
        }

        return strRet;

    }

    public ArrayList getListQueryFields() {
        return listQueryFieldToGetTheValueFrom;

    }

    public ArrayList getListFieldsValue() {
        return listQueryValue;

    }

    /*
     *
     */
    public String replaceStringFromString(int intFrom, int intTo, String strNew, String fromString) {
        String retBefore = "";
        String retAfter = "";
        //ret //= fromString.replaceAll(strToremove, "");
        //System.out.println("UtilsString.removeStringFromString fromString "+fromString);
        //System.out.println("UtilsString.removeStringFromString strToRemove "+strToRemove);
        retBefore = fromString.substring(0, intFrom);
        retAfter = fromString.substring(intTo, fromString.length());


        return retBefore + strNew + retAfter;
    }

    public boolean hasQuerySelectColumnName(String queryIn, String columndbName) {
        boolean retBool = false;
        queryIn = queryIn.substring(0, queryIn.toUpperCase().indexOf("FROM"));
        int index = queryIn.toUpperCase().indexOf(columndbName.toUpperCase());
        if (index == -1) {
            retBool = false;
        } else {
            retBool = true;
        }

        //System.out.println("UtilsString.hasQuerySelectColumnName  retBool:"+retBool+" columndbName:"+columndbName+"  queryIn:"+queryIn);

        return retBool;
    }

    public String removeStringFromString(int intFrom, int intTo, String fromString) {
        String retBefore = "";
        String retAfter = "";
        //ret //= fromString.replaceAll(strToremove, "");
        //System.out.println("UtilsString.removeStringFromString fromString "+fromString);
        //System.out.println("UtilsString.removeStringFromString strToRemove "+strToRemove);
        retBefore = fromString.substring(0, intFrom);
        retAfter = fromString.substring(intTo, fromString.length());


        return retBefore + retAfter;
    }

    public String replaceWhereWithAnd(String query) {
        String ret = "";

        if (query != null && !query.equalsIgnoreCase("") && hasQueryWhere(query)) {
            ret = " AND " + removeStringFromString("WHERE", query);
        }

        return ret;
    }

    public String replaceAndWithWhereQuery(String query) {


     /* query.replaceFirst("AND", "WHERE");
        String ret="";

         int indexOfWhere = query.toUpperCase().indexOf("WHERE".toUpperCase());
          int indexOfAnd = -1;
          indexOfAnd = query.toUpperCase().indexOf("AND".toUpperCase());
          String queryBeforeWhere =query.substring(0, indexOfAnd);
        if(query!=null && !query.equalsIgnoreCase("") && indexOfWhere==-1 && indexOfAnd!=-1 )
        {
             ret = replaceStringFromString(indexOfAnd,3,"WHERE","AND");//queryBeforeWhere+" WHERE "+removeStringFromString("AND",query);
        }*/

        return query.replaceFirst("AND", "WHERE");
    }

    // replace the subquery only
    public String replaceAndWithWhere(String query) {
        String ret = "";


        int index = -1;
        index = query.toUpperCase().indexOf("AND".toUpperCase());

        if (query != null && !query.equalsIgnoreCase("") && index != -1) {
            ret = " WHERE " + removeStringFromString("AND", query);
        }

        return ret;
    }

    public String addSubqueryAndToWhereBeforeOrderBy(String queryIn, String strTableToBeAdded, String strFieldToBeAdded, String strValueToBeAdded) {
        //System.out.println("UtilsString.addSubqueryAndToWhereBeforeOrderBy   input  "+queryIn);
        if (strValueToBeAdded != null && !strValueToBeAdded.trim().equalsIgnoreCase("")) {
            if (hasQueryWhere(queryIn)) {
                String queryOrderBy = "";
                if (hasQueryOrderby(queryIn)) {
                    queryOrderBy = getOrderbySubQuery(queryIn);
                }
                String queryWhere = queryIn.substring(indexOfQueryWhere(queryIn), queryIn.indexOf(queryOrderBy));
                String queryBeforeWhere = queryIn.substring(0, indexOfQueryWhere(queryIn));
                queryWhere = queryWhere + " AND " + strTableToBeAdded + "." + strFieldToBeAdded + " LIKE " + strValueToBeAdded + " ";
                queryIn = queryBeforeWhere + queryWhere + queryOrderBy;

            } else// for example if it has as a query 'orderby' with out 'where'
            {
                String queryOrderBy = "";
                if (hasQueryOrderby(queryIn)) {
                    queryOrderBy = getOrderbySubQuery(queryIn);
                }
                //String queryWhere = query.substring(utilsString.indexOfQueryWhere(query), query.indexOf(queryOrderBy));
                String queryBeforeWhere = queryIn.substring(0, queryIn.indexOf(queryOrderBy));
                String queryWhere = " WHERE " + strTableToBeAdded + "." + strFieldToBeAdded + " LIKE " + strValueToBeAdded + " ";
                queryIn = queryBeforeWhere + queryWhere + queryOrderBy;
            }


        } else // if value is empty, ie in panel many data many panel
        {

        }
        // System.out.println("PanelODMRData.addSubqueryAndToWhereBeforeOrderBy   output  "+queryIn);
        return queryIn;


    }

    /*
     *  used with "#"
     */
    public String replaceTextOfAStringWithText(String textToBeReplacedBy, String str, String[] textToReplace, String ifEmptyTextStringReplaceWithThis) {
        ArrayList listIndices = new ArrayList();
        listIndices = getIndicesOf(str, textToBeReplacedBy);

        if (ifEmptyTextStringReplaceWithThis == null) {
            ifEmptyTextStringReplaceWithThis = "";
        }


        if (textToReplace != null && textToReplace.length == listIndices.size() && textToReplace.length > 0) {

            for (int i = 0; i < listIndices.size(); i++) {
                //System.out.println("UtilsString.replaceTextOfAStringWithText  "+i+"  textToBeReplacedBy:"+textToBeReplacedBy+" textToReplace[i]:"+textToReplace[i]);
                if (textToReplace[i] == null) {
                    System.out.println("  Error   UtilsString.replaceTextOfAStringWithText    textToBeReplacedBy:" + textToBeReplacedBy + " textToReplace[i]:" + textToReplace[i] + "       str:" + str);
                } else if (textToReplace[i].equalsIgnoreCase("")) {
                    //System.out.println("UtilsString.replaceTextOfAStringWithText    "+i+"       textToReplace[i]:"+textToReplace[i]);
                    str = str.replaceFirst(textToBeReplacedBy, ifEmptyTextStringReplaceWithThis);
                } else if (textToReplace[i] != null && !textToReplace[i].equalsIgnoreCase(""))//&& !textToReplace[i].trim().equalsIgnoreCase(""))
                {
                    str = str.replaceFirst(textToBeReplacedBy, textToReplace[i]);
                } else {
                    System.out.println("error   UtilsString.replaceTextOfAStringWithText  " + i + "    textToReplace[i]:" + textToReplace[i] + "     ifEmptyTextStringReplaceWithThis:" + ifEmptyTextStringReplaceWithThis + "    " + textToReplace.length + "=" + listIndices.size());
                }

            }
        } else {
            if (listIndices.size() == 0) {
            } else {
                System.out.println("error  UtilsString.replaceTextOfAStringWithText  textToReplace.length:" + textToReplace.length + "  =  listIndices of#:" + listIndices.size() + "    ifEmptyTextStringReplaceWithThis:" + ifEmptyTextStringReplaceWithThis);
            }
        }

        return str;
    }

    private double Round(double Rval, int Rpl) {
        double p = (double) Math.pow(10, Rpl);
        Rval = Rval * p;
        double tmp = Math.round(Rval);
        return (double) tmp / p;
    }

    public String getVerbal(double money) {
        return getVerbal(money, true);
    }

    public String getVerbal(double money, boolean showZero) {
        return getVerbal(money, showZero, true, "a");
    }

    public String getVerbal(double money, boolean showZero, boolean showCurrency, String noGroup) // no group = a all   d decimal, h hundrend,t thousant, m milions, b billion
    {
        //System.out.println("UtilsString.getVerbal "+money);

        StringBuffer str = new StringBuffer("");
        short index = 0;
        boolean isZero = true;
        boolean isNegative = false;


        if (money < 0) {
            money = -money;
            isNegative = true;
        }

        if (money != (long) money) {
            short value = (short) Round(100 * money - 100 * Math.floor(money), 0);
            if (value >= 100) {
                value -= 100;
                money += 1.0;
            }

            money = (long) money;
            if (value > 0) {
                isZero = false;

                if (money >= 1 && value > 0) {
                    if (noGroup.equalsIgnoreCase("a") || noGroup.equalsIgnoreCase("d")) {
                        str.append(STR_AND);
                    }

                }
                // decimal
                if (noGroup.equalsIgnoreCase("a") || noGroup.equalsIgnoreCase("d")) {
                    str.append(GetValue(value, index, showCurrency, noGroup));
                    //System.out.println("UtilsString append "+noGroup+" "+GetValue(value, index, showCurrency,noGroup));
                }


            }
        }

        while (money >= 1) {
            isZero = false;
            short value = (short) ((long) money % 1000);
            money /= 1000;
            index += 1;  // 1 hundreds, 2 thousands, 3 million, 4 billion

            if (noGroup.equalsIgnoreCase("a") || (noGroup.equalsIgnoreCase("h") && index == 1) || (noGroup.equalsIgnoreCase("t") && index == 2) ||
                    (noGroup.equalsIgnoreCase("m") && index == 3) || (noGroup.equalsIgnoreCase("b") && index == 4)) {
                str.insert(0, GetValue(value, index, showCurrency, noGroup));
                //System.out.println("UtilsString insert "+noGroup+" "+index+" "+GetValue(value, index, showCurrency,noGroup)) ;
            }


            money = (long) money;
        }

        if (isZero) {
            if (showZero) {
                str.delete(0, str.length());
                str.append("ΜΗΔΕΝ ");
                if (showCurrency) {
                    str.append(idx[1]);
                }
            }
        } else {
            if (isNegative)
                str.insert(0, "MEION ");
        }

        return str.toString();
    }

    private String GetValue(short money, short index, boolean showCurrency, String noGroup) // no group = a all   d decimal, h hundrend,t thousant, m milions
    {
        if (index == 2 && money == 1) {
            return "ΧΙΛΙΑ ";
        }

        String str = "";
        int dekmon = money % 100;
        int monades = dekmon % 10;
        int ekatontades = (int) (money / 100);
        int dekades = (int) (dekmon / 10);

//EKATONTADES


        if (ekatontades == 1) {
            if (dekmon == 0) {
                str = e[1] + " ";
            } else {
                str = e[1] + "Ν ";
            }
        } else if (ekatontades > 1) {
            if (index == 2) {
                str = e[ekatontades] + "ΕΣ ";
            } else {
                str = e[ekatontades] + "Α ";
            }
        }


//DEKADES
        switch (dekmon) {
            case 10:
                str += d1[monades]; //"DEKA " with space at the end
                break;
            case 11:
                str += d1[monades];
                monades = 0;
                break;
            case 12:
                str += d1[monades];
                monades = 0;
                break;
            default:
                str += d[dekades];
                break;
        }

//MONADES
        if ((index == 2) &&
                (monades == 1 || monades == 3 || monades == 4)) {
            str += mF[monades];
        } else {
            if (dekmon < 10 || dekmon > 12) {
                str += m[monades];
            }
        }


        if (str.length() > 0 || index == 1) {
            if (index == 0 && money == 1) {
                if (showCurrency) {
                    str += "ΛΕΠΤΟ";
                }
            } else {
                if (index > 1 || showCurrency) {
                    str += idx[index];
                    if (index > 2) {
                        if (index > 3) {
                            str += idx[3];
                        }
                        if (money > 1) {
                            str += "Α ";
                        } else {
                            str += "Ο ";
                        }
                    }
                }
            }
        }

        return str;
    }

    public boolean checkGreekAFM(String afm) {

        boolean ret = false;
        int length = 9;
        int lengthIntl = 2;
        String afmGreekIntl = "EL";
        String afmGreekIntlReversed = "LE";

//if(afm.matches("/^d{"+length+"}$/"))

        if (afm.length() == (length + lengthIntl) && afm.substring(0, 2).equals(afmGreekIntl)) {
            length = length + lengthIntl;
        } else {
            length = length;
        }
//System.out.println("UtilsString.checkAFM "+afm+" "+afm.substring(0,2)+" length "+afm.length());
        if (afm.length() == length || (afm.length() == (length) && afm.substring(0, 2).equals(afmGreekIntl))) {


//afm = afm.split("")reverse().join("");
            char[] ch = new char[length];
            ch = afm.toCharArray();

//System.out.println("checkafm ch.length "+ch.length);

// reverse it
            char[] chReverse = new char[length];
            int c = 0;
            afm = "";
            for (int i = length; i > 0; i--) {
                //chReverse[c]=ch[i];
                //System.out.println("checkafm i "+(i-1));
                afm = afm + ch[i - 1];

                c++;
            }


            int Num1 = 0;
            int pre = 2;
//System.out.println("UtilsString.checkAFM length "+afm.length()+" "+afm.substring(length-2,length)+" for "+afm);
            if (afm.substring(length - 2, length).equals(afmGreekIntlReversed)) {
                for (int iDigit = 1; iDigit <= length + 1; iDigit++) {
                    //Num1 += afm.charAt(iDigit) << iDigit;
                    if (iDigit == 1) {
                        pre = 2;
                    } else {
                        pre = pre * 2;
                    }

                    if (iDigit < length - lengthIntl) {
                        //System.out.println("UtilsString.checkAFM with EL "+iDigit+" "+pre+" for char "+afm.charAt(iDigit));
                        Num1 = Num1 + (Integer.valueOf(afm.charAt(iDigit) + "") * pre);
                    } else {

                    }

                }
            } else if (afm.length() == length) {
                //System.out.println("UtilsString.checkAFM length "+afm.length()+" for "+afm);
                for (int iDigit = 1; iDigit <= length - 1; iDigit++) {
                    //Num1 += afm.charAt(iDigit) << iDigit;
                    if (iDigit == 1) {
                        pre = 2;
                    } else {
                        pre = pre * 2;
                    }
                    //System.out.println("UtilsString.checkAFM without EL "+iDigit+" "+pre+" for char "+afm.charAt(iDigit));
                    Num1 = Num1 + (Integer.valueOf(afm.charAt(iDigit) + "") * pre);
                }
            } else {
                System.out.println("error UtilsString.checkAFM undefined rule for afm " + afm);
            }

            //ret = (Num1 % 11) % 10 == afm.charAt(0);
            if (((Num1 % 11) % 10) == Integer.valueOf(afm.charAt(0) + "")) {
                ret = true;
                //System.out.println("checkafm is afm "+afm+" correct "+((Num1 % 11) % 10)+"="+afm.charAt(0));
            } else {
                ret = false;
            }


        } else {
            ret = false;
        }

        return ret;
    }

    /*
     *   by ggps, utilsString
     */
    public boolean checkGreekAFMSyntax(String afm) {
        if (afm == null) {
            return false;
        }

        if (afm.equals("000000000")) {
            return false;
        }

        String chkafm = "afm accepted";
        int i, sum;
        int afmdigits[] = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        boolean afmchk = true;
        boolean afmchkletter = true;
        boolean afmchkdigit = true;
        boolean result;

        sum = 0;

        for (i = 0; i < afm.length(); i++) {
            if (!Character.isDigit(afm.charAt(i)))
                afmchkletter = false;
        }

        if (afmchkletter == true) {
            if ((afm.length() != 9) || (afm.equals(""))) {
                afmchk = false;
                afmchkdigit = false;
            } else {
                for (i = 0; i < 8; i++) {
                    afmdigits[i] = Integer.valueOf(afm.substring(i, i + 1))
                            .intValue();
                    ;
                    sum += afmdigits[i] * (int) Math.pow(2, 8 - i);
                }
                afmdigits[8] = Integer.valueOf(afm.substring(8, 9)).intValue();
                int ypol = sum % 11;

                if ((ypol == 10 && afmdigits[8] != 0)
                        || ((ypol < 10) && (ypol != afmdigits[8])))
                    afmchk = false;
            }
        }

        result = afmchk && afmchkletter && afmchkdigit;


        return result;
    }

    /*
     *   by ggps, utilsString
     * Method that checks if an amka is syntactically correct (e.g. 12310237149 is a correct one)
     */
    public boolean checkGreekAMKASyntax(String amka) {

        if (amka == null) {
            return false;
        }

        int i, sum;
        int amkadigits[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int checkDigit;
        boolean amkachk = true;
        boolean amkachkletter = true;
        boolean amkachkday = true;
        boolean amkachkmonth = true;
        boolean result;

        sum = 0;

        for (i = 0; i < amka.length(); i++) {
            //System.out.println("Char ["+i+"] is "+amka.charAt(i));
            if (!Character.isDigit(amka.charAt(i))) {
                //System.out.println("No Char ["+i+"] is "+amka.charAt(i));
                amkachkletter = false;
            }
        }

        if (amkachkletter == true) {
            if ((amka.length() != 11) || (amka.equals(""))) {
                amkachk = false;
                amkachkletter = false;
            } else {
                for (i = 0; i < 10; i++) {
                    amkadigits[i] = Integer.valueOf(amka.substring(i, i + 1))
                            .intValue();
                    ;
                    if (i % 2 == 1) {
                        if (amkadigits[i] < 5) sum += amkadigits[i] * 2;
                        else {

                            sum = sum + 1 + ((amkadigits[i] * 2) % 10);
                        }
                    } else sum += amkadigits[i];

                }


                checkDigit = Integer.valueOf(amka.substring(10, 11)).intValue();
                int ypol = 10 - sum % 10;

                if ((ypol == 10 && checkDigit != 0)
                        || ((ypol != 10) && (ypol != checkDigit))) {

                    amkachk = false;
                }
            }

            // Check if first 2 digits are between 00 and 31
            // and next two digits are between 01 and 12
            if (amkachk && amkachkletter) {
                int day = Integer.valueOf(amka.substring(0, 2));
                if (day > 31 || day < 0) amkachkday = false;
                int month = Integer.valueOf(amka.substring(2, 4));
                if (month > 12 || month < 1) amkachkmonth = false;

            }
        }

        result = amkachk && amkachkletter && amkachkday && amkachkmonth;

        return result;

    }

    //https://howtodoinjava.com/security/aes-256-encryption-decryption/
    public String encrypt(String strToEncrypt) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
            //UtilsGui utilsGui = new UtilsGui();
            //utilsGui.showMessageError("Error while encrypting: " + e.getMessage()+". Application will terminate");
            System.out.println("UtilsString.encrypt ERROR while encrypting: " + e.getMessage() + ". Application will terminate");
            //System.exit(0);


        }
        return null;
    }

    //https://howtodoinjava.com/security/aes-256-encryption-decryption/
    public String decrypt(String strToDecrypt) {
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
            //UtilsGui utilsGui = new UtilsGui();
            //utilsGui.showMessageError(
            System.out.println("UtilsString.decrypt ERROR in setup configuration. Application will terminate.");

            //System.exit(0);

        }
        return null;
    }

    /*
     *    https://stackoverflow.com/questions/6481627/java-security-illegal-key-size-or-default-parameters
     */
    public void fixEncryptDecryptKeyLength() {
        String errorString = "Failed manually overriding key-length permissions.";
        int newMaxKeyLength;
        try {
            if ((newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES")) < 256) {
                Class c = Class.forName("javax.crypto.CryptoAllPermissionCollection");
                Constructor con = c.getDeclaredConstructor();
                con.setAccessible(true);
                Object allPermissionCollection = con.newInstance();
                Field f = c.getDeclaredField("all_allowed");
                f.setAccessible(true);
                f.setBoolean(allPermissionCollection, true);

                c = Class.forName("javax.crypto.CryptoPermissions");
                con = c.getDeclaredConstructor();
                con.setAccessible(true);
                Object allPermissions = con.newInstance();
                f = c.getDeclaredField("perms");
                f.setAccessible(true);
                ((Map) f.get(allPermissions)).put("*", allPermissionCollection);

                c = Class.forName("javax.crypto.JceSecurityManager");
                f = c.getDeclaredField("defaultPolicy");
                f.setAccessible(true);
                Field mf = Field.class.getDeclaredField("modifiers");
                mf.setAccessible(true);
                mf.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                f.set(null, allPermissions);

                newMaxKeyLength = Cipher.getMaxAllowedKeyLength("AES");
            }
        } catch (Exception e) {
            throw new RuntimeException(errorString, e);
        }
        if (newMaxKeyLength < 256)
            throw new RuntimeException(errorString); // hack failed
    }


    public boolean isNumeric(String strNum) {
        Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
    }


}



