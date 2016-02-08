[![Build Status](https://travis-ci.org/sysdevone/gab-cmdline.svg?branch=integration)](https://travis-ci.org/sysdevone/gab-cmdline)
[![Build Status](https://travis-ci.org/sysdevone/gab-cmdline.svg?branch=master)](https://travis-ci.org/sysdevone/gab-cmdline)


GAB-CmdLine
=======

The GAB Social Command Line Parser for Java.  The purpose of this project is to analyze and examine how I would create a command line parser for Java.  Comments are welcome.  Thank you.


Build
---------
Supports JDK 7 or 8.  Use Maven to build - `mvn package`.


Example
---------
```text
myApp [options] [target [target2 [target3] ...]]
  Options: 
  -help                  print this message
  -version               print the version information and exit
  -quiet                 be extra quiet
  -verbose               be extra verbose
  -debug                 print debugging information
  -logfile <file>        use given file for log
  -logger <classname>    the class which is to perform logging
  -listener <classname>  add an instance of class as a project listener
  -D<property>=<value>   use value for given property
  -find <file>           search for file towards the root of the
                         filesystem and use it
```

```java
// define a listener implmentation of the CommandListener interface.
private class CmdLineListener implements CommandListener
{
    @Override
    public void handle(final Command command)
    {
        System.out.println( command );
    }
}
// create an instance of the listener.
final CmdLineListener listener = new CmdLineListener();

// define/declare the commands the parser should parse.
CmdLine.defineCommand("-help, #print this message")
       .defineCommand("-version, #print the version information and exit")
       .defineCommand("-quiet, #be extra quiet")
       .defineCommand("-verbose, #be extra verbose")
       .defineCommand("-debug, #print debugging information")
       .defineCommand("-logfile, !logFile, #use given file for log")
       .defineCommand("-logger, !logClass, #the class which is to perform logging")
       .defineCommand("-listener, !listenerClass, #add an instance of class as a project listener")
       .defineCommand("-find, !buildFile, #search for file towards the root of the filesystem and use it");

Note:  The format of "-D<property>=<value>" is automatically supported and doesnt need to be defined.  
If a -D<property>=<value> is seen on the command line, it is parsed and set 
in the System properties.  In addition, a command is created and sent to the listener.

// parse the command line args and pass matching commands to the listener for processing.
CmdLine.parse( args, listener );
```


Dependencies
---------
This project has dependencies on the jar files under the ./lib directory.  Once those files have matured, they will be added to the Maven central repository.


More Documentation
------------------
Check the project [wiki][].


License
-------
This codebase is licensed under the Apache v2.0 License [license].


Feedback
---------
Comments and feedback are greatly appreciated!!!



[license]:https://github.com/sysdevone/gab-cmdline/tree/master/LICENSE
[wiki]:https://github.com/sysdevone/gab-cmdline/wiki
