/**
 * 
 */
package com.ca.apm.systemtest.fld.common.files;

/**
 * This class represents a line point in file where new text should be inserted. 
 * 
 * @author KEYJA01
 * @author Alexander Sinyushkin (sinal04@ca.com)
 */
public final class InsertPoint {
    public enum Location {
        Before, After, EndOfFile
    }
    
    private Location location;
    private String searchText;
    
    private InsertPoint(Location location, String searchText) {
        this.location = location;
        this.searchText = searchText;
    }
    
    public static InsertPoint before(String searchText) {
        return new InsertPoint(Location.Before, searchText);
    }
    
    public static InsertPoint after(String searchText) {
        return new InsertPoint(Location.After, searchText);
    }

    /**
     * Creates insert point for appending to the end of the file, 
     * i.e. {@link InsertPoint#getLocation() getLocation()} is {@link Location#EndOfFile}.
     * 
     * @return
     */
    public static InsertPoint endOfFile() {
        return new InsertPoint(Location.EndOfFile, null);
    }
    
    public Location getLocation() {
        return location;
    }

    public String getSearchText() {
        return searchText;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "InsertPoint [location=" + location + ", searchText=" + searchText + "]";
    }
    
    
}
