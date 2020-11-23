
                            ReqSimile 1.2
                            -------------
 
                        by Johan Natt och Dag
 
                   http://reqsimile.sourceforge.net
                            
         "Query-less search engine for requirements management".

Contents:
1. Introduction
2. License
3. System Requirements
4. Installation
5. Launching
6. Quick help
7. Known issues
8. References

=========================================================================      
1. Introduction
-------------------------------------------------------------------------

ReqSimile is a Java application that operates on requirement sets.

Upon selection of a requirement in one requirement set, ReqSimile
calculates the similarity to all the requirements in another
requirements set (which could also be the same set in order for
finding duplicates and other relationsships).

It is like bringing intelligent search engine functionality to
requirements management. The only difference is that you never
start empty-handed and decide on a relevant query. The query is
made up of the requirement you select! 

ReqSimile may be used interactively to assign links between
requirements. The links are stored in a separate database table and
may represent any relationship you prefer depending on the requirements
management process (e.g. same, duplicate, similar, related, etc.).


=========================================================================
2. License
-------------------------------------------------------------------------

ReqSimile is released under the GNU GPL. Please see the file LICENCE.txt
for details.

=========================================================================
3. System requirements
-------------------------------------------------------------------------

JRE 1.4.2

=========================================================================
4. Installation
-------------------------------------------------------------------------

Extract all files into a folder. Make sure to preserve folder names.

=========================================================================
5. Launching
-------------------------------------------------------------------------

Windows:
* Double-click The ReqSimile batch file (ReqSimile.bat)

Unix:
> chmod 555 ReqSimile
> ./ReqSimile
Please note that the current example database is in Access format. It is
perfectly possible to convert it and use any other format as long as
there is a JDBC driver.

=========================================================================
6. Quick help
-------------------------------------------------------------------------

* Browsing

To get started just run the application. On Windows, the example database
will load and you will see a list of requirements. On UNIX you must first
select some data sources. See below before proceeding.

The requirements sets are shown in two differnt tabs in the top half of
the window. For each requirement set, a summary list of requirements is
provided on the left. On the right, the details of a selected requirements
is shown. You can change the width of the columns in the summary view, 
by clicking and dragging the edge of a column header. You may change the
height of the rows in the detail view by clicking and dragging the edge
of a row header.

Upon clicking a requirement, besides showing the requirement's details
on the left, all similar requirements in the other set will be fetched
and presented in the "Candidate requirements" tab in the botton half
of the window. The requirements in this list may also be selected in
order to show their details on the right.

* Linking

By clicking the "Link" button next to a requirement in the bottom half of
the window a link is established from that requirement to the requirement
selected in the top half of the window. The button changes text to
"Unlink", making it possible to remove the link. A links are immediately
stored in the links table (see Known issues below on where it is stored).

* Colors
Blue  - this requirement is selected and its details are shown on the right
Green - These requirements are linked and are the ones you are currently 
        working with. In the top half, only one requirement will be shaded
        green. In the bottom half, all requirements are shaded green that
        are linked to the requirement that is shaded green in the top half
        of the window. The green-shaded requirements in the bottom half of
        the window may be unlinked by pressing the "Unlink" button.
Gray -  Indicates that these requirements have been linked to another
        requirement. It is possible to click the gray-shaded requirements 
        in the top half of the window to show which requirements are linked
        to that requirement.
Green-Gray - This requirement has been linked to more than one other
             requirement.

* Selecting data sources and prepare the data

Bring up the current project's setting via the menu "Data->Set sources...".
When you have saved the settings, close the dialog.
Then you must read the new data by selecting "Data->Fetch requirements".
Finally, you must preprocess the requirements by selecting
"Data->Preprocess requirements".


=========================================================================
7. Known issues
-------------------------------------------------------------------------

* Preprocessing tables are placed in the same database as the one
  specified for requirements set A
  
* Link table is placed in the same database as the one specified for
  requirements set A

* New projects cannot be created from within ReqSimile. To create a new
  project, copy the .rqs file and open it from the file menu. Provide your
  new database settings (Data->Set sources) and push "Save".
  
