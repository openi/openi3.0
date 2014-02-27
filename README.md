OpenI Plugin for Pentaho
=========================

The OpenI plugin for Pentaho provides a simple and clean user interface to visualize data in OLAP cubes. It supports both direct Mondrian and xmla based connections, plus provides OpenI specific features like Explore Cube Data, custom drillthrough, etc.


Current Version
===============
3.0.2


Pre-requisites
================
Pentaho BI Server 3.9 or greater
You can download the latest version here http://sourceforge.net/projects/pentaho/files/Business%20Intelligence%20Server/


How to install/deploy on existing pentaho instance
==================================================
1. Unzip the plugin archive and copy the plugin folder "openipentaho" into "<<pentaho BI server>>/pentaho-solutions/system" folder location.
2. Copy the "Sample OpenI Solution" under the openipentaho folder into "<<pentaho BI server>>/pentaho-solutions" folder location.
3. Restart the pentaho BI server


Known Issues:
==============
1. Due to conflict issue with the new version of Apache FOP library under Pentaho web lib, PDF Export doesn't work.
Work around:
Copy the library to <<Plugin folder>>/lib/xmlgraphics-commons-1.3.1.jar into << pentaho BI server>>/tomcat/webapps/pentaho/WEB-INF/lib

2. XMLA Datasource Type works for Mondrian XMLA only. Haven't tested yet with SSAS XMLA.


Documentation
=============
http://wiki.openi.org/


**********************************************************************
Licenses
**********************************************************************

OpenI is licensed under the GNU General Public License (GPL) version 
2. The license texts for OpenI and the thirdparty components it uses 
is in the "docs/licenses" directory of the distribution. 

For more information, see: 

docs/LICENSE.txt - license agreement for OpenI
docs/licenses - folder with full license text for all individual  
  		licenses for 3rd party libraries


**********************************************************************
About OpenI, Inc.
**********************************************************************

OpenI, Inc. provides complete software integration, customization, and
support services to build custom business intelligence solutions. The 
OpenI project started in 2005 with the goal to provide a simple, 
intuitive Business Intelligence software product. Today, OpenI 
provides an out-of-box solution to easily visualize data from OLAP and 
relational databases, where users intuitively build and publish 
interactive reports, analyses, and dashboards. OpenI, Inc. sponsors 
the OpenI project and provides commercial support and services to 
enable successful OpenI deployments.


Copyright (c) 2005 - 2012, OpenI, Inc.
