rem result file name
set FILE_NAME=umda
rem B(inary) | F(loating)P(oint)
set GENOTYPE=B
rem workenvironment package
set WEnP=minFunctionEX1
rem test number
set TEST=5

rem test author
set OWNER=karlo.knezevic@fer.hr
rem directory for log file
set LOG=log
rem directory for results
set RESULTS=results\%WEnP%\%GENOTYPE%
rem lof filename
set LOG_NAME=%LOG%\log.txt

rem messages
set MSG_START_TEST=---START test ...
set MSG_END_TEST=###END test ...

rem dirs
if not exist %LOG% mkdir %LOG%
if not exist %RESULTS% mkdir %RESULTS%

echo %MSG_START_TEST%#%TIME%>>%LOG_NAME%

for /L %%i in (1 1 %TEST%) do (
	rem unique file name
	set DATEANDTIME=%DATE:~4,2%%DATE:~7,2%%DATE:~10,4%%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%_%%i
	echo %%i#%FILE_NAME%#%GENOTYPE%#%OWNER%>>%LOG_NAME%
	java -jar EDAF.jar EDAFParameters%GENOTYPE%.xml | tee %RESULTS%\%FILE_NAME%_%DATE:~4,2%%DATE:~7,2%%DATE:~10,4%%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%_%%i.txt
	rem if not mingw installed, use: 
	rem java -jar EDAF.jar EDAFParameters%GENOTYPE%.xml >> %RESULTS%\%FILE_NAME%_%DATE:~4,2%%DATE:~7,2%%DATE:~10,4%%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%_%%i.txt
	rem results will not be seen in cmd
)

echo %MSG_END_TEST%>>%LOG_NAME%
