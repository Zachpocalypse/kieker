@echo off 

REM @author Nils Christian Ehmke

setlocal enabledelayedexpansion

SET JAVAARGS=-Dkieker.common.logging.Log=JDK -Djava.util.logging.config.file=./logging.properties -Xms56m -Xmx1024m
SET MAINCLASSNAME=kieker.tools.traceAnalysis.TraceAnalysisTool

REM Set every variable we will need for the execution.
SET BINDIR=%~dp0

REM Now start the tool, but don't forget to deliver the parameters.
java %JAVAARGS% -cp "%BINDIR%\..\lib\*";"%BINDIR%\..\dist\*" %MAINCLASSNAME% %*

REM Don't close the window immediately.
@echo on

@PAUSE