package reqsimile.simcalc;
	/*
	preprocessor.flex - 
		tokenizer, 
		morphological analyser / lemmatiser &
		stop word remover

	2004-08-10. The following changes have been made to the morphological
	            analyzer to work correctly in conjunction with tokenization
	            and stop word removal:

	    Changed <noun,any>({A}|".")+"s." rule to not return period. Periods
	    after sentences should not be returned but the original rule returned
	    period after plural words.
	    
	    Added <verb,noun,any>{A}+"'s" rule to remove apostrophe after genitive
	    form of nouns.
				
	Copyright (c) 1995-2001 University of Sheffield, University of Sussex
	All rights reserved.
	
	Redistribution and use of source and derived binary forms are
	permitted provided that: 
	- they are not  used in commercial products
	- the above copyright notice and this paragraph are duplicated in
	all such forms
	- any documentation, advertising materials, and other materials
	related to such distribution and use acknowledge that the software
	was developed by Kevin Humphreys <kwh@dcs.shef.ac.uk> and John
	Carroll <john.carroll@cogs.susx.ac.uk> and Guido Minnen
	<Guido.Minnen@cogs.susx.ac.uk> and refer to the following related
	publication:
	
	Guido Minnen, John Carroll and Darren Pearce. 2000. Robust, Applied
	Morphological Generation. In Proceedings of the First International
	Natural Language Generation Conference (INLG), Mitzpe Ramon, Israel.
	201-208.
	
	
	Covers the English productive affixes:
	
	-s	plural of nouns, 3rd sing pres of verbs
	-ed	past tense
	-en	past participle
	-ing	progressive of verbs
	
	
	Kevin Humphreys <kwh@dcs.shef.ac.uk> 
	original version: 30/03/95 - quick hack for LaSIE in the MUC6 dry-run
	    revised: 06/12/95 - run stand-alone for Gerald Gazdar's use
	    revised: 20/02/96 - for VIE, based on reports from Gerald Gazdar
	
	John Carroll <John.Carroll@cogs.susx.ac.uk>
	    revised: 21/03/96 - made handling of -us and -use more accurate
	    revised: 26/06/96 - many more exceptions from corpora and MRDs
	
	Guido Minnen <Guido.Minnen@cogs.susx.ac.uk>
	revised: 03/12/98 - normal form and  different treatment of 
	                        consonant doubling introduced in order to 
				support automatic reversal; usage of 
				external list of verb 
				stems (derived from the BNC) that allow 
				for consonant doubling in British English
	                        introduced
	    revised: 19/05/99 - improvement of option handling
	    revised: 03/08/99 - introduction of option -f; adaption of 
	                        normal form to avoid the loss of case 
				information
	    revised: 01/09/99 - changed from normal form to compiler 
	                        directives format
	    revised: 06/10/99 - addition of the treatment of article
				and sentence initial markings,
				i.e. <ai> and <si> markings at the
				beginning of a word.  Modification of
				the scanning of the newline symbol
				such that no problems arise in
				interactive mode. Extension of the
				list of verbstems allowing for
				consonant doubling and incorporation
				of exception lists based on the CELEX
				lexical database.
	    revised: 02/12/99 - incorporated data extracted from the 
	 			CELEX lexical databases 
	    revised: 07/06/00 - adaption of Makefile to enable various 
	                        Flex optimizations
	
	John Carroll <John.Carroll@cogs.susx.ac.uk>
	revised: 25/01/01 - new version of inversion program,
	                    associated changes to directives; new
	                    C preprocessor flag 'interactive'
	
	Diana McCarthy <dianam@cogs.susx.ac.uk>
	revised: 23/02/02 - fixed bug reading in verbstem file.
	
	John Carroll <J.A.Carroll@sussex.ac.uk>
	    revised: 28/08/03 - fixes to -o(e) and -ff(e) words, a few
	            more irregulars, preferences for generating
	            alternative irregular forms.
	        
	Exception lists are taken from WordNet 1.5, the CELEX lexical
	database (Copyright Centre for Lexical Information; Baayen,
	Piepenbrock and Van Rijn; 1993) and various other corpora and MRDs.
	
	Further exception lists are taken from the CELEX lexical database
	(Copyright Centre for Lexical Information; Baayen, Piepenbrock and
	Van Rijn; 1993).
	
	Many thanks to Tim Baldwin, Chris Brew, Bill Fisher, Gerald Gazdar,
	Dale Gerdemann, Adam Kilgarriff and Ehud Reiter for suggested
	improvements.
	
	WordNet> WordNet 1.5 Copyright 1995 by Princeton University.
	WordNet> All rights reseved.
	WordNet>
	WordNet> THIS SOFTWARE AND DATABASE IS PROVIDED "AS IS" AND PRINCETON
	WordNet> UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES, EXPRESS OR
	WordNet> IMPLIED.  BY WAY OF EXAMPLE, BUT NOT LIMITATION, PRINCETON
	WordNet> UNIVERSITY MAKES NO REPRESENTATIONS OR WARRANTIES OF MERCHANT-
	WordNet> ABILITY OR FITNESS FOR ANY PARTICULAR PURPOSE OR THAT THE USE
	WordNet> OF THE LICENSED SOFTWARE, DATABASE OR DOCUMENTATION WILL NOT
	WordNet> INFRINGE ANY THIRD PARTY PATENTS, COPYRIGHTS, TRADEMARKS OR
	WordNet> OTHER RIGHTS.
	WordNet>
	WordNet> The name of Princeton University or Princeton may not be used in
	WordNet> advertising or publicity pertaining to distribution of the software
	WordNet> and/or database.  Title to copyright in this software, database and
	WordNet> any associated documentation shall at all times remain with
	WordNet> Princeton Univerisy and LICENSEE agrees to preserve same.
	
	*/
  
	import java.util.*;
	import java.io.*;
%%

%class PreProcessor
%type String
%unicode
%char
%ignorecase

%{
	HashSet verbStemList;
	HashSet stopWordList;

	/**
	 * Reads the specified consonant doubling verb stem list
	 * @param fileName
	 */
	private boolean readVerbStemList(String fileName) {
		String line, word;
		StringTokenizer tokenizer;
		try {
            File stemListFile = new File(fileName);
            BufferedReader stemListRdr  
                           = new BufferedReader(new FileReader(stemListFile));
            while ((line = stemListRdr.readLine()) != null) {
                tokenizer = new StringTokenizer(line);
                while (tokenizer.hasMoreTokens()) {
                    word = tokenizer.nextToken();
                    verbStemList.add(word);
                }
            }
		} catch (IOException iox) {
            System.out.println("Could not find consonant doubling verb stem list.");
            return false;
		}
		return true;
	}

	/**
	 * Reads the specified stop word list
	 * @param fileName
	 */
	private boolean readStopWordList(String fileName) {
		String line, word;
		StringTokenizer tokenizer;
		try {
            File stemListFile = new File(fileName);
            BufferedReader stemListRdr  
                           = new BufferedReader(new FileReader(stemListFile));
            while ((line = stemListRdr.readLine()) != null) {
                tokenizer = new StringTokenizer(line);
                while (tokenizer.hasMoreTokens()) {
                    word = tokenizer.nextToken();
                    stopWordList.add(word);
                }
            }
		} catch (IOException iox) {
            System.out.println("Could not find consonant doubling verb stem list.");
            return false;
		}
		return true;
	}

	/**
	 * Ignores a string if it is found in the stop word list
	 * @return
	 */
	private String stopWord(String str) {
		if(stopWordList.contains(str.toLowerCase()))
			try {
				return yylex(); // Skip this word and continue with next token
			} catch (IOException e) {
				return "";
			}
		else
			return str.toLowerCase();
	}


	/**
	 * Stems a string
	 * @param del Number of characters to remove from the end
	 * @param add The string to add to the end
	 * @param affix The stem's affix
	 * @return String
	 */
	private String stem(int del, String add) {
		// Strip 'del' characters at the end
		String str = yytext().substring(0, yytext().length() - del);
		// Add the ground form ending ('add')
		str += add;
		// Convert to lowercase
		str.toLowerCase();
		// Return the stemmed string
		return stopWord(str);
	}

	/**
	 * Stems a string with consonant doubling 
	 * @param del Number of characters to remove from the end
	 * @param add Not currently used
	 * @param affix The stem's affix
	 * @return String
	 */
	private String stemConDub(int del, String add) {
		// Strip 'del' characters at the end
		// and one extra for the consonant doubling
		String str = yytext().substring(0, yytext().length() - del - 1);
		// Convert to lowercase
		str.toLowerCase();
		// If the stem is not found in the external verb stem list
		// then add the double consonant to the output
		if (!verbStemList.contains(str))
				str += str.substring(str.length()-1, str.length());
		// Return the stem
		return stopWord(str);
	}


	/**
	 * Stems a string ending on -es, -ed or -ing
	 * @param del Number of additional characters to remove from the end
	 * @param add The string to add to the end (before the affix)
	 * @return String
	 */
	private String stemSemiReg(int del, String add) {
		// Fetch the string
		String str = yytext();
		// Check the last char in the string
		switch (str.charAt(str.length()-1)) {
			case 's':
			case 'S':
				// Remove -es and 'del' additional characters
				str = str.substring(0, str.length() - 2 - del);
				break;
			case 'd':
			case 'D':
				// Remove -ed and 'del' additional characters
				str = str.substring(0, str.length() - 2 - del);
				break;
			case 'g':
			case 'G':
				// Remove -ing and 'del' additional characters
				str = str.substring(0, str.length() - 3 - del);
				break;
		}
		// Convert to lowercase
		str.toLowerCase();
		// Add ending
		str += add;
		// Return the stem
		return stopWord(str);
	}
	
	/**
	 * Returns an unstemmed stem with initial characters capitalized
	 * @return String
	 */
	private String stemProperName() {
		return stopWord(yytext().toLowerCase());
	}
		
	/**
	 * Returns a unstemmed stem
	 * @return String
	 */
	private String stemCommonNoun() {
		return stopWord(yytext().toLowerCase());
	}
				
	/**
	 * Returns an unstemmed string for a word
	 * where the +ed/ed form is the same as the stem
	 * @return String
	 */
	private String stemNull() {
		return (stemCommonNoun());
	}

	/**
	 * Returns an unstemmed string for a word that
	 * is a singular- or plural-only noun
	 * @return String
	 */
	private String stemNullX() {
		return (stemCommonNoun());		
	}

	/**
	 * Returns an unstemmed string for a word where
	 * all inflected forms are the same as the stem
	 * @return String
	 */
	private String stemNullY() {
		return (stemCommonNoun());		
	}

	/**
	 * Returns an unstemmed string 
	 * This form is actually the stem so don't apply any generic analysis rules
	 * @return String
	 */
	private String stemNullC() {
		return (stemCommonNoun());		
	}

%}

%init{
	verbStemList = new HashSet();
	stopWordList = new HashSet();
	readVerbStemList("verbstemlist.txt");
	readStopWordList("stopwordlist.txt");
%init}


%x verb noun any

/* Stemmer part */                                          
A=['+a-z0-9]
V=[aeiou]
VY=[aeiouy]
C=[bcdfghjklmnpqrstvwxyz]
CXY=[bcdfghjklmnpqrstvwxz]
CXY2="bb"|"cc"|"dd"|"ff"|"gg"|"hh"|"jj"|"kk"|"ll"|"mm"|"nn"|"pp"|"qq"|"rr"|"ss"|"tt"|"vv"|"ww"|"xx"|"zz"
S2="ss"|"zz"
S=[sxz]|([cs]"h")
PRE="be"|"ex"|"in"|"mis"|"pre"|"pro"|"re"
EDING="ed"|"ing"
ESEDING="es"|"ed"|"ing"

/* Tokenizer part */
DIGIT=[0-9]
CHAR=[a-zàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿß]
FLOAT={DIGIT}+"."{DIGIT}+
STANDARD=({CHAR}+{DIGIT}+)+("."({CHAR}|{DIGIT})+)+
REFERENCE=(({CHAR}|{DIGIT})+"-"{DIGIT}+)+
ID={CHAR}+({CHAR}|{DIGIT})+
WORD={CHAR}+
NUMTOKEN={DIGIT}+{ID}*
TOKEN={ID}|{FLOAT}|{STANDARD}|{REFERENCE}

/* Anything else */
SKIP=[^a-z0-9àáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿß]

%%

<YYINITIAL> {	/* State will never change */

<verb,any>"shall"  { return(stemNullY()); }
<verb,any>"would"  { return(stemNullY()); }
<verb,any>"may"  { return(stemNullY()); }
<verb,any>"might"  { return(stemNullY()); }
<verb,any>"ought"  { return(stemNullY()); }
<verb,any>"should"  { return(stemNullY()); }
<verb,any>"am"  { return(stem(2,"be")); }
<verb,any>"are"  { return(stem(3,"be")); // disprefer 
}
<verb,any>"is"  { return(stem(2,"be")); }           
<verb,any>"was"  { return(stem(3,"be")); }
<verb,any>"wast"  { return(stem(4,"be")); }	/* disprefer */
<verb,any>"wert"  { return(stem(4,"be")); }	/* disprefer */
<verb,any>"were"  { return(stem(4,"be")); }	/* disprefer */
<verb,any>"being"  { return(stem(5,"be")); }        
<verb,any>"been"  { return(stem(4,"be")); }          
<verb,any>"had"  { return(stem(3,"have")); }	/* en */
<verb,any>"has"  { return(stem(3,"have")); }          
<verb,any>"hath"  { return(stem(4,"have")); }	/* disprefer */
<verb,any>"does"  { return(stem(4,"do")); }            
<verb,any>"did"  { return(stem(3,"do")); }          
<verb,any>"done"  { return(stem(4,"do")); }          
<verb,any>"didst"  { return(stem(5,"do")); }	/* disprefer */
<verb,any>"'ll"  { return(stem(3,"will")); }
<verb,any>"'m"  { return(stem(2,"be")); }	/* disprefer */
<verb,any>"'re"  { return(stem(3,"be")); }	/* disprefer */
<verb,any>"'ve"  { return(stem(3,"have")); }

<verb,any>("beat"|"browbeat")"en"  { return(stem(2,"")); }
<verb,any>("beat"|"beset"|"bet"|"broadcast"|"browbeat"|"burst"|"cost"|"cut"|"hit"|"let"|"set"|"shed"|"shut"|"slit"|"split"|"put"|"quit"|"spread"|"sublet"|"spred"|"thrust"|"upset"|"hurt"|"bust"|"cast"|"forecast"|"inset"|"miscast"|"mishit"|"misread"|"offset"|"outbid"|"overbid"|"preset"|"read"|"recast"|"reset"|"telecast"|"typecast"|"typeset"|"underbid"|"undercut"|"wed"|"wet") { return(stemNull()); }
<verb,noun,any>"aches"  { return(stem(2,"e")); }     
<verb,any>"aped"    { return(stem(2,"e")); }	/* en */
<verb,any>"axed" { return(stem(2,"")); }	/* en */
<verb,any>("bias"|"canvas")"es"  { return(stem(2,"")); }
<verb,any>("cadd"|"v")"ied"  { return(stem(2,"e")); }	/* en */
<verb,any>("cadd"|"v")"ying"  { return(stem(4,"ie")); }     
<verb,noun,any>"cooees"  { return(stem(2,"e")); }
<verb,any>"cooeed"  { return(stem(3,"ee")); }	/* en */
<verb,any>("ey"|"dy")"ed"  { return(stem(2,"e")); }	/* en */
<verb,any>"eyeing"  { return(stem(3,"")); }                 
<verb,any>"eying"  { return(stem(3,"e")); }	/* disprefer */
<verb,any>"dying"  { return(stem(4,"ie")); }       
<verb,any>("geld"|"gild")"ed"  { return(stem(2,"")); }       
<verb,any>("outvi"|"hi")"ed"  { return(stem(2,"e")); }  
<verb,any>"outlay"  { return(stem(2,"ie")); }	/* en */
<verb,any>"rebound"  { return(stem(4,"ind")); }	/* en */
<verb,any>"plummets"  { return(stem(1,"")); }                 
<verb,any>"queueing"  { return(stem(3,"")); }               
<verb,any>"stomachs"  { return(stem(1,"")); }                 
<verb,any>"trammels"  { return(stem(1,"")); }                 
<verb,any>"tarmacked"  { return(stem(3,"")); }	/* en */
<verb,any>"transfixed"  { return(stem(2,"")); }	/* en */
<verb,any>"underlay"  { return(stem(2,"ie")); }     
<verb,any>"overlay"  { return(stem(2,"ie")); }      
<verb,any>"overflown"  { return(stem(3,"y")); }     
<verb,any>"relaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"shat"  { return(stem(3,"hit")); }	/* en */
<verb,any>"bereft"  { return(stem(3,"eave")); }	/* en */
<verb,any>"clave"  { return(stem(3,"eave")); }	/* disprefer */
<verb,any>"wrought"  { return(stem(6,"ork")); }	/* disprefer */
<verb,any>"durst"  { return(stem(4,"are")); }	/* disprefer */
<verb,any>"foreswore"  { return(stem(3,"ear")); }	/* en */
<verb,any>"outfought"  { return(stem(5,"ight")); }	/* en */
<verb,any>"garotting"  { return(stem(3,"e")); }	/* en */
<verb,any>"shorn"  { return(stem(3,"ear")); }
<verb,any>"spake"  { return(stem(3,"eak")); }	/* disprefer */
<verb,any>("analys"|"paralys"|"cach"|"brows"|"glimps"|"collaps"|"eclips"|"elaps"|"laps"|"traips"|"relaps"|"puls"|"repuls"|"cleans"|"rins"|"recompens"|"condens"|"dispens"|"incens"|"licens"|"sens"|"tens")"es" { return(stem(1,"")); }
<verb,any>"cached" { return(stem(2,"e")); }         
<verb,any>"caching" { return(stem(3,"e")); }       
<verb,any>("tun"|"gangren"|"wan"|"grip"|"unit"|"coher"|"comper"|"rever"|"semaphor"|"commun"|"reunit"|"dynamit"|"superven"|"telephon"|"ton"|"aton"|"bon"|"phon"|"plan"|"profan"|"importun"|"enthron"|"elop"|"interlop"|"sellotap"|"sideswip"|"slop"|"scrap"|"mop"|"lop"|"expung"|"lung"|"past"|"premier"|"rang"|"secret"){EDING} { return(stemSemiReg(0,"e")); }
<verb,any>("unroll"|"unscroll"){EDING} { return(stemSemiReg(0,"")); }
<verb,any>"unseat"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"whang"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>("bath"|"billet"|"collar"|"ballot"|"earth"|"fathom"|"fillet"|"mortar"|"parrot"|"profit"|"ransom"|"slang"){EDING} { return(stemSemiReg(0,"")); }
<verb,any>("disunit"|"aquaplan"|"enplan"|"reveng"|"ripost"|"sein")"ed" { return(stem(2,"e")); }	/* en */
<verb,any>"toping" { return(stem(3,"e")); }	/* disprefer */
<verb,any>("disti"|"fulfi"|"appa")"lls" { return(stem(2,"")); }
<verb,any>("overca"|"misca")"lled" { return(stem(2,"")); }   
<verb,any>"catcalling" { return(stem(3,"")); }              
<verb,any>("catcall"|"squall")"ing" { return(stem(3,"")); } 
<verb,any>("browbeat"|"ax"|"dubbin")"ing" { return(stem(3,"")); }
<verb,any>"summonses" { return(stem(2,"")); }        
<verb,any>"putted" { return(stem(2,"")); }          
<verb,any>"summonsed" { return(stem(2,"")); }       
<verb,any>("sugar"|"tarmacadam"|"beggar"|"betroth"|"boomerang"|"chagrin"|"envenom"|"miaou"|"pressgang")"ed" { return(stem(2,"")); }
<verb,any>"abode"  { return(stem(3,"ide")); }	/* en */
<verb,any>"abought"  { return(stem(5,"y")); }	/* en */
<verb,any>"abyes"  { return(stem(2,"")); }           
<verb,any>"addrest"  { return(stem(3,"ess")); }	/* disprefer */
<verb,any>"ageing"  { return(stem(4,"e")); }       
<verb,any>"agreed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"anted"  { return(stem(3,"te")); }	/* en */
<verb,any>"antes"  { return(stem(2,"e")); }          
<verb,any>"arisen"  { return(stem(3,"se")); }       
<verb,any>"arose"  { return(stem(3,"ise")); }       
<verb,any>"ate"  { return(stem(3,"eat")); }         
<verb,any>"awoke"  { return(stem(3,"ake")); }       
<verb,any>"awoken"  { return(stem(4,"ake")); }      
<verb,any>"backbit"  { return(stem(3,"bite")); }    
<verb,any>"backbiting"  { return(stem(4,"te")); }  
<verb,any>"backbitten"  { return(stem(3,"e")); }    
<verb,any>"backslid"  { return(stem(3,"lide")); }   
<verb,any>"backslidden"  { return(stem(3,"e")); }   
<verb,any>"bad"  { return(stem(3,"bid")); }	/* disprefer */
<verb,any>"bade"  { return(stem(3,"id")); }         
<verb,any>"bandieds"  { return(stem(4,"y")); }       
<verb,any>"became"  { return(stem(3,"ome")); }	/* en */
<verb,any>"befallen"  { return(stem(3,"l")); }      
<verb,any>"befalling"  { return(stem(4,"l")); }    
<verb,any>"befell"  { return(stem(3,"all")); }      
<verb,any>"began"  { return(stem(3,"gin")); }       
<verb,any>"begat"  { return(stem(3,"get")); }	/* disprefer */       
<verb,any>"begirt"  { return(stem(3,"ird")); }	/* en */
<verb,any>"begot"  { return(stem(3,"get")); }
<verb,any>"begotten"  { return(stem(5,"et")); }     
<verb,any>"begun"  { return(stem(3,"gin")); }       
<verb,any>"beheld"  { return(stem(3,"old")); }      
<verb,any>"beholden"  { return(stem(3,"d")); }      
<verb,any>"benempt"  { return(stem(4,"ame")); }	/* en */
<verb,any>"bent"  { return(stem(3,"end")); }	/* en */
<verb,any>"besought"  { return(stem(5,"eech")); }	/* en */
<verb,any>"bespoke"  { return(stem(3,"eak")); }     
<verb,any>"bespoken"  { return(stem(4,"eak")); }    
<verb,any>"bestrewn"  { return(stem(3,"ew")); }     
<verb,any>"bestrid"  { return(stem(3,"ride")); }	/* disprefer */
<verb,any>"bestridden"  { return(stem(3,"e")); }    
<verb,any>"bestrode"  { return(stem(3,"ide")); }    
<verb,any>"betaken"  { return(stem(3,"ke")); }      
<verb,any>"bethought"  { return(stem(5,"ink")); }	/* en */
<verb,any>"betook"  { return(stem(3,"ake")); }      
<verb,any>"bidden"  { return(stem(3,"")); }         
<verb,any>"bit"  { return(stem(3,"bite")); }        
<verb,any>"biting"  { return(stem(4,"te")); }      
<verb,any>"bitten"  { return(stem(3,"e")); }        
<verb,any>"bled"  { return(stem(3,"leed")); }	/* en */
<verb,any>"blest"  { return(stem(3,"ess")); }	/* disprefer */
<verb,any>"blew"  { return(stem(3,"low")); }        
<verb,any>"blown"  { return(stem(3,"ow")); }        
<verb,any>"bogged-down"  { return(stem(8,"-down")); }	/* en */
<verb,any>"bogginGMdown"  { return(stem(9,"-down")); }
<verb,any>"bogs-down"  { return(stem(6,"-down")); }  
<verb,any>"boogied"  { return(stem(3,"ie")); }	/* en */
<verb,any>"boogies"  { return(stem(2,"e")); }        
<verb,any>"bore"  { return(stem(3,"ear")); }        
<verb,any>"borne"  { return(stem(4,"ear")); }	/* disprefer */
<verb,any>"born"  { return(stem(3,"ear")); }        
<verb,any>"bought"  { return(stem(5,"uy")); }	/* en */
<verb,any>"bound"  { return(stem(4,"ind")); }	/* en */
<verb,any>"breastfed"  { return(stem(3,"feed")); }	/* en */
<verb,any>"bred"  { return(stem(3,"reed")); }	/* en */
<verb,any>"breid"  { return(stem(3,"ei")); }	/* en */
<verb,any>"bringing"  { return(stem(4,"g")); }     
<verb,any>"broke"  { return(stem(3,"eak")); }       
<verb,any>"broken"  { return(stem(4,"eak")); }      
<verb,any>"brought"  { return(stem(5,"ing")); }	/* en */
<verb,any>"built"  { return(stem(3,"ild")); }	/* en */
<verb,any>"burnt"  { return(stem(3,"rn")); }	/* disprefer */
<verb,any>"bypast"  { return(stem(3,"ass")); }	/* disprefer */
<verb,any>"came"  { return(stem(3,"ome")); }	/* en */
<verb,any>"caught"  { return(stem(4,"tch")); }	/* en */
<verb,any>"chassed"  { return(stem(3,"se")); }	/* en */
<verb,any>"chasseing"  { return(stem(4,"e")); }    
<verb,any>"chasses"  { return(stem(2,"e")); }        
<verb,any>"chevied"  { return(stem(5,"ivy")); }	/* disprefer */
<verb,any>"chevies"  { return(stem(5,"ivy")); }	/* disprefer */
<verb,any>"chevying"  { return(stem(6,"ivy")); }	/* disprefer */
<verb,any>"chid"  { return(stem(3,"hide")); }	/* disprefer */
<verb,any>"chidden"  { return(stem(3,"e")); }	/* disprefer */
<verb,any>"chivvied"  { return(stem(4,"y")); }	/* en */
<verb,any>"chivvies"  { return(stem(4,"y")); }       
<verb,any>"chivvying"  { return(stem(5,"y")); }    
<verb,any>"chose"  { return(stem(3,"oose")); }      
<verb,any>"chosen"  { return(stem(3,"ose")); }      
<verb,any>"clad"  { return(stem(3,"lothe")); }	/* en */
<verb,any>"cleft"  { return(stem(3,"eave")); }	/* disprefer */
<verb,any>"clept"  { return(stem(3,"epe")); }	/* disprefer */
<verb,any>"clinging"  { return(stem(4,"g")); }     
<verb,any>"clove"  { return(stem(3,"eave")); }      
<verb,any>"cloven"  { return(stem(4,"eave")); }     
<verb,any>"clung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"countersank"  { return(stem(3,"ink")); } 
<verb,any>"countersunk"  { return(stem(3,"ink")); } 
<verb,any>"crept"  { return(stem(3,"eep")); }	/* en */
<verb,any>"crossbred"  { return(stem(3,"reed")); }	/* en */
<verb,any>"curettes"  { return(stem(3,"")); }        
<verb,any>"curst"  { return(stem(3,"rse")); }	/* disprefer */
<verb,any>"dealt"  { return(stem(3,"al")); }	/* en */
<verb,any>"decreed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"degases"  { return(stem(2,"")); }         
<verb,any>"deleing"  { return(stem(4,"e")); }      
<verb,any>"disagreed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"disenthralls"  { return(stem(2,"")); }	/* disprefer */
<verb,any>"disenthrals"  { return(stem(2,"l")); }    
<verb,any>"dought"  { return(stem(4,"w")); }	/* en */
<verb,any>"dove"  { return(stem(3,"ive")); }	/* disprefer */
<verb,any>"drank"  { return(stem(3,"ink")); }       
<verb,any>"drawn"  { return(stem(3,"aw")); }        
<verb,any>"dreamt"  { return(stem(3,"am")); }	/* en */
<verb,any>"dreed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"drew"  { return(stem(3,"raw")); }        
<verb,any>"driven"  { return(stem(3,"ve")); }       
<verb,any>"drove"  { return(stem(3,"ive")); }       
<verb,any>"drunk"  { return(stem(3,"ink")); }       
<verb,any>"dug"  { return(stem(3,"dig")); }	/* en */
<verb,any>"dwelt"  { return(stem(3,"ell")); }	/* en */
<verb,any>"eaten"  { return(stem(3,"t")); }         
<verb,any>"emceed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"enwound"  { return(stem(4,"ind")); }	/* en */
<verb,any>"facsimileing"  { return(stem(4,"e")); } 
<verb,any>"fallen"  { return(stem(3,"l")); }        
<verb,any>"fed"  { return(stem(3,"feed")); }	/* en */
<verb,any>"fell"  { return(stem(3,"all")); }        
<verb,any>"felt"  { return(stem(3,"eel")); }	/* en */
<verb,any>"filagreed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"filigreed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"fillagreed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"fled"  { return(stem(3,"lee")); }	/* en */
<verb,any>"flew"  { return(stem(3,"ly")); }         
<verb,any>"flinging"  { return(stem(4,"g")); }     
<verb,any>"floodlit"  { return(stem(3,"light")); }	/* en */
<verb,any>"flown"  { return(stem(3,"y")); }         
<verb,any>"flung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"flyblew"  { return(stem(3,"low")); }     
<verb,any>"flyblown"  { return(stem(3,"ow")); }     
<verb,any>"forbade"  { return(stem(3,"id")); }
<verb,any>"forbad"  { return(stem(3,"bid")); }	/* disprefer */
<verb,any>"forbidden"  { return(stem(3,"")); }      
<verb,any>"forbore"  { return(stem(3,"ear")); }     
<verb,any>"forborne"  { return(stem(4,"ear")); }    
<verb,any>"fordid"  { return(stem(3,"do")); }       
<verb,any>"fordone"  { return(stem(3,"o")); }       
<verb,any>"foredid"  { return(stem(3,"do")); }      
<verb,any>"foredone"  { return(stem(3,"o")); }      
<verb,any>"foregone"  { return(stem(3,"o")); }      
<verb,any>"foreknew"  { return(stem(3,"now")); }    
<verb,any>"foreknown"  { return(stem(3,"ow")); }    
<verb,any>"foreran"  { return(stem(3,"run")); }	/* en */
<verb,any>"foresaw"  { return(stem(3,"see")); }     
<verb,any>"foreseen"  { return(stem(3,"ee")); }     
<verb,any>"foreshown"  { return(stem(3,"ow")); }    
<verb,any>"forespoke"  { return(stem(3,"eak")); }   
<verb,any>"forespoken"  { return(stem(4,"eak")); }  
<verb,any>"foretelling"  { return(stem(4,"l")); }  
<verb,any>"foretold"  { return(stem(3,"ell")); }	/* en */
<verb,any>"forewent"  { return(stem(4,"go")); }     
<verb,any>"forgave"  { return(stem(3,"ive")); }     
<verb,any>"forgiven"  { return(stem(3,"ve")); }     
<verb,any>"forgone"  { return(stem(3,"o")); }       
<verb,any>"forgot"  { return(stem(3,"get")); }      
<verb,any>"forgotten"  { return(stem(5,"et")); }    
<verb,any>"forsaken"  { return(stem(3,"ke")); }     
<verb,any>"forsook"  { return(stem(3,"ake")); }     
<verb,any>"forspoke"  { return(stem(3,"eak")); }    
<verb,any>"forspoken"  { return(stem(4,"eak")); }   
<verb,any>"forswore"  { return(stem(3,"ear")); }    
<verb,any>"forsworn"  { return(stem(3,"ear")); }    
<verb,any>"forwent"  { return(stem(4,"go")); }      
<verb,any>"fought"  { return(stem(5,"ight")); }	/* en */
<verb,any>"found"  { return(stem(4,"ind")); }	/* en */
<verb,any>"freed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"fricasseed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"froze"  { return(stem(3,"eeze")); }      
<verb,any>"frozen"  { return(stem(4,"eeze")); }     
<verb,any>"gainsaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"gan"  { return(stem(3,"gin")); }         
<verb,any>"garnisheed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"gases"  { return(stem(2,"")); }           
<verb,any>"gave"  { return(stem(3,"ive")); }        
<verb,any>"geed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"gelt"  { return(stem(3,"eld")); }	/* en */
<verb,any>"genned-up"  { return(stem(6,"-up")); }	/* en */
<verb,any>"genninGMup"  { return(stem(7,"-up")); } 
<verb,any>"gens-up"  { return(stem(4,"-up")); }      
<verb,any>"ghostwriting"  { return(stem(4,"te")); }
<verb,any>"ghostwritten"  { return(stem(3,"e")); }  
<verb,any>"ghostwrote"  { return(stem(3,"ite")); }  
<verb,any>"gilt"  { return(stem(3,"ild")); }	/* disprefer */
<verb,any>"girt"  { return(stem(3,"ird")); }	/* disprefer */
<verb,any>"given"  { return(stem(3,"ve")); }        
<verb,any>"gnawn"  { return(stem(3,"aw")); }        
<verb,any>"gone"  { return(stem(3,"o")); }          
<verb,any>"got"  { return(stem(3,"get")); }         
<verb,any>"gotten"  { return(stem(5,"et")); }       
<verb,any>"graven"  { return(stem(3,"ve")); }       
<verb,any>"greed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"grew"  { return(stem(3,"row")); }        
<verb,any>"gript"  { return(stem(3,"ip")); }	/* disprefer */
<verb,any>"ground"  { return(stem(4,"ind")); }	/* en */
<verb,any>"grown"  { return(stem(3,"ow")); }        
<verb,any>"guaranteed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"hacksawn"  { return(stem(3,"aw")); }     
<verb,any>"hamstringing"  { return(stem(4,"g")); } 
<verb,any>"hamstrung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"handfed"  { return(stem(3,"feed")); }	/* en */
<verb,any>"heard"  { return(stem(3,"ar")); }	/* en */
<verb,any>"held"  { return(stem(3,"old")); }	/* en */
<verb,any>"hewn"  { return(stem(3,"ew")); }         
<verb,any>"hid"  { return(stem(3,"hide")); }        
<verb,any>"hidden"  { return(stem(3,"e")); }        
<verb,any>"honied"  { return(stem(3,"ey")); }	/* en */
<verb,any>"hove"  { return(stem(3,"eave")); }	/* disprefer */
<verb,any>"hung"  { return(stem(3,"ang")); }	/* en */
<verb,any>"impanells"  { return(stem(2,"")); }       
<verb,any>"inbred"  { return(stem(3,"reed")); }	/* en */
<verb,any>"indwelling"  { return(stem(4,"l")); }   
<verb,any>"indwelt"  { return(stem(3,"ell")); }	/* en */
<verb,any>"inlaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"interbred"  { return(stem(3,"reed")); }	/* en */
<verb,any>"interlaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"interpled"  { return(stem(3,"lead")); }	/* disprefer */
<verb,any>"interwove"  { return(stem(3,"eave")); }  
<verb,any>"interwoven"  { return(stem(4,"eave")); } 
<verb,any>"inwove"  { return(stem(3,"eave")); }     
<verb,any>"inwoven"  { return(stem(4,"eave")); }    
<verb,any>"joint"  { return(stem(3,"in")); }	/* disprefer */
<verb,any>"kent"  { return(stem(3,"en")); }	/* en */
<verb,any>"kept"  { return(stem(3,"eep")); }	/* en */
<verb,any>"kneed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"knelt"  { return(stem(3,"eel")); }	/* en */
<verb,any>"knew"  { return(stem(3,"now")); }        
<verb,any>"known"  { return(stem(3,"ow")); }        
<verb,any>"laden"  { return(stem(3,"de")); }        
<verb,any>"ladyfied"  { return(stem(5,"ify")); }	/* en */
<verb,any>"ladyfies"  { return(stem(5,"ify")); }     
<verb,any>"ladyfying"  { return(stem(6,"ify")); }  
<verb,any>"laid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"lain"  { return(stem(3,"ie")); }         
<verb,any>"leant"  { return(stem(3,"an")); }	/* disprefer */
<verb,any>"leapt"  { return(stem(3,"ap")); }	/* en */
<verb,any>"learnt"  { return(stem(3,"rn")); }	/* en */
<verb,any>"led"  { return(stem(3,"lead")); }	/* en */
<verb,any>"left"  { return(stem(3,"eave")); }	/* en */
<verb,any>"lent"  { return(stem(3,"end")); }	/* en */
<verb,any>"lit"  { return(stem(3,"light")); }	/* en */
<verb,any>"lost"  { return(stem(3,"ose")); }	/* en */
<verb,any>"made"  { return(stem(3,"ake")); }	/* en */
<verb,any>"meant"  { return(stem(3,"an")); }	/* en */
<verb,any>"met"  { return(stem(3,"meet")); }	/* en */
<verb,any>"misbecame"  { return(stem(3,"ome")); }	/* en */
<verb,any>"misdealt"  { return(stem(3,"al")); }	/* en */
<verb,any>"misgave"  { return(stem(3,"ive")); }     
<verb,any>"misgiven"  { return(stem(3,"ve")); }     
<verb,any>"misheard"  { return(stem(3,"ar")); }	/* en */
<verb,any>"mislaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"misled"  { return(stem(3,"lead")); }	/* en */
<verb,any>"mispled"  { return(stem(3,"lead")); }	/* disprefer */
<verb,any>"misspelt"  { return(stem(3,"ell")); }	/* disprefer */
<verb,any>"misspent"  { return(stem(3,"end")); }	/* en */
<verb,any>"mistaken"  { return(stem(3,"ke")); }     
<verb,any>"mistook"  { return(stem(3,"ake")); }	/* en */
<verb,any>"misunderstood"  { return(stem(3,"and")); }	/* en */
<verb,any>"molten"  { return(stem(5,"elt")); }      
<verb,any>"mown"  { return(stem(3,"ow")); }         
<verb,any>"outbidden"  { return(stem(3,"")); }	/* disprefer */
<verb,any>"outbred"  { return(stem(3,"reed")); }	/* en */
<verb,any>"outdid"  { return(stem(3,"do")); }       
<verb,any>"outdone"  { return(stem(3,"o")); }       
<verb,any>"outgone"  { return(stem(3,"o")); }       
<verb,any>"outgrew"  { return(stem(3,"row")); }     
<verb,any>"outgrown"  { return(stem(3,"ow")); }     
<verb,any>"outlaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"outran"  { return(stem(3,"run")); }	/* en */
<verb,any>"outridden"  { return(stem(3,"e")); }     
<verb,any>"outrode"  { return(stem(3,"ide")); }     
<verb,any>"outselling"  { return(stem(4,"l")); }   
<verb,any>"outshone"  { return(stem(3,"ine")); }	/* en */
<verb,any>"outshot"  { return(stem(3,"hoot")); }	/* en */
<verb,any>"outsold"  { return(stem(3,"ell")); }	/* en */
<verb,any>"outstood"  { return(stem(3,"and")); }	/* en */
<verb,any>"outthought"  { return(stem(5,"ink")); }	/* en */
<verb,any>"outwent"  { return(stem(4,"go")); }	/* en */
<verb,any>"outwore"  { return(stem(3,"ear")); }     
<verb,any>"outworn"  { return(stem(3,"ear")); }     
<verb,any>"overbidden"  { return(stem(3,"")); }	/* disprefer */
<verb,any>"overblew"  { return(stem(3,"low")); }    
<verb,any>"overblown"  { return(stem(3,"ow")); }    
<verb,any>"overbore"  { return(stem(3,"ear")); }    
<verb,any>"overborne"  { return(stem(4,"ear")); }   
<verb,any>"overbuilt"  { return(stem(3,"ild")); }	/* en */
<verb,any>"overcame"  { return(stem(3,"ome")); }	/* en */
<verb,any>"overdid"  { return(stem(3,"do")); }      
<verb,any>"overdone"  { return(stem(3,"o")); }      
<verb,any>"overdrawn"  { return(stem(3,"aw")); }    
<verb,any>"overdrew"  { return(stem(3,"raw")); }    
<verb,any>"overdriven"  { return(stem(3,"ve")); }   
<verb,any>"overdrove"  { return(stem(3,"ive")); }   
<verb,any>"overflew"  { return(stem(3,"ly")); }	/* en */
<verb,any>"overgrew"  { return(stem(3,"row")); }    
<verb,any>"overgrown"  { return(stem(3,"ow")); }    
<verb,any>"overhanging"  { return(stem(4,"g")); }  
<verb,any>"overheard"  { return(stem(3,"ar")); }	/* en */
<verb,any>"overhung"  { return(stem(3,"ang")); }	/* en */
<verb,any>"overlaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"overlain"  { return(stem(3,"ie")); }     
<verb,any>"overlies"  { return(stem(2,"e")); }       
<verb,any>"overlying"  { return(stem(4,"ie")); }   
<verb,any>"overpaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"overpast"  { return(stem(3,"ass")); }	/* en */
<verb,any>"overran"  { return(stem(3,"run")); }	/* en */
<verb,any>"overridden"  { return(stem(3,"e")); }    
<verb,any>"overrode"  { return(stem(3,"ide")); }    
<verb,any>"oversaw"  { return(stem(3,"see")); }     
<verb,any>"overseen"  { return(stem(3,"ee")); }     
<verb,any>"overselling"  { return(stem(4,"l")); }  
<verb,any>"oversewn"  { return(stem(3,"ew")); }     
<verb,any>"overshot"  { return(stem(3,"hoot")); }	/* en */
<verb,any>"overslept"  { return(stem(3,"eep")); }	/* en */
<verb,any>"oversold"  { return(stem(3,"ell")); }	/* en */
<verb,any>"overspent"  { return(stem(3,"end")); }	/* en */
<verb,any>"overspilt"  { return(stem(3,"ill")); }	/* disprefer */
<verb,any>"overtaken"  { return(stem(3,"ke")); }    
<verb,any>"overthrew"  { return(stem(3,"row")); }   
<verb,any>"overthrown"  { return(stem(3,"ow")); }   
<verb,any>"overtook"  { return(stem(3,"ake")); }    
<verb,any>"overwound"  { return(stem(4,"ind")); }	/* en */
<verb,any>"overwriting"  { return(stem(4,"te")); } 
<verb,any>"overwritten"  { return(stem(3,"e")); }   
<verb,any>"overwrote"  { return(stem(3,"ite")); }   
<verb,any>"paid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"partaken"  { return(stem(3,"ke")); }     
<verb,any>"partook"  { return(stem(3,"ake")); }     
<verb,any>"peed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"pent"  { return(stem(3,"en")); }	/* disprefer */
<verb,any>"pled"  { return(stem(3,"lead")); }	/* disprefer */
<verb,any>"prepaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"prologs"  { return(stem(2,"gue")); }      
<verb,any>"proven"  { return(stem(3,"ve")); }       
<verb,any>"pureed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"quartersawn"  { return(stem(3,"aw")); }  
<verb,any>"queued"  { return(stem(3,"ue")); }	/* en */
<verb,any>"queues"  { return(stem(2,"e")); }         
<verb,any>"queuing"  { return(stem(4,"ue")); }	/* disprefer */
<verb,any>"ran"  { return(stem(3,"run")); }	/* en */
<verb,any>"rang"  { return(stem(3,"ing")); }        
<verb,any>"rarefied"  { return(stem(3,"y")); }	/* en */
<verb,any>"rarefies"  { return(stem(3,"y")); }       
<verb,any>"rarefying"  { return(stem(4,"y")); }    
<verb,any>"razeed"  { return(stem(3,"ee")); }       
<verb,any>"rebuilt"  { return(stem(3,"ild")); }	/* en */
<verb,any>"recced"  { return(stem(3,"ce")); }	/* en */
<verb,any>"red"  { return(stem(3,"red")); }	/* en */
<verb,any>"redid"  { return(stem(3,"do")); }        
<verb,any>"redone"  { return(stem(3,"o")); }        
<verb,any>"refereed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"reft"  { return(stem(3,"eave")); }	/* en */
<verb,any>"remade"  { return(stem(3,"ake")); }	/* en */
<verb,any>"repaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"reran"  { return(stem(3,"run")); }	/* en */
<verb,any>"resat"  { return(stem(3,"sit")); }	/* en */
<verb,any>"retaken"  { return(stem(3,"ke")); }      
<verb,any>"rethought"  { return(stem(5,"ink")); }	/* en */
<verb,any>"retook"  { return(stem(3,"ake")); }      
<verb,any>"rewound"  { return(stem(4,"ind")); }	/* en */
<verb,any>"rewriting"  { return(stem(4,"te")); }   
<verb,any>"rewritten"  { return(stem(3,"e")); }     
<verb,any>"rewrote"  { return(stem(3,"ite")); }     
<verb,any>"ridden"  { return(stem(3,"e")); }        
<verb,any>"risen"  { return(stem(3,"se")); }        
<verb,any>"riven"  { return(stem(3,"ve")); }        
<verb,any>"rode"  { return(stem(3,"ide")); }        
<verb,any>"rose"  { return(stem(3,"ise")); }        
<verb,any>"rove"  { return(stem(3,"eeve")); }	/* en */
<verb,any>"rung"  { return(stem(3,"ing")); }        
<verb,any>"said"  { return(stem(3,"ay")); }	/* en */
<verb,any>"sang"  { return(stem(3,"ing")); }        
<verb,any>"sank"  { return(stem(3,"ink")); }        
<verb,any>"sat"  { return(stem(3,"sit")); }	/* en */
<verb,any>"saw"  { return(stem(3,"see")); }         
<verb,any>"sawn"  { return(stem(3,"aw")); }         
<verb,any>"seen"  { return(stem(3,"ee")); }         
<verb,any>"sent"  { return(stem(3,"end")); }	/* en */
<verb,any>"sewn"  { return(stem(3,"ew")); }         
<verb,any>"shaken"  { return(stem(3,"ke")); }       
<verb,any>"shaven"  { return(stem(3,"ve")); }       
<verb,any>"shent"  { return(stem(3,"end")); }	/* en */
<verb,any>"shewn"  { return(stem(3,"ew")); }        
<verb,any>"shod"  { return(stem(3,"hoe")); }	/* en */
<verb,any>"shone"  { return(stem(3,"ine")); }	/* en */
<verb,any>"shook"  { return(stem(3,"ake")); }       
<verb,any>"shot"  { return(stem(3,"hoot")); }	/* en */
<verb,any>"shown"  { return(stem(3,"ow")); }        
<verb,any>"shrank"  { return(stem(3,"ink")); }      
<verb,any>"shriven"  { return(stem(3,"ve")); }      
<verb,any>"shrove"  { return(stem(3,"ive")); }      
<verb,any>"shrunk"  { return(stem(3,"ink")); }      
<verb,any>"shrunken"  { return(stem(5,"ink")); }	/* disprefer */
<verb,any>"sightsaw"  { return(stem(3,"see")); }    
<verb,any>"sightseen"  { return(stem(3,"ee")); }    
<verb,any>"ski'd"  { return(stem(3,"i")); }	/* en */
<verb,any>"skydove"  { return(stem(3,"ive")); }	/* en */
<verb,any>"slain"  { return(stem(3,"ay")); }        
<verb,any>"slept"  { return(stem(3,"eep")); }	/* en */
<verb,any>"slew"  { return(stem(3,"lay")); }        
<verb,any>"slid"  { return(stem(3,"lide")); }       
<verb,any>"slidden"  { return(stem(3,"e")); }       
<verb,any>"slinging"  { return(stem(4,"g")); }     
<verb,any>"slung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"slunk"  { return(stem(3,"ink")); }	/* en */
<verb,any>"smelt"  { return(stem(3,"ell")); }	/* disprefer */
<verb,any>"smit"  { return(stem(3,"mite")); }       
<verb,any>"smiting"  { return(stem(4,"te")); }     
<verb,any>"smitten"  { return(stem(3,"e")); }       
<verb,any>"smote"  { return(stem(3,"ite")); }	/* disprefer */
<verb,any>"sold"  { return(stem(3,"ell")); }	/* en */
<verb,any>"soothsaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"sortied"  { return(stem(3,"ie")); }	/* en */
<verb,any>"sorties"  { return(stem(2,"e")); }        
<verb,any>"sought"  { return(stem(5,"eek")); }	/* en */
<verb,any>"sown"  { return(stem(3,"ow")); }         
<verb,any>"spat"  { return(stem(3,"pit")); }	/* en */
<verb,any>"sped"  { return(stem(3,"peed")); }	/* en */
<verb,any>"spellbound"  { return(stem(4,"ind")); }	/* en */
<verb,any>"spelt"  { return(stem(3,"ell")); }	/* disprefer */
<verb,any>"spent"  { return(stem(3,"end")); }	/* en */
<verb,any>"spilt"  { return(stem(3,"ill")); }	/* disprefer */
<verb,any>"spoilt"  { return(stem(3,"il")); }	/* en */
<verb,any>"spoke"  { return(stem(3,"eak")); }       
<verb,any>"spoken"  { return(stem(4,"eak")); }      
<verb,any>"spotlit"  { return(stem(3,"light")); }	/* en */
<verb,any>"sprang"  { return(stem(3,"ing")); }      
<verb,any>"springing"  { return(stem(4,"g")); }    
<verb,any>"sprung"  { return(stem(3,"ing")); }      
<verb,any>"spun"  { return(stem(3,"pin")); }	/* en */
<verb,any>"squeegeed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"stank"  { return(stem(3,"ink")); }       
<verb,any>"stinging"  { return(stem(4,"g")); }     
<verb,any>"stole"  { return(stem(3,"eal")); }       
<verb,any>"stolen"  { return(stem(4,"eal")); }      
<verb,any>"stood"  { return(stem(3,"and")); }	/* en */
<verb,any>"stove"  { return(stem(3,"ave")); }	/* en */
<verb,any>"strewn"  { return(stem(3,"ew")); }       
<verb,any>"stridden"  { return(stem(3,"e")); }      
<verb,any>"stringing"  { return(stem(4,"g")); }    
<verb,any>"striven"  { return(stem(3,"ve")); }      
<verb,any>"strode"  { return(stem(3,"ide")); }      
<verb,any>"strove"  { return(stem(3,"ive")); }      
<verb,any>"strown"  { return(stem(3,"ow")); }       
<verb,any>"struck"  { return(stem(3,"ike")); }	/* en */
<verb,any>"strung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"stuck"  { return(stem(3,"ick")); }	/* en */
<verb,any>"stung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"stunk"  { return(stem(3,"ink")); }       
<verb,any>"sung"  { return(stem(3,"ing")); }        
<verb,any>"sunk"  { return(stem(3,"ink")); }        
<verb,any>"sunken"  { return(stem(5,"ink")); }	/* disprefer */
<verb,any>"swam"  { return(stem(3,"wim")); }        
<verb,any>"swept"  { return(stem(3,"eep")); }	/* en */
<verb,any>"swinging"  { return(stem(4,"g")); }     
<verb,any>"swollen"  { return(stem(5,"ell")); }     
<verb,any>"swore"  { return(stem(3,"ear")); }       
<verb,any>"sworn"  { return(stem(3,"ear")); }       
<verb,any>"swum"  { return(stem(3,"wim")); }        
<verb,any>"swung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"taken"  { return(stem(3,"ke")); }        
<verb,any>"taught"  { return(stem(5,"each")); }	/* en */
<verb,any>"taxying"  { return(stem(4,"i")); }	/* disprefer */
<verb,any>"teed"  { return(stem(3,"ee")); }	/* en */
<verb,any>"thought"  { return(stem(5,"ink")); }	/* en */
<verb,any>"threw"  { return(stem(3,"row")); }       
<verb,any>"thriven"  { return(stem(3,"ve")); }	/* disprefer */      
<verb,any>"throve"  { return(stem(3,"ive")); }	/* disprefer */      
<verb,any>"thrown"  { return(stem(3,"ow")); }       
<verb,any>"tinged"  { return(stem(3,"ge")); }	/* en */
<verb,any>"tingeing"  { return(stem(4,"e")); }     
<verb,any>"tinging"  { return(stem(4,"ge")); }	/* disprefer */
<verb,any>"told"  { return(stem(3,"ell")); }	/* en */
<verb,any>"took"  { return(stem(3,"ake")); }        
<verb,any>"tore"  { return(stem(3,"ear")); }        
<verb,any>"torn"  { return(stem(3,"ear")); }        
<verb,any>"tramels"  { return(stem(3,"mel")); }	/* disprefer */
<verb,any>"transfixt"  { return(stem(3,"ix")); }	/* disprefer */
<verb,any>"tranship"  { return(stem(3,"ship")); }	/* en */
<verb,any>"trod"  { return(stem(3,"read")); }       
<verb,any>"trodden"  { return(stem(5,"ead")); }     
<verb,any>"typewriting"  { return(stem(4,"te")); } 
<verb,any>"typewritten"  { return(stem(3,"e")); }   
<verb,any>"typewrote"  { return(stem(3,"ite")); }   
<verb,any>"unbent"  { return(stem(3,"end")); }	/* en */
<verb,any>"unbound"  { return(stem(4,"ind")); }	/* en */
<verb,any>"unclad"  { return(stem(3,"lothe")); }	/* en */
<verb,any>"underbought"  { return(stem(5,"uy")); }	/* en */
<verb,any>"underfed"  { return(stem(3,"feed")); }	/* en */
<verb,any>"undergirt"  { return(stem(3,"ird")); }	/* en */
<verb,any>"undergone"  { return(stem(3,"o")); }     
<verb,any>"underlaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"underlain"  { return(stem(3,"ie")); }    
<verb,any>"underlies"  { return(stem(2,"e")); }      
<verb,any>"underlying"  { return(stem(4,"ie")); }  
<verb,any>"underpaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"underselling"  { return(stem(4,"l")); } 
<verb,any>"undershot"  { return(stem(3,"hoot")); }	/* en */
<verb,any>"undersold"  { return(stem(3,"ell")); }	/* en */
<verb,any>"understood"  { return(stem(3,"and")); }	/* en */
<verb,any>"undertaken"  { return(stem(3,"ke")); }   
<verb,any>"undertook"  { return(stem(3,"ake")); }   
<verb,any>"underwent"  { return(stem(4,"go")); }    
<verb,any>"underwriting"  { return(stem(4,"te")); }
<verb,any>"underwritten"  { return(stem(3,"e")); }  
<verb,any>"underwrote"  { return(stem(3,"ite")); }  
<verb,any>"undid"  { return(stem(3,"do")); }        
<verb,any>"undone"  { return(stem(3,"o")); }        
<verb,any>"unfroze"  { return(stem(3,"eeze")); }    
<verb,any>"unfrozen"  { return(stem(4,"eeze")); }   
<verb,any>"unlaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"unlearnt"  { return(stem(3,"rn")); }	/* en */
<verb,any>"unmade"  { return(stem(3,"ake")); }	/* en */
<verb,any>"unrove"  { return(stem(3,"eeve")); }	/* en */
<verb,any>"unsaid"  { return(stem(3,"ay")); }	/* en */
<verb,any>"unslinging"  { return(stem(4,"g")); }   
<verb,any>"unslung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"unspoke"  { return(stem(3,"eak")); }     
<verb,any>"unspoken"  { return(stem(4,"eak")); }    
<verb,any>"unstringing"  { return(stem(4,"g")); }  
<verb,any>"unstrung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"unstuck"  { return(stem(3,"ick")); }	/* en */
<verb,any>"unswore"  { return(stem(3,"ear")); }     
<verb,any>"unsworn"  { return(stem(3,"ear")); }     
<verb,any>"untaught"  { return(stem(5,"each")); }	/* en */
<verb,any>"unthought"  { return(stem(5,"ink")); }	/* en */
<verb,any>"untrod"  { return(stem(3,"read")); }     
<verb,any>"untrodden"  { return(stem(5,"ead")); }   
<verb,any>"unwound"  { return(stem(4,"ind")); }	/* en */
<verb,any>"upbuilt"  { return(stem(3,"ild")); }	/* en */
<verb,any>"upheld"  { return(stem(3,"old")); }	/* en */
<verb,any>"uphove"  { return(stem(3,"eave")); }	/* en */
<verb,any>"upped"  { return(stem(3,"")); }	/* en */
<verb,any>"upping"  { return(stem(4,"")); }        
<verb,any>"uprisen"  { return(stem(3,"se")); }      
<verb,any>"uprose"  { return(stem(3,"ise")); }      
<verb,any>"upsprang"  { return(stem(3,"ing")); }    
<verb,any>"upspringing"  { return(stem(4,"g")); }  
<verb,any>"upsprung"  { return(stem(3,"ing")); }    
<verb,any>"upswept"  { return(stem(3,"eep")); }	/* en */
<verb,any>"upswinging"  { return(stem(4,"g")); }   
<verb,any>"upswollen"  { return(stem(5,"ell")); }	/* disprefer */
<verb,any>"upswung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"visaed"  { return(stem(3,"a")); }	/* en */
<verb,any>"visaing"  { return(stem(4,"a")); }      
<verb,any>"waylaid"  { return(stem(3,"ay")); }      
<verb,any>"waylain"  { return(stem(3,"ay")); }      
<verb,any>"went"  { return(stem(4,"go")); }         
<verb,any>"wept"  { return(stem(3,"eep")); }	/* en */
<verb,any>"whipsawn"  { return(stem(3,"aw")); }     
<verb,any>"winterfed"  { return(stem(3,"feed")); }	/* en */
<verb,any>"wiredrawn"  { return(stem(3,"aw")); }    
<verb,any>"wiredrew"  { return(stem(3,"raw")); }    
<verb,any>"withdrawn"  { return(stem(3,"aw")); }    
<verb,any>"withdrew"  { return(stem(3,"raw")); }    
<verb,any>"withheld"  { return(stem(3,"old")); }	/* en */
<verb,any>"withstood"  { return(stem(3,"and")); }	/* en */
<verb,any>"woke"  { return(stem(3,"ake")); }        
<verb,any>"woken"  { return(stem(4,"ake")); }       
<verb,any>"won"  { return(stem(3,"win")); }	/* en */
<verb,any>"wore"  { return(stem(3,"ear")); }        
<verb,any>"worn"  { return(stem(3,"ear")); }        
<verb,any>"wound"  { return(stem(4,"ind")); }	/* en */
<verb,any>"wove"  { return(stem(3,"eave")); }       
<verb,any>"woven"  { return(stem(4,"eave")); }      
<verb,any>"wringing"  { return(stem(4,"g")); }     
<verb,any>"writing"  { return(stem(4,"te")); }     
<verb,any>"written"  { return(stem(3,"e")); }       
<verb,any>"wrote"  { return(stem(3,"ite")); }       
<verb,any>"wrung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"ycleped"  { return(stem(7,"clepe")); }	/* disprefer */
<verb,any>"yclept"  { return(stem(6,"clepe")); }	/* disprefer */
<noun,any>"ABCs"  { return(stem(4,"ABC")); }         
<noun,any>"bacteria"  { return(stem(1,"um")); }      
<noun,any>"loggias"  { return(stem(1,"")); }                  
<noun,any>"bases"    { return(stem(2,"is")); }       
<noun,any>"schemata"  { return(stem(2,"")); }        
<noun,any>("curi"|"formul"|"vertebr"|"larv"|"uln"|"alumn")"ae"  { return(stem(1,"")); }
<noun,any>("beldam"|"boss"|"crux"|"larynx"|"sphinx"|"trellis"|"yes"|"atlas")"es"  { return(stem(2,"")); }
<noun,any>("alumn"|"loc"|"thromb"|"tars"|"streptococc"|"stimul"|"solid"|"radi"|"mag"|"cumul"|"bronch"|"bacill")"i"  { return(stem(1,"us")); }
<noun,any>("Brahman"|"German"|"dragoman"|"ottoman"|"shaman"|"talisman"|"Norman"|"Pullman"|"Roman")"s"  { return(stem(1,"")); }
<noun,any>("Czech"|"diptych"|"Sassenach"|"abdomen"|"alibi"|"aria"|"bandit"|"begonia"|"bikini"|"caryatid"|"colon"|"cornucopia"|"cromlech"|"cupola"|"dryad"|"eisteddfod"|"encyclopaedia"|"epoch"|"eunuch"|"flotilla"|"gardenia"|"gestalt"|"gondola"|"hierarch"|"hose"|"impediment"|"koala"|"loch"|"mania"|"manservant"|"martini"|"matriarch"|"monarch"|"oligarch"|"omen"|"parabola"|"pastorale"|"patriarch"|"pea"|"peninsula"|"pfennig"|"phantasmagoria"|"pibroch"|"poly"|"real"|"safari"|"sari"|"specimen"|"standby"|"stomach"|"swami"|"taxi"|"tech"|"toccata"|"triptych"|"villa"|"yogi"|"zloty")"s" { return(stem(1,"")); }
<noun,any>("asyl"|"sanct"|"rect"|"pl"|"pendul"|"mausole"|"hoodl"|"for")"ums"  { return(stem(1,"")); }
<noun,any>("Bantu"|"Bengalese"|"Beninese"|"Boche"|"Burmese"|"Chinese"|"Congolese"|"Gabonese"|"Guyanese"|"Japanese"|"Javanese"|"Lebanese"|"Maltese"|"Olympics"|"Portuguese"|"Senegalese"|"Siamese"|"Singhalese"|"Sinhalese"|"Sioux"|"Sudanese"|"Swiss"|"Taiwanese"|"Togolese"|"Vietnamese"|"aircraft"|"anopheles"|"apparatus"|"asparagus"|"barracks"|"bellows"|"bison"|"bluefish"|"bob"|"bourgeois"|"bream"|"brill"|"butterfingers"|"carp"|"catfish"|"chassis"|"chub"|"cod"|"codfish"|"coley"|"contretemps"|"corps"|"crawfish"|"crayfish"|"crossroads"|"cuttlefish"|"dace"|"dice"|"dogfish"|"doings"|"dory"|"downstairs"|"eldest"|"finnan"|"firstborn"|"fish"|"flatfish"|"flounder"|"fowl"|"fry"|"fries"|{A}+"-works"|"gasworks"|"glassworks"|"globefish"|"goldfish"|"grand"|"gudgeon"|"gulden"|"haddock"|"hake"|"halibut"|"headquarters"|"herring"|"hertz"|"horsepower"|"hovercraft"|"hundredweight"|"ironworks"|"jackanapes"|"kilohertz"|"kurus"|"kwacha"|"ling"|"lungfish"|"mackerel"|"means"|"megahertz"|"moorfowl"|"moorgame"|"mullet"|"offspring"|"pampas"|"parr"|"patois"|"pekinese"|"penn'orth"|"perch"|"pickerel"|"pike"|"pince-nez"|"plaice"|"precis"|"quid"|"rand"|"rendezvous"|"revers"|"roach"|"roux"|"salmon"|"samurai"|"series"|"shad"|"sheep"|"shellfish"|"smelt"|"spacecraft"|"species"|"starfish"|"stockfish"|"sunfish"|"superficies"|"sweepstakes"|"swordfish"|"tench"|"tope"|"triceps"|"trout"|"tuna"|"tunafish"|"tunny"|"turbot"|"undersigned"|"veg"|"waterfowl"|"waterworks"|"waxworks"|"whiting"|"wildfowl"|"woodworm"|"yen")  { return(stemNullX()); }
<noun,any>"Aries" { return(stem(1,"s")); }           
<noun,any>"Pisces" { return(stem(1,"s")); }          
<noun,any>"Bengali" { return(stem(1,"i")); }         
<noun,any>"Somali" { return(stem(1,"i")); }          
<noun,any>"cicatrices" { return(stem(3,"x")); }      
<noun,any>"cachous" { return(stem(1,"")); }          
<noun,any>"confidantes" { return(stem(1,"")); }
<noun,any>"weltanschauungen" { return(stem(2,"")); } 
<noun,any>"apologetics" { return(stem(1,"")); }      
<noun,any>"dues" { return(stem(1,"")); }             
<noun,any>"whirrs" { return(stem(2,"")); }                    
<noun,any>"emus" { return(stem(1,"")); }
<noun,any>"equities" { return(stem(3,"y")); }        
<noun,any>"ethics" { return(stem(1,"")); }           
<noun,any>"extortions" { return(stem(1,"")); }       
<noun,any>"folks" { return(stem(1,"")); }            
<noun,any>"fumes" { return(stem(1,"")); }            
<noun,any>"fungi" { return(stem(1,"us")); }            
<noun,any>"ganglia" { return(stem(1,"on")); }        
<noun,any>"gnus" { return(stem(1,"")); }
<noun,any>"goings" { return(stem(1,"")); }           
<noun,any>"groceries" { return(stem(3,"y")); }       
<noun,any>"gurus" { return(stem(1,"")); }            
<noun,any>"halfpence" { return(stem(2,"ny")); }      
<noun,any>"hostilities" { return(stem(3,"y")); }     
<noun,any>"hysterics" { return(stem(1,"")); }        
<noun,any>"impromptus" { return(stem(1,"")); }       
<noun,any>"incidentals" { return(stem(1,"")); }      
<noun,any>"jujus" { return(stem(1,"")); }            
<noun,any>"landaus" { return(stem(1,"")); }          
<noun,any>"loins" { return(stem(1,"")); }            
<noun,any>"mains" { return(stem(1,"")); }            
<noun,any>"menus" { return(stem(1,"")); }            
<noun,any>"milieus" { return(stem(1,"")); }	/* disprefer */
<noun,any>"mockers" { return(stem(1,"")); }          
<noun,any>"morals" { return(stem(1,"")); }           
<noun,any>"motions" { return(stem(1,"")); }          
<noun,any>"mus" { return(stem(1,"")); }              
<noun,any>"nibs" { return(stem(1,"")); }             
<noun,any>"ninepins" { return(stem(1,"")); }         
<noun,any>"nippers" { return(stem(1,"")); }          
<noun,any>"oilskins" { return(stem(1,"")); }         
<noun,any>"overtones" { return(stem(1,"")); }        
<noun,any>"parvenus" { return(stem(1,"")); }         
<noun,any>"plastics" { return(stem(1,"")); }         
<noun,any>"polemics" { return(stem(1,"")); }         
<noun,any>"races" { return(stem(1,"")); }            
<noun,any>"refreshments" { return(stem(1,"")); }     
<noun,any>"reinforcements" { return(stem(1,"")); }   
<noun,any>"reparations" { return(stem(1,"")); }      
<noun,any>"returns" { return(stem(1,"")); }          
<noun,any>"rheumatics" { return(stem(1,"")); }       
<noun,any>"rudiments" { return(stem(1,"")); }        
<noun,any>"sadhus" { return(stem(1,"")); }           
<noun,any>"shires" { return(stem(1,"")); }           
<noun,any>"shivers" { return(stem(1,"")); }          
<noun,any>"sis" { return(stem(1,"")); }              
<noun,any>"spoils" { return(stem(1,"")); }           
<noun,any>"stamens" { return(stem(1,"")); }          
<noun,any>"stays" { return(stem(1,"")); }            
<noun,any>"subtitles" { return(stem(1,"")); }        
<noun,any>"tares" { return(stem(1,"")); }            
<noun,any>"thankyous" { return(stem(1,"")); }        
<noun,any>"thews" { return(stem(1,"")); }            
<noun,any>"toils" { return(stem(1,"")); }            
<noun,any>"tongs" { return(stem(1,"")); }            
<noun,any>"Hindus" { return(stem(1,"")); }           
<noun,any>"ancients" { return(stem(1,"")); }         
<noun,any>"bagpipes" { return(stem(1,"")); }         
<noun,any>"bleachers" { return(stem(1,"")); }        
<noun,any>"buttocks" { return(stem(1,"")); }         
<noun,any>"commons" { return(stem(1,"")); }          
<noun,any>"Israelis" { return(stem(1,"")); }         
<noun,any>"Israeli" { return(stem(1,"i")); }	/* disprefer */
<noun,any>"dodgems" { return(stem(1,"")); }          
<noun,any>"causeries" { return(stem(1,"")); }        
<noun,any>"quiches" { return(stem(1,"")); }          
<noun,any>"rations" { return(stem(1,"")); }          
<noun,any>"recompenses" { return(stem(1,"")); }      
<noun,any>"rinses" { return(stem(1,"")); }           
<noun,any>"lieder" { return(stem(2,"")); }           
<noun,any>"passers-by" { return(stem(4,"-by")); }    
<noun,any>"prolegomena" { return(stem(1,"on")); }    
<noun,any>"signore" { return(stem(1,"a")); }         
<noun,any>"nepalese" { return(stem(1,"e")); }        
<noun,any>"algae" { return(stem(1,"")); }            
<noun,any>"clutches" { return(stem(2,"")); }         
<noun,any>"continua" { return(stem(1,"um")); }       
<noun,any>"diggings" { return(stem(1,"")); }         
<noun,any>"K's" { return(stem(2,"")); }              
<noun,any>"seychellois" { return(stem(1,"s")); }     
<noun,any>"afterlives" { return(stem(3,"fe")); }     
<noun,any>"avens" { return(stem(1,"s")); }           
<noun,any>"axes" { return(stem(2,"is")); }           
<noun,any>"bonsai" { return(stem(1,"i")); }          
<noun,any>"coypus" { return(stem(1,"")); }
<noun,any>"duodena" { return(stem(1,"um")); }        
<noun,any>"genii" { return(stem(1,"e")); }           
<noun,any>"leaves" { return(stem(3,"f")); }          
<noun,any>"mantelshelves" { return(stem(3,"f")); }   
<noun,any>"meninges" { return(stem(3,"x")); }        
<noun,any>"moneybags" { return(stem(1,"s")); }       
<noun,any>"obbligati" { return(stem(1,"o")); }       
<noun,any>"orchises" { return(stem(2,"")); }         
<noun,any>"palais" { return(stem(1,"s")); }          
<noun,any>"pancreases" { return(stem(2,"")); }       
<noun,any>"phalanges" { return(stem(3,"x")); }       
<noun,any>"portcullises" { return(stem(2,"")); }     
<noun,any>"pubes" { return(stem(1,"s")); }           
<noun,any>"pulses" { return(stem(1,"")); }           
<noun,any>"ratlines" { return(stem(2,"")); }         
<noun,any>"signori" { return(stem(1,"")); }          
<noun,any>"spindle-shanks" { return(stem(1,"s")); }  
<noun,any>"substrata" { return(stem(1,"um")); }      
<noun,any>"woolies" { return(stem(3,"ly")); }        
<noun,any>"moggies" { return(stem(3,"y")); }         
<noun,any>("ghill"|"group"|"honk"|"mean"|"road"|"short"|"smooth"|"book"|"cabb"|"hank"|"toots"|"tough"|"trann")"ies" { return(stem(2,"e")); }
<noun,any>("christmas"|"judas")"es" { return(stem(2,"")); }
<noun,any>("flamb"|"plat"|"portmant"|"tabl"|"b"|"bur"|"trouss")"eaus" { return(stem(2,"u")); }	/* disprefer */
<noun,any>("maharaj"|"raj"|"myn"|"mull")"ahs"  { return(stem(2,"")); }
<noun,any>("Boch"|"apocalyps"|"aps"|"ars"|"avalanch"|"backach"|"tens"|"relaps"|"barouch"|"brioch"|"cloch"|"collaps"|"cops"|"crech"|"crevass"|"douch"|"eclips"|"expans"|"expens"|"finess"|"glimps"|"gouach"|"heartach"|"impass"|"impuls"|"laps"|"mans"|"microfich"|"mouss"|"nonsens"|"pastich"|"peliss"|"poss"|"prolaps"|"psych")"es" { return(stem(1,"")); }
<noun,any>"addenda"  { return(stem(2,"dum")); }      
<noun,any>"adieux"  { return(stem(2,"u")); }         
<noun,any>"aides-de-camp"  { return(stem(9,"-de-camp")); }
<noun,any>"aliases"  { return(stem(2,"")); }         
<noun,any>"alkalies"  { return(stem(2,"")); }        
<noun,any>"alti"  { return(stem(2,"to")); }     
<noun,any>"amanuenses"  { return(stem(2,"is")); }    
<noun,any>"analyses"  { return(stem(2,"is")); }      
<noun,any>"anthraces"  { return(stem(3,"x")); }      
<noun,any>"antitheses"  { return(stem(2,"is")); }    
<noun,any>"aphides"  { return(stem(3,"s")); }        
<noun,any>"apices"  { return(stem(4,"ex")); }        
<noun,any>"appendices"  { return(stem(3,"x")); }     
<noun,any>"arboreta"  { return(stem(2,"tum")); }     
<noun,any>"atlantes"  { return(stem(4,"s")); }	/* disprefer */
<noun,any>"aurar"  { return(stem(5,"eyrir")); }     
<noun,any>"automata"  { return(stem(2,"ton")); }     
<noun,any>"axises"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"bambini"  { return(stem(2,"no")); }       
<noun,any>"bandeaux"  { return(stem(2,"u")); }       
<noun,any>"banditti"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"bassi"  { return(stem(2,"so")); }         
<noun,any>"beaux"  { return(stem(2,"u")); }          
<noun,any>"beeves"  { return(stem(3,"f")); }         
<noun,any>"bicepses"  { return(stem(2,"")); }        
<noun,any>"bijoux"  { return(stem(2,"u")); }         
<noun,any>"billets-doux"  { return(stem(6,"-doux")); }
<noun,any>"boraces"  { return(stem(3,"x")); }        
<noun,any>"bossies"  { return(stem(3,"")); }	/* disprefer */
<noun,any>"brainchildren"  { return(stem(3,"")); }   
<noun,any>"brothers-in-law"  { return(stem(8,"-in-law")); }
<noun,any>"buckteeth"  { return(stem(4,"ooth")); }   
<noun,any>"bunde"  { return(stem(2,"d")); }          
<noun,any>"bureaux"  { return(stem(2,"u")); }        
<noun,any>"cacti"  { return(stem(1,"us")); }         
<noun,any>"calves"  { return(stem(3,"f")); }         
<noun,any>"calyces"  { return(stem(3,"x")); }        
<noun,any>"candelabra"  { return(stem(2,"rum")); }   
<noun,any>"capricci"  { return(stem(2,"cio")); }	/* disprefer */
<noun,any>"caribous"  { return(stem(2,"u")); }
<noun,any>"carides"  { return(stem(4,"yatid")); }	/* disprefer */
<noun,any>"catalyses"  { return(stem(2,"is")); }     
<noun,any>"cerebra"  { return(stem(2,"rum")); }      
<noun,any>"cervices"  { return(stem(3,"x")); }       
<noun,any>"chateaux"  { return(stem(2,"u")); }       
<noun,any>"children"  { return(stem(3,"")); }        
<noun,any>"chillies"  { return(stem(2,"")); }        
<noun,any>"chrysalides"  { return(stem(3,"s")); }    
<noun,any>"chrysalises"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"ciceroni"  { return(stem(2,"ne")); }      
<noun,any>"cloverleaves"  { return(stem(3,"f")); }   
<noun,any>"coccyges"  { return(stem(3,"x")); }       
<noun,any>"codices"  { return(stem(4,"ex")); }       
<noun,any>"colloquies"  { return(stem(3,"y")); }     
<noun,any>"colones"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"concertanti"  { return(stem(2,"te")); }   
<noun,any>"concerti"  { return(stem(2,"to")); }      
<noun,any>"concertini"  { return(stem(2,"no")); }    
<noun,any>"conquistadores"  { return(stem(2,"")); }  
<noun,any>"consortia"  { return(stem(1,"um")); }     
<noun,any>"contralti"  { return(stem(2,"to")); }     
<noun,any>"corpora"  { return(stem(3,"us")); }       
<noun,any>"corrigenda"  { return(stem(2,"dum")); }   
<noun,any>"cortices"  { return(stem(4,"ex")); }      
<noun,any>"crescendi"  { return(stem(2,"do")); }	/* disprefer */
<noun,any>"crises"  { return(stem(2,"is")); }        
<noun,any>"criteria"  { return(stem(2,"ion")); }     
<noun,any>"cruces"  { return(stem(3,"x")); }	/* disprefer */
<noun,any>"culs-de-sac"  { return(stem(8,"-de-sac")); }
<noun,any>"cyclopes"  { return(stem(2,"s")); }       
<noun,any>"cyclopses"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"data"  { return(stem(2,"tum")); }         
<noun,any>"daughters-in-law"  { return(stem(8,"-in-law")); }
<noun,any>"desiderata"  { return(stem(2,"tum")); }   
<noun,any>"diaereses"  { return(stem(2,"is")); }     
<noun,any>"diaerses"  { return(stem(3,"esis")); }	/* disprefer */
<noun,any>"dialyses"  { return(stem(2,"is")); }      
<noun,any>"diathses"  { return(stem(3,"esis")); }    
<noun,any>"dicta"  { return(stem(2,"tum")); }        
<noun,any>"diereses"  { return(stem(2,"is")); }      
<noun,any>"dilettantes"  { return(stem(2,"e")); }    
<noun,any>"dilettanti"  { return(stem(2,"te")); }	/* disprefer */
<noun,any>"divertimenti"  { return(stem(2,"to")); }  
<noun,any>"dogteeth"  { return(stem(4,"ooth")); }    
<noun,any>"dormice"  { return(stem(3,"ouse")); }     
<noun,any>"dryades"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"dui"  { return(stem(2,"uo")); }	/* disprefer */
<noun,any>"duona"  { return(stem(2,"denum")); }	/* disprefer */
<noun,any>"duonas"  { return(stem(3,"denum")); }	/* disprefer */
<noun,any>"tutus"  { return(stem(1,"")); }                    
<noun,any>"vicissitudes"  { return(stem(1,"")); }             
<noun,any>"virginals"  { return(stem(1,"")); }                
<noun,any>"volumes"  { return(stem(1,"")); }                  
<noun,any>"zebus"  { return(stem(1,"")); }                    
<noun,any>"dwarves"  { return(stem(3,"f")); }        
<noun,any>"eisteddfodau"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"ellipses"  { return(stem(2,"is")); }      
<noun,any>"elves"  { return(stem(3,"f")); }          
<noun,any>"emphases"  { return(stem(2,"is")); }      
<noun,any>"epicentres"  { return(stem(2,"e")); }     
<noun,any>"epiglottides"  { return(stem(3,"s")); }   
<noun,any>"epiglottises"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"errata"  { return(stem(2,"tum")); }       
<noun,any>"exegeses"  { return(stem(2,"is")); }      
<noun,any>"eyeteeth"  { return(stem(4,"ooth")); }    
<noun,any>"fathers-in-law"  { return(stem(8,"-in-law")); }
<noun,any>"feet"  { return(stem(3,"oot")); }         
<noun,any>"fellaheen"  { return(stem(3,"")); }       
<noun,any>"fellahin"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"femora"  { return(stem(3,"ur")); }        
<noun,any>"flagstaves"  { return(stem(3,"ff")); }	/* disprefer */
<noun,any>"flambeaux"  { return(stem(2,"u")); }      
<noun,any>"flatfeet"  { return(stem(3,"oot")); }     
<noun,any>"fleurs-de-lis"  { return(stem(8,"-de-lis")); }
<noun,any>"fleurs-de-lys"  { return(stem(8,"-de-lys")); }
<noun,any>"flyleaves"  { return(stem(3,"f")); }      
<noun,any>"fora"  { return(stem(2,"rum")); }	/* disprefer */
<noun,any>"forcipes"  { return(stem(4,"eps")); }     
<noun,any>"forefeet"  { return(stem(3,"oot")); }     
<noun,any>"fulcra"  { return(stem(2,"rum")); }       
<noun,any>"gallowses"  { return(stem(2,"")); }       
<noun,any>"gases"  { return(stem(2,"")); }           
<noun,any>"gasses"  { return(stem(3,"")); }	/* disprefer */
<noun,any>"gateaux"  { return(stem(2,"u")); }        
<noun,any>"geese"  { return(stem(4,"oose")); }       
<noun,any>"gemboks"  { return(stem(4,"sbok")); }     
<noun,any>"genera"  { return(stem(3,"us")); }        
<noun,any>"geneses"  { return(stem(2,"is")); }       
<noun,any>"gentlemen-at-arms"  { return(stem(10,"an-at-arms")); }
<noun,any>"gestalten"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"glissandi"  { return(stem(2,"do")); }     
<noun,any>"glottides"  { return(stem(3,"s")); }	/* disprefer */
<noun,any>"glottises"  { return(stem(2,"")); }       
<noun,any>"godchildren"  { return(stem(3,"")); }     
<noun,any>"goings-over"  { return(stem(6,"-over")); }
<noun,any>"grandchildren"  { return(stem(3,"")); }   
<noun,any>"halves"  { return(stem(3,"f")); }         
<noun,any>"hangers-on"  { return(stem(4,"-on")); }   
<noun,any>"helices"  { return(stem(3,"x")); }        
<noun,any>"hooves"  { return(stem(3,"f")); }         
<noun,any>"hosen"  { return(stem(2,"e")); }	/* disprefer */
<noun,any>"hypotheses"  { return(stem(2,"is")); }    
<noun,any>"iambi"  { return(stem(2,"b")); }          
<noun,any>"ibices"  { return(stem(4,"ex")); }	/* disprefer */
<noun,any>"ibises"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"impedimenta"  { return(stem(2,"t")); }	/* disprefer */
<noun,any>"indices"  { return(stem(4,"ex")); }       
<noun,any>"intagli"  { return(stem(2,"lio")); }	/* disprefer */
<noun,any>"intermezzi"  { return(stem(2,"zo")); }    
<noun,any>"interregna"  { return(stem(2,"num")); }   
<noun,any>"irides"  { return(stem(3,"s")); }	/* disprefer */
<noun,any>"irises"  { return(stem(2,"")); }          
<noun,any>"is"  { return(stem(2,"is")); }            
<noun,any>"jacks-in-the-box"  { return(stem(12,"-in-the-box")); }
<noun,any>"kibbutzim"  { return(stem(2,"")); }       
<noun,any>"knives"  { return(stem(3,"fe")); }        
<noun,any>"kohlrabies"  { return(stem(2,"")); }      
<noun,any>"kronen"  { return(stem(2,"e")); }	/* disprefer */
<noun,any>"kroner"  { return(stem(2,"e")); }
<noun,any>"kronor"  { return(stem(2,"a")); }
<noun,any>"kronur"  { return(stem(2,"a")); }	/* disprefer */
<noun,any>"kylikes"  { return(stem(3,"x")); }        
<noun,any>"ladies-in-waiting"  { return(stem(14,"y-in-waiting")); }
<noun,any>"larynges"  { return(stem(3,"x")); }	/* disprefer */
<noun,any>"latices"  { return(stem(4,"ex")); }       
<noun,any>"leges"  { return(stem(3,"x")); }          
<noun,any>"libretti"  { return(stem(2,"to")); }      
<noun,any>"lice"  { return(stem(3,"ouse")); }        
<noun,any>"lire"  { return(stem(2,"ra")); }          
<noun,any>"lives"  { return(stem(3,"fe")); }         
<noun,any>"loaves"  { return(stem(3,"f")); }         
<noun,any>"loggie"  { return(stem(2,"ia")); }	/* disprefer */
<noun,any>"lustra"  { return(stem(2,"re")); }        
<noun,any>"lyings-in"  { return(stem(4,"-in")); }    
<noun,any>"macaronies"  { return(stem(2,"")); }      
<noun,any>"maestri"  { return(stem(2,"ro")); }       
<noun,any>"mantes"  { return(stem(2,"is")); }        
<noun,any>"mantises"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"markkaa"  { return(stem(2,"a")); }        
<noun,any>"marquises"  { return(stem(2,"")); }       
<noun,any>"masters-at-arms"  { return(stem(9,"-at-arms")); }
<noun,any>"matrices"  { return(stem(3,"x")); }       
<noun,any>"matzoth"  { return(stem(2,"")); }         
<noun,any>"mausolea"  { return(stem(2,"eum")); }	/* disprefer */
<noun,any>"maxima"  { return(stem(2,"mum")); }       
<noun,any>"memoranda"  { return(stem(2,"dum")); }    
<noun,any>"men-at-arms"  { return(stem(10,"an-at-arms")); }
<noun,any>"men-o'-war"  { return(stem(9,"an-of-war")); }	/* disprefer */
<noun,any>"men-of-war"  { return(stem(9,"an-of-war")); }
<noun,any>"menservants"  { return(stem(10,"anservant")); }	/* disprefer */
<noun,any>"mesdemoiselles"  { return(stem(13,"ademoiselle")); }
<noun,any>"messieurs"  { return(stem(8,"onsieur")); }
<noun,any>"metatheses"  { return(stem(2,"is")); }    
<noun,any>"metropolises"  { return(stem(2,"")); }    
<noun,any>"mice"  { return(stem(3,"ouse")); }        
<noun,any>"milieux"  { return(stem(2,"u")); }        
<noun,any>"minima"  { return(stem(2,"mum")); }       
<noun,any>"momenta"  { return(stem(2,"tum")); }      
<noun,any>"monies"  { return(stem(3,"ey")); }        
<noun,any>"monsignori"  { return(stem(2,"r")); }     
<noun,any>"mooncalves"  { return(stem(3,"f")); }     
<noun,any>"mothers-in-law"  { return(stem(8,"-in-law")); }
<noun,any>"naiades"  { return(stem(2,"")); }         
<noun,any>"necropoleis"  { return(stem(3,"is")); }	/* disprefer */
<noun,any>"necropolises"  { return(stem(2,"")); }    
<noun,any>"nemeses"  { return(stem(2,"is")); }       
<noun,any>"novelle"  { return(stem(2,"la")); }       
<noun,any>"oases"  { return(stem(2,"is")); }         
<noun,any>"obloquies"  { return(stem(3,"y")); }      
<noun,any>{A}+"hedra"  { return(stem(2,"ron")); }    
<noun,any>"optima"  { return(stem(2,"mum")); }       
<noun,any>"ora"  { return(stem(2,"s")); }            
<noun,any>"osar"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"ossa"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"ova"  { return(stem(2,"vum")); }          
<noun,any>"oxen"  { return(stem(2,"")); }            
<noun,any>"paralyses"  { return(stem(2,"is")); }     
<noun,any>"parentheses"  { return(stem(2,"is")); }   
<noun,any>"paris-mutuels"  { return(stem(9,"-mutuel")); }
<noun,any>"pastorali"  { return(stem(2,"le")); }	/* disprefer */
<noun,any>"patresfamilias"  { return(stem(11,"erfamilias")); }
<noun,any>"pease"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"pekingese"  { return(stem(4,"ese")); }	/* disprefer */
<noun,any>"pelves"  { return(stem(2,"is")); }	/* disprefer */
<noun,any>"pelvises"  { return(stem(2,"")); }        
<noun,any>"pence"  { return(stem(2,"ny")); }         
<noun,any>"penes"  { return(stem(2,"is")); }	/* disprefer */
<noun,any>"penises"  { return(stem(2,"")); }         
<noun,any>"penknives"  { return(stem(3,"fe")); }     
<noun,any>"perihelia"  { return(stem(2,"ion")); }    
<noun,any>"pfennige"  { return(stem(2,"g")); }	/* disprefer */
<noun,any>"pharynges"  { return(stem(3,"x")); }      
<noun,any>"phenomena"  { return(stem(2,"non")); }    
<noun,any>"philodendra"  { return(stem(2,"ron")); }  
<noun,any>"pieds-a-terre"  { return(stem(9,"-a-terre")); }
<noun,any>"pineta"  { return(stem(2,"tum")); }       
<noun,any>"plateaux"  { return(stem(2,"u")); }       
<noun,any>"plena"  { return(stem(2,"num")); }        
<noun,any>"pocketknives"  { return(stem(3,"fe")); }  
<noun,any>"portmanteaux"  { return(stem(2,"u")); }   
<noun,any>"potlies"  { return(stem(4,"belly")); }    
<noun,any>"praxes"  { return(stem(2,"is")); }	/* disprefer */
<noun,any>"praxises"  { return(stem(2,"")); }        
<noun,any>"proboscides"  { return(stem(3,"s")); }	/* disprefer */
<noun,any>"proboscises"  { return(stem(2,"")); }     
<noun,any>"prostheses"  { return(stem(2,"is")); }    
<noun,any>"protozoa"  { return(stem(2,"oan")); }     
<noun,any>"pudenda"  { return(stem(2,"dum")); }      
<noun,any>"putti"  { return(stem(2,"to")); }         
<noun,any>"quanta"  { return(stem(2,"tum")); }       
<noun,any>"quarterstaves"  { return(stem(3,"ff")); } 
<noun,any>"reales"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"recta"  { return(stem(2,"tum")); }	/* disprefer */
<noun,any>"referenda"  { return(stem(2,"dum")); }    
<noun,any>"reis"  { return(stem(2,"al")); }	/* disprefer */
<noun,any>"rondeaux"  { return(stem(2,"u")); }       
<noun,any>"rostra"  { return(stem(2,"rum")); }       
<noun,any>"runners-up"  { return(stem(4,"-up")); }   
<noun,any>"sancta"  { return(stem(2,"tum")); }	/* disprefer */
<noun,any>"sawboneses"  { return(stem(2,"")); }      
<noun,any>"scarves"  { return(stem(3,"f")); }        
<noun,any>"scherzi"  { return(stem(2,"zo")); }	/* disprefer */
<noun,any>"scrota"  { return(stem(2,"tum")); }       
<noun,any>"secretaries-general"  { return(stem(11,"y-general")); }
<noun,any>"selves"  { return(stem(3,"f")); }         
<noun,any>"sera"  { return(stem(2,"rum")); }	/* disprefer */
<noun,any>"seraphim"  { return(stem(2,"")); }        
<noun,any>"sheaves"  { return(stem(3,"f")); }        
<noun,any>"shelves"  { return(stem(3,"f")); }        
<noun,any>"simulacra"  { return(stem(2,"rum")); }    
<noun,any>"sisters-in-law"  { return(stem(8,"-in-law")); }
<noun,any>"soli"  { return(stem(2,"lo")); }	/* disprefer */
<noun,any>"soliloquies"  { return(stem(3,"y")); }    
<noun,any>"sons-in-law"  { return(stem(8,"-in-law")); }
<noun,any>"spectra"  { return(stem(2,"rum")); }      
<noun,any>"sphinges"  { return(stem(3,"x")); }	/* disprefer */
<noun,any>"splayfeet"  { return(stem(3,"oot")); }    
<noun,any>"sputa"  { return(stem(2,"tum")); }        
<noun,any>"stamina"  { return(stem(3,"en")); }	/* disprefer */
<noun,any>"stelae"  { return(stem(2,"e")); }         
<noun,any>"stepchildren"  { return(stem(3,"")); }    
<noun,any>"sterna"  { return(stem(2,"num")); }       
<noun,any>"strata"  { return(stem(2,"tum")); }       
<noun,any>"stretti"  { return(stem(2,"to")); }       
<noun,any>"summonses"  { return(stem(2,"")); }       
<noun,any>"swamies"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"swathes"  { return(stem(2,"")); }         
<noun,any>"synopses"  { return(stem(2,"is")); }      
<noun,any>"syntheses"  { return(stem(2,"is")); }     
<noun,any>"tableaux"  { return(stem(2,"u")); }       
<noun,any>"taxies"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"teeth"  { return(stem(4,"ooth")); }       
<noun,any>"tempi"  { return(stem(2,"po")); }         
<noun,any>"tenderfeet"  { return(stem(3,"oot")); }   
<noun,any>"testes"  { return(stem(2,"is")); }        
<noun,any>"theses"  { return(stem(2,"is")); }        
<noun,any>"thieves"  { return(stem(3,"f")); }        
<noun,any>"thoraces"  { return(stem(3,"x")); }       
<noun,any>"titmice"  { return(stem(3,"ouse")); }     
<noun,any>"tootses"  { return(stem(2,"")); }         
<noun,any>"torsi"  { return(stem(2,"so")); }	/* disprefer */
<noun,any>"tricepses"  { return(stem(2,"")); }	/* disprefer */
<noun,any>"triumviri"  { return(stem(2,"r")); }      
<noun,any>"trousseaux"  { return(stem(2,"u")); }	/* disprefer */
<noun,any>"turves"  { return(stem(3,"f")); }         
<noun,any>"tympana"  { return(stem(2,"num")); }      
<noun,any>"ultimata"  { return(stem(2,"tum")); }     
<noun,any>"vacua"  { return(stem(2,"uum")); }	/* disprefer */      
<noun,any>"vertices"  { return(stem(4,"ex")); }      
<noun,any>"vertigines"  { return(stem(4,"o")); }     
<noun,any>"virtuosi"  { return(stem(2,"so")); }      
<noun,any>"vortices"  { return(stem(4,"ex")); }      
<noun,any>"wagons-lits"  { return(stem(6,"-lit")); } 
<noun,any>"weirdies"  { return(stem(2,"e")); }       
<noun,any>"werewolves"  { return(stem(3,"f")); }     
<noun,any>"wharves"  { return(stem(3,"f")); }        
<noun,any>"whippers-in"  { return(stem(4,"-in")); }  
<noun,any>"wolves"  { return(stem(3,"f")); }         
<noun,any>"woodlice"  { return(stem(3,"ouse")); }    
<noun,any>"yogin"  { return(stem(2,"i")); }	/* disprefer */
<noun,any>"zombies"  { return(stem(2,"e")); }        
<verb,any>"cryed"  { return(stem(3,"y")); }	/* disprefer */
<verb,any>"forted"  { return(stem(3,"te")); }	/* en */
<verb,any>"forteing"  { return(stem(4,"e")); }     
<verb,any>"picknicks"  { return(stem(2,"")); }       
<verb,any>"resold"  { return(stem(3,"ell")); }	/* en */
<verb,any>"retold"  { return(stem(3,"ell")); }	/* en */
<verb,any>"retying"  { return(stem(4,"ie")); }     
<verb,any>"singed"  { return(stem(3,"ge")); }	/* en */
<verb,any>"singeing"  { return(stem(4,"e")); }     
<verb,any>"trecked"  { return(stem(4,"k")); }	/* en */
<verb,any>"trecking"  { return(stem(5,"k")); }     
<noun,any>"canvases"  { return(stem(2,"")); }        
<noun,any>"carcases"  { return(stem(1,"")); }        
<noun,any>"lenses"  { return(stem(2,"")); }          
<verb,any>"buffetts"  { return(stem(2,"")); }        
<verb,any>"plummetts"  { return(stem(2,"")); }	/* disprefer */
<verb,any>"gunslung"  { return(stem(3,"ing")); }	/* en */
<verb,any>"gunslinging"  { return(stem(4,"g")); }  
<noun,any>"biases"  { return(stem(2,"")); }          
<noun,any>"biscotti"  { return(stem(2,"to")); }      
<noun,any>"bookshelves"  { return(stem(3,"f")); }    
<noun,any>"palazzi"  { return(stem(2,"zo")); }       
<noun,any>"daises"  { return(stem(2,"")); }          
<noun,any>"reguli"  { return(stem(2,"lo")); }        
<noun,any>"steppes"  { return(stem(2,"e")); }        
<noun,any>"obsequies"  { return(stem(3,"y")); }      
<verb,noun,any>"busses"  { return(stem(3,"")); }
<verb,any>"bussed"  { return(stem(3,"")); }	/* en */
<verb,any>"bussing"  { return(stem(4,"")); }       
<verb,noun,any>"hocus-pocusses"  { return(stem(3,"")); }
<verb,noun,any>"hocusses"  { return(stem(3,"")); }   
<noun,any>"corpses"  { return(stem(1,"")); }

<verb,any>"ach"{EDING}    { return(stemSemiReg(0,"e")); }       
<verb,any>"accustom"{EDING} { return(stemSemiReg(0,"")); }      
<verb,any>"blossom"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"boycott"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"catalog"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>{PRE}*"creat"{EDING} { return(stemSemiReg(0,"e")); }  
<verb,any>"finess"{ESEDING} { return(stemSemiReg(0,"e")); }     
<verb,any>"interfer"{EDING} { return(stemSemiReg(0,"e")); }     
<verb,any>{PRE}*"rout"{EDING} { return(stemSemiReg(0,"e")); }   
<verb,any>"tast"{ESEDING} { return(stemSemiReg(0,"e")); }       
<verb,any>"wast"{ESEDING} { return(stemSemiReg(0,"e")); }       
<verb,any>"acquitt"{EDING} { return(stemSemiReg(1,"")); }       
<verb,any>"ante"{ESEDING} { return(stemSemiReg(0,"")); }        
<verb,any>"arc"{EDING} { return(stemSemiReg(0,"")); }           
<verb,any>"arck"{EDING} { return(stemSemiReg(1,"")); }	/* disprefer */
<verb,any>"banquet"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"barrel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"bedevil"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"beguil"{EDING} { return(stemSemiReg(0,"e")); }       
<verb,any>"bejewel"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"bevel"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"bias"{ESEDING} { return(stemSemiReg(0,"")); }        
<verb,any>"biass"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"bivouack"{EDING} { return(stemSemiReg(1,"")); }      
<verb,any>"buckram"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"bushel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"canal"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"cancel"{EDING} { return(stemSemiReg(0,"")); }	/* disprefer */
<verb,any>"carol"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"cavil"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"cbel"{EDING} { return(stemSemiReg(0,"")); }          
<verb,any>"cbell"{EDING} { return(stemSemiReg(1,""));	}	/* disprefer */
<verb,any>"channel"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"chisel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"clep"{EDING} { return(stemSemiReg(0,"e")); }         
<verb,any>"cloth"{ESEDING} { return(stemSemiReg(0,"e")); }      
<verb,any>"coiff"{ESEDING} { return(stemSemiReg(1,"")); }         
<verb,any>"concertina"{EDING} { return(stemSemiReg(0,"")); }    
<verb,any>"conga"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"coquett"{EDING} { return(stemSemiReg(1,"")); }       
<verb,any>"counsel"{EDING} { return(stemSemiReg(0,""));}	/* disprefer */
<verb,any>"croquet"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"cudgel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"cupel"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"debuss"{ESEDING} { return(stemSemiReg(1,"")); }      
<verb,any>"degass"{ESEDING} { return(stemSemiReg(1,"")); }      
<verb,any>"devil"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"diall"{EDING} { return(stemSemiReg(1,"")); }         
<verb,any>"disembowel"{EDING} { return(stemSemiReg(0,"")); }    
<verb,any>"dishevel"{EDING} { return(stemSemiReg(0,"")); }      
<verb,any>"drivel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"duell"{EDING} { return(stemSemiReg(1,"")); }         
<verb,any>"embuss"{ESEDING} { return(stemSemiReg(1,"")); }      
<verb,any>"empanel"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"enamel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"equal"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"equall"{EDING} { return(stemSemiReg(1,""));}	/* disprefer */
<verb,any>"equipp"{EDING} { return(stemSemiReg(1,"")); }        
<verb,any>"flannel"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"frivol"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"frolick"{EDING} { return(stemSemiReg(1,"")); }       
<verb,any>"fuell"{EDING} { return(stemSemiReg(1,"")); }         
<verb,any>"funnel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"gambol"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"gass"{ESEDING} { return(stemSemiReg(1,"")); }        
<verb,any>"gell"{EDING} { return(stemSemiReg(1,"")); }          
<verb,any>"glace"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"gravel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"grovel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"gypp"{EDING} { return(stemSemiReg(1,"")); }          
<verb,any>"hansel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"hatchel"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"hocus-pocuss"{EDING} { return(stemSemiReg(1,"")); }  
<verb,any>"hocuss"{EDING} { return(stemSemiReg(1,"")); }        
<verb,any>"housel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"hovel"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"impanel"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"initiall"{EDING} { return(stemSemiReg(1,"")); }      
<verb,any>"jewel"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"kennel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"kernel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"label"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"laurel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"level"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"libel"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"marshal"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"marvel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"medal"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"metal"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"mimick"{EDING} { return(stemSemiReg(1,"")); }        
<verb,any>"misspell"{EDING} { return(stemSemiReg(0,"")); }      
<verb,any>"model"{EDING} { return(stemSemiReg(0,""));}	/* disprefer */
<verb,any>"nickel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"nonpluss"{ESEDING} { return(stemSemiReg(1,"")); }    
<verb,any>"outgass"{ESEDING} { return(stemSemiReg(1,"")); }     
<verb,any>"outgeneral"{EDING} { return(stemSemiReg(0,"")); }    
<verb,any>"overspill"{EDING} { return(stemSemiReg(0,"")); }     
<verb,any>"pall"{EDING} { return(stemSemiReg(0,"")); }          
<verb,any>"panel"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"panick"{EDING} { return(stemSemiReg(1,"")); }        
<verb,any>"parallel"{EDING} { return(stemSemiReg(0,"")); }      
<verb,any>"parcel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"pedal"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"pencil"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"physick"{EDING} { return(stemSemiReg(1,"")); }       
<verb,any>"picnick"{EDING} { return(stemSemiReg(1,"")); }       
<verb,any>"pistol"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"polka"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"pommel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"precancel"{EDING} { return(stemSemiReg(0,""));}	/* disprefer */
<verb,any>"prolog"{EDING} { return(stemSemiReg(0,"ue")); }      
<verb,any>"pummel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"quarrel"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"quipp"{EDING} { return(stemSemiReg(1,"")); }         
<verb,any>"quitt"{EDING} { return(stemSemiReg(1,"")); }         
<verb,any>"ravel"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"recce"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"refuell"{EDING} { return(stemSemiReg(1,"")); }       
<verb,any>"revel"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"rival"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"roquet"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"rowel"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"samba"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"saute"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"shellack"{EDING} { return(stemSemiReg(1,"")); }      
<verb,any>"shovel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"shrivel"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"sick"{EDING} { return(stemSemiReg(1,"")); }          
<verb,any>"signal"{EDING} { return(stemSemiReg(0,""));}	/* disprefer */
<verb,any>"ski"{EDING} { return(stemSemiReg(0,"")); }           
<verb,any>"snafu"{ESEDING} { return(stemSemiReg(0,"")); }       
<verb,any>"snivel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"sol-fa"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"spancel"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"spiral"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"squatt"{EDING} { return(stemSemiReg(1,"")); }        
<verb,any>"squibb"{EDING} { return(stemSemiReg(1,"")); }        
<verb,any>"squidd"{EDING} { return(stemSemiReg(1,"")); }        
<verb,any>"stencil"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"subpoena"{EDING} { return(stemSemiReg(0,"")); }      
<verb,any>"subtotal"{EDING} { return(stemSemiReg(0,""));}	/* disprefer */
<verb,any>"swivel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"symbol"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"symboll"{EDING} { return(stemSemiReg(1,""));}	/* disprefer */
<verb,any>"talc"{EDING} { return(stemSemiReg(0,"")); }          
<verb,any>"talck"{EDING} { return(stemSemiReg(1,""));}	/* disprefer */
<verb,any>"tassel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"taxi"{ESEDING} { return(stemSemiReg(0,"")); }        
<verb,any>"tinsel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"total"{EDING} { return(stemSemiReg(0,""));}	/* disprefer */
<verb,any>"towel"{EDING} { return(stemSemiReg(0,"")); }         
<verb,any>"traffick"{EDING} { return(stemSemiReg(1,"")); }      
<verb,any>"tramel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"tramell"{EDING} { return(stemSemiReg(1,""));}	/* disprefer */
<verb,any>"travel"{EDING} { return(stemSemiReg(0,""));}	/* disprefer */
<verb,any>"trowel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"tunnel"{EDING} { return(stemSemiReg(0,"")); }        
<verb,any>"uncloth"{ESEDING} { return(stemSemiReg(0,"e")); }    
<verb,any>"unkennel"{EDING} { return(stemSemiReg(0,"")); }      
<verb,any>"unravel"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"upswell"{EDING} { return(stemSemiReg(0,"")); }       
<verb,any>"victuall"{EDING} { return(stemSemiReg(1,"")); }      
<verb,any>"vitrioll"{EDING} { return(stemSemiReg(1,"")); }      
<verb,any>"viva"{EDING} { return(stemSemiReg(0,"")); }          
<verb,any>"yodel"{EDING} { return(stemSemiReg(0,"")); }         

<verb,any>("di"|"ti"|"li"|"unti"|"beli"|"hogti"|"stymi")"es"  { return(stem(2,"e")); }	/* en */
<verb,any>("di"|"ti"|"li"|"unti"|"beli"|"hogti"|"stymi")"ed"  { return(stem(2,"e")); }	/* en */
<verb,any>("d"|"t"|"l"|"unt"|"bel"|"hogt"|"stym")"ying"  { return(stem(4,"ie")); }	/* en */
<verb,any>"bias" { return(stemNullC()); }                         
<verb,any>"canvas" { return(stemNullC()); }                       
<verb,any>"canvas"{ESEDING} { return(stemSemiReg(0,"")); }      
<verb,any>"embed" { return(stemNullC()); }	/* disprefer */
<verb,any>"focuss"{ESEDING} { return(stemSemiReg(1,"")); }      
<verb,any>"gas" { return(stemNullC()); }
<verb,any>"picknick"{EDING} { return(stemSemiReg(1,"")); }      
<verb,any>("adher"|"ador"|"attun"|"bast"|"bor"|"can"|"centr"|"cit"|"compet"|"cop"|"complet"|"concret"|"condon"|"contraven"|"conven"|"cran"|"delet"|"delineat"|"dop"|"drap"|"dron"|"escap"|"excit"|"fort"|"gap"|"gazett"|"grop"|"hon"|"hop"|"ignit"|"ignor"|"incit"|"interven"|"inton"|"invit"|"landscap"|"manoeuvr"|"nauseat"|"normalis"|"outmanoeuvr"|"overaw"|"permeat"|"persever"|"pip"|"por"|"postpon"|"prun"|"rap"|"recit"|"reshap"|"rop"|"shap"|"shor"|"snor"|"snip"|"ston"|"tap"|"wip"){ESEDING} { return(stemSemiReg(0,"e")); }
<verb,any>("ape"|"augur"|"belong"|"berth"|"burr"|"conquer"|"egg"|"forestall"|"froth"|"install"|"lacquer"|"martyr"|"mouth"|"murmur"|"pivot"|"preceed"|"prolong"|"purr"|"quell"|"recall"|"refill"|"remill"|"resell"|"retell"|"smooth"|"throng"|"twang"|"unearth"){EDING} { return(stemSemiReg(0,"")); }
<noun,any>(({A}*"metr")|({A}*"litr")|({A}+"ett")|"acr"|"Aussi"|"budgi"|"catastroph"|"centr"|"clich"|"commi"|"cooli"|"curi"|"demesn"|"employe"|"evacue"|"fibr"|"headach"|"hord"|"magpi"|"manoeuvr"|"moggi"|"moustach"|"movi"|"nighti"|"programm"|"queu"|"sabr"|"sorti"|"tast"|"theatr"|"timbr"|"titr"|"wiseacr")"es" { return(stem(1,"")); }
<noun,any>"burnurns" { return(stem(1,"")); }                  
<noun,any>"carriageways" { return(stem(1,"")); }              
<noun,any>"cills" { return(stem(1,"")); }                     
<noun,any>("umbrell"|"utopi")"as" { return(stem(1,"")); }     
<noun,any>(({A}+"itis")|"abdomen"|"acacia"|"achimenes"|"alibi"|"alkali"|"ammonia"|"amnesia"|"anaesthesia"|"anesthesia"|"aria"|"arris"|"asphyxia"|"aspidistra"|"aubrietia"|"axis"|"begonia"|"bias"|"bikini"|"cannula"|"canvas"|"chili"|"chinchilla"|"Christmas"|"cornucopia"|"cupola"|"cyclamen"|"diabetes"|"diphtheria"|"dysphagia"|"encyclopaedia"|"ennui"|"escallonia"|"ferris"|"flotilla"|"forsythia"|"ganglia"|"gas"|"gondola"|"grata"|"guerrilla"|"haemophilia"|"hysteria"|"inertia"|"insignia"|"iris"|"khaki"|"koala"|"lens"|"macaroni"|"manilla"|"mania"|"mantis"|"martini"|"matins"|"memorabilia"|"metropolis"|"moa"|"morphia"|"nostalgia"|"omen"|"pantometria"|"parabola"|"paraphernalia"|"pastis"|"patella"|"patens"|"pelvis"|"peninsula"|"phantasmagoria"|"pneumonia"|"polyuria"|"portcullis"|"pyrexia"|"regalia"|"safari"|"salami"|"sari"|"saturnalia"|"spaghetti"|"specimen"|"subtopia"|"suburbia"|"syphilis"|"taxi"|"toccata"|"trellis"|"tutti"|"umbrella"|"utopia"|"villa"|"zucchini") { return(stemNullC()); }
<noun,any>("acumen"|"Afrikaans"|"aphis"|"brethren"|"caries"|"confetti"|"contretemps"|"dais"|"debris"|"extremis"|"gallows"|"hors"|"hovis"|"hustings"|"innards"|"isosceles"|"maquis"|"minutiae"|"molasses"|"mortis"|"patois"|"pectoris"|"plumbites"|"series"|"tares"|"tennis"|"turps") { return(stemNullX()); }
<noun,any>("accoutrements"|"aerodynamics"|"aeronautics"|"aesthetics"|"algae"|"amends"|"annals"|"arrears"|"assizes"|"auspices"|"backwoods"|"bacteria"|"banns"|"battlements"|"bedclothes"|"belongings"|"billiards"|"binoculars"|"bitters"|"blandishments"|"bleachers"|"blinkers"|"blues"|"breeches"|"brussels"|"clothes"|"clutches"|"commons"|"confines"|"contents"|"credentials"|"crossbones"|"damages"|"dealings"|"dentures"|"depths"|"devotions"|"diggings"|"doings"|"downs"|"dues"|"dynamics"|"earnings"|"eatables"|"eaves"|"economics"|"electrodynamics"|"electronics"|"entrails"|"environs"|"equities"|"ethics"|"eugenics"|"filings"|"finances"|"folks"|"footlights"|"fumes"|"furnishings"|"genitals"|"glitterati"|"goggles"|"goods"|"grits"|"groceries"|"grounds"|"handcuffs"|"headquarters"|"histrionics"|"hostilities"|"humanities"|"hydraulics"|"hysterics"|"illuminations"|"italics"|"jeans"|"jitters"|"kinetics"|"knickers"|"latitudes"|"leggings"|"likes"|"linguistics"|"lodgings"|"loggerheads"|"mains"|"manners"|"mathematics"|"means"|"measles"|"media"|"memoirs"|"metaphysics"|"mockers"|"motions"|"multimedia"|"munitions"|"news"|"nutria"|"nylons"|"oats"|"odds"|"oils"|"oilskins"|"optics"|"orthodontics"|"outskirts"|"overalls"|"pants"|"pantaloons"|"papers"|"paras"|"paratroops"|"particulars"|"pediatrics"|"phonemics"|"phonetics"|"physics"|"pincers"|"plastics"|"politics"|"proceeds"|"proceedings"|"prospects"|"pyjamas"|"rations"|"ravages"|"refreshments"|"regards"|"reinforcements"|"remains"|"respects"|"returns"|"riches"|"rights"|"savings"|"scissors"|"seconds"|"semantics"|"shades"|"shallows"|"shambles"|"shorts"|"singles"|"slacks"|"specifics"|"spectacles"|"spoils"|"statics"|"statistics"|"summons"|"supplies"|"surroundings"|"suspenders"|"takings"|"teens"|"telecommunications"|"tenterhooks"|"thanks"|"theatricals"|"thermodynamics"|"tights"|"toils"|"trappings"|"travels"|"troops"|"tropics"|"trousers"|"tweeds"|"underpants"|"vapours"|"vicissitudes"|"vitals"|"wages"|"wanderings"|"wares"|"whereabouts"|"whites"|"winnings"|"withers"|"woollens"|"workings"|"writings"|"yes") { return(stemNullX()); }
<noun,any>("boati"|"bonhomi"|"clippi"|"creepi"|"deari"|"droppi"|"gendarmeri"|"girli"|"goali"|"haddi"|"kooki"|"kyri"|"lambi"|"lassi"|"mari"|"menageri"|"petti"|"reveri"|"snotti"|"sweeti")"es" { return(stem(1,"")); }
<verb,any>("buffet"|"plummet")"t"{EDING} { return(stemSemiReg(1,"")); }
<verb,any>"gunsling" { return(stemNullC()); }
<verb,any>"hamstring" { return(stemNullC()); }
<verb,any>"shred" { return(stemNullC()); }
<verb,any>"unfocuss"{ESEDING} { return(stemSemiReg(1,"")); }    
<verb,any>("accret"|"clon"|"deplet"|"dethron"|"dup"|"excret"|"expedit"|"extradit"|"fet"|"finetun"|"gor"|"hing"|"massacr"|"obsolet"|"reconven"|"recreat"|"recus"|"reignit"|"swip"|"videotap"|"zon"){ESEDING} { return(stemSemiReg(0,"e")); }
<verb,any>("backpedal"|"bankroll"|"bequeath"|"blackball"|"bottom"|"clang"|"debut"|"doctor"|"eyeball"|"factor"|"imperil"|"landfill"|"margin"|"multihull"|"occur"|"overbill"|"pilot"|"prong"|"pyramid"|"reinstall"|"relabel"|"remodel"|"snowball"|"socall"|"squirrel"|"stonewall"|"wrong"){EDING} { return(stemSemiReg(0,""));}	/* disprefer */
<noun,any>("beasti"|"browni"|"cach"|"cadr"|"calori"|"champagn"|"cologn"|"cooki"|"druggi"|"eateri"|"emigr"|"emigre"|"employe"|"freebi"|"genr"|"kiddi"|"massacr"|"mooni"|"neckti"|"nich"|"prairi"|"softi"|"toothpast"|"willi")"es" { return(stem(1,"")); }
<noun,any>(({A}*"phobia")|"accompli"|"aegis"|"alias"|"anorexia"|"anti"|"artemisia"|"ataxia"|"beatlemania"|"blini"|"cafeteria"|"capita"|"cola"|"coli"|"deli"|"dementia"|"downstairs"|"upstairs"|"dyslexia"|"jakes"|"dystopia"|"encyclopedia"|"estancia"|"euphoria"|"euthanasia"|"fracas"|"fuss"|"gala"|"gorilla"|"GI"|"habeas"|"haemophilia"|"hemophilia"|"hoopla"|"hula"|"impatiens"|"informatics"|"intelligentsia"|"jacuzzi"|"kiwi"|"mafia"|"magnolia"|"malaria"|"maquila"|"marginalia"|"megalomania"|"mercedes"|"militia"|"mufti"|"muni"|"olympics"|"pancreas"|"paranoia"|"pastoris"|"pastrami"|"pepperoni"|"pepsi"|"pi"|"piroghi"|"pizzeria"|"pneumocystis"|"potpourri"|"proboscis"|"rabies"|"reggae"|"regimen"|"rigatoni"|"salmonella"|"sarsaparilla"|"semen"|"ski"|"sonata"|"spatula"|"stats"|"subtilis"|"sushi"|"tachyarrhythmia"|"tachycardia"|"tequila"|"tetris"|"thrips"|"timpani"|"tsunami"|"vaccinia"|"vanilla") { return(stemNullC()); }
<noun,any>("acrobatics"|"athletics"|"basics"|"betters"|"bifocals"|"bowels"|"briefs"|"checkers"|"cognoscenti"|"denims"|"doldrums"|"dramatics"|"dungarees"|"ergonomics"|"genetics"|"gravitas"|"gymnastics"|"hackles"|"haves"|"hubris"|"ides"|"incidentals"|"ironworks"|"jinks"|"leavings"|"leftovers"|"logistics"|"makings"|"microelectronics"|"miniseries"|"mips"|"mores"|"oodles"|"pajamas"|"pampas"|"panties"|"payola"|"pickings"|"plainclothes"|"pliers"|"ravings"|"reparations"|"rudiments"|"scads"|"splits"|"stays"|"subtitles"|"sunglasss"|"sweepstakes"|"tatters"|"toiletries"|"tongs"|"trivia"|"tweezers"|"vibes"|"waterworks"|"woolens") { return(stemNullX()); }
<noun,any>("biggi"|"bourgeoisi"|"bri"|"camaraderi"|"chinoiseri"|"coteri"|"doggi"|"geni"|"hippi"|"junki"|"lingeri"|"moxi"|"preppi"|"rooki"|"yuppi")"es"  { return(stem(1,"")); }
<verb,any>("chor"|"sepulchr"|"silhouett"|"telescop"){ESEDING}  { return(stemSemiReg(0,"e")); }
<verb,any>("subpena"|"suds"){EDING} { return(stemSemiReg(0,"")); }
<noun,any>(({A}+"philia")|"fantasia"|"Feis"|"Gras"|"Mardi")  { return(stemNullC()); }
<noun,any>("calisthenics"|"heroics"|"rheumatics"|"victuals"|"wiles")  { return(stemNullX()); }
<noun,any>("aunti"|"anomi"|"coosi"|"quicki")"es" { return(stem(1,"")); }
<noun,any>("absentia"|"bourgeois"|"pecunia"|"Syntaxis"|"uncia")  { return(stemNullC()); }
<noun,any>("apologetics"|"goings"|"outdoors")  { return(stemNullX()); }
<noun,any>"collies"  { return(stem(1,"")); }                   
<verb,any>"imbed"  { return(stemNullC()); }
<verb,any>"precis"  { return(stemNullC()); }
<verb,any>"precis"{ESEDING} { return(stemSemiReg(0,"")); }      
<noun,any>("assagai"|"borzoi"|"calla"|"camellia"|"campanula"|"cantata"|"caravanserai"|"cedilla"|"cognomen"|"copula"|"corolla"|"cyclopaedia"|"dahlia"|"dhoti"|"dolmen"|"effendi"|"fibula"|"fistula"|"freesia"|"fuchsia"|"guerilla"|"hadji"|"hernia"|"houri"|"hymen"|"hyperbola"|"hypochondria"|"inamorata"|"kepi"|"kukri"|"mantilla"|"monomania"|"nebula"|"ovata"|"pergola"|"petunia"|"pharmacopoeia"|"phi"|"poinsettia"|"primula"|"rabbi"|"scapula"|"sequoia"|"sundae"|"tarantella"|"tarantula"|"tibia"|"tombola"|"topi"|"tortilla"|"uvula"|"viola"|"wisteria"|"zinnia")  { return(stemNullC()); }
<noun,any>("tibi"|"nebul"|"uvul")"ae"  { return(stem(1,"")); }	/* disprefer */
<noun,any>("arras"|"clitoris"|"muggins")"es" { return(stem(2,"")); }
<noun,any>("alms"|"biceps"|"calends"|"elevenses"|"eurhythmics"|"faeces"|"forceps"|"jimjams"|"jodhpurs"|"menses"|"secateurs"|"shears"|"smithereens"|"spermaceti"|"suds"|"trews"|"triceps"|"underclothes"|"undies"|"vermicelli")  { return(stemNullX()); }
<noun,any>("albumen"|"alopecia"|"ambergris"|"amblyopia"|"ambrosia"|"analgesia"|"aphasia"|"arras"|"asbestos"|"asia"|"assegai"|"astrophysics"|"aubrietia"|"aula"|"avoirdupois"|"beriberi"|"bitumen"|"broccoli"|"cadi"|"callisthenics"|"collywobbles"|"curia"|"cybernetics"|"cyclops"|"cyclopedia"|"dickens"|"dietetics"|"dipsomania"|"dyspepsia"|"epidermis"|"epiglottis"|"erysipelas"|"fascia"|"finis"|"fives"|"fleur-de-lis"|"geophysics"|"geriatrics"|"glottis"|"haggis"|"hara-kiri"|"herpes"|"hoop-la"|"ibis"|"insomnia"|"kleptomania"|"kohlrabi"|"kris"|"kumis"|"litchi"|"litotes"|"loggia"|"magnesia"|"man-at-arms"|"manila"|"marquis"|"master-at-arms"|"mattins"|"melancholia"|"minutia"|"muggins"|"mumps"|"mi"|"myopia"|"necropolis"|"neuralgia"|"nibs"|"numismatics"|"nymphomania"|"obstetrics"|"okapi"|"onomatopoeia"|"ophthalmia"|"paraplegia"|"patchouli"|"paterfamilias"|"penis"|"piccalilli"|"praxis"|"precis"|"prophylaxis"|"pyrites"|"raffia"|"revers"|"rickets"|"rounders"|"rubella"|"saki"|"salvia"|"sassafras"|"sawbones"|"scabies"|"schnapps"|"scintilla"|"scrofula"|"sepia"|"stamen"|"si"|"swami"|"testis"|"therapeutics"|"tiddlywinks"|"verdigris"|"wadi"|"wapiti"|"yogi")  { return(stemNullC()); }
<noun,any>("aeri"|"birdi"|"bogi"|"caddi"|"cock-a-leeki"|"colli"|"corri"|"cowri"|"dixi"|"eyri"|"faeri"|"gaucheri"|"gilli"|"knobkerri"|"laddi"|"mashi"|"meali"|"menageri"|"organdi"|"patisseri"|"pinki"|"pixi"|"stymi"|"talki")"es" { return(stem(1,"")); }
<noun,any>"humans"                  { return(stem(1,"")); }   
<noun,any>"slums"                   { return(stem(1,"")); }
<verb,any>(({A}*"-us")|"abus"|"accus"|"amus"|"arous"|"bemus"|"carous"|"contus"|"disabus"|"disus"|"dous"|"enthus"|"excus"|"grous"|"misus"|"mus"|"overus"|"perus"|"reus"|"rous"|"sous"|"us"|({A}*[hlmp]"ous")|({A}*[af]"us")){ESEDING} { return(stemSemiReg(0,"e")); }
<noun,any>(({A}*"-abus")|({A}*"-us")|"abus"|"burnous"|"cayus"|"chanteus"|"chartreus"|"chauffeus"|"crus"|"disus"|"excus"|"grous"|"hypotenus"|"masseus"|"misus"|"mus"|"Ous"|"overus"|"poseus"|"reclus"|"reus"|"rus"|"us"|({A}*[hlmp]"ous")|({A}*[af]"us"))"es" { return(stem(1,"")); }
<noun,any>("ablutions"|"adenoids"|"aerobatics"|"afters"|"astronautics"|"atmospherics"|"bagpipes"|"ballistics"|"bell-bottoms"|"belles-lettres"|"blinders"|"bloomers"|"butterfingers"|"buttocks"|"bygones"|"cahoots"|"castanets"|"clappers"|"dodgems"|"dregs"|"duckboards"|"edibles"|"eurythmics"|"externals"|"extortions"|"falsies"|"fisticuffs"|"fleshings"|"fleur-de-lys"|"fours"|"gentleman-at-arms"|"geopolitics"|"giblets"|"gleanings"|"handlebars"|"heartstrings"|"homiletics"|"housetops"|"hunkers"|"hydroponics"|"kalends"|"knickerbockers"|"lees"|"lei"|"lieder"|"literati"|"loins"|"meanderings"|"meths"|"muniments"|"necessaries"|"nines"|"ninepins"|"nippers"|"nuptials"|"orthopaedics"|"paediatrics"|"phonics"|"polemics"|"pontificals"|"prelims"|"pyrotechnics"|"ravioli"|"rompers"|"ructions"|"scampi"|"scrapings"|"serjeant-at-arms"|"shires"|"smalls"|"steelworks"|"sweepings"|"vespers"|"virginals"|"waxworks") { return(stemNullX()); }
<noun,any>("cannabis"|"corgi"|"envoi"|"hi-fi"|"kwela"|"lexis"|"muesli"|"sheila"|"ti"|"yeti") { return(stemNullC()); }

<noun,any>("mounti"|"brasseri"|"granni"|"koppi"|"rotisseri")"es" { return(stem(1,"")); }

<noun,any>"cantharis"    { return(stem(1,"de")); }
<noun,any>"chamois"      { return(stem(1,"x")); }
<noun,any>"submatrices"  { return(stem(3,"x")); }
<noun,any>"mafiosi"      { return(stem(1,"o")); }
<noun,any>"pleura"       { return(stem(1,"on")); } 
<noun,any>"vasa"         { return(stem(1,"")); } 
<noun,any>"antipasti"    { return(stem(1,"o")); }

  /* -o / -oe */

<verb,any>("bastinado"|"bunco"|"bunko"|"carbonado"|"contango"|"crescendo"|"ditto"|"echo"|"embargo"|"fresco"|"hallo"|"halo"|"lasso"|"niello"|"radio"|"solo"|"stiletto"|"stucco"|"tally-ho"|"tango"|"torpedo"|"veto"|"zero")"ed"  { return(stem(2,"")); }	/* en */
<verb,any>"ko'd"  { return(stem(3,"o")); }	/* en */
<verb,any>"ko'ing"  { return(stem(4,"")); }        
<verb,any>"ko's"  { return(stem(2,"")); }            
<verb,any>"tally-ho'd"  { return(stem(3,"")); }	/* disprefer */
<noun,any>("co"|"do"|"ko"|"no")"'s"   { return(stem(2,"")); }

<noun,any>("aloe"|"archfoe"|"canoe"|"doe"|"felloe"|"floe"|"foe"|"hammertoe"|"hoe"|"icefloe"|"mistletoe"|"oboe"|"roe"|({A}*"shoe")|"sloe"|"throe"|"tiptoe"|"toe"|"voe"|"woe")"s"  { return(stem(1,"")); }
<verb,any>("canoe"|"hoe"|"outwoe"|"rehoe"|({A}*"shoe")|"tiptoe"|"toe")"s"  { return(stem(1,"")); }

<noun,any>("tornedos"|"throes")  { return(stemNullX()); }

  /* redundant in analysis; but in generation e.g. buffalo+s -> buffaloes */

<noun,any>("antihero"|"buffalo"|"dingo"|"domino"|"echo"|"go"|"grotto"|"hero"|"innuendo"|"mango"|"mato"|"mosquito"|"mulatto"|"potato"|"peccadillo"|"pentomino"|"superhero"|"tomato"|"tornado"|"torpedo"|"veto"|"volcano")"es" { return(stem(2,"")); }
<verb,any>("echo"|"forego"|"forgo"|"go"|"outdo"|"overdo"|"redo"|"torpedo"|"undergo"|"undo"|"veto")"es"  { return(stem(2,"")); }            

  /* -os / -oses */

<noun,any>("bathos"|"cross-purposes"|"kudos")  { return(stemNullX()); }
<noun,any>"cos"                                { return(stemNullC()); }

<noun,any>("chaos"|"cosmos"|"ethos"|"parados"|"pathos"|"rhinoceros"|"tripos"|"thermos"|"OS"|"reredos") { return(stemNullC()); }
<noun,any>("chaos"|"cosmos"|"ethos"|"parados"|"pathos"|"rhinoceros"|"tripos"|"thermos"|"OS"|"reredos")"es"  { return(stem(2,"")); }

<noun,any>("anastomos"|"apotheos"|"arterioscleros"|"asbestos"|"cellulos"|"dermatos"|"diagnos"|"diverticulos"|"exostos"|"hemicellulos"|"histocytos"|"hypnos"|"meios"|"metamorphos"|"metempsychos"|"mitos"|"neuros"|"prognos"|"psychos"|"salmonellos"|"symbios"|"scleros"|"stenos"|"symbios"|"synchondros"|"treponematos"|"zoonos")"es"  { return(stem(2,"is")); }     

<noun,any>"pharoses"   { return(stem(4,"isee")); }	/* disprefer */

  /* -zes */

<noun,any>("adze"|"bronze")"s"        { return(stem(1,"")); } 
<noun,any>("fez"|"quiz")"zes"         { return(stem(3,"")); }
<noun,any>("fez"|"quiz")"es"          { return(stem(2,"")); }	/* disprefer */
<verb,any>("adz"|"bronz"){ESEDING}    { return(stemSemiReg(0,"e")); }         
<verb,any>("quiz"|"whiz")"z"{ESEDING} { return(stemSemiReg(1,"")); }
<verb,any>("quiz"|"whiz"){ESEDING} { return(stemSemiReg(0,""));}	/* disprefer */

<verb,noun,any>{A}+"uses"                { return(stem(2,"")); }
<verb,any>{A}+"used"                     { return(stem(2,"")); }	/* en */
<verb,any>{A}+"using"                    { return(stem(3,"")); }

<noun,any>"pp." { return(stem(2,".")); }             
<noun,any>"m.p.s." { return(stem(6,"m.p.")); }       
<noun,any>("cons"|"miss"|"mrs"|"ms"|"n-s"|"pres"|"ss")"." { return(stemNullC()); }
<noun,any>({A}|".")+".s."                { return(stemNullC()); }
<noun,any>({A}|".")+".'s."               { return(stem(4,".")); }	/* disprefer */
<noun,any>({A}|".")+"s."                 { return(stem(2,"")); }	/* Changed by Natt och Dag */

<noun,any>{A}*"men"                 { return(stem(2,"an")); }
<noun,any>{A}*"wives"               { return(stem(3,"fe")); } 
<noun,any>{A}+"zoa"                 { return(stem(1,"on")); }
<noun,any>{A}+"iia"                 { return(stem(2,"um")); }	/* disprefer */
<noun,any>{A}+"e"[mn]"ia"           { return(stemNullC()); }
<noun,any>{A}+"ia"                  { return(stem(1,"um")); }	/* disprefer */
<noun,any>{A}+"la"                  { return(stem(1,"um")); }
<noun,any>{A}+"i"                   { return(stem(1,"us")); }	/* disprefer */
<noun,any>{A}+"ae"                  { return(stem(2,"a")); }	/* disprefer */
<noun,any>{A}+"ata"                 { return(stem(3,"a")); }	/* disprefer */

<verb,noun,any>("his"|"hers"|"theirs"|"ours"|"yours"|"as"|"its"|"this"|"during"|"something"|"nothing"|"anything"|"everything") { return(stemNullC()); }
<verb,noun,any>{A}*("us"|"ss"|"sis"|"eed") { return(stemNullC()); }
<verb,noun,any>{A}*{V}"ses"            { return(stem(1,"")); }
<verb,noun,any>{A}+{CXY}"zes"          { return(stem(2,"")); }
<verb,noun,any>{A}*{VY}"zes"           { return(stem(1,"")); }
<verb,noun,any>{A}+{S2}"es"            { return(stem(2,"")); }
<verb,noun,any>{A}+{V}"rses"           { return(stem(1,"")); }
<verb,noun,any>{A}+"onses"             { return(stem(1,"")); }
<verb,noun,any>{A}+{S}"es"             { return(stem(2,"")); }
<verb,noun,any>{A}+"thes"              { return(stem(1,"")); }
<verb,noun,any>{A}+{CXY}[cglsv]"es"    { return(stem(1,"")); }
<verb,noun,any>{A}+"ettes"             { return(stem(1,"")); }
<verb,noun,any>{A}+{C}"ies"            { return(stem(3,"y")); }
<verb,noun,any>{A}*{CXY}"oes"          { return(stem(2,"")); }	/* disprefer */
<verb,noun,any>{A}+"'s"                { return(stem(2,"")); }  /* Added by Natt och Dag */
<verb,noun,any>{A}+"s"                 { return(stem(1,"")); }

<verb,any>{A}+{CXY}"zed"          { return(stem(2,"")); }	/* en */
<verb,any>{A}*{VY}"zed"           { return(stem(1,"")); }	/* en */
<verb,any>{A}+{S2}"ed"            { return(stem(2,"")); }	/* en */
<verb,any>{A}+{CXY}"zing"         { return(stem(3,"")); }
<verb,any>{A}*{VY}"zing"          { return(stem(3,"e")); }
<verb,any>{A}+{S2}"ing"           { return(stem(3,"")); } 
<verb,any>{C}+{V}"lled"           { return(stem(2,"")); }	/* en */
<verb,any>{C}+{V}"lling"          { return(stem(3,"")); } 
<verb,any>{A}*{C}{V}{CXY2}"ed"    { return(stemConDub(2,"")); }	/* en */
<verb,any>{A}*{C}{V}{CXY2}"ing"   { return(stemConDub(3,"")); }

<verb,any>{CXY}+"ed"                { return(stemNullC()); }
<verb,any>{PRE}*{C}{V}"nged"        { return(stem(2,"")); }	/* en */
<verb,any>{A}+"icked"               { return(stem(2,"")); }	/* en */
<verb,any>{A}*{C}"ined"             { return(stem(2,"e")); }	/* en */
<verb,any>{A}*{C}{V}[npwx]"ed"      { return(stem(2,"")); }	/* disprefer */
<verb,any>{PRE}*{C}+"ored"          { return(stem(2,"e")); }	/* en */
<verb,any>{A}+"ctored"              { return(stem(2,"")); }	/* disprefer */
<verb,any>{A}*{C}[clnt]"ored"       { return(stem(2,"e")); }	/* en */
<verb,any>{A}+[eo]"red"             { return(stem(2,"")); }	/* en */
<verb,any>{A}+{C}"ied"              { return(stem(3,"y")); }	/* en */
<verb,any>{A}*"qu"{V}{C}"ed"        { return(stem(2,"e")); }	/* en */
<verb,any>{A}+"u"{V}"ded"           { return(stem(2,"e")); }	/* en */
<verb,any>{A}*{C}"leted"            { return(stem(2,"e")); }	/* en */
<verb,any>{PRE}*{C}+[ei]"ted"       { return(stem(2,"e")); }	/* en */
<verb,any>{A}+[ei]"ted"             { return(stem(2,"")); }	/* en */
<verb,any>{PRE}({CXY}{2})"eated"    { return(stem(2,"")); }	/* en */
<verb,any>{A}*{V}({CXY}{2})"eated"  { return(stem(2,"e")); }	/* en */
<verb,any>{A}+[eo]"ated"            { return(stem(2,"")); }	/* en */
<verb,any>{A}+{V}"ated"             { return(stem(2,"e")); }	/* en */
<verb,any>{A}*({V}{2})[cgsv]"ed"    { return(stem(2,"e")); }	/* en */
<verb,any>{A}*({V}{2}){C}"ed"       { return(stem(2,"")); }	/* en */
<verb,any>{A}+[rw]"led"             { return(stem(2,"")); }	/* en */
<verb,any>{A}+"thed"                { return(stem(2,"e")); }	/* en */
<verb,any>{A}+"ued"                 { return(stem(2,"e")); }	/* en */
<verb,any>{A}+{CXY}[cglsv]"ed"      { return(stem(2,"e")); }	/* en */
<verb,any>{A}+({CXY}{2})"ed"        { return(stem(2,"")); }	/* en */
<verb,any>{A}+({VY}{2})"ed"         { return(stem(2,"")); }	/* en */
<verb,any>{A}+"ed"                  { return(stem(2,"e")); }	/* en */

<verb,any>{CXY}+"ing"                  { return(stemNullC()); }
<verb,any>{PRE}*{C}{V}"nging"          { return(stem(3,"")); } 
<verb,any>{A}+"icking"                 { return(stem(3,"")); } 
<verb,any>{A}*{C}"ining"               { return(stem(3,"e")); }
<verb,any>{A}*{C}{V}[npwx]"ing"        { return(stem(3,"")); }	/* disprefer */
<verb,any>{A}*"qu"{V}{C}"ing"          { return(stem(3,"e")); }
<verb,any>{A}+"u"{V}"ding"             { return(stem(3,"e")); }
<verb,any>{A}*{C}"leting"              { return(stem(3,"e")); }
<verb,any>{PRE}*{C}+[ei]"ting"         { return(stem(3,"e")); }
<verb,any>{A}+[ei]"ting"               { return(stem(3,"")); } 
<verb,any>{A}*{PRE}({CXY}{2})"eating"  { return(stem(3,"")); }
<verb,any>{A}*{V}({CXY}{2})"eating"    { return(stem(3,"e")); }
<verb,any>{A}+[eo]"ating"              { return(stem(3,"")); } 
<verb,any>{A}+{V}"ating"               { return(stem(3,"e")); }
<verb,any>{A}*({V}{2})[cgsv]"ing"      { return(stem(3,"e")); }
<verb,any>{A}*({V}{2}){C}"ing"         { return(stem(3,"")); } 
<verb,any>{A}+[rw]"ling"               { return(stem(3,"")); } 
<verb,any>{A}+"thing"                  { return(stem(3,"e")); }
<verb,any>{A}+{CXY}[cglsv]"ing"        { return(stem(3,"e")); }
<verb,any>{A}+({CXY}{2})"ing"          { return(stem(3,"")); } 
<verb,any>{A}+"uing"                   { return(stem(3,"e")); }
<verb,any>{A}+({VY}{2})"ing"           { return(stem(3,"")); } 
<verb,any>{A}+"ying"                   { return(stem(3,"")); } 
<verb,any>{A}*{CXY}"oing"              { return(stem(3,"")); } 
<verb,any>{PRE}*{C}+"oring"            { return(stem(3,"e")); }
<verb,any>{A}+"ctoring"                { return(stem(3,"")); }	/* disprefer */

<verb,any>{A}*{C}[clt]"oring"          { return(stem(3,"e")); }
<verb,any>{A}+[eo]"ring"               { return(stem(3,"")); } 
<verb,any>{A}+"ing"                    { return(stem(3,"e")); }

/* Take care of the remaining text that do not comprise accepted stems */

<any>{WORD}                  { return stopWord(yytext()); } /* Any other word */
<any>{NUMTOKEN}              {  } /* Remove numbers and any token that starts with a number  */
<any>{TOKEN}                 { return yytext().toLowerCase(); } /* Any other acceptable token */
<any>{SKIP}                  {  } /* Anything else will be removed */

}
