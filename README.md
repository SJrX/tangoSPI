# Tango SPI

## About

Tango SPI is a Gradle plugin that is designed to automatically generate the *provider-configuration file* necessary for using the [java.util.ServiceLoader](https://docs.oracle.com/javase/7/docs/api/java/util/ServiceLoader.html) functionality. 

For a general introduction to the Service Provider Interface (SPI) see [this Java tutorial](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html), but at a high level
this allows you to create a plugin system where you can add/remove plugins by adding jars to the classpath and not introducing a compile-time dependency. 

## Requirements

1. This plugin only supports Gradle versions >= 3.0 (and by extension Java 7)
2. This plugin requires the [Java Plugin](https://docs.gradle.org/3.3/userguide/java_plugin.html) to be applied to your project

## Usage

### Simple Case 
1. Import the plugin to gradle 
```groovy
plugins { 
    id "net.sjrx.tangospi" version "0.1"
}
```

2. Configure the interfaces that you would like to auto generate:
```groovy
tangospi {
    interfaces = ['example.foo.interfaceName']
}
```

3. Compile, and the necessary files should be generated.

### Changes made to project


### Custom Source Sets

Not supported at this time (probably have to manually apply the task, but I haven't tested that yet)

## Comparison to Related Projects

1. Previously the author used the unmantained [Mango SDK](https://code.google.com/archive/p/spi/) package however because it uses annotation processing,
 it does not work with non Java languages like Scala. Additionally the annotation used has a Source.RetentionPolicy and so it could not be used.
 
2. Searching google found a Maven plugin [META-INFO/services auto-generation](http://metainf-services.kohsuke.org/), obviously however this is for Maven.