# FX2J - FXML to Java Builder

FX2J is a post-processor that converts fxml files
into Java builder source files.

The resulting builder files are equivalent java
to construct the view represented by the fxml file.

## FXML Specification

FX2J follows the FXML specification as found on the openjfx
website at https://openjfx.io/javadoc/21/javafx.fxml/javafx/fxml/doc-files/introduction_to_fxml.html

### Not Yet Implemented

The following aspects are not yet implemented from the spec:

* Binding expressions
* Location resolution
* Event handler expressions

### Not Planned

The following aspects are not planned to be implemented from the spec:

* Custom builder factories
* Scripts

### Limitations

Since the builder source files do not use reflection and are in a separate module
all fields intended to be set from the fxml file must have an appropriately named
setter or must be publicly accessible. @FXML annotations are not sufficient and will result
in a failure to compile the fxml file

Additionally, the controller class must be fully specified at compile time to ensure
proper processing. This can be done by using the fx:controller attribute in the root of the
view graph or by adding a <?controllerType > directive which specifies the fully qualified
name of the upper bounds for the controller class

## Usage

### Runtime

To use the FX2J provided builder files at runtime you must add the api module as a dependency of your application

#### Gradle

```kotlin
dependencies {
    implementation("io.github.sheikah45.fx2j:fx2j-api:${version}")
}
```

#### Maven

```xml

<dependencies>
    <dependency>
        <groupId>io.github.sheikah45.fx2j</groupId>
        <artifactId>fx2j-api</artifactId>
        <version>${version}</version>
    </dependency>
</dependencies>
```

For easy loading the FX2JLoader is provided as a direct FXMLLoader replacement. It is responsible for determining which
builder to use and
if a matching builder cannot be found it falls back to the FXMLLoader if it is available on the class/module path.
All views should be constructed through the FX2JLoader as the exact interface and implementation of the builder classes
are considered an internal detail and may change between versions.

### Build Time

FX2J outputs all of your FXML Java builders into its own separate module that can be added as
a runtime dependency to your application. The module provides an implementation of the FX2JBuilderFinder
which is loaded as a ServiceProvider and used by the FX2JLoader at runtime to select a builder based on the provided url
location.

Currently, FX2J provides a gradle plugin to assist in configuration of the build process

#### Gradle Plugin Configuration

The gradle plugin is designed to compile fxml files in the resources directory of the main sourceSet. It adds
its owns fx2j sourceSet which will be the output location for the builder source files.

It adds the compileFx2j task which is responsible for using the compiled output of the main sourceSet to produce the
builder source files. The fx2jClasses task compiles those source files into byte code and the fx2jJar task packages them
into the jar that the plugin adds as a runtimeOnly dependency to your application

The following additional configuration is available for the plugin

```kotlin
fx2j {
    baseSourceSetName = "main" // The name of the source set that contains the fxml
    basePackage =
        "fx2j.builder" // The root package for your builder files. Sub packages are derived from the directory structure relative to the resources root
    includes =
        mutableSetOf() // A set of path matchers for which only matching fxml files will be included. By default all fxml files in the resources directory are included
    exclues =
        mutableSetOf() // A set of path matchers for which matching fxml files should be excluded from compilation.
    strict =
        false // Indicates if fx2j builder compilation errors should fail the build. Defaults to false so only a warning is produced
    modularizeIfPossible =
        true // Attempt to create the jar as a module. This may fail if any non module jars are on the module path
}
```