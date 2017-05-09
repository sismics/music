Sismics Music [![Build Status](https://secure.travis-ci.org/sismics/music.png)](http://travis-ci.org/sismics/music)
=============

What is Music?
---------------

Music is an open source, Web-based music server.

Music is written in Java, and may be run on any operating system with Java support.

Features
--------

- Organize your music collection
- Download music from various sources
- Android app

Downloads
---------

Music is not yet release, please follow the instructions to build from sources.

License
-------

Music is released under the terms of the GPL license. See `COPYING` for more
information or see <http://opensource.org/licenses/GPL-2.0>.

Translations
------------

- English
- French

How to build Music from the sources
------------------------------------

Prerequisites: JDK 8, Maven 3, NPM

Music is organized in several Maven modules:

  - music-core
  - music-web
  - music-web-common
  - music-android

First off, clone the repository: `git clone git://github.com/sismics/music.git`
or download the sources from GitHub.

#### Launch the build

From the root directory:

    mvn clean -DskipTests install

#### Run a stand-alone version

From the `music-web` directory:

    mvn jetty:run

#### Build a .war to deploy to your servlet container

From the `music-web` directory:

    mvn -Pprod -DskipTests clean install

You will get your deployable WAR in the `target` directory.
