package com.dbms.csmq;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

import com.dbms.entity.cqt.RefConfigCodeList;
import com.dbms.service.IRefCodeListService;
import com.dbms.util.CqtConstants;




//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;


/**
 * Class description
 *
 *
 * @version        1.0, 10/10/21
 * @author         Tom Struzik
 */
@ApplicationScoped
@ManagedBean(name="appCSMQBean")
public class CSMQBean {

        
    String initialize = null;
    
    // ******** VERSION **********
    public static final String APP_VERSION = "01.09";
    public static final String APP_BUILD = "00";
    public static final String APP_BUILD_DATE = "21-SEP-2016";
    public static final String RESOURCE_BUNDLE_NAME = "NMAT";
    // ******** VERSION **********

    // ******** GENERAL *********
    public static String appName;
    public static String supportURL;
    public static String customMQName;
    public static final String SMQ_NAME = "SMQ";
    private String tmsURL;
    private String dbURL;
    public static final int HISTORY_LENGTH = 10;

    // RESOURCES
 //  public static final Logger logger = Logger.getRootLogger();
    private static ResourceBundle resourceBundle;

    //DUMMY USER INFO TO ALLOW FOR USERS TO BE AUTHENTICATED ON THE DB
    public static final String DUMMY_USER = "DUMMY";
    public static final String DUMMY_PASSWORD = "123456789";
    public static final String DUMMY_GROUP = "NMAT";

    // DML CODES
    public static final String DML_INSERT = "I";
    public static final String DML_UPDATE = "U";
    public static final String DML_NONE = "X";

    // WIZARD MODES
    public static final String WIZARD_MODE_INSERT = "CREATING";
    public static final String WIZARD_MODE_EDIT = "UPDATING";


    // USER ACTION MODES
    public static final int MODE_INSERT_NEW = 1;
    public static final int MODE_UPDATE_EXISTING = 2;
    public static final int MODE_COPY_EXISTING = 3;
    public static final int MODE_UPDATE_SMQ = 4;
    public static final int MODE_BROWSE_SEARCH = 5;
    public static final int MODE_HISTORIC = 6;
    public static final int MODE_IMPACT_ASSESSMENT = 7;
    public static final int MODE_VIEW_VERSION_IMPACT = 8;

    // CRITICAL EVENTS
    public static final String CRITICAL_EVENT_NARROW = "Y_NARROW";
    public static final String CRITICAL_EVENT_BROAD = "Y_BROAD";
    public static final String CRITICAL_EVENT_NO = "N";

    // IMPACT STATUSES
    public static final String DELETED_MERGED_MOVED_TERM_RELATION = "1020";
    public static final String NON_CURRENT_LLT = "1030";
    public static final String MEDDRA_INSERTED_ADDED_TERM_RELATION = "1040";
    public static final String CHANGE_IN_TERMSCP = "1050";
    public static final String MQM_INSERTED_ADDED_TERM_RELATION_NEW = "1070";
    public static final String MQM_INSERTED_ADDED_TERM_RELATION_EXISTING = "1110";
    public static final String RENAMED_TERMS = "1080";
    
    // RESOURCES


    //SEARCH PARAMS
    public static final String BOTH_ACTIVITY_STATUSES = "ALL";
    public static final String ACTIVE_ACTIVITY_STATUS = "A";
    public static final String INACTIVE_ACTIVITY_STATUS = "I";
    public static final String PENDING_ACTIVITY_STATUS = "P";
    public static final String BOTH_RELEASE_STATUSES = "ALL";
    public static final String CURRENT_RELEASE_STATUS = "CURRENT";
    public static final String PENDING_RELEASE_STATUS = "PENDING";
    public static final String CURRENT_IF_PENDING_NULL = "PENDING_NULL";
    public static final String CURRENT_RELEASE_STATUS_IA = "CURRENT_IA";
    public static final String PENDING_RELEASE_STATUS_IA = "PENDING_IA";
    public static final String CURRENT_IF_PENDING_NULL_IA = "PENDING_NULL_IA";
    

    public static final String WILDCARD = "%";
    public static final String NMQ_SMQ_SEARCH = "%MQ%";
    public static final String HAS_SCOPE = "Y";
    public static final String DEFAULT_ALGORITHM = "N";
    public static final String TRUE = "Y";
    public static final String FALSE = "N";

    // ROLES
    public final static String ROLE_USER = "User";
    public final static String ROLE_MQM = "MQM";
    public final static String ROLE_REQUESTOR = "Requestor";
    public final static String ROLE_ADMIN = "Administrator";
    /*
     * @author MTW
     * 06/30/2014
     * @fsds NMAT-UC01.02 & NMAT-UC11.02
     */
    public final static String ROLE_DESIGNEE = "Designee";

    // APP-DEFINED ORACLE ERRORS
    public static final String NAME_IN_USE_ERROR = "ORA-20111";
    public static final String NAME_IN_USE_ERROR_NEW = "ORA-20010";
    public static final String INVALID_PROMOTION_ERROR = "ORA-20010";
    public static final String ACTIVATION_ERRORS = "ORA-20997";
    public static final String CHECK_MODE_ERRORS = "ORA-20998";
    public static final String INVALID_PROMOTION_SEQUENCE_ERROR = "ORA-20991";
    public static final String PROMOTION_DEPENDENCY_ERROR = "ORA-20019";
    public static final String INVALID_STATE_CHANGE_ERROR = "ORA-20901";
    public static final String INVALID_STATE_CHANGE_FROM_PENDING_TO_DRAFT_ERROR = "ORA-20101";
    public static final String RECORD_LOCKED_ERROR = "ORA-20000";    
    public static final String MUST_BE_NMQ_OR_SMQ_ERROR = "ORA-20902";
    public static final String GENERIC_ACTIVATION_ERROR = "ORA-20903";
    public static final String DATABASE_CONFIGURATION_ERROR = "ORA-20992";
    public static final String SUCCESS = "Success";
    public static final String FAILURE = "Failure";
    
    // ORACLE ERROR CODES
    public static final int PASSWORD_EXPIRY_WARNING = 28002;

    // STATE CODES
    public static final String STATE_PROPOSED = "Proposed";
    public static final String STATE_REQUESTED = "Requested";
    public static final String STATE_DRAFT = "Draft";
    public static final String STATE_PENDING_IMPACT_ASSESSMENT = "Pending Impact Assessment";
    public static final String STATE_REVIEWED = "Reviewed";
    public static final String STATE_APPROVED = "Approved";
    public static final String STATE_PUBLISHED = "Published";
    public static final String FINAL = "Final";
    public static final String ALL_STATES = "ALL";

    // IA STATES
    public static final String IA_STATE_REVIEWED = "Reviewed IA";
    public static final String IA_STATE_APPROVED = "Approved IA";
    public static final String IA_STATE_PUBLISHED = "Published IA";
    public static final String STATE_ACTIVATED = "Activated";

    // TERM TYPES
    // GENERIC
    public static final String SMQ = "MQ";
    public static final String NMQ = "NMQ";
    public static final String SOC = "SOC";
    public static final String HLGT = "HLGT";
    public static final String HLT = "HLT";
    public static final String PT = "PT";
    public static final String LLT = "LLT";
    public static final String CMQ = "CMQ";
    
    // SPECIFIC
    public static final String NMQ_LEVEL_1 = "NMQ1";
    public static final String SMQ_LEVEL_1 = "MQ1";
    public static final String NMQ_LEVEL_2 = "NMQ2";
    public static final String SMQ_LEVEL_2 = "MQ2";
    public static final String NMQ_LEVEL_3 = "NMQ3";
    public static final String SMQ_LEVEL_3 = "MQ3";
    public static final String NMQ_LEVEL_4 = "NMQ4";
    public static final String SMQ_LEVEL_4 = "MQ4";
    public static final String NMQ_LEVEL_5 = "NMQ5";
    public static final String SMQ_LEVEL_5 = "MQ5";
    public static final String NMQ_SEARCH = "NMQ%";
    public static final String SMQ_SEARCH = "MQ%";

    // SCOPES
    public static final String FULL_NMQ_SMQ = "0";
    public static final String SCOPE_BROAD = "1";
    public static final String SCOPE_NARROW = "2";
    public static final String SCOPE_CHILD_NARROW = "3";
    public static final String SCOPE_FULL = "4";

    // Categories
    public static final String CATEGORY_A = "A";
    public static final String CATEGORY_S = "S";
    public static final String CATEGORY_B = "B";
    public static final String CATEGORY_C = "C";
    public static final String CATEGORY_D = "D";
    public static final String CATEGORY_E = "E";
    public static final String CATEGORY_F = "F";
    public static final String CATEGORY_G = "G";
    public static final String CATEGORY_H = "H";
    public static final String CATEGORY_I = "I";

    // IMPACT TYPE
    public static final String IMPACT_TYPE_IMPACTED = "IMPACTED";
    public static final String IMPACT_TYPE_NONIMPACTED = "NON-IMPACTED";
    public static final String IMPACT_TYPE_ICC = "ICC";
    public static final String IMPACT_TYPE_IPC = "IPC";
    public static final String IMPACT_TYPE_ICS = "ICS";
    
    // MISC
    public static final String DEFAULT_PRODUCT = "STANDARD";
    // This is to prevent queries from running.  It adds a if 1=:killSwichValue to the query.
    // If the value is anyting but 1 then it will not run.
//    public static final Integer KILL_SWITCH_OFF = 1;
//    public static final Integer KILL_SWITCH_ON = 11; // this one goes to 11
//    public static final Integer HIERARCHY_KILL_SWITCH = 0;
    public static final char DEFAULT_SEARCH_DELIMETER_CHAR = '^';
    public static final String DEFAULT_SEARCH_DELIMETER_STRING = "^";
    public static final char DEFAULT_DELIMETER_CHAR = '|';
    public static final String DEFAULT_DELIMETER_STRING = "\\|";
    public static final String DEFAULT_END_DATE = "3501-08-15";
    public static final String DEFAULT_END_DATE_FORMAT = "yyyy-mm-dd";
//    public static final Integer SHOW_PASSWORD_WARNING_CUTOFF = 10;
   public static final Integer NULL_PASSWORD_EXPIRATION = -11;
    
    public static final int MQ_INIT = 0;
    public static final int MQ_SAVED = 1;
    public static final int MQ_MODIFIED = 2;
    public static final int MQ_SAVE_ERROR = 3;
    public static final int MQ_REFRESHED = 4;

    // DICTIONARIES
    public static String defaultFilterDictionaryShortName;
    public static String defaultBaseDictionaryShortName;
    private String validDictionaryList;

    // RELEASE GROUPS - these are for EL access
    public static String defaultMedDRAReleaseGroup;
    public static String defaultDraftReleaseGroup;
    public static String defaultPublishReleaseGroup;
    public static String defaultMEDSMQReleaseGroup;
    
    // PRIMEFACES GROWL COMPONENT MESSAGE LIFE
    public static int DEFAULT_GROWL_LIFE = 6000;
    
    /*
     * @author MTW
     * 06/20/2014
     * @fsds NMAT-UC01.01 & NMAT-UC11.01
     */
    public static String defaultExtension;
    public static final String ALL_EXTENSIONS = "%";
    public static final String FILTER_LEVEL_ONE = "1";
    public static final String BASE_LEVEL_ONE = "SOC";

//    private String version;
//    private static final Hashtable activationGroupSelectItems = new Hashtable();
//    private static final Hashtable levelsSelectItems = new Hashtable();
//    private static final ArrayList<SelectItem> reportsSelectItems = new ArrayList<SelectItem>();
//    private String configFileName;
//    private static Hashtable properties;
    
    
    @ManagedProperty("#{RefCodeListService}")
	private IRefCodeListService refCodeListService;

//    public CSMQBean() {
// 
//        
// 
//        logger.info ("****************************");
//        logger.info ("*** STARTING NMAT SERVER ***");
//        logger.info ("***                      ***");
//        
//        loadProperties();
//        
//        logger.info ("*** " + appName + " SERVER STARTED");
//        logger.info ("*** VERSION: " + APP_VERSION + "." + APP_BUILD);
//        logger.info ("*** DATE: " + APP_BUILD_DATE);
//        logger.info ("*** DB URL: " + dbURL);
//        logger.info ("*** TMS URL: " + tmsURL);
//        logger.info ("*** START DATE: " + new Date().toString());
//        logger.info ("****************************");
//        logger.info ("****************************");
//        
//    }
//
//    
//    
    private void loadProperties() {
//        logger.info ("*** LOADING PROPERTIES   ***");
//        Boolean propsLoaded = loadPropsFromDB ();
//        dbURL = DMLUtils.getJDBCURL();
//        
//        if (!propsLoaded) {
//            logger.info("!!!! UNABLE TO LOAD PROPERTIES !!!! ");
//            return;
//        }
//        
//        this.defaultFilterDictionaryShortName = getProperty("DEFAULT_FILTER_DICTIONARY_SHORT_NAME");
//        this.defaultBaseDictionaryShortName = getProperty("DEFAULT_BASE_DICTIONARY_SHORT_NAME");
//
//        this.defaultMedDRAReleaseGroup = getProperty("DEFAULT_MEDDRA_RELEASE_GROUP");
//        this.defaultDraftReleaseGroup = getProperty("DEFAULT_DRAFT_RELEASE_GROUP");
//        this.defaultPublishReleaseGroup = getProperty("DEFAULT_PUBLISH_RELEASE_GROUP");
//        this.defaultMEDSMQReleaseGroup = getProperty("DEFAULT_MEDSMQ_RELEASE_GROUP");
//
        this.appName = getProperty("APPLICATION_NAME");
//        this.tmsURL = getProperty("TMS_URL");
//
//
//
//        setAGSelItems();
//        setQuerySelItems();
//        getReportSelItems();
//        defaultExtension = getProperty("DEFAULT_EXTENSION");
//        
//        /*
//         * @author MTW
//         * 06/20/2014
//         * @fsds NMAT-UC01.01 & NMAT-UC11.01
//         */
//        this.customMQName = defaultExtension;
    }
//    
//    
//    private void testReports() {
//        
//        String reportDir = getProperty("REPORT_SOURCE");
//        File f = new File(reportDir);
//              
//        logger.info("TESTING REPORTS...");
//        logger.info("REPORT DIRECTORY: " + reportDir + " [" + (f.exists() && f.isDirectory()) + "]");
//        
//        String reportList = getProperty("REPORT_LIST");
//        String[] reports = reportList.split(",");
//
//        for (String report : reports) {
//            String[] selItemRawData = report.split("\\|");
//            String reportFile = reportDir + selItemRawData[1] + ".jrxml";
//            f = new File (reportFile);
//            logger.info(" REPORT: " + selItemRawData[0]);
//            logger.info("         " + reportFile + " [" + f.exists() + "]");
//            }
//    }
//
//
//    private void setAGSelItems() {
//        
//        logger.info("LOADING ACTIVATION GROUPS...");
//        String dictList = getProperty("VALID_DICTIONARY_LIST");
//        String[] dictionaries = dictList.split(",");
//
//        for (String dictionary : dictionaries) {
//            ArrayList<SelectItem> actGroupList = new ArrayList<SelectItem>();
//            String actGroups = getProperty(dictionary);
//            String[] activationGroups = actGroups.split(",");
//            actGroupList.add(new SelectItem(WILDCARD, "ALL", null, false, false, true)); // ADD THE ALL TO EACH
//            for (String activationGroup : activationGroups) {
//                String label = getProperty(activationGroup);
//                logger.info ("ADDING GROUP: " + activationGroup + ":" + label);
//                SelectItem se = new SelectItem(activationGroup, label);
//                actGroupList.add(se);
//            }
//            activationGroupSelectItems.put(dictionary, actGroupList);
//        }
//    }
//
//
//    private void getReportSelItems() {
//        testReports();
//        
//        String queryType = getProperty("REPORT_LIST");
//        String[] reports = queryType.split(",");
//        if (null != reportsSelectItems && reportsSelectItems.size() > 0){
//            reportsSelectItems.clear();
//        }
//        for (String report : reports) {
//            String[] selItemRawData = report.split("\\|");
//            SelectItem se = new SelectItem(selItemRawData[1], selItemRawData[0]);
//            reportsSelectItems.add(se);
//        }
//    }
//
//
//    private void setQuerySelItems() {
//        
//        logger.info("LOADING MEDDRA LEVELS...");
//        
//        String queryType = getProperty("MEDRA_SELECT_ITEMS");
//        String[] levels = queryType.split(",");
//        ArrayList<SelectItem> levelList = new ArrayList<SelectItem>();
//        
//
//        for (String level : levels) {
//            String[] selItemRawData = level.split("\\|");
//            SelectItem se = new SelectItem(selItemRawData[1], selItemRawData[0]);
//            levelList.add(se);
//            
//        }
//        levelList.add(new SelectItem(WILDCARD, "ALL", null, false, false, true)); // add a wildcard to select all N/SMQs
//        levelsSelectItems.put("MEDRA_SELECT_ITEMS", levelList);
//        
//        logger.info("LOADING MQ LEVELS...");
//        queryType = getProperty("SMQ_SELECT_ITEMS");
//        levels = queryType.split(",");
//        levelList = new ArrayList<SelectItem>();
//        for (String level : levels) {
//            String[] selItemRawData = level.split("\\|");
//            SelectItem se = new SelectItem(selItemRawData[1], selItemRawData[0]);
//            levelList.add(se);            
//        }
//        levelsSelectItems.put("SMQ_SELECT_ITEMS", levelList);
//        
//        logger.info("LOADING CUSTOM LEVELS...");
//        queryType = getProperty("NMQ_SELECT_ITEMS");
//        levels = queryType.split(",");
//        levelList = new ArrayList<SelectItem>();
//        for (String level : levels) {
//            String[] selItemRawData = level.split("\\|");
//            SelectItem se = new SelectItem(selItemRawData[1], selItemRawData[0]);
//            levelList.add(se);
//        }
//        levelsSelectItems.put("NMQ_SELECT_ITEMS", levelList);
//
//        // create the combined list
//        ArrayList<SelectItem> nmqList = (ArrayList<SelectItem>)(levelsSelectItems.get("NMQ_SELECT_ITEMS"));
//        ArrayList<SelectItem> smqList = (ArrayList<SelectItem>)(levelsSelectItems.get("SMQ_SELECT_ITEMS"));
//        ArrayList<SelectItem> NMQ_SMQ_List = new ArrayList<SelectItem>();
//
//        // add the wildcars to each level list 
//        nmqList.add(new SelectItem(NMQ_SEARCH, "ALL NMQ", null, false, false, true)); // add a wildcard to select all NMQs
//        smqList.add(new SelectItem(SMQ_SEARCH, "ALL SMQ", null, false, false, true)); // add a wildcard to select all SMQs
//
//        //combine them
//        NMQ_SMQ_List.addAll(nmqList);
//        NMQ_SMQ_List.addAll(smqList);
//        NMQ_SMQ_List.add(new SelectItem(NMQ_SMQ_SEARCH, "All Levels", null, false, false, true)); // add a wildcard to select all N/SMQs
//        // add them to the combined list
//        levelsSelectItems.put("NMQ_SQM_SELECT_ITEMS", NMQ_SMQ_List);
//    
//        //AMC -- This is where we can edit the wildcard to include levels instead
//        
//        // add a separate wildcard list for just "query type" use (they don't need levels)
//        levelList = new ArrayList<SelectItem>();
//        levelList.add(new SelectItem(NMQ_SMQ_SEARCH, "BOTH", null, false, false, true));
//        levelList.add(new SelectItem(NMQ_SEARCH, customMQName, null, false, false, true));
//        levelList.add(new SelectItem(SMQ_SEARCH, "SMQ", null, false, false, true));
//        levelsSelectItems.put("QUERY_TYPE", levelList);
//    }
//
//
//    public ArrayList<SelectItem> getAGsForDictionary(String dictionary) {
//        if (dictionary == null)
//            dictionary = this.getDefaultFilterDictionaryShortName();
//        return (ArrayList<SelectItem>)activationGroupSelectItems.get(dictionary);
//    }
//
//
//    public ArrayList<SelectItem> getLevelsForQueryType(String queryType) {
//        return (ArrayList<SelectItem>)levelsSelectItems.get(queryType);
//    }
//
//    /*
//        public static void setProperty (String property, String value) {
//                Properties properties = new Properties();
//                FacesContext facesContext = JSFUtils.getFacesContext();
//                ExternalContext ectx = facesContext.getExternalContext();
//
//                try {
//                    properties.load(ectx.getResourceAsStream("/NMAT.properties"));
//                } catch (IOException e) {
//                }
//
//                // Write properties file.
//                try {
//                    properties.store(new FileOutputStream("filename.properties"), null);
//                } catch (IOException e) {
//                }
//            }
//        */
//
//    public static ArrayList<SelectItem> getDictionaryList() {
//        ArrayList<SelectItem> retVal = new ArrayList<SelectItem>();
//        return retVal;
//    }
//
//
//    public static void setCurrentFilterDictionary(String shortName) {
//        CSMQBean.logger.info("SETTING DEFAULT FILTER DICTIONARY ON DB: " + shortName);
//        String sql = "{call smq_codelist_pkg.set_smq_dictionary(?)}";
//        DBTransaction dBTransaction = DMLUtils.getDBTransaction();
//        CallableStatement cstmt = dBTransaction.createCallableStatement(sql, DBTransaction.DEFAULT);
//        try {
//            cstmt.setString(1, shortName);
//            cstmt.executeUpdate();
//            cstmt.close();
//        }
//        catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    // CHANGE TO USE DB
    public static String getProperty(String property) {
        try {
            Object retVal = resourceBundle.getObject(property);
            if (retVal != null)
                return retVal.toString();
        }
        catch (Exception e) {
        }
        return null;
    }
//
//
//    
//    private boolean loadPropsFromDB () {
//        
//        if (properties == null) properties = new Hashtable();
//        properties.clear();
//        
//        String sql = "SELECT * FROM NMAT_PROPERTIES";
//        DBTransaction dBTransaction = DMLUtils.getDBTransaction();
//        CallableStatement cstmt = dBTransaction.createCallableStatement(sql, DBTransaction.DEFAULT);
//        ResultSet rs;
//
//
//        try {
//            rs = cstmt.executeQuery();
//        } catch (SQLException e) {
//            return false;
//        }
//
//
//        try {
//            String p, v = null;
//            while (rs.next()) {
//                p = rs.getString("PROP_NAME");
//                v = rs.getString("PROP_VALUE") + "";
//                
//                logger.info ("*** " + p + "=" + v);
//                properties.put(p,v);
//                }
//        } catch (SQLException e) {
//        return false;
//        }
//        
//        return true;
//        
//    }
//
//    /*
//     * @author MTW
//     * 06/20/2014
//     * @fsds NMAT-UC01.01 & NMAT-UC11.01
//     
//    private String loadDefaultExtension () {
//        String retVal = "";
//        
//        String sql = "SELECT DEFAULT_SHORT_VALUE FROM rxc.reference_codelists WHERE reference_codelist_name = 'MQ_EXT_LIST' ";
//        DBTransaction dBTransaction = DMLUtils.getDBTransaction();
//        CallableStatement cstmt = dBTransaction.createCallableStatement(sql, DBTransaction.DEFAULT);
//        ResultSet rs = null;
//
//
//        try {
//            rs = cstmt.executeQuery();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//
//        try {
//            if (rs.next()) {
//                retVal = rs.getString("DEFAULT_SHORT_VALUE");
//                }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        
//        return retVal;
//        
//    }*/
//
//    public void setDefaultFilterDictionaryShortName(String defaultFilterDictionaryShortName) {
//        this.defaultFilterDictionaryShortName = defaultFilterDictionaryShortName;
//    }
//
//    public String getDefaultFilterDictionaryShortName() {
//        return defaultFilterDictionaryShortName;
//    }
//
//    public void setDefaultBaseDictionaryShortName(String defaultBaseDictionaryShortName) {
//        this.defaultBaseDictionaryShortName = defaultBaseDictionaryShortName;
//    }
//
//    public String getDefaultBaseDictionaryShortName() {
//        return defaultBaseDictionaryShortName;
//    }
//
//    public void setDefaultMedDRAReleaseGroup(String defaultReleaseGroup) {
//        this.defaultMedDRAReleaseGroup = defaultReleaseGroup;
//    }
//
//    public String getDefaultMedDRAReleaseGroup() {
//        return defaultMedDRAReleaseGroup;
//    }
//
//    public void setDefaultDraftReleaseGroup(String defaultDraftReleaseGroup) {
//        this.defaultDraftReleaseGroup = defaultDraftReleaseGroup;
//    }
//
//    public String getDefaultDraftReleaseGroup() {
//        return defaultDraftReleaseGroup;
//    }
//
//    public void setValidDictionaryList(String validDictionaryList) {
//        this.validDictionaryList = validDictionaryList;
//    }
//
//    public String getValidDictionaryList() {
//        this.validDictionaryList = getProperty("VALID_DICTIONARY_LIST");
//        return validDictionaryList;
//    }
//
//    public void setVersion(String version) {
//        this.version = version;
//    }
//
//    public String getVersion() {
//        version = "Version: " + APP_VERSION + "." + APP_BUILD + "\nDate: " + APP_BUILD_DATE;
//        return version;
//    }
//
//    public void setActivationGroupSelectItems(Hashtable activationGroupSelectItems) {
//        //this.activationGroupSelectItems = activationGroupSelectItems;
//    }
//
//    public Hashtable getActivationGroupSelectItems() {
//        return activationGroupSelectItems;
//    }
//
//
//    public void setLevelsSelectItems(Hashtable levelsSelectItems) {
//        //this.levelsSelectItems = levelsSelectItems;
//    }
//
//    public Hashtable getLevelsSelectItems() {
//        return levelsSelectItems;
//    }
//
//    public void setDefaultPublishReleaseGroup(String defaultPublishReleaseGroup) {
//        this.defaultPublishReleaseGroup = defaultPublishReleaseGroup;
//    }
//
//    public String getDefaultPublishReleaseGroup() {
//        return defaultPublishReleaseGroup;
//    }
//
//    public void setDefaultMEDSMQReleaseGroup(String defaultMEDSMQReleaseGroup) {
//        this.defaultMEDSMQReleaseGroup = defaultMEDSMQReleaseGroup;
//    }
//
//    public String getDefaultMEDSMQReleaseGroup() {
//        return defaultMEDSMQReleaseGroup;
//    }
//
//    public void setReportsSelectItems(ArrayList<SelectItem> reportsSelectItems) {
//        //this.reportsSelectItems = reportsSelectItems;
//    }
//
//    public ArrayList<SelectItem> getReportsSelectItems() {
//        return reportsSelectItems;
//    }
//
//    public void setAppName(String appName) {
//        this.appName = appName;
//    }
//
//    public String getAppName() {
//        return appName;
//    }
//
//    public void setCustomMQName(String customMQName) {
//        this.customMQName = customMQName;
//    }
//
//    public String getCustomMQName() {
//        return customMQName;
//    }
//
//
//    public void setConfigFileName(String configFileName) {
//        this.configFileName = configFileName;
//    }
//
//    public String getConfigFileName() {
//        return RESOURCE_BUNDLE_NAME;
//    }
//
//    public void setTmsURL(String tmsURL) {
//        this.tmsURL = tmsURL;
//    }
//
//    public String getTmsURL() {
//        return tmsURL;
//    }
//    
//    public static String getProperty (String property) {
//        String retVal = null;
//        if (properties == null) return "";
//        Object o = properties.get(property);
//        if (o!=null) retVal = (String)o;
//        return retVal;
//        
//    }
//
//    public String refreshProperties() {
//        loadProperties();
//        return null;
//    }
//
//    public void setDbURL(String dbURL) {
//        this.dbURL = dbURL;
//    }
//
//    public String getDbURL() {
//        return dbURL;
//    }
//
//    /*
//     * @author MTW
//     * 06/20/2014
//     * @fsds NMAT-UC01.01 & NMAT-UC11.01
//     */
//    public static void setDefaultExtension(String defaultExtension) {
//        CSMQBean.defaultExtension = defaultExtension;
//    }
//
//    public static String getDefaultExtension() {
//        return defaultExtension;
//    }
//
//
//    public void setInitialize(String initialize) {
//        this.initialize = initialize;
//    }
//
//    public String getInitialize() {
//        initialize = "APP INIT";
//        return initialize;
//    }
	public String getAppName() {
		return appName;
	}
	public static void setAppName(String appName) {
		CSMQBean.appName = appName;
	}
	public String getCustomMQName() {
		return customMQName;
	}
	public static void setCustomMQName(String customMQName) {
		CSMQBean.customMQName = customMQName;
	}
	public String getDefaultFilterDictionaryShortName() {
		return defaultFilterDictionaryShortName;
	}
	public static void setDefaultFilterDictionaryShortName(String defaultFilterDictionaryShortName) {
		CSMQBean.defaultFilterDictionaryShortName = defaultFilterDictionaryShortName;
	}
	public String getDefaultMedDRAReleaseGroup() {
		return defaultMedDRAReleaseGroup;
	}
	public static void setDefaultMedDRAReleaseGroup(String defaultMedDRAReleaseGroup) {
		CSMQBean.defaultMedDRAReleaseGroup = defaultMedDRAReleaseGroup;
	}
	public String getDefaultDraftReleaseGroup() {
		return defaultDraftReleaseGroup;
	}
	public static void setDefaultDraftReleaseGroup(String defaultDraftReleaseGroup) {
		CSMQBean.defaultDraftReleaseGroup = defaultDraftReleaseGroup;
	}
	public String getDefaultPublishReleaseGroup() {
		return defaultPublishReleaseGroup;
	}
	public static void setDefaultPublishReleaseGroup(String defaultPublishReleaseGroup) {
		CSMQBean.defaultPublishReleaseGroup = defaultPublishReleaseGroup;
	}
	public String getDefaultMEDSMQReleaseGroup() {
		return defaultMEDSMQReleaseGroup;
	}
	public static void setDefaultMEDSMQReleaseGroup(String defaultMEDSMQReleaseGroup) {
		CSMQBean.defaultMEDSMQReleaseGroup = defaultMEDSMQReleaseGroup;
	}
	public String getDefaultExtension() {
		return defaultExtension;
	}
	public static void setDefaultExtension(String defaultExtension) {
		CSMQBean.defaultExtension = defaultExtension;
	}
	public String getValidDictionaryList() {
		return validDictionaryList;
	}
	public void setValidDictionaryList(String validDictionaryList) {
		this.validDictionaryList = validDictionaryList;
	}
	public String getDefaultDelimeterString() {
		return DEFAULT_DELIMETER_STRING;
	}
	
	public int getDefaultGrowlLife() {
        RefConfigCodeList s = getRefCodeListService().findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG, "DEFAULT_GROWL_LIFE");
        if(s != null) {
            try {
                return Integer.parseInt(s.getValue());
            } catch(NumberFormatException nfe) {
                return DEFAULT_GROWL_LIFE;
            }
        }
		return DEFAULT_GROWL_LIFE;
	}
    
    public boolean isDegugMode() {
        RefConfigCodeList s = getRefCodeListService().findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG, "DEBUG_MESSAGE");
        if(s != null && "YES".equalsIgnoreCase(s.getValue()))
            return true;
        return false;
    }
	
	public List<String[]> getCqtBaseScopes() {
		return Arrays.asList(new String[][] {
			new String[] { SCOPE_NARROW, "Narrow" },
			new String[] { SCOPE_BROAD, "Broad" },
//			new String[] { SCOPE_CHILD_NARROW, "Child Narrow" },
			new String[] { SCOPE_FULL, "Full" }
		});
	}
    
    public String interpretCqtBaseScope(String scopeVal) {
        if(SCOPE_NARROW.equals(scopeVal))
            return "Narrow";
        else if(SCOPE_BROAD.equals(scopeVal))
            return "Broad";
        else if(SCOPE_CHILD_NARROW.equals(scopeVal))
            return "Child Narrow";
        else if(SCOPE_FULL.equals(scopeVal))
            return "Full";
        return "";
    }
	
    public static String getCqtBaseScopeByName(String scopeName) {
        if("Narrow".equalsIgnoreCase(scopeName))
            return SCOPE_NARROW;
        else if("Broad".equalsIgnoreCase(scopeName))
            return SCOPE_BROAD;
        else if("Child Narrow".equalsIgnoreCase(scopeName))
            return SCOPE_CHILD_NARROW;
        else 
            return SCOPE_FULL;
    }
    
	public List<String[]> getCqtBaseCategories() {
		return Arrays.asList(new String[][] {
			new String[] { CATEGORY_A, "A" },
			new String[] { CATEGORY_S, "S" },
			new String[] { CATEGORY_B, "B" },
			new String[] { CATEGORY_C, "C" },
			new String[] { CATEGORY_D, "D" },
			new String[] { CATEGORY_E, "E" },
			new String[] { CATEGORY_F, "F" },
			new String[] { CATEGORY_G, "G" },
			new String[] { CATEGORY_H, "H" },
			new String[] { CATEGORY_I, "I" }
		});
	}
    
    public String interpretCqtBaseCategory(String categoryVal) {
        return categoryVal;
    }
    
    public String getSupportURL() {
		RefConfigCodeList s = getRefCodeListService().findByConfigTypeAndInternalCode(CqtConstants.CODE_LIST_TYPE_SYSTEM_CONFIG, "SUPPORT_URL");
		System.out.println(s.getValue());
		return s.getValue();
	}
    /**
     * @return the refCodeListService
     */
    public IRefCodeListService getRefCodeListService() {
        return refCodeListService;
    }

    /**
     * @param refCodeListService the refCodeListService to set
     */
    public void setRefCodeListService(IRefCodeListService refCodeListService) {
        this.refCodeListService = refCodeListService;
    }
}


