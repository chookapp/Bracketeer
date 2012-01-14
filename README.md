Overview
========

Bracketeer is a platform used to enrich the editors in Eclipse.
#### Features:

- Coloring the brackets surrounding the cursor & brackets which have no match.
- Showing automatic comments on brackets closing: loops, functions, etc...
- Highly customizable.

#### Current supported editors:

- JDT (java) 
- CDT (C/C++).

Installation 
============
In your Eclipse, go to: Help->Install new software...  
Enter the URL (in "Work with"): **http://chookapp.github.com/ChookappUpdateSite/**  
From the chookapp category, choose any Bracketeer editor support you want (CDT - for C/C++, JDT - for java, etc...).  
For example, choosing "Bracketeer CDT support" will install the Bracketeer infrastructure (if not already installed) and the CDT editor support. *It is not advised to install a Bracketeer support for an editor type which is not already installed in your Eclipse (although it shouldn't do any actual harm)*.

Enjoy.

Screen shots
============

![screenshot](https://github.com/chookapp/Bracketeer/raw/master/Bracketeer/doc/all-surrounding-hyperlink.JPG)

You can find more information and screenshots in the [Wiki](https://github.com/chookapp/Bracketeer/wiki) or in the preferences help pages (after installing Bracketeer in Eclipse)

Feedback
========

* Found a bug? Have a feature request? Visit the [issues page](https://github.com/chookapp/Bracketeer/issues).
* Have a comment you want to share? Want to support Bracketeer? Visit the Eclipse Marketplace pages [Bracketeer for C/C++](http://marketplace.eclipse.org/content/bracketeer-cc-cdt), [Bracketeer for Java](http://marketplace.eclipse.org/content/bracketeer-java-jdt)

Build notes
============

Build 1.1.0 (next release)
-----------

* Fixed [issue 1](https://github.com/chookapp/Bracketeer/issues/1) - support Google's WindowBuilder.
* Fixed [issue 2](https://github.com/chookapp/Bracketeer/issues/2).
* Option of using a rectangle outline when highlighting brackets.
* Option to ignore pairs which are close to eachother.
* Added annotation to "missing pair".
* Fixed [issue 4](https://github.com/chookapp/Bracketeer/issues/4) - Support also C statements (not only C++).
