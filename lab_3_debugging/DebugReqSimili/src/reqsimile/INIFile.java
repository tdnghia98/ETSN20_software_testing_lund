package reqsimile;
/*------------------------------------------------------------------------------
 * Change Log:
 * by Johan Natt och Dag (johan@nattochdag.org):
 * 13/08/2004    - Added support for comments TODO:Fix so that comments are saved properly
 * 13/08/2004    - Added support for initial/terminal spaces in keys & values 
 * 13/08/2004    - Changed from HashMap to LinkedHashMap to preserve section and key order
 * 13/08/2004    - Added support for default values
 * 13/08/2004    - Removed main() method.
 * 13/08/2004    - Code readability: Changed name of checkFile to fileExists
 * 13/08/2004    - Fixed bugs: iter.hasNext() at the wrong place in for clause
 * 13/08/2004    - Fixed bug: negation in Constructor for checkFile()
 * By original author:
 * 05/07/2004    - Added support for date time formats.
 *                 Added support for environment variables.
 * 07/07/2004    - Added support for data type specific getters and setters.
 *                 Updated main method to reflect above changes.
 *-----------------------------------------------------------------------------*/
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.regex.*;

/**
 * INIFile class provides methods for manipulating (Read/Write) files in the
 * INI format (Microsoft).
 * 
 * @author Johan Natt och Dag
 * @author Prasad P. Khandekar
 * @since 1.4.2
 */
public class INIFile
{
    /** Variable to represent the date format */
    private String mstrDateFmt = "yyyy-MM-dd";

    /** Variable to represent the timestamp format */
    private String mstrTimeStampFmt = "yyyy-MM-dd HH:mm:ss";

    /** Variable to denote the successful load operation. */
    private boolean mblnLoaded = false;

    /** Variable to hold the ini file name and full path */
    private String mstrFile;

    /** Variable to hold the sections in an ini file. */
    private LinkedHashMap mhmapSections;

    /** Variable to hold environment variables **/
    private Properties mpropEnv;

    /**
     * Create a iniFile object from the file named in the parameter.
     * @param pstrPathAndName The full path and name of the ini file to be used.
     */
    public INIFile(String pstrPathAndName)
    {
        this.mpropEnv = getEnvVars();
        this.mhmapSections = new LinkedHashMap();
        this.mstrFile = pstrPathAndName;
        if (fileExists(pstrPathAndName))
            loadFile();
    }

/*------------------------------------------------------------------------------
 * Getters
------------------------------------------------------------------------------*/
    /**
     * Returns the INI file name used.
     * 
     * @return The INI file name.
     */
    public String getFileName()
    {
        return this.mstrFile;
    }
    
    /**
     * Inidicates whether a file has been loaded properly or not
     * 
     * @return True if a file has been loaded, false otherwise.
     */
    public boolean isLoaded() {
    	return this.mblnLoaded;
    }

    /**
     * Returns the specified string property from the specified section.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the string property value.
     */
    public String getStringProperty(String pstrSection, String pstrProp)
    {
        int        intStart = 0;
        int        intEnd   = 0;
        String     strRet   = null;
        String     strVar   = null;
        String     strVal   = null;
        INISection objSec   = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec != null)
        {
            strRet = objSec.getProperty(pstrProp);
            if (strRet != null)
            {
                intStart = strRet.indexOf("%");
                if (intStart >= 0)
                {
                    intEnd = strRet.indexOf("%", intStart + 1);
                    strVar = strRet.substring(intStart + 1, intEnd);
                    strVal = this.mpropEnv.getProperty(strVar);
                    if (strVal != null)
                    {
                        strRet = strRet.substring(0, intStart) + strVal + strRet.substring(intEnd + 1);
                    }
                }
            }
        }

        return strRet;
    }
    
    /**
     * Returns the specified string property from the specified section.
     * If it is not availble, the specified default value is returned
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @param pstrDef the default value of the property
     * @return the string property value.
     */
    public String getStringProperty(String pstrSection, String pstrProp, String pstrDef)
    {
    	String strRet = getStringProperty(pstrSection, pstrProp);
    	if (strRet == null)
    		return pstrDef;
    	else
    		return strRet;
    }

    /**
     * Returns the specified boolean property from the specified section.
     * This method considers the following values as boolean values.
     * <ol>
     *      <li>YES/yes/Yes - boolean true</li>
     *      <li>NO/no/No  - boolean false</li>
     *      <li>1 - boolean true</li>
     *      <li>0 - boolean false</li>
     *      <li>TRUE/True/true - boolean true</li>
     *      <li>FALSE/False/false - boolean false</li>
     * </ol>
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the boolean value
     */
    public Boolean getBooleanProperty(String pstrSection, String pstrProp)
    {
        Boolean    blnRet = null;
        String     strVal = null;
        INISection objSec = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec != null)
        {
            strVal = objSec.getProperty(pstrProp);
            strVal = strVal.toUpperCase();
            if (strVal != null)
            {
                if (strVal.equals("YES") || strVal.equals("TRUE") ||
                    strVal.equals("1"))
                {
                    blnRet = new Boolean(true);
                }
                else if (strVal.equals("NO") || strVal.equals("FALSE") ||
                        strVal.equals("0"))
                {
                    blnRet = new Boolean(false);
                }
            }
        }
        return blnRet;
    }
    
    /**
     * Returns the specified boolean property from the specified section.
     * If it is not availble, the specified default value is returned
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the boolean value
     */
    public Boolean getBooleanProperty(String pstrSection, String pstrProp, Boolean blnDef)
    {
    	Boolean blnRet = getBooleanProperty(pstrSection, pstrProp);
    	if (blnRet == null)
    		return blnDef;
    	else
    		return blnRet;
    }


    /**
     * Returns the specified integer property from the specified section.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the integer property value.
     */
    public Integer getIntegerProperty(String pstrSection, String pstrProp)
    {
        Integer    intRet   = null;
        String     strVal   = null;
        INISection objSec   = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec != null)
        {
            strVal = objSec.getProperty(pstrProp);
            try
            {
                if (strVal != null)
                    intRet = new Integer(strVal);
            }
            catch (NumberFormatException NFExIgnore)
            {
            }
        }
        return intRet;
    }
    
    /**
     * Returns the specified integer property from the specified section.
     * If it is not availble, the specified default value is returned
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the integer property value.
     */
    public Integer getIntegerProperty(String pstrSection, String pstrProp, Integer intDef)
    {
        Integer intRet = getIntegerProperty(pstrSection, pstrProp);
    	if (intRet == null)
    		return intDef;
    	else
    		return intRet;
    }

    /**
     * Returns the specified long property from the specified section.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the long property value.
     */
    public Long getLongProperty(String pstrSection, String pstrProp)
    {
        Long       lngRet   = null;
        String     strVal   = null;
        INISection objSec   = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec != null)
        {
            strVal = objSec.getProperty(pstrProp);
            try
            {
                if (strVal != null)
                    lngRet = new Long(strVal);
            }
            catch (NumberFormatException NFExIgnore)
            {
            }
        }
        return lngRet;
    }
    
    /**
     * Returns the specified long property from the specified section.
     * If it is not availble, the specified default value is returned
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the long property value.
     */
    public Long getLongProperty(String pstrSection, String pstrProp, Long lngDef)
    {
        Long lngRet = getLongProperty(pstrSection, pstrProp);
    	if (lngRet == null)
    		return lngDef;
    	else
    		return lngRet;    	
    }


    /**
     * Returns the specified double property from the specified section.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the double property value.
     */
    public Double getDoubleProperty(String pstrSection, String pstrProp)
    {
        Double     dblRet   = null;
        String     strVal   = null;
        INISection objSec   = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec != null)
        {
            strVal = objSec.getProperty(pstrProp);
            try
            {
                if (strVal != null)
                    dblRet = new Double(strVal);
            }
            catch (NumberFormatException NFExIgnore)
            {
            }
        }
        return dblRet;
    }

    /**
     * Returns the specified double property from the specified section.
     * If it is not availble, the specified default value is returned
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the double property value.
     */
    public Double getDoubleProperty(String pstrSection, String pstrProp, Double dblDef)
    {
        Double dblRet = getDoubleProperty(pstrSection, pstrProp);
    	if (dblRet == null)
    		return dblDef;
    	else
    		return dblRet;    	
    }

    /**
     * Returns the specified date property from the specified section.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the date property value.
     */
    public Date getDateProperty(String pstrSection, String pstrProp)
    {
        Date       dtRet  = null;
        String     strVal = null;
        DateFormat dtFmt  = null;
        INISection objSec = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec != null)
        {
            strVal = objSec.getProperty(pstrProp);
            try
            {
                dtFmt = new SimpleDateFormat(this.mstrDateFmt);
                dtRet = dtFmt.parse(strVal);
            }
            catch (ParseException PExIgnore)
            {
            }
            catch (NullPointerException NPExIgnore)
            {
            }
            catch (IllegalArgumentException IAEx)
            {
            }
        }
        return dtRet;
    }

    /**
     * Returns the specified date property from the specified section.
     * If it is not availble, the specified default value is returned
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the date property value.
     */
    public Date getDateProperty(String pstrSection, String pstrProp, Date dtDef)
    {
        Date dtRet = getDateProperty(pstrSection, pstrProp);
    	if (dtRet == null)
    		return dtDef;
    	else
    		return dtRet;    	
    }

    /**
     * Returns the specified date property from the specified section.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the date property value.
     */
    public Date getTimestampProperty(String pstrSection, String pstrProp)
    {
        Timestamp  tsRet  = null;
        Date       dtTmp  = null;
        String     strVal = null;
        DateFormat dtFmt  = null;
        INISection objSec = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec != null)
        {
            strVal = objSec.getProperty(pstrProp);
            try
            {
                dtFmt = new SimpleDateFormat(this.mstrDateFmt);
                dtTmp = dtFmt.parse(strVal);
                tsRet = new Timestamp(dtTmp.getTime());
            }
            catch (ParseException PExIgnore)
            {
            }
            catch (NullPointerException NPExIgnore)
            {
            }
            catch (IllegalArgumentException IAEx)
            {
            }
        }
        return tsRet;
    }

    /**
     * Returns the specified date property from the specified section.
     * If it is not availble, the specified default value is returned
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be retrieved.
     * @return the date property value.
     */
    public Date getTimestampProperty(String pstrSection, String pstrProp, Timestamp tsDef)
    {
        Date tsRet = getTimestampProperty(pstrSection, pstrProp);
    	if (tsRet == null)
    		return tsDef;
    	else
    		return tsRet;    	
    }
    
/*------------------------------------------------------------------------------
 * Setters
------------------------------------------------------------------------------*/
    /**
     * Sets the specified string property.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be set.
     * @param pstrVal the string value to be persisted
     */
    public void setStringProperty(String pstrSection, String pstrProp, String pstrVal)
    {
        INISection objSec   = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec == null)
        {
            objSec = new INISection(pstrSection);
            this.mhmapSections.put(pstrSection, objSec);
        }
        objSec.setProperty(pstrProp, pstrVal);
    }

    /**
     * Sets the specified boolean property.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be set.
     * @param pblnVal the boolean value to be persisted
     */
    public void setBooleanProperty(String pstrSection, String pstrProp, boolean pblnVal)
    {
        INISection objSec   = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec == null)
        {
            objSec = new INISection(pstrSection);
            this.mhmapSections.put(pstrSection, objSec);
        }
        if (pblnVal)
            objSec.setProperty(pstrProp, "TRUE");
        else
            objSec.setProperty(pstrProp, "FALSE");
    }

    /**
     * Sets the specified integer property.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be set.
     * @param pintVal the int property to be persisted.
     */
    public void setIntegerProperty(String pstrSection, String pstrProp, int pintVal)
    {
        INISection objSec   = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec == null)
        {
            objSec = new INISection(pstrSection);
            this.mhmapSections.put(pstrSection, objSec);
        }
        objSec.setProperty(pstrProp, Integer.toString(pintVal));
    }

    /**
     * Sets the specified long property.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be set.
     * @param plngVal the long value to be persisted.
     */
    public void setLongProperty(String pstrSection, String pstrProp, Long plngVal)
    {
        INISection objSec   = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec == null)
        {
            objSec = new INISection(pstrSection);
            this.mhmapSections.put(pstrSection, objSec);
        }
        objSec.setProperty(pstrProp, Long.toString(plngVal.longValue()));
    }

    /**
     * Sets the specified double property.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be set.
     * @param pdblVal the double value to be persisted.
     */
    public void setDoubleProperty(String pstrSection, String pstrProp, double pdblVal)
    {
        INISection objSec   = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec == null)
        {
            objSec = new INISection(pstrSection);
            this.mhmapSections.put(pstrSection, objSec);
        }
        objSec.setProperty(pstrProp, Double.toString(pdblVal));
    }

    /**
     * Sets the specified java.util.Date property.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be set.
     * @param pdtVal the date value to be persisted.
     */
    public void setDateProperty(String pstrSection, String pstrProp, Date pdtVal)
    {
        INISection objSec   = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec == null)
        {
            objSec = new INISection(pstrSection);
            this.mhmapSections.put(pstrSection, objSec);
        }
        objSec.setProperty(pstrProp, utilDateToStr(pdtVal, this.mstrDateFmt));
    }

    /**
     * Sets the specified java.sql.Timestamp property.
     * @param pstrSection the INI section name.
     * @param pstrProp the property to be set.
     * @param ptsVal the timestamp value to be persisted.
     */
    public void setTimestampProperty(String pstrSection, String pstrProp, Timestamp ptsVal)
    {
        INISection objSec   = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec == null)
        {
            objSec = new INISection(pstrSection);
            this.mhmapSections.put(pstrSection, objSec);
        }
        objSec.setProperty(pstrProp, timeToStr(ptsVal, this.mstrTimeStampFmt));
    }

    /**
     * Sets the format to be used to interpreat date values.
     * @param pstrDtFmt the format string
     * @throws IllegalArgumentException if the if the given pattern is invalid
     */
    public void setDateFormat(String pstrDtFmt) throws IllegalArgumentException
    {
        if (!checkDateTimeFormat(pstrDtFmt))
            throw new IllegalArgumentException("The specified date pattern is invalid!");
        this.mstrDateFmt = pstrDtFmt;
    }

    /**
     * Sets the format to be used to interpreat timestamp values.
     * @param pstrTSFmt the format string
     * @throws IllegalArgumentException if the if the given pattern is invalid
     */
    public void setTimeStampFormat(String pstrTSFmt)
    {
        if (!checkDateTimeFormat(pstrTSFmt))
            throw new IllegalArgumentException("The specified timestamp pattern is invalid!");
        this.mstrTimeStampFmt = pstrTSFmt;
    }

/*------------------------------------------------------------------------------
 * Public methods
------------------------------------------------------------------------------*/
    /**
     * Returns a string array containing names of all sections in INI file.
     * @return the string array of section names
     */
    public String[] getAllSectionNames()
    {
        int      iCntr  = 0;
        Iterator iter   = null;
        String[] arrRet = null;

        try
        {
            if (this.mhmapSections.size() > 0)
            {
                arrRet = new String[this.mhmapSections.size()];
                for (iter = this.mhmapSections.keySet().iterator(); iter.hasNext(); )
                    arrRet[iCntr++] = ((INISection) iter.next()).getName();
            }
        }
        catch (NoSuchElementException NSEExIgnore)
        {
        }
        finally
        {
            if (iter != null) iter = null;
        }
        return arrRet;
    }

    /**
     * Returns a string array containing names of all the properties under specified section.
     * @param pstrSection the name of the section for which names of properties is to be retrieved.
     * @return the string array of property names.
     */
    public String[] getAllPropertyNames(String pstrSection)
    {
        String[]   arrRet = null;
        INISection objSec = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec != null)
            arrRet = objSec.getProperties();

        if (objSec != null) objSec = null;
        return arrRet;
    }

    /**
     * Removed specified property from the specified section. If the specified
     * section or the property does not exist, does nothing.
     * @param pstrSection the section name.
     * @param pstrProp the name of the property to be removed.
     */
    public void removeProperty(String pstrSection, String pstrProp)
    {
        INISection objSec = null;

        objSec = (INISection) this.mhmapSections.get(pstrSection);
        if (objSec != null)
            objSec.removeProperty(pstrProp);

        if (objSec != null) objSec = null;
    }

    /**
     * Removes the specified section if one exists, otherwise does nothing.
     * @param pstrSection the name of the section to be removed.
     */
    public void removeSection(String pstrSection)
    {
        if (this.mhmapSections.containsKey(pstrSection))
            this.mhmapSections.remove(pstrSection);
    }

    /**
     * Flush changes back to the disk file. If the disk file does not exists then
     * creates the new one. 
     */
    public boolean save()
    {
        boolean    blnRet    = false;
        File       objFile   = null;
        String     strName   = null;
        String     strTemp   = null;
        Iterator   itrSec    = null;
        INISection objSec    = null;
        FileWriter objWriter = null;

        try
        {
            if (this.mhmapSections.size() == 0) return false;
            objFile = new File(this.mstrFile);
            if (objFile.exists()) objFile.delete();
            objWriter = new FileWriter(objFile);
            objWriter.write(";ReqSimile " + APPCONSTANTS.APP_VERSION + "\r\n");
            itrSec = this.mhmapSections.keySet().iterator();
            while (itrSec.hasNext())
            {
                strName = (String) itrSec.next();
                objSec = (INISection) this.mhmapSections.get(strName);
                strTemp = objSec.toString();
                objWriter.write(strTemp);
                objWriter.write("\r\n");
                objSec = null;
            }
            blnRet = true;
        }
        catch (IOException IOExIgnore)
        {
        }
        finally
        {
            if (objWriter != null)
            {
                closeWriter(objWriter);
                objWriter = null;
            }
            if (objFile != null) objFile = null;
            if (itrSec != null) itrSec = null;
        }
        return blnRet;
    }

/*------------------------------------------------------------------------------
 * Helper functions
 *----------------------------------------------------------------------------*/
    /**
     * Procedure to read environment variables.
     * Thanx to http://www.rgagnon.com/howto.html for this implementation.
     */
    private Properties getEnvVars()
    {
        Process p = null;
        Properties envVars = new Properties();

        try
        {
            Runtime r = Runtime.getRuntime();
            String OS = System.getProperty("os.name").toLowerCase();

            if (OS.indexOf("windows 9") > -1)
            {
                p = r.exec("command.com /c set");
            }
            else if ((OS.indexOf("nt") > -1) ||
                     (OS.indexOf("windows 2000") > -1) ||
                     (OS.indexOf("windows xp") > -1))
            {
                p = r.exec("cmd.exe /c set");
            }
            else
            {
                // our last hope, we assume Unix (thanks to H. Ware for the fix)
                p = r.exec("env");
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while((line = br.readLine()) != null)
            {
                int idx = line.indexOf('=');
                String key = line.substring(0, idx);
                String value = line.substring(idx + 1);
                envVars.setProperty(key, value);
            }
        }
        catch (Exception ExIgnore)
        {
        }
        return envVars;
    }

    /**
     * Helper function to check the date time formats.
     * @param pstrDtFmt the date time format string to be checked.
     * @return true for valid date/time format, false otherwise.
     */
    private boolean checkDateTimeFormat(String pstrDtFmt)
    {
        boolean    blnRet = false;
        DateFormat objFmt = null;

        try
        {
            objFmt = new SimpleDateFormat(pstrDtFmt);
            blnRet = true;
        }
        catch (NullPointerException NPExIgnore)
        {
        }
        catch (IllegalArgumentException IAExIgnore)
        {
        }
        finally
        {
            if (objFmt != null) objFmt = null;
        }
        return blnRet;
    }

    /**
     * Reads the INI file and load its contentens into a section collection after 
     * parsing the file line by line. 
     */
    private void loadFile()
    {
        int            iPos       = -1;
        String         strLine    = null;
        String         strSection = null;
        BufferedReader objBRdr    = null;
        FileReader     objFRdr    = null;
        INISection     objSec     = null;

        try
        {
            objFRdr = new FileReader(this.mstrFile);
            if (objFRdr != null)
            {
                objBRdr = new BufferedReader(objFRdr);
                if (objBRdr != null)
                {
                    while (objBRdr.ready())
                    {
                        iPos = -1;
                        strLine  = null;
                        strLine = objBRdr.readLine().trim();
                                             
                        if (strLine.startsWith("[") && strLine.endsWith("]"))
                        {
                        	// Section start reached, create new section
                        	strSection = strLine.substring(1, strLine.length() - 1);
                        	objSec = new INISection(strSection); 
                        	this.mhmapSections.put(strSection, objSec);
                        }
                        else if ((iPos = strLine.indexOf("=")) > 0 && objSec != null 
                        		&& (!strLine.startsWith(";"))) // Skip comments
                        {
                        	// read the key value pair "KEY = 789"
                        	// objSec should be set here, but if it isn't (because of
                        	// wrongly formatted INI-file, i.e. no section start before property),
                        	// then ignore the orphaned property
                        	if (objSec != null ) {
                        		// Trim away any space characters around keys and values
                        		objSec.setProperty(trim(strLine.substring(0, iPos)), 
                        				           trim(strLine.substring(iPos + 1)));
                        	}
                        }
                        
                    }
                    this.mblnLoaded = true;
                }
            }
        }
        catch (FileNotFoundException FNFExIgnore)
        {
        	System.out.println("Property file not found!");
            this.mhmapSections.clear();
        }
        catch (IOException IOExIgnore)
        {
        	System.out.println("IoEx exception!");
            this.mhmapSections.clear();
        }
        catch (NullPointerException NPExIgnore)
        {
        	System.out.println("NPEx exception!");
            this.mhmapSections.clear();
        }
        finally
        {
            if (objBRdr != null)
            {
                closeReader(objBRdr);
                objBRdr = null;
            }
            if (objFRdr != null)
            {
                closeReader(objFRdr);
                objFRdr = null;
            }
            if (objSec != null) objSec = null;
        }
    }

    /**
     * Helper function to close a reader object.
     * 
     * @param pobjRdr the reader to be closed.
     */
    private void closeReader(Reader pobjRdr)
    {
        if (pobjRdr == null) return;
        try
        {
            pobjRdr.close();
        }
        catch (IOException IOExIgnore)
        {
        }
    }

    /**
     * Helper function to close a writer object.
     * @param pobjWriter the writer to be closed.
     */
    private void closeWriter(Writer pobjWriter)
    {
        if (pobjWriter == null) return;

        try
        {
            pobjWriter.close();
        }
        catch (IOException IOExIgnore)
        {
        }
    }
    
    /**
     * Helper method to check the existance of a file.
     * 
     * @param the full path and name of the file to be checked.
     * @return true if file exists, false otherwise.
     */
    private boolean fileExists(String pstrFile)
    {
        boolean blnRet  = false;
        File    objFile = null;

        try
        {
            objFile = new File(pstrFile);
            blnRet = (objFile.exists() && objFile.isFile());
        }
        catch (Exception e)
        {
            blnRet = false;
        }
        finally
        {
            if (objFile != null) objFile = null;
        }
        return blnRet;
    }

    /**
     * Converts a java.util.date into String 
     * @param pd Date that need to be converted to String 
     * @param pstrFmt The date format pattern.
     * @return String
     */
    private String utilDateToStr(Date pdt, String pstrFmt)
    {
        String strRet = null;
        SimpleDateFormat dtFmt = null;

        try
        {
            dtFmt = new SimpleDateFormat(pstrFmt);
            strRet = dtFmt.format(pdt);
        }
        catch (Exception e)
        {
            strRet = null;
        }
        finally
        {
            if (dtFmt != null) dtFmt = null;
        }
        return strRet;
    }

    /**
     * Converts the given sql timestamp object to a string representation. The format
     * to be used is to be obtained from the configuration file.
     *  
     * @param pobjTS the sql timestamp object to be converted.
     * @param pblnGMT If true formats the string using GMT  timezone 
     * otherwise using local timezone. 
     * @return the formatted string representation of the timestamp.
     */
    private String timeToStr(Timestamp pobjTS, String pstrFmt)
    {
        String strRet = null;
        SimpleDateFormat dtFmt = null;

        try
        {
            dtFmt = new SimpleDateFormat(pstrFmt);
            strRet = dtFmt.format(pobjTS);
        }
        catch (IllegalArgumentException  iae)
        {
            strRet = "";
        }
        catch (NullPointerException npe)
        {
            strRet = "";
        }
        finally
        {
            if (dtFmt != null) dtFmt = null;
        }
        return strRet;
    }
    
    /**
     * Trim away any initial/terminal spaces and any enclosing braces
     * @param str The string to be trimmed
     * @return The trimmed string
     */
    private String trim(String str) 
    {
    	Pattern pattern = Pattern.compile("(?s)( *)(.*)( *)");
    	Matcher matcher = pattern.matcher(str);
    	if (matcher.matches()) {
    		// Return second group in match
    		String strRet = matcher.group(2);
    		if (strRet.startsWith("{") && strRet.endsWith("}"))
    			return strRet.substring(1, strRet.length()-1);	
    		else
    			return strRet;
    	} else {
    		// Something went wrong. Return same string
    		return str;
    	}
    }


/*------------------------------------------------------------------------------
 * Private class representing the INI Section.
 *----------------------------------------------------------------------------*/
    /**
     * Class to represent the individual ini file section.
     * @author Prasad P. Khandekar
     * @version 1.0
     * @since 1.0
     */
    private class INISection
    {
        /** Variable to hold the section name. */
        private String mstrName;
        
        /** Variable to hold the properties falling under this section. */
        private LinkedHashMap mhmapProps;

        /**
         * Construct a new section object identified by the name specified in 
         * parameter.
         * @param pstrSection The new sections name.
         */
        public INISection(String pstrSection)
        {
            this.mstrName =  pstrSection;
            this.mhmapProps = new LinkedHashMap();
        }

        /**
         * Returns name of the section.
         * @return Name of the section.
         */
        public String getName()
        {
            return this.mstrName;
        }

        /**
         * Removes specified property value from this section. 
         * @param pstrProp The name of the property to be removed.
         */
        public void removeProperty(String pstrProp)
        {
            if (this.mhmapProps.containsKey(pstrProp))
                this.mhmapProps.remove(pstrProp);
        }

        /**
         * Creates or modifies the specified property value.
         * @param pstrProp The name of the property to be created or modified. 
         * @param pstrValue The new value for the property.
         */
        public void setProperty(String pstrProp, String pstrValue)
        {
            this.mhmapProps.put(pstrProp, pstrValue);
        }

        /**
         * Returns a string array containing names of all the properties under 
         * this section. 
         * @return the string array of property names.
         */
        public String[] getProperties()
        {
            int      iCntr  = 0;
            String[] arrRet = null;
            Iterator iter   = null;

            try
            {
                if (this.mhmapProps.size() > 0)
                {
                    arrRet = new String[this.mhmapProps.size()]; 
                    for (iter = this.mhmapProps.keySet().iterator();iter.hasNext();)
                    {
                        arrRet[iCntr++] = (String) iter.next();
                    }
                }
            }
            catch (NoSuchElementException NSEExIgnore)
            {
                arrRet = null;
            }
            return arrRet;
        }

        /**
         * Returns underlying value of the specified property. 
         * @param pstrProp the property whose underlying value is to be etrieved.
         * @return the property value.
         */
        public String getProperty(String pstrProp)
        {
            String strRet = null;

            if (this.mhmapProps.containsKey(pstrProp))
                strRet = (String) this.mhmapProps.get(pstrProp);
            return strRet;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString()
        {
            Set          colKeys = null;
            String       strRet  = "";
            String       strProp = null;
            String       strVal  = null;
            Iterator     iter    = null;
            StringBuffer objBuf  = new StringBuffer();

            objBuf.append("[" + this.mstrName + "]\r\n");
            colKeys = this.mhmapProps.keySet();
            if (colKeys != null)
            {
                iter = colKeys.iterator();
                if (iter != null)
                {
                    while (iter.hasNext())
                    {
                        strProp = (String) iter.next();
                        strVal = (String) this.mhmapProps.get(strProp);
                        if ( strVal.startsWith(" ") | strVal.endsWith(" ") | 
                        	(strVal.startsWith("{") && strVal.endsWith("}")) )
                        	strVal = "{" + strVal + "}";
                        objBuf.append(strProp + "=" + strVal + "\r\n");
                        strProp = null;
                        strVal = null;
                    }
                }
            }
            strRet = objBuf.toString();

            objBuf  = null;
            iter    = null;
            colKeys = null;
            return strRet;
        }
    }
}