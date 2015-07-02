# Microsoft Azure API Management Plugin

===================

A plugin for Ready! API that allows you to import APIs directly from a MS Azure API Managment Service. 

Installation
------------

The plugin isn't enabled at the Integrated Plugin Repository yet.


Build it yourself
-----------------

You can build the plugin by oneself by cloning this repository locally - make sure you have java and maven 3.X correctly 
installed - and run 

```mvn clean install assembly:single```

in the project folder. The plugin dist.jar will be created in the target folder and can be installed via the 
Plugin Managers' "Load from File" action. 

Usage
-----

Once installed there will have two ways to import an API from an Azure API Management Service:

* Via the "Add API From Azure API Management" option on the Project menu in the "Projects" tab
* Via the "Azure API Management" option in the "Create project from..." drop-down when creating a new project

In both cases you will be prompted for:
* the Management REST API URL to an Azure API Management REST API, that exposes API metadata
* the Access Token to authenticate in the API Management REST API service. 
The Management REST API URL conforms to the template https://{servicename}.management.azure-api.net.

Once a valid Management REST API URL and Access Token have been specified you will be presented with a list of available APIs and
import options. Now you can easily:

* send ad-hoc requests to the API to explore its functionality
* create functional tests of the API which you can further use to create Load Tests, Security Tests and API Monitors 
(in the SoapUI NG module)
* create a load tests of the API (in the LoadUI NG module)
* create a security tests of the API (in the Secure module)
* create a virtualized version of the API for sandboxing/simulation purposes (in the ServiceV module).