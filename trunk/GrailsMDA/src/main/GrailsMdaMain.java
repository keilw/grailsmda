package main;

import java.net.URL;

import net.sf.groovymda.GroovyMDA;

public class GrailsMdaMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// <!-- model source file -->
		// <arg value="${modelpath}" />
		// <!-- folder to drop into -->
		// <arg value="${genpath}" />
		// <!-- custom model processor-->
		// <arg value="${modelprocessorpath}" />
		String[] bla = new String[3];
		bla[0] = args[0];
		// set generating path to
		bla[1] = args[1];
		// set specific modelprocessor
		// bla[2] = "file:/templates/domain/DomainModelProcessor.groovy";
		URL test = Thread.currentThread().getContextClassLoader().getResource("templates/domain/DomainModelProcessor.groovy");
		bla[2] = "jar:" + test.getPath();

		GroovyMDA.main(bla);
	}
}
