package templates.domain
import groovy.text.SimpleTemplateEngine 
import org.omg.uml.foundation.core.Attribute
import org.omg.uml.foundation.core.DataType
import org.omg.uml.foundation.core.UmlClass 
import org.omg.uml.foundation.datatypes.AggregationKindEnum
import org.omg.uml.modelmanagement.Model 
import groovy.text.SimpleTemplateEngine
import org.omg.uml.foundation.core.Attribute
import org.omg.uml.foundation.core.DataType
import org.omg.uml.foundation.core.UmlClass
import org.omg.uml.foundation.datatypes.OrderingKindEnum
import org.omg.uml.modelmanagement.Model



class DomainModelProcessor  {
	
	
	def getPackageName = { modelElement ->
		def namespace = modelElement
		def buffer = new StringBuffer()
		while (true) {
			namespace = namespace.namespace
			if (namespace instanceof Model) break
			if (buffer.length() > 0) buffer.insert(0, '.')
			buffer.insert(0, namespace.name)
		}
		return buffer.toString().trim()
	}
	
	def getFullyQualifiedName = { modelElement ->
		def className = modelElement.name
		def packageName = getPackageName(modelElement)
		return packageName.length() > 0 ? "${packageName}.${className}" : className
	}
	
	def getAssociationEnds = { model, classifier ->
		return model.core.AParticipantAssociation.getAssociation(classifier)
	}
	
	def getAllClasses = { model ->
		return model.core.umlClass.refAllOfType()
	}
	
	def javaType = { umlType ->
		if (umlType instanceof UmlClass) {
			return getFullyQualifiedName(umlType)
		}
		if (umlType instanceof DataType) {
			return umlType.name
		}
		return "UNKNOWN TYPE: ${umlType}"
	}
	
	def upperRange = { associationEnd ->
		def x = 0
		associationEnd.multiplicity.range.each{ x += it.upper
		}
		return x
	}
	
	def isOneToMany = { source, target ->
		return (upperRange(source) == 1 && upperRange(target) == -1)
	}
	
	def isManyToOne = { source, target ->
		return (upperRange(source) == -1 && upperRange(target) == 1)
	}
	
	def isManyToMany = { source, target ->
		return (upperRange(source) == -1 && upperRange(target) == -1)
	}
	
	def isOneToOne = { source, target ->
		return (upperRange(source) == 1 && upperRange(target) == 1)
	}
	
	def isCollection = { end ->
		return (upperRange(end) == -1)
	}
	
	def findFeatures = { classifier, featureType ->
		return classifier.feature.findAll { feature ->
			featureType.isAssignableFrom(feature.class)
		}
	}
	
	def firstCharUpper = { s ->
		def chars = s.toCharArray()
		if (chars.length > 0) {
			chars[0] = Character.toUpperCase(chars[0])
		}
		return new String(chars)
	}
	
	def firstCharLower = { s ->
		def chars = s.toCharArray()
		if (chars.length > 0) {
			chars[0] = Character.toLowerCase(chars[0])
		}
		return new String(chars)
	}
	
	def taggedValues = { modelElement ->
		def tags = [:]
		modelElement.taggedValue.each { taggedValue ->
			def key = taggedValue.type.name
			def valueBuffer = new StringBuffer()
			taggedValue.dataValue.each { value ->
				if (valueBuffer.length() > 0) {
					valueBuffer.append(",")
				}
				valueBuffer.append(value)
			}
			tags.put(key, valueBuffer.toString())
		}
		return tags
	}
	
	def javaToSql = { s ->
		def buffer = new StringBuffer()
		def chars = s.toCharArray()
		for (c in chars) {
			if (buffer.length() > 0 && Character.isUpperCase(c)) {
				buffer.append('_')
			}
			buffer.append(Character.toLowerCase(c))
		}
		buffer.toString()
	}
	
	def hasStereotype = { modelElement, stereotype ->
		return (modelElement?.stereotype?.find { stereotype?.equals( it.name )
		} != null)
	}
	
	def findElementsByStereotype = { model, stereotype ->
		return model.core.modelElement.refAllOfType().findAll { hasStereotype(it, stereotype)
		}
	}
	
	def isOrdered = { associationEnd ->
		return (associationEnd.ordering == OrderingKindEnum.OK_ORDERED)
	}
	
	def isOwner = { association, associationEnd ->
		def owner = association.connection.find { end -> taggedValues(end).owner
		}
		if (owner == null) {
			throw new IllegalStateException("no owner defined for ")
		}
		return (owner == associationEnd)
	}
	
	def getEndType = { associationEnd ->
		def type
		if (isCollection(associationEnd)) {
			type = isOrdered(associationEnd) ? "java.util.List" : "java.util.Set"
			type += "<${associationEnd.participant.name}>"
		} else {
			type = getFullyQualifiedName(associationEnd.participant)
		}
		return type
	}
	
	def getEndName = { associationEnd ->
		def name = associationEnd.name
		if (!name) {
			name = firstCharLower(associationEnd.participant.name)
			if (isCollection(associationEnd)) {
				name = "${name}s"
			}
		}
		return name
	}
	
	def getClassName = { clazz ->
		return clazz?.name
	}
	
	def getAttributes = { classifier ->
		return findFeatures(classifier, Attribute.class)
	}
	
	def loadResourceStream = { name ->
		def inputStream
		def file = new File(name)
		if (file.exists()) {
			inputStream = new FileInputStream(file)
		} else {
			inputStream = getClass().getResourceAsStream("/${name}")
		}
		return inputStream
	}
	
	def prepareBinding = { map ->
		def binding = [
		"javaToSql":javaToSql,
		"javaType":javaType,
		"firstCharUpper":firstCharUpper,
		"firstCharLower":firstCharLower,
		"getPackageName":getPackageName,
		"getAttributes":getAttributes,
		"getAssociationEnds":getAssociationEnds,
		"getEndType":getEndType,
		"getEndName":getEndName,
		"taggedValues":taggedValues,
		"isOneToOne":isOneToOne,
		"isOneToMany":isOneToMany,
		"isManyToOne":isManyToOne,
		"isManyToMany":isManyToMany,
		"isOwner":isOwner,
		"isCollection":isCollection
		]
		if (map) {
			binding.putAll(map)
		}
		return binding
	}
	
	def processTemplate = { templateName, outputName, context ->
		def outputFile = new File(getOutputPath(context, outputName))
		outputFile.parentFile.mkdirs()
		outputFile << new SimpleTemplateEngine()
		.createTemplate(new InputStreamReader(loadResourceStream(templateName)))
		.make(prepareBinding(context))
		.toString()
	}
	
	def getOutputPath(context, path) {
		if (context.outputDirectory) {
			return new File(context.outputDirectory, path).toString()
		}
		return path
	}
	
	//	void process(Map context) {
	//		getAllClasses(context.model).each { modelElement ->
	//			
	//			context.currentModelElement = modelElement
	//			
	//			def fullyQualifiedName = getFullyQualifiedName(context.currentModelElement)
	//			if (!fullyQualifiedName.startsWith("java") && fullyQualifiedName.size() > 0) {
	//				
	//				def templateName = "templates/JpaEntity.gtl"
	//				def outputName = "${fullyQualifiedName.replace('.','/')}.java"
	//				
	//				processTemplate(templateName, outputName, context)
	//				
	//			}
	//			
	//		}
	//	}
	
	
	void process(Map context) {
		// ITERATE THROUGH EACH CLASS IN THE MODEL
		getAllClasses(context.model).each { modelElement ->
			// ADD THE CURRENT MODEL ELEMENT TO THE CONTEXT
			context.currentModelElement = modelElement
			
			// GET THE FULLY QUALIFIED NAME FOR THE CLASS
			def fullyQualifiedName = getFullyQualifiedName(context.currentModelElement)
			
			// ONLY PROCESS NON JRE CLASSES (java.lang.String does not need to be generated)
			if (!fullyQualifiedName.startsWith("java") && fullyQualifiedName.size() > 0) {
				
				// YOU CAN BIND CLOSURES TO THE CONTEXT TO MAKE THEM ACCESSIBLE TO YOU TEMPLATES   
				context.isComposite= { end ->
					return end.aggregation == AggregationKindEnum.AK_COMPOSITE
					//.equals(AggregationKindEnum.COMPOSITE.typeName) 
				}
				
				context.taggedValueMap = { taggedValueKey, taggedValueModel ->
					def tags = [:]
					taggedValueModel.taggedValue.each { taggedValue ->
						def key = taggedValue.type.name
						if(key==taggedValueKey){
							def valueBuffer = new StringBuffer()
							taggedValue.dataValue.each { value ->
								if (valueBuffer.length() > 0) {
									valueBuffer.append(",")
								}
								valueBuffer.append(value)
							}
							if(tags.containsKey("${key}")){
								key="${key}"+(tags.size()+1)
							}
							tags.put("${key}", "${valueBuffer.toString()}")
						}
					}
					return tags
				}
				
				context.constraintValueMap = { constraintValueKey, constraintValueModel ->
					def tags = [:]
					constraintValueModel.constraint.each { constraint ->
						def key = constraint.name
						if(key==constraintValueKey){
							def valueBuffer = new StringBuffer()
							constraint.body.each { value ->
								if (valueBuffer.length() > 0) {
									valueBuffer.append(",")
								}
								valueBuffer.append(value)
							}
							if(tags.containsKey("${key}")){
								key="${key}"+(tags.size()+1)
							}
							tags.put("${key}", "${valueBuffer.toString()}")
						}
					}
					return tags
				}
				// SET THE TEMPLATE TO USE
				def templateName = "templates/domain/DomainModel.gtl"
				
				// SET THE OUTPUT FILE NAME FOR THE FULLY QUALIFIED NAME
				def outputName = "${fullyQualifiedName.replace('.','/')}.groovy"
				
				// PROCESS THE TEMPLATE
				processTemplate(templateName, outputName, context)
				
			}
		}
	}
	
}