package reqsimile;

import java.awt.Color;
import java.util.Collection;

/**
 * Application constants
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public interface APPCONSTANTS {
	public static final String INI_FILE = "reqsimile.ini";	// Preference in INI format
	public static final String APP_ICON_FILENAME = "reqsimile.gif"; // Application icon file
	public static final String APP_NAME = "ReqSimile";
	public static final String APP_VERSION = "1.2";
	
	public static final String PROJECT_FILE_EXT = "rqs";
	public static final String PROJECT_FILE_ID = ";ReqSimile";

	// Menu items
	public static final int FILE_EXIT = 101;
	public static final int FILE_OPEN = 102;
	public static final int FILE_SAVE = 103;
	
	public static final int DATA_SOURCES = 201;
	public static final int DATA_FETCH = 202;
	public static final int DATA_PREPROCESS = 203;
	
	public static final int VIEW_A = 401;
	public static final int VIEW_B = 402;
	public static final int VIEW_LOG = 403;
	public static final int VIEW_RESULT = 404;

	public static final int REPORT_TOKENCOUNT = 501;
	public static final int REPORT_SUMMARY = 502;	
	
	public static final int HELP_ABOUT = 601;

	//	
	public static final int N = 2;	// # requirements sets
	public static final int NR = 1;	// # resulting requirements sets 
	public static final int A = 0;	// Requirements set A
	public static final int B = 1;	// Requirements set B
	public static final int S = 2;	// Requirements set with similar requirements 

	// Property sections
	public static final String PREF_SECTION_PROJECT = "PROJECT";
	public static final String PREF_SECTION_DB_GENERAL   = "DB";
	public static final String[] PREF_SECTION_DB = {"DB_A", "DB_B"};
	public static final String PREF_SECTION_LINKS = "LINKS";

	// Property names
	public static final String PREF_PROJECT = "LastProject";
	public static final String PREF_DB_TOKENS_TABLE = "tokenTable";
	public static final String PREF_DB_NAME = "name";
	public static final String PREF_DB_DRIVER_URL = "driverURL";
	public static final String PREF_DB_DRIVER_CLASSNAME = "driverClassName";
	public static final String PREF_DB_URL = "URL";
	public static final String PREF_DB_USERNAME = "userName";
	public static final String PREF_DB_PASSWORD = "password";
	public static final String PREF_DB_TABLE = "table";
	public static final String PREF_DB_FIELD_KEY = "keyField";
	public static final String PREF_DB_FIELDS_SUMMARY = "summaryFields";
	public static final String PREF_DB_FIELDS_DETAIL = "detailFields";
	public static final String PREF_DB_FIELDS_CALC = "calcFields";
	public static final String PREF_DB_PREPROCESS_TABLE = "preProcessTable";
	public static final String PREF_DB_TOKENWEIGHTS_TABLE = "tokenWeightsTable"; 
	public static final String[] PREF_DB_FIELD_LINK_KEY = {"linksFieldA", "linksFieldB"};
		
	// Default property values
	public static final String PREF_DEF_PROJECT = "default.ini";
	
	public static final String PREF_DEF_DB_TOKENS_TABLE = "rsTokens";
	public static final String[] PREF_DEF_DB_NAME = {"Requirements set A", "Requirements set B"};
	public static final String PREF_DEF_DB_DRIVER_URL = "";
	public static final String PREF_DEF_DB_DRIVER_CLASSNAME = "sun.jdbc.odbc.JdbcOdbcDriver";
	public static final String PREF_DEF_DB_URL = "jdbc:odbc:";
	public static final String PREF_DEF_DB_USERNAME = "";
	public static final String PREF_DEF_DB_PASSWORD = "";
	public static final String PREF_DEF_DB_TABLE = "";
	public static final String PREF_DEF_DB_FIELD_KEY = "";
	public static final String PREF_DEF_DB_FIELDS_SUMMARY = "[]"; // Empty list. Must be stored in this format as it is parsed.
	public static final String PREF_DEF_DB_FIELDS_DETAIL = "[]"; // Empty list. Must be stored in this format as it is parsed.
	public static final String PREF_DEF_DB_FIELDS_CALC = "[]"; // Empty list. Must be stored in this format as it is parsed.
	public static final String[] PREF_DEF_DB_PREPROCESS_TABLE = {"rsPreProcessedA", "rsPreProcessedB"};
	public static final String[] PREF_DEF_DB_TOKENWEIGHTS_TABLE = {"rsTokenWeightsA", "rsTokenWeightsB"}; 
	public static final String[] PREF_DEF_DB_FIELD_LINKS_KEY = {"Key_A", "Key_B"};

	static final int FIELD_SUMMARY = 1;		// Field type: Is shown in summary view
	static final int FIELD_DETAIL = 2;		// Field type: Is shown in detail view
	static final int FIELD_CALC = 4;		// Field type: Is used in similarity calculation
	static final int FIELD_RESULT = 8;		// Field type: Is used in result view

	static final String RESULT_FIELD_SIMVALUE = "Similarity";
	static final String RESULT_FIELD_LINK_BUTTON = "Link";
	static final Collection RESULT_FIELDS = Common.toCollection("[" + 
			RESULT_FIELD_SIMVALUE  + ", " +
			RESULT_FIELD_LINK_BUTTON + "]");
	
	// Colors
	static final Color CLR_ACTIVE_LINK = new Color(170, 230, 170);
	static final Color CLR_PASSIVE_LINK = new Color(220, 220, 220);
}
