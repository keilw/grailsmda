# Introduction #
GrailsMDA is an opensource project based on GroovyMDA. It will help you to generate Grails domain, service and controller classes from your UML-Model.

# News #
## GrailsMDA 0.2.2 released ##
A new version of GrailsMDA is now available for download.

New features:
  * New stereotypes: "GrailsService", "Controller" and "ValueObject" are now recognized if assigned to a class. As a matter of fact each class whithout a stereotype will be treated as a domain class. For each service there will be an interface and an implementing class generated, though only the interface will be rewritten and the implementation only generated if it is not existing, yet.

  * For each domain class there will be also an implementing class generated, that extends the domain. Therefore the mapping property "tablePerHierarchy" will be set to "true" as default value. Again only the domain classes will be rewritten in each build cycle, so your implementation code should only go to the "Impl" classes, if you have to add things.


## GrailsMDA 0.2.1 released ##
A new version of GrailsMDA is now available for download.

New features:
  * New tagValue: "valueObject" is now recognized if assigned to a class. "valueObject = true" will cause the generation of an appropriate value object class. Since not all classes have to have this value set, all relations will be ignored.
  * New tagValue: "createController" assigned to a class will force the generator to create the default scaffolding controller for that class

Bug fixes:
  * If the **genpath** property is pointing to your grails project root all classes will now be generated correctly to their belonging folders.
