package eu.riscoss.rdc;

/**
 * @author Mirko Morandini
 */

import java.util.Collection;

public class LicenseEntry {
	public int count;
	private final String name;
	public String licensetype="unknown";

	public LicenseEntry(int c, String n) {
		count=c;
		name=n;
	}
	public LicenseEntry matches(String s){
		if (name.contains(s))
			return this;
		return null;
	}
	
	private LicenseEntry startsWith(String s){
		if (name.startsWith(s))
			return this;
		return null;
	}
	
	/**
	 * For matching fossology license strings
	 * @param si list of licenses for a licenseType
	 * @param licenseType e.g. copyleft, permissive, commercial
	 * @return
	 */
	public boolean matchesOneOf(Collection<String> si, String licenseType){
		for (String string : si) {
			if (startsWith(string)!=null){
				this.licensetype=licenseType;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * For matching Maven license strings - per word matching
	 * @param si list of licenses for a licenseType
	 * @param licenseType e.g. copyleft, permissive, commercial
	 * @return
	 */
	public boolean matchesOneOf_Maven(Collection<String> si, String licenseType){
		for (String string : si) {
			if (containsWord(string)!=null){
				this.licensetype=licenseType;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * returns the first word of the stored name, that matches a license or null if none
	 * @param s the license
	 */
	private String containsWord(String s) {
		String[] wordList = name.trim().split(" ");
		for (String word : wordList) {
			if (word.trim() != null)
				if (word.trim().equalsIgnoreCase(s.trim()))
					return word;
		}
		return null;
	}
	
	@Override
	public String toString(){
		return count+" \t"+name+" \t\tType "+licensetype;
	}
	
	public String getName() {
		return name;
	}
}
