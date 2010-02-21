package main;

import java.net.URL;

import net.sf.groovymda.GroovyMDA;

public class GrailsMdaMain {

	/**
	 * @param args
	 *            First argument is the path to the UML model file, second is
	 *            path to the project in which all classes should be generated
	 *            into, third argument is the path to the model processor
	 */
	public static void main(String[] args) {
		/**
		 * As defined in the example build.xml <!-- model source file --> <arg
		 * value="${modelpath}" /> <!-- folder to drop into --> <arg
		 * value="${genpath}" /> <!-- custom model processor--> <arg
		 * value="${modelprocessorpath}" />
		 */

		// Generating Domain classes
		String[] extendedArgs = new String[3];
		extendedArgs[0] = args[0];
		// set generating path to
		extendedArgs[1] = args[1] + "/grails-app/domain";
		// set specific modelprocessor
		URL processorPath = Thread.currentThread().getContextClassLoader().getResource("templates/domain/DomainModelProcessor.groovy");
		extendedArgs[2] = "jar:" + processorPath.getPath();

		GroovyMDA.main(extendedArgs);

		// Generating ValueObject classes
		extendedArgs = new String[3];
		extendedArgs[0] = args[0];
		// set generating path to
		extendedArgs[1] = args[1] + "/src/groovy";
		// set specific modelprocessor
		processorPath = Thread.currentThread().getContextClassLoader().getResource("templates/valueobject/ValueObjectProcessor.groovy");
		extendedArgs[2] = "jar:" + processorPath.getPath();

		GroovyMDA.main(extendedArgs);
	}
}
