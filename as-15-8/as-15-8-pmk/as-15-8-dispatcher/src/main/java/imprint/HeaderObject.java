package imprint;

import java.util.ArrayList;

public class HeaderObject{
	
	private String label1;
	private String label2;
	private String label3;
	private String label4;
	private String label5;
	private String label6;
	private String label7;
	
	public String getLabel1() { return label1; }
	public void setLabel1(String v) { label1 = v; }
	
	public String getLabel2() { return label2; }
	public void setLabel2(String v) { label2 = v; }
	
	public String getLabel3() { return label3; }
	public void setLabel3(String v) { label3 = v; }
	
	public String getLabel4() { return label4; }
	public void setLabel4(String v) { label4 = v; }
	
	public String getLabel5() { return label5; }
	public void setLabel5(String v) { label5 = v; }
	
	public String getLabel6() { return label6; }
	public void setLabel6(String v) { label6 = v; }
	
	public String getLabel7() { return label7; }
	public void setLabel7(String v) { label7 = v; }
	
	public String[] getAllLabels() {
		ArrayList<String> labels = new ArrayList<String>();
		if (label1 != null) labels.add(label1);
		if (label2 != null) labels.add(label2);
		if (label3 != null) labels.add(label3);
		if (label4 != null) labels.add(label4);
		if (label5 != null) labels.add(label5);
		if (label6 != null) labels.add(label6);
		if (label7 != null) labels.add(label7);
		return labels.toArray(new String[labels.size()]);
	}
	
}