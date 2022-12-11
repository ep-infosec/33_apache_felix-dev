# Apache Felix Service Component Runtime (SCR)

The Apache Felix Service Component Runtime described by the [OSGi Declarative Services Specification](https://osgi.org/specification/osgi.cmpn/7.0.0/service.component.html) is implemented by this bundle. The service component model described by `Declarative Services` uses a declarative way for publishing, finding and binding to OSGi services

The Java annotations defined by the specification make implementing components easy and reduce the amount of code that needs be written. These annotations are processed at build time and translated into XML descriptor files which in turn are listed in the `Service-Component` header of the declaring bundle. But the good news is, you usually don't have to worry about this XML, however in case things don't work as expected , it's good to know how these things work.

The Apache Felix Declarative Services implementation is the reference implementation for the OSGi Declarative Services Specification Version 1.4 (R7) and therefore passes the OSGi CT.

## Example Usage

To help you get a head start, here is an example of using Declarative Services. As mentioned the used annotations are processed at build time, creating XML descriptor files. These component declarations are read when the declaring bundle is started and the respective components are verified and activated depending on their declaration.

### Component

First of all the component must be implemented in a simple Java class. The Declarative Services Specification basically places no restrictions on the contents of this class.

Lets define a very simple class, which implements a `java.util.Comparator` service:

```java
package sample;

import java.util.Comparator;
import org.osgi.service.component.annotations.Component;

@Component(service = Comparator.class)
public class SampleComparator implements Comparator
{
    public int compare( Object o1, Object o2 )
    {
        // TODO: calculate the result
        return o1.equals( o2 ) ? 0 : -1;
    }
}
```

This is of course a very simple and not very intelligently implemented comparator...

### Activation

It may well be that the component needs to be notified, when it is activated and deactivated. For this, the component may implement an `activate` method/constructor and a `deactivate` method. Both methods must be `public` or `protected` and can take a variety of parameters.
Here is the initial class extended with activation and deactivation methods:

```java
package sample;

import java.util.Comparator;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;

@Component(service = Comparator.class)
public class SampleComparator implements Comparator
{
    public SampleComparator()
    {
        // TODO: Do something on activation
    }

    public int compare( Object o1, Object o2 )
    {
        // TODO: calculate the result
        return o1.equals( o2 ) ? 0 : -1;
    }

    @Deactivate
    protected void deactivate()
    {
        // TODO: Do something on deactivation
    }
}
```

### Service Binding

The next step would probably be to do some service binding. This is somewhat more overhead, as the referenced services must be declared. On the other hand, you do not have to care to listen for these services. As examples of these strategies we will use the event strategy to access an OSGi `LogService`.

### Looking up the Service

To use the service, the reference must be declared by using the @Reference annotation. Here is the respective declaration for a log service to lookup:

```java
private LogService log;

@Reference
protected void bindLog(LogService log)
{
    this.log = log;
}

protected void unbindLog(LogService log)
{
    this.log = null;
}
```

Or if you want to use field injection, it is just:

```java
@Reference
private LogService log;
```

You can also use contructor injection:

```java
private final LogService log;

@Activate
public Comparator(@Reference LogService logService)
{
    this.log = logService;
}
```

## Apache Maven Support

Both, the [maven-bundle-plugin](http://felix.apache.org/documentation/subprojects/apache-felix-maven-bundle-plugin-bnd.html) as well as the [bnd-maven-plugin](https://github.com/bndtools/bnd/tree/master/maven) supports processing the annotations and creating the XML component descriptors.

## Configuration

The Apache Felix Declarative Services implementation can be configured with Framework properties which are read on startup of the implementation bundle and Configuration Admin Service configuration which is provided by the Configuration Admin Service to the service PID `org.apache.felix.scr.ScrService`.

The following properties are supported:

| Property | Default Value | Description |
|--|--|--|
| `ds.loglevel` | 1 | Defines a logging level at which messages are logged. This configuration property is converted to an `int` number used as the OSGi Log Service logging level. |
| `ds.showtrace` | `false` | sets the log level to `debug` if set to `true` and the `ds.loglevel` cannot be converted to a value log level |
| `ds.showerrors` | `true` | Disables logging completely if set to `false` and the `ds.loglevel` cannot be converted to a value log level and the `ds.showtrace` is not set to `true` |
| `ds.factory.enabled` | `false` | Enables Component Factory functionality not compliant with the Declarative Services specification if set to `true`. Only set this if you really know you need this. See the *Non-Standard Component Factory Behaviour* section below for more details. |
| `ds.delayed.keepInstances` | `false` | Whether or not to keep instances of delayed components once they are not referred to any more. The Declarative Services specifications suggests that instances of delayed components are disposed off if there is not used any longer. Setting this flag causes the components to not be disposed off and thus prevent them from being constantly recreated if often used. Examples of such components may be EventHandler services. The default is to dispose off unused components. See [FELIX-3039](https://issues.apache.org/jira/browse/FELIX-3039) for details. |

The `ds.loglevel` property is treated as follows:

* If the property is a number, the `int` value of the number is used
* If the property is a string parseable to an `int` the parsed value is used
* If the property is any of the strings *debug*, *info*, *warn*, or *error*, the respective log level of `4`, `3`, `2`, or `1` is used
* Otherwise, unless the `ds.showtrace` or `ds.showerrors` property is set, the default value is assumed

This configuration mechanism is implemented in the [ScrConfiguration](https://github.com/apache/felix-dev/blob/master/scr/src/main/java/org/apache/felix/scr/impl/config/ScrConfigurationImpl.java) and its helper classes.


## Non-Standard Component Factory Behaviour

<div class="note" markdown="1">
If you don't know what this section is about, just ignore it and leave the <code>ds.factory.enabled</code> configuration property unconfigured.
</div>

This behaviour assumes the component name of the Component Factory component to be Service Factory PID and each configuration with this Service Factory PID causes the service component runtime to actually create and activate an instance of the Component Factory component automatically. This is not foreseen by the specification which defines instantiation of Component Factory components as being purely application controled and not configuration induced.

To have components instantiated with factory configurations, regular components should be used. This case each factory configuration instance will create a component instance.

If you know that you are using Component Factory components depending on this non-standard behaviour you may set the `ds.factory.enabled` configuration property to `true` (the default of this property is `false` thus disabling this functionality for specification compliance).

For details also refer to [FELIX-1416](https://issues.apache.org/jira/browse/FELIX-1416)


## Administration

The OSGi Compendium specification defines an administrative API for Declarative Services through the [Service Component Runtime](https://osgi.org/specification/osgi.cmpn/7.0.0/service.component.html#service.component-service.component.runtime). This bundle implements that service, too.


Based on the runtime api, the [Declarative Service Plugin](https://github.com/apache/felix-dev/tree/master/webconsole-plugins/ds) for the [Apache Felix Web Console](http://felix.apache.org/documentation/subprojects/apache-felix-web-console.html) provides support for Declarative Services administration through a browser.

This bundle itself also has a Felix Shell Command providing easy commands to introspect the states of the registered components.

### Shell Command

The management API is made available to the Felix Shell as the `scr` command with a short list of subcommands:

| Synopsis | Description |
|--|--|
| `scr help [ <subcommand> ]` | Show help of the specific `<subcommand>` or list all known subcommands |
| `scr list [ <bundleId> ]` | List registered components of the bundle specified by `<bundleId>` or list all components. Each component is listed with its component ID, the state and the name. `<bundleId>` man be either the ID of a bundle or the symbolic name of a bundle. |
| `scr info <componentId>` | Show a complete information dump of the given component. This dump includes the name, status, provided services and information on the service references. `<componentId>` may be either the ID of a component or the component's name. The component name must be used for disabled components. |
| `scr enable <componentId>` | Enable the given component if not already enabled. If the component is already destroyed or enabled, this command has no effect. `<componentId>` may be either the ID of a component or the component's name. The component name must be used for disabled components. |
| `scr disable <componentId>` | Disable the given component if not already disabled. If the component is already destroyed or disabled, this command has no effect. `<componentId>` may be either the ID of a component or the component's name. The component name must be used for disabled components. |

The administrative API commands are also available in the Gogo shell where the subcommand names must be prefixed with the name space `scr`. Thus the `list` command corresponds to `scr:list` in the Gogo shell.
