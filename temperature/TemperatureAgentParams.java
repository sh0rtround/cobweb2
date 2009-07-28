package temperature;

import ga.GeneticParams.Phenotype;
import cobweb.params.AbstractReflectionParams;
import cobweb.params.ConfDisplayName;
import cobweb.params.ConfXMLTag;

public class TemperatureAgentParams extends AbstractReflectionParams {

	private static final long serialVersionUID = -832525422408970835L;

	@ConfXMLTag("PreferedTemp")
	@ConfDisplayName("Prefered temperature")
	public int preferedTemp;
	
	@ConfXMLTag("DifferenceFactor")
	@ConfDisplayName("Difference factor")
	public float differenceFactor;
	
	@ConfXMLTag("Parameter")
	@ConfDisplayName("Parameter")
	public Phenotype parameter = new Phenotype();
}