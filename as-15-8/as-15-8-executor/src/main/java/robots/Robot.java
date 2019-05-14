package robots;

import org.testng.xml.XmlTest;

import enums.AccessToolUnit;

public interface Robot {
	
	AccessToolUnit getAccessToolUnit();
	
	XmlTest createTest(String name, Long arrangenmentID, Long erdiID, String url);
	
}
