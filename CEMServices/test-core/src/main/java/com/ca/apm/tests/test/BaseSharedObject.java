package com.ca.apm.tests.test;

import com.ca.apm.tests.utility.WebdriverWrapper;
import com.gargoylesoftware.htmlunit.WebClient;
import org.openqa.selenium.WebDriver;
import org.testng.TestException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BaseSharedObject extends JBaseTest{
protected String gridBaseLocator = getORPropValue("grid.base.locator");
    
    public final String gridHeaderCheckAllCheckbox = getORPropValue("grid.header.checkbox");
    protected final String gridHeaderLocator = getORPropValue("grid.header.locator");
    protected final String gridHeaderCellLocator = getORPropValue("grid.header.cellLocator");
    
    protected final String gridBodyLocator = "']/tbody/tr[";
    protected final String gridNameCellLocator = "]/td[2]";
    
    //Other common properties
    public static final String classErrorValue = "error";
    public static final String classNonErrorLableValue = "nonerror_label";
    public static final String classNonErrorValue = "data";
    public static final String classErrorLabelValue= "error_label";
    //timeout and sleep waits
    public static Long wait = 500L;
    
    public final String messagesDetail = getORPropValue("messagesDiv") + "/div/table/tbody/tr/td[3]";
    
	public BaseSharedObject(){
        //empty constructor
    }
    
    public BaseSharedObject(WebDriver driver){
        this.driver = driver;
    }
   
    /**
     * Return the pId parameter for a row of the grid based on the name in the row
     * @param grid
     * @param name
     * @return
     */
    public String getPIdForName(String grid, String name)throws Exception{
        String href = getAttributeFromNameCell(grid, name, "href");
        String [] parts = href.split("\\?");
        //toss things to the left of the?
        String query = parts[1];
        //parse out value to use to check the box
        String [] params = query.split("&");
        HashMap<String, String> map = new HashMap<String, String>();  
        for (String param : params){  
            String key = param.split("=")[0];  
            String value = param.split("=")[1];  
            map.put(key, value);  
        }  
        return map.get("pId");
    }
    
        /**
     * Return the pId parameter for a row of the grid based on the name in the row
     * @param grid
     * @param row
     * @param col
     * @return
     */
    public String getPIdForRowAndCol(String grid, int row, int col){
        try{
        String href = WebdriverWrapper.getAttribute(driver, getORPropValue("xpath")+"//table[@id='"+grid+"']/tbody/tr["+row+"]/td["+col+"]/a@href", "value");
        String [] parts = href.split("\\?");
        //toss things to the left of the?
        String query = parts[1];
        //parse out value to use to check the box
        String [] params = query.split("&");
        HashMap<String, String> map = new HashMap<String, String>();  
        for (String param : params){  
            String key = param.split("=")[0];  
            String value = param.split("=")[1];  
            map.put(key, value);  
        }  
        return map.get("pId");
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    
    /**
     * Checks to see if the desired grid is visible
     * @param grid
     * @return
     * @throws Exception 
     */
    public Boolean isGridPresent(String grid) throws Exception{
        String gridLocator = getORPropValue("grid.base.locator")+grid+getORPropValue("grid.header.locator")+"1"+getORPropValue("grid.header.cellLocator");
        return WebdriverWrapper.isObjectPresent(driver, gridLocator);
    }
    
    public boolean isClassErrorLabelElementPresent() throws Exception {
        return WebdriverWrapper.isObjectPresent(driver,classErrorLabelValue);         
    }
    
    /**
     * Click checkbox in header
     * @param grid
     * @throws Exception 
     */
    public void checkAllGridRows(String grid) throws Exception{
        WebdriverWrapper.selectCheckBox(driver, gridHeaderCheckAllCheckbox);
    }
    /**
     * Uncheck header checkbox
     * @param grid
     * @throws Exception 
     */
    public void uncheckAllGridRows(String grid) throws Exception{
        WebdriverWrapper.deselectCheckBox(driver, gridHeaderCheckAllCheckbox);
    }
    
    /**
     * Pass column number and the content of the cell this will return the first row number containing the text
     * @param grid
     * @param content
     * @param col
     * @return
     * @throws Exception 
     */
    public Integer getRowNumByContentAndColumn(String grid, String content, Integer col) throws Exception{
        Integer row=1;
        boolean found=false;
        String gridLocator = gridBaseLocator+grid+gridBodyLocator;
        String colLocator = "]/td["+col.toString()+"]";
        while (WebdriverWrapper.isObjectPresent(driver,gridLocator+row+colLocator)){
            if (WebdriverWrapper.verifyTextPresent(driver,gridLocator+row+colLocator,content)){
                found = true;
                break;
            }
            row++;
        }
        
        if (found){ 
            return row;
        }else{
            return 0;
        }
        
    }
    
    /**
     * Pass column number and the content of the cell this will return the first row number containing the text
     * @param grid
     * @param content
     * @param colTitle
     * @return
     * @throws Exception 
     */
    public Integer getRowNumByContentAndColTitle(String grid, String content, String colTitle) throws Exception{
        Integer col = getColNumByColTitle(grid, colTitle);
        Integer row = getRowNumByContentAndColumn(grid, content, col);
        return row;
    }
    
    /**
     * Scans table header for desired column and returns column number
     * @param grid
     * @param title
     * @return column number starting from 1, 0 if not found
     * @throws Exception 
     */
    public Integer getColNumByColTitle(String grid, String title) throws Exception{
        Integer col = 1;
        boolean found = false;
        
        String gridLocator = gridBaseLocator+grid+gridHeaderLocator;
        while (WebdriverWrapper.isObjectPresent(driver, gridLocator+col+gridHeaderCellLocator)){
            if(WebdriverWrapper.verifyTextPresent(driver,gridLocator+col+gridHeaderCellLocator,title))
            {
                found=true;
                break;
            }
            col++;
        }
        if (found){ 
            return col;
        }else{
            return 0;
        }
    }
    
    /**
     * This method will return the row number that has the passed content in specified column
     * @param grid
     * @param content
     * @param col
     * @return
     */
    public ArrayList<Integer> getRowNumsByContentAndColumn(String grid, String content, Integer col)throws Exception{
        
        ArrayList<Integer> rowIds=new ArrayList<Integer>();
        Integer row=1;
        String gridLocator = gridBaseLocator+grid+gridBodyLocator;
        String colLocator = "]/td["+col.toString()+"]";
        while (WebdriverWrapper.isObjectPresent(driver, gridLocator+row+colLocator)){
            if ((WebdriverWrapper.getElementText(driver,gridLocator+row+colLocator)).equals(content)){
                rowIds.add(row);
            }
            row++;
        }
                
            return rowIds;      
    }
    
    /**
     * This method will check the check box for the passed grid and name col value
     * @param grid the type of the grid
     * @param name 
     */
    public void checkGridRow(String grid, String name) throws Exception{
        String value = getPIdForName(grid, name);

        if(!WebdriverWrapper.isElementSelected(driver, getORPropValue("xpath")+"//input[@value='"+value+"']"))
        {
            WebdriverWrapper.selectCheckBox(driver, getORPropValue("xpath")+"//input[@value='"+value+"']");
        }
        
    }
    
    /**
     * Pass in the value of the name cell and will uncheck that row
     * @param grid
     * @param name
     */
    public void uncheckGridRow(String grid, String name)throws Exception{
        String value = getPIdForName(grid, name);
        if (WebdriverWrapper.isElementSelected(driver, getORPropValue("xpath")+"//input[@value='"+value+"']")){
            WebdriverWrapper.selectCheckBox(driver,getORPropValue("xpath")+"//input[@value='"+value+"']");
        }
    }
    
    /**
     * Returns a String containing the text of the Application table cell bases on application name and column title
     * @param grid - the grid to get the data from
     * @param name - the name of the application
     * @param colTitle - the title of the column (ie. synchronized or domainConfigurationStatus) this string is used as a key in the applicationColumnNameLocation hash map
     * @return String containing cell text
     */
    public String getGridCellValue(String grid, String name, String colTitle )throws Exception{
        String gridLocator = gridBaseLocator+grid+gridBodyLocator;
        int row = getTableRowForName(grid, name);
        int col = getColNumByColTitle(grid, colTitle);
        if (row==0){
            return "not found";
        }
        return WebdriverWrapper.getElementText(driver, gridLocator+row+"]/td["+col+"]");
        //return sel.getText(gridLocator+row+"]/td["+col+"]");
    }
    /**
     * @param grid the grid to get the data from
     * @param name String that equals column name
     * @param attribute This should represent the html attribute with leading slash and @
     *                  i.e. /@href
     * @return String value of attribute
     */
    public String getAttributeFromNameCell(String grid, String name, String attribute)throws Exception{
        int row = getTableRowForName(grid, name);
        String gridLocator = gridBaseLocator+grid+gridBodyLocator;
        //return WebdriverWrapper.getAttribute(driver,gridLocator+row+gridNameCellLocator+"/a"+attribute , attribute);
        return WebdriverWrapper.getAttribute(driver,gridLocator+row+gridNameCellLocator+"/a" , attribute);
        //return WebdriverWrapper.getAttribute(driver,gridLocator+row+gridNameCellLocator+"/a", attribute);
       // System.out.println("Driver Method " + driver.findElement(By.xpath("//table[@id='tranDefGroup']/tbody/tr[2]/td[2]")).getText());
       // System.out.println("Driver Method " + driver.findElement(By.xpath("//table[@id='tranDefGroup']/tbody/tr[2]/td[2]/a")).getText());
       // System.out.println("Driver Method " + driver.findElement(By.xpath("//table[@id='tranDefGroup']/tbody/tr[2]/td[2]/a")).getAttribute("href"));
       // return WebdriverWrapper.getAttribute(driver,gridLocator+row+gridNameCellLocator, attribute);
       // return driver.findElement(By.xpath("//table[@id='tranDefGroup']/tbody/tr[2]/td[2]/a")).getAttribute("href");
    }
    /**
     * Pass row number and column number to get back the value of the attribute
     * @param grid
     * @param row
     * @param col
     * @param attribute (like /@href..., /@value, or whatever from the type of input in the td, 
     *         ie. the value of an input in a table cell is obtained by passing '/input/@value' or /a/@href or)
     * @return
     */
    public String getAttributeFromCell(String grid, Integer row, Integer col, String attribute)throws Exception{
        
            String gridLocator = gridBaseLocator+grid+gridBodyLocator;
            //String cellLocator = "]";
            //return WebdriverWrapper.getAttribute(driver, gridLocator+row+"]/td["+col+cellLocator+"/a"+attribute, attribute);
            //return WebdriverWrapper.getAttribute(driver,gridLocator+row+gridNameCellLocator+"/a", attribute);
            String[] attributes = attribute.split("@",2);
            int index=attributes[0].lastIndexOf('/');
            if(index!=-1)
            attributes[0]=attributes[0].substring(0, index);
            System.out.println("Getting attribute "+attributes[1]+" for this cell: "+gridLocator+row+"]/td["+col+"]"+attributes[0]);
            return WebdriverWrapper.getAttribute(driver,gridLocator+row+"]/td["+col+"]"+attributes[0], attributes[1]);
    }
    
    /**
     * Pass row number and column title to get back the value of the attribute
     * @param grid
     * @param row
     * @param colTitle
     * @param attribute (like /@href..., /@value
     * @return
     */
    public String getAttributeFromCell(String grid, Integer row, String colTitle, String attribute) throws Exception{
        Integer col = getColNumByColTitle(grid, colTitle);
        return getAttributeFromCell(grid, row, col, attribute);
    }
    
    /**
     * Pass row content, column title and get back the value of the attribute
     * @param grid
     * @param rowContent
     * @param colTitle
     * @param attribute (like /@href..., /@value
     * @return
     */
    public String getAttributeFromCell(String grid, String rowContent, String colTitle, String attribute)throws Exception{
        Integer col = getColNumByColTitle(grid, colTitle);
        Integer row = getRowNumByContentAndColumn(grid, rowContent, col);
        return getAttributeFromCell(grid, row, col, attribute);
    }
    
    /**
     * Pass row content, column title and get back the value of the attribute
     * @param grid
     * @param rowContentCol
     * @param rowContent
     * @param colTitle
     * @param attribute (like /@href..., /@value
     * @return
     */
    public String getAttributeFromCell(String grid,  String rowContentCol, String rowContent, String colTitle, String attribute)throws Exception{
        Integer col = getColNumByColTitle(grid, colTitle);
        Integer rowContentcol = getColNumByColTitle(grid, rowContentCol);
        Integer row = getRowNumByContentAndColumn(grid, rowContent, rowContentcol);
        return getAttributeFromCell(grid, row, col, attribute);
    }
    
    
    /**
     * Scans applications table and finds the row that corresponds to the application name
     * @param grid - the grid to get the data from
     * @param name - name from the name column to look for
     * @return - int row number, 0 indicates not found
     */
    public int getTableRowForName(String grid, String name)throws Exception{
        return(getRowNumByContentAndColumn(grid, name, 2));
    }
    /**
     * Pass column number and the content of the cell this will return the first row number containing the text
     * @param grid
     * @param colTH1 
     * @param colValue1
     * @param colTH2 
     * @param colValue2
     * @return
     */
    public Integer getRowNumBy2ColNameValue(String grid, String colTH1, String colValue1, String colTH2, String colValue2)throws Exception{
        int totalRow = 0;
        totalRow = getRowCountFromTable(grid);
        if (totalRow==0) {
            return 0;
        }
        for (int i=1; i<=totalRow; i++ ) {
            if (getCellContentsByRowAndCol(grid, i, colTH1).equalsIgnoreCase(colValue1) && getCellContentsByRowAndCol(grid, i, colTH2).equalsIgnoreCase(colValue2)) {
                return i;
            }
        }
        return 0;   
    }
    /**
     * Main table getter, most others should ultimately call this one, uses Integers for row and column
     * @param grid
     * @param row
     * @param col
     * @return String that is in that table cell, 'not found' if cell is not present
     */
    public String getCellContentsByRowAndCol(String grid, Integer row, Integer col) throws Exception{
        String gridLocator = gridBaseLocator+grid+gridBodyLocator;
        
        if (row.equals(0) || col.equals(0)){
            return "not found";
        }
        
        
        if(!WebdriverWrapper.isObjectPresent(driver, gridLocator+row+"]/td["+col+"]")){ 
        return "not found";
        } 
        
        
        return WebdriverWrapper.getElementText(driver, gridLocator+row+"]/td["+col+"]");
    }
    /**
     * Pass column title and row number 
     * @param grid
     * @param row
     * @param colTitle
     * @return
     */
    public String getCellContentsByRowAndCol(String grid, Integer row, String colTitle)throws Exception{
        Integer colNum = getColNumByColTitle(grid, colTitle);
        return getCellContentsByRowAndCol(grid, row, colNum);
    }
    
    
    /**
     * Pass column title and the row contents of the name column  
     * @param grid
     * @param rowContentInNameCol
     * @param colTitle
     * @return
     */
    public String getCellContentsByRowAndCol(String grid, String rowContentInNameCol, String colTitle) throws Exception{
        Integer colNum = getColNumByColTitle(grid, colTitle);
        Integer nameColNumber = getColNumByColTitle(grid, "Name");
        Integer rowNum = getRowNumByContentAndColumn(grid, rowContentInNameCol, nameColNumber);
        return getCellContentsByRowAndCol(grid, rowNum, colNum);
    }
    
    /**
     * Tell me how many rows a table has
     * @param grid
     * @return
     */
    public Integer getRowCountFromTable(String grid) throws Exception{
        Integer rows = 0;
        Integer nextRow = rows+1;
        String gridLocator = gridBaseLocator+grid+gridBodyLocator;
        while(WebdriverWrapper.isObjectPresent(driver, gridLocator+nextRow+gridNameCellLocator)){
        
            rows++;
            nextRow++;
        }
        return rows;
    }
    /**
     * Return the messageDiv string if visible, otherwise return empty string
     * @return
     */
    public String getMessageFromErrorDiv() throws Exception{
        if(WebdriverWrapper.isObjectPresent(driver,messagesDetail))
        {
            if(WebdriverWrapper.isObjectPresent(driver, getORPropValue("messagesDiv")))
                    {
                return WebdriverWrapper.getElementText(driver, messagesDetail);
                    }
        }
        return "";
    }
   
    /**
     * Will return true if that grid row is checked
     * @param grid
     * @param name
     * @return
     */
    public Boolean isGridRowChecked(String grid, String name)throws Exception{
        String href = getAttributeFromNameCell(grid, name, "/@href");
        //parse out value to use to check the box
        int lastEquals = href.lastIndexOf("=");
        String value = href.substring(lastEquals + 1);
        return WebdriverWrapper.isElementSelected(driver, getORPropValue("xpath")+"//input[@value='"+value+"']");
    }
    
    /**
     * Scans table header for desired column and returns column number
     * @param grid
     * @param title
     * @return column number starting from 1, 0 if not found
     * This method can override public String getCellContentsByRowAndCol(String grid, String rowContentInNameCol, String colTitle) with passing "id" as gridAttr.
     */
    public Integer getColNumByColTitleTableAttr(String gridAttr, String grid, String title) throws Exception{
        Integer col = 1;
        boolean found = false;
        String gridBaseLocator = getORPropValue("xpath")+"//table[@"+gridAttr+"='";
        
        String gridLocator = gridBaseLocator+grid+gridHeaderLocator;
        while (WebdriverWrapper.isObjectPresent(driver, gridLocator+col+gridHeaderCellLocator)){
            if (WebdriverWrapper.getElementText(driver, gridLocator+col+gridHeaderCellLocator).equals(title)){
                found=true;
                break;
            }
            col++;
        }
        if (found){ 
            return col;
        }else{
            return 0;
        }
    }
    
    
    
    /**
     * Check is column title existed in table
     * @param grid
     * @param title
     * @return true if existed.
     */
    public boolean isColTitleExisted(String grid, String title) throws Exception{
        if (getColNumByColTitle(grid,title)>0) 
            return true;
        else
            return false;
    }
    
    /**
     * Check is column title existed in table as colNum
     * @param grid
     * @param title
     * @return true if existed.
     */
    public boolean isColTitleExistedAtColNum(String grid, String colTitle, int colNum)throws Exception{
        if (getColNumByColTitle(grid,colTitle)==colNum) 
            return true;
        else
            return false;
    }
    
    /**
     * Main table getter, most others should ultimately call this one, uses Integers for row and column
     * @param gridAttr
     * @param grid
     * @param row
     * @param col
     * @return String that is in that table cell, 'not found' if cell is not present
     * This method can override public String getCellContentsByRowAndCol(String grid, String rowContentInNameCol, String colTitle) with passing "id" as gridAttr.
     */
    public String getCellContentsByRowAndColTableAttr(String gridAttr, String grid, Integer row, Integer col)throws Exception{
        if (row.equals(0) || col.equals(0)){
            return "not found";
        }
        String gridLocator = "//table[@"+gridAttr+"='"+grid+gridBodyLocator;
        if (!WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath")+gridLocator+row+"]/td["+col+"]")) {
            return "not found";
        } 
        return WebdriverWrapper.getElementText(driver, getORPropValue("xpath")+gridLocator+row+"]/td["+col+"]");
    }
    
    /**
     * Pass column title and row number 
     * @param gridAttr
     * @param grid
     * @param row
     * @param colTitle
     * @return
     * This method can override public String getCellContentsByRowAndCol(String grid, String rowContentInNameCol, String colTitle) with passing "id" as gridAttr.
     */
    public String getCellContentsByRowAndColTableAttr(String gridAttr, String grid, Integer row, String colTitle)throws Exception{
        Integer colNum = getColNumByColTitleTableAttr(gridAttr, grid, colTitle);
        return getCellContentsByRowAndColTableAttr(gridAttr, grid, row, colNum);
    }
  
    /**
     * Tell me how many rows a table has
     * @param attributeofTable
     * @param grid
     * @return
     */
    public Integer getRowCountFromTableAttribute(String attributeofTable, String grid)throws Exception{
        Integer rows = 0;
        Integer nextRow = rows+1;
        String gridLocator = "//table[@" + attributeofTable + "='" + grid + gridBodyLocator;
        String gridLocatorEnd = "]";
        
        while (WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath")+gridLocator+nextRow+gridLocatorEnd)){
            rows++;
            nextRow++;
        }
        return rows;
    }
    
    /**
     * Pass column number and the content of the cell this will return the first row number containing the text
     * @param grid
     * @param content
     * @param col
     * @return
     */
    public Integer getRowNumByContentAndColumnFromTableAttribute(String attribute, String grid, String content, Integer col) throws Exception{
        Integer row=1;
        boolean found=false;
        String gridLocator = "//table[@"+attribute+"='"+grid+gridBodyLocator;
        String colLocator = "]/td["+col.toString()+"]";
        while (WebdriverWrapper.isObjectPresent(driver, getORPropValue("xpath")+gridLocator+row+colLocator)){
            if (WebdriverWrapper.getElementText(driver, getORPropValue("xpath")+gridLocator+row+colLocator).equals(content)){
                found = true;
                break;
            }
            row++;
        }
        
        if (found){ 
            return row;
        }else{
            return 0;
        }
        
    }
    /**
     * 
     * @returns Error Message
     */
    public String getClassErrorValue() throws Exception{
        return WebdriverWrapper.getElementText(driver, "//span[@class='" + classErrorValue + "']");
    }
    
    public boolean isClassNonErrorLableElementPresent() throws Exception{
        return WebdriverWrapper.isObjectPresent(driver, "//span[@class='" + classNonErrorLableValue + "']");
    }
    
    public String getClassNonErrorValue() throws Exception{
        return WebdriverWrapper.getElementText(driver, "//span[@class='" + classNonErrorValue + "']");
    }
    
    /**
     * This method in the abstract class will use reflection to return a list of Field
     * containing the public members. For most shared classes this will consist of locator strings.
     * <p>
     * Watch out because not all locators are visible in all page states. If you are doing tests on the 
     * public locators be sure the widget is visible in all states if you plan to assert isVisible.
     * 
     * @return List of Field
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public List <Field> getPublicFields() throws ClassNotFoundException{
        Field[] fields;
        List <Field> publicFields = new ArrayList<Field>();
        Class cls = this.getClass();
        fields = cls.getFields();
        for (Field field: fields){
            int mod = field.getModifiers();
            if (Modifier.toString(mod).contains("public")){
                publicFields.add(field);
            }
        }
        return publicFields;
    }
    
    public void waitForVisible(String descriptor) throws TestException{
        for (int second = 0;; second++) {
            if (second >= 120) throw new TestException ("timeout");
            try { 
                if (WebdriverWrapper.isObjectPresent(driver, descriptor)){
                    break; 
                }
            } 
            catch (Exception e) {}
            // a little extra sleep is nice
            try{
                Thread.sleep(1000);
            }catch(Exception e){
                //do nothing
            }
        }
    }
    
    public static void sleep(Long ms){
        try{
            Thread.sleep(ms);
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }
    
    public static void sendHttpRequest(String url) {
        WebClient web = new WebClient();
        try {
            web.getPage(url);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        web.closeAllWindows();
        System.out.println("sent to: "+url);
    }
    
    public static void repeatSendHttpRequest(int repeat, String url) {
        if (repeat<=0) {
            return;
        }
        for (int i=1; i<=repeat;i++) {
            sendHttpRequest(url);
            sleep(wait);
        }
    }
    
    public String getSelectedTab() throws Exception{
        String selectedTabText = "";
        selectedTabText = WebdriverWrapper.getElementText(driver, getORPropValue("grid.selectedtab"));
        return selectedTabText;
    }
    
    public String getSelectedTabList() throws Exception{
        String selectedTabText = "";
        selectedTabText = WebdriverWrapper.getElementText(driver, getORPropValue("grid.selectedtablist"));
        return selectedTabText;     
    }
}
