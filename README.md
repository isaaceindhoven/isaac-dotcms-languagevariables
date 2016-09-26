# dotCMS Language Variables

[DotCMS](https://dotcms.com/) has built in multi language support. Content can be created in many languages and they also have the concept of "Language Variables". These enable you to define a key and for that key a value per language. The keys can be used in VTL files to make the file language independent. This standard dotCMS feature unfortunately is not host dependent. This plugin solves that.

Previous releases of this plugin for older dotCMS versions can be found [here](../../releases).

## Features

* Add host specific language variables.

## Installation

To use it, you obviously first need to install the plugin. To install it take these steps:

* Clone this repository.
* Open the console and go the the folder containing pom.xml
* Execute the following maven command: **mvn clean package**
* The build should succeed and a folder named "target" will be created.
* Open the "target" folder and check if the **.jar** file exists.
* Open dotCMS and go to the dotCMS Dynamic Plugins page by navigating to "System" > "Dynamic Plugins".
* Click on "Exported Packages" and add these packages to the list (make sure to add a comma to the last package in the existing list):

```java
com.dotcms.repackage.com.dotmarketing.osgi,
com.dotmarketing.beans,
com.dotmarketing.portlets.languagesmanager.business,
com.dotmarketing.portlets.languagesmanager.model,
com.liferay.portal
```

* Click on "Save Packages".
* Click on "Upload Plugin" and select the .jar file located in the "/target" folder.
* Click on "Upload Plugin" and the plugin should install and automatically start.
* To check if the plugin is working, visit the following URL and replace **your-url-here** with your own site address: **http://your-url-here/app/servlets/monitoring/isaac-dotcms-languagevariables**

That's it, you've installed the plugin.

## Usage

### Creating language variables

Language variables are nothing more than content, so you can create, edit and delete Language Variables in the Content Tab. Another upside is that you can also permission it, add workflows etc. Everything that you can do with normal content you can do with the Language Variables.

To retrieve a language variable, use **$languageVariables.get("my-key")**, just like the old glossary. See the [API](#api) section for more details.

### Language keys without value

To find out the language keys without value, you have to add this plugin to a tab.

* Open dotCMS and go to the Roles & Tabs page by navigating to "System" > "Roles & Tabs".
* On the left sidemenu click on "System" and select the user you want to create the tab for.
* The page should refresh and you will see a small tabmenu followed by a list of existing tabs.
* Click on the "CMS Tabs" link from the tabmenu.
* At the top-right corner click on "Create Custom Tab", a pop-up should appear.
* Name and order the tab, and select the "Language Variables - Keys without value" tool from the tools drop-downlist.
* Click on "Add" and finally hit "Save".
* The tab should be added to the list of existing tabs.
* Check the created tab from the list and click on "Save".
* Refresh the page and the tab should be added to the dotCMS tabmenu.

## Tips and Tricks

You can modify settings in the plugin.properties. With these settngs you can:

* Change the structure that is used for the Language Variables, by changing the **structure.velocityvarname**.
* Change the field names and values that are used by the structure, by changing the **structure.key**, **structure.key.label**, **structure.value** and **structure.value.label** properties (these are used when auto-generating the structure).
* Change what is displayed when a key can't be found.You can display the key (**show-key-if-value-empty**=**true**), an empty value (**show-key-if-value-empty**=**false**, **replacement-value.show-empty-string**=**false**) or a replacement value (**show-key-if-value-empty**=**false**, **replacement-value.show-empty-string**=**true**, **replacement-if-value-is-empty**=**xxx**).
* Display all the keys used on a page, by adding the parameter **showKeys=true** to your URL. You can even modify what parameter to use by changing the **parameter.display-keys** property.

## <a name="api"></a>API

Below are all the public calls that this plugin provides.

Returns the value of the language variable in the current language for the current host.

|Method|
|------|
|$languageVariables.get(key)|
|$languageVariables.get(key, languageId)|

#### Signature

|Parameter|Type|Description|
|---------|----|-----------|
|key|String|The key of the language variable. This is case sensitive.
|languageId|String|The dotCMS language id you want to get the value for. You can look up this id by opening the dotCMS admin and navigating to "System" > "Languages" > "Edit Language".

## Meta

[ISAAC - 100% Handcrafted Internet Solutions](https://www.isaac.nl) – [@ISAAC](https://twitter.com/isaaceindhoven) – [info@isaac.nl](mailto:info@isaac.nl)

Distributed under the [Creative Commons Attribution 3.0 Unported License](https://creativecommons.org/licenses/by/3.0/).
