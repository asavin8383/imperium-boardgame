/*
 * Copyright (c) 2010, nmap4j.org
 *
 * All rights reserved.
 *
 * This license covers only the Nmap4j library.  To use this library with
 * Nmap, you must also comply with Nmap's license.  Including Nmap within
 * commercial applications or appliances generally requires the purchase
 * of a commercial Nmap license (see http://nmap.org/book/man-legal.html).
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, 
 *      this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright 
 *      notice, this list of conditions and the following disclaimer in the 
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the nmap4j.org nor the names of its contributors 
 *      may be used to endorse or promote products derived from this software 
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package nmap4j.parser;

import nmap4j.data.NMapRun;
import nmap4j.data.host.*;
import nmap4j.data.host.os.OsClass;
import nmap4j.data.host.os.OsMatch;
import nmap4j.data.host.os.PortUsed;
import nmap4j.data.host.ports.ExtraPorts;
import nmap4j.data.host.ports.Port;
import nmap4j.data.host.trace.Hop;
import nmap4j.data.host.trace.Trace;
import nmap4j.data.nmaprun.*;
import nmap4j.data.nmaprun.host.ports.extraports.ExtraReasons;
import nmap4j.data.nmaprun.host.ports.port.Service;
import nmap4j.data.nmaprun.host.ports.port.State;
import nmap4j.data.nmaprun.hostnames.Hostname;
import nmap4j.data.nmaprun.runstats.Finished;
import nmap4j.data.nmaprun.runstats.Hosts;
import org.xml.sax.Attributes;

/**
 * This interface defines the functionality necessary to create the various
 * nmap XML objects based on the parsed data.  It's primary purpose is to
 * allow a discrete way to handle creating data objects from the XML data.
 * <p>
 * The methods defined here are specifically for loading the XML attributes
 * and not the child elements.  That is handled in the DefaultHandler
 * implementation.  In essence, this is a utility class for creating Objects 
 * from the XML attributes.
 * 
 * @author jsvede
 *
 */
 interface INMapRunHandler {
	
	 NMapRun createNMapRun(Attributes attributes) ;

	 Host createHost(Attributes attributes) ;

	 Distance createDistance(Attributes attributes) ;

	 Address createAddress(Attributes attributes) ;

	 Hostnames createHostnames(Attributes attributes) ;

	 Hostname createHostname(Attributes attributes) ;

	 IpIdSequence createIpIdSequence(Attributes attributes) ;

	 Os createOs(Attributes attributes) ;

	 Ports createPorts(Attributes attributes) ;

	 Status createStatus(Attributes attributes) ;

	 TcpSequence createTcpSequence(Attributes attributes) ;

	 TcpTsSequence createTcpTsSequence(Attributes attributes) ;

	 Times createTimes(Attributes attributes) ;

	 Uptime createUptime(Attributes attributes) ;

	 OsClass createOsClass(Attributes attributes) ;

	 OsMatch createOsMatch(Attributes attributes) ;

	 PortUsed createPortUsed(Attributes attributes) ;

	 ExtraPorts createExtraPorts(Attributes attributes) ;

	 Port createPort(Attributes attributes) ;

	 Debugging createDebugging(Attributes attributes) ;

	 RunStats createRunStats(Attributes attributes) ;

	 ScanInfo createScanInfo(Attributes attributes) ;

	 Verbose createVerbose(Attributes attributes) ;

	 ExtraReasons createExtraReasons(Attributes attributes) ;

	 Service createService(Attributes attributes) ;

	 State createState(Attributes attributes) ;

	 Finished createFinished(Attributes attributes) ;

	 Hosts createHosts(Attributes attributes) ;

	 Cpe createCpe(Attributes attributes) ;

	 Trace createTrace(Attributes attributes) ;

	 Hop createHop(Attributes attributes) ;

}
