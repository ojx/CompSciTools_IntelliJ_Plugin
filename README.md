# CompSci Tools

Allows you to download, upload and evaluate the project files for programming lab assignments.

## Features

- simple setup using webservice URL from assignments
- push / pull tasks managed by a simple button click
- activity tree view for remote evaluation results

## Release Notes

The latest release is still a prototype that has not yet been tested at scale.

## Overview

For details about IntelliJ plugin development, you can refer to [the JetBrains documentation](https://plugins.jetbrains.com/docs/intellij/developing-plugins.html).\
The plugin is based on Gradle with Kotlin.

Here is a quick description of the different packages in `src/main/java`:

```text
src/main/java
├── action          Code for buttons and new project wizard
├── clock           Code of the timer in status bar
├── comments        Handle comments on specific portions of the code
├── evaluation      Code of the evaluation counter in the status bar
├── module          Creation of new projects and wizard steps
├── notifications   Util class with method to display notifications
├── service         Code for persistent data and interaction with the web service
├── settings        Code for plugin settings
├── sideWindow      The side window contains vpl description and evaluation results
└── ui
    └── icons       Icons management
```

## Acknowledgement

This plugin was developed from the Caseine VPL Plugin by Joshua Monteiller, Astor Bizard, Christophe Saint-Marcel, Nicolas Catusse, Lee Yee, Valentin Geiller, and Yanis Guezi.
