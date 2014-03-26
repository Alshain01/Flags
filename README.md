Flags
=====

A Bukkit Plugin that allows developers to add flags to various cuboid systems.

Website: <http://dev.bukkit.org/bukkit-plugins/flags/>  
Bugs/Suggestions/Discussion: <http://dev.bukkit.org/bukkit-plugins/flags/forum/>  

Compiliation
------------

Flags uses Maven to handle dependencies.  However not all of Flag's dependencies are available from a Maven repository. Which means to compile you will need to manually download those dependancies.
* Install [Maven 3](http://maven.apache.org/download.html)
* Download the dependencies Grief Prevention 7.8, PlotMe, Factions, MCore, Residence, and Regios
* If needed you can temporarily edit dependenciydir property in pom.xml to compile. (Default is C:\build_libraries)
* Check out this repo and: `mvn clean install`
