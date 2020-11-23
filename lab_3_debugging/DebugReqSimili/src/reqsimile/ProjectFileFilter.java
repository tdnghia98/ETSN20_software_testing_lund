package reqsimile;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Provides the project extension filter to the file dialog
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public class ProjectFileFilter extends FileFilter {

    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File file) {
        String filename = file.getName();
        return filename.endsWith("." +  APPCONSTANTS.PROJECT_FILE_EXT);
    }

    /* (non-Javadoc)
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription() {
        return "." +  APPCONSTANTS.PROJECT_FILE_EXT;
    }

}
