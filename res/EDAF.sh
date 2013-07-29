#result file name
FILE_NAME="umda";
#B(inary) | F(loating)P(oint)
GENOTYPE="B";
#workenvironment package
WEnP="minFunctionEX1";
#test - goto 34. line

#test author
OWNER=karlo.knezevic@fer.hr;
#directory for log file
LOG=log;
#directory for results
RESULTS="results"/$WEnP/$GENOTYPE;
#lof filename
LOG_NAME=$LOG/log.txt;

#messages
MSG_START_TEST="---START test ...";
MSG_END_TEST="###END test ...";

#dirs
if [ ! -d $LOG ] ; then 
	mkdir $LOG 
fi
if [ ! -d $RESULTS ] ; then 
	mkdir -p $RESULTS 
fi

TIME=`date +%X`;
echo "$MSG_START_TEST $TIME" >> $LOG_NAME;
UNIQUE=`date +%s`;

#1..TEST times
for i in {1..5} 
do
	echo "$i#$FILE_NAME#$GENOTYPE#$OWNER" >> $LOG_NAME;
	java -jar EDAF.jar "EDAFParameters$GENOTYPE.xml" | tee $RESULTS/"$FILE_NAME"_"$UNIQUE"_"$i".txt
done

echo "$MSG_END_TEST" >> $LOG_NAME;
