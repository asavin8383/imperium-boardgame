import org.junit.Test;
import org.nmap4j.core.flags.Flag;
import org.nmap4j.core.nmap.ExecutionResults;
import org.nmap4j.core.nmap.NMapExecutionException;
import org.nmap4j.core.nmap.NMapInitializationException;
import org.nmap4j.core.scans.BaseScan;
import org.nmap4j.core.scans.ParameterValidationFailureException;

public class TestNMap {

    @Test
    public void testNMap() throws NMapExecutionException, NMapInitializationException, ParameterValidationFailureException {
        BaseScan baseScan = new BaseScan( "D:\\nmap", true) ;

        baseScan.includeHost( "192.168.5.12" ) ;
        baseScan.addPorts(new int[]{ 80 } ) ;
        baseScan.addFlag( Flag.CONNECT_SCAN ) ;
        baseScan.addFlag(Flag.TREAT_HOSTS_AS_ONLINE);
        baseScan.addFlag(Flag.NEVER_DO_DNS);

        boolean open = false ;

        ExecutionResults results = baseScan.executeScan() ;
        System.out.println( results.getExecutedCommand() ) ;
        /*System.out.println( results.getOutput() ) ;
        if( results.hasErrors() ) {
            System.out.println(open);
        }

        OnePassParser opp = new OnePassParser() ;
        NMapRun nmapRun = opp.parse(results.getOutput(), OnePassParser.STRING_INPUT ) ;

        String output = nmapRun.getHosts().get(0).getPorts().getPorts().get(0).getState().getState() ;

        if( output != null ) {
            if( output.toLowerCase().equals( "open" ) ) {
                open = true ;
            }
        }

        System.out.println(open);*/
    }

}
