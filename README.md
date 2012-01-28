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

Build 1.2.0 (next release)
-----------

* Modified default hot-keys for enable/disable hints (**Ctrl-[, Ctrl-H**) and enable/disable surrounding brackets (**Ctrl-[, Ctrl-S**), as suggested in [issue 7](https://github.com/chookapp/Bracketeer/issues/7).
* The popup now has colors and highlights the matching bracket.
* Added hints for break/continue from within "for", "while", "do", switch's "case"/"default" and Java's "foreach". This should address [issue 3](https://github.com/chookapp/Bracketeer/issues/3).
* Improved support for matching pairs of angular brackets when used in template expressions (both in C++ and in Java).
* Improved pair matching when there is inactive code (C++ only, obviously).
* Some small bug fixes.

Build 1.1.0
-----------

* Fixed [issue 1](https://github.com/chookapp/Bracketeer/issues/1) - Support Google's WindowBuilder.
* Fixed [issue 2](https://github.com/chookapp/Bracketeer/issues/2) and [issue 5](https://github.com/chookapp/Bracketeer/issues/5) - Null pointer exceptions.
* Option of using a rectangle outline when highlighting brackets.
* Option to ignore pairs which are close to each other.
* Added annotation to "missing pair".
* Fixed [issue 4](https://github.com/chookapp/Bracketeer/issues/4) - Support also C statements (not only C++).
* Show "hint" when hovering (overriding "regular" behavior)
* Show basic popup when hovering over a closing bracket (will be improved on the next version)
* Fixed [issue 6](https://github.com/chookapp/Bracketeer/issues/6) - Join hint's multiple lines to one line.
* Some small bug fixes.

