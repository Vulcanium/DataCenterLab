package fr.sorbonne_u.datacenter.software.requestdispatcher;

import java.net.InetAddress;

import fr.sorbonne_u.datacenter.software.requestdispatcher.interfaces.RequestDispatcherDataI;


/**
 * La classe <code>RequestDispatcherData</code> implemente le composant representant
 * le capteur du Controleur de Performance dans le Data Center.
 * 
 * <p><strong>Description</strong></p>
 * 
 * Le RequestDispatcherData recupere les donnees de temps de traitement des requetes envoyees par le <code>RequestDispatcher</code> 
 * aux differentes <code>ApplicationVM</code>. Ces donnees sont ensuite transmises au <code>PerformanceController</code>.
 */
public class RequestDispatcherData 
implements RequestDispatcherDataI{

	private static final long serialVersionUID = 1L;
	
    /** Heure locale de l'horodatage au format UNIX. */
    protected final long      timestamp;
    
    /** Adresse IP du composant qui a effectué l'horodatage. */
    protected final String    timestamperIP;
    
    /** URI du RequestDispatcher auquel les donnees dynamiques se rapportent. */
    protected final String    requestDispatcherURI;
    
    /** Temps moyen de traitement des requetes */
    protected final long    requestProcessingAvg;
    

    /**
     * Cree un composant de RequestDispatcherData
     * 
     * @param rdUri						URI du RequestDispatcher lie au RequestDispatcherData.
     * @param requestProcessingAvg		Temps de traitement moyen des requetes.
     * @throws Exception
     */
    public RequestDispatcherData( String rdUri , long requestProcessingAvg ) throws Exception {
        super();
        this.timestamp = System.currentTimeMillis();
        this.timestamperIP = InetAddress.getLocalHost().getHostAddress();
        this.requestDispatcherURI = rdUri;
        this.requestProcessingAvg = requestProcessingAvg;

    }

    /**
     * Recupere l'horodatage.
     * 
     * @return Horodatage
     */
    @Override
    public long getTimeStamp() {
        return this.timestamp;
    }

    
    /**
     * Recupere l'adresse IP du composant qui a effectue l'horodatage.
     * 
     * @return Adresse IP du composant qui a effectue l'horodatage
     */
    @Override
    public String getTimeStamperId() {

        return this.timestamperIP;
    }

    /**
     * Recupere l'URI du RequestDispatcher auquel les donnees dynamiques se rapportent.
     * 
     * @return URI du RequestDispatcher auquel les donnees dynamiques se rapportent.
     */
    @Override
    public String getRequestDispatcherURI() {
        return this.requestDispatcherURI;
    }

    /**
     * Recupere le temps moyen de traitement des requetes.
     * 
     * @return Temps moyen de traitement des requetes.
     */
    @Override
    public double getRequestProcessingAvg() {
        return this.requestProcessingAvg;
    }
	

}
