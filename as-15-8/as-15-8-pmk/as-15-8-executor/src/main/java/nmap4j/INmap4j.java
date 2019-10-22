package nmap4j;

import nmap4j.core.nmap.ExecutionResults;
import nmap4j.core.nmap.NMapExecutionException;
import nmap4j.core.nmap.NMapInitializationException;
import nmap4j.data.NMapRun;

public interface INmap4j {
	void execute() throws NMapInitializationException, NMapExecutionException;
	void addFlags(String flagSet);
	void includeHosts(String hosts);
	void excludeHosts(String hosts);
	String getOutput();
	NMapRun getResult();
	boolean hasError();
	ExecutionResults getExecutionResults();
}
