grailsHome = Ant.project.properties.'environment.GRAILS_HOME'
includeTargets << new File("$grailsHome/scripts/Init.groovy")

pathToModel = ''
def grailsMdaConfig
loadConfig = {
	GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader())
	Class clazz = loader.parseClass(new File("$basedir/grails-app/conf/GrailsMdaConfig.groovy"))
	grailsMdaConfig = new ConfigSlurper().parse(clazz)
	pathToModel = grailsMdaConfig.grailsmda.model.path
	println "Model path set to: $pathToModel"
}


target('default': 'Creates Domain classes for given model path') {
	loadConfig()
	createDomains()
}

private void createDomains(){
	ant.java(fork:"true", jar:"${grailsMdaPluginPluginDir}/lib/grailsMDA-0.2.2.jar"){
		arg(value:"$pathToModel")
		arg(value:"$basedir")
	}	
}
