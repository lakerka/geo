package org.geotools.main;


public enum Roles {
	 
    AddLayer("Add Layer"),
    Select("Select"),
    ZoomToSelection("Zoom to selection"),
    OpenFile("Open file"),
    GetFeatures("Get features"),
    CountRows("Count rows"),
    DisplaySelectedFeatures("Get selected in map features"),
    SelectInMapSelectedInTable("Select in map selected in table features"),
    DisplayAttributeTable("Attribute table"),
    AddLayersFromMap("Add layers from map"),
    DisplayIntersectWindow("Intersect");
    
    public final String label;
	
	private Roles(String label) {
	    
	    this.label = label;
	}
}
