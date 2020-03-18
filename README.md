lab7-specific tables are in rjmiddle

a) Ethan Tucker, Robert Middleton, Brendan Dunning

b) We used the system variables outlined in the slides
    APP_JDBC_URL
    APP_JDBC_USER
    APP_JDBC_PW
    
    To compile and run add connector jar to classpath
    % export CLASSPATH=$CLASSPATH:mysql-connector-java-8.0.16.jar:.
    
    Then run % javac InnReservations.java
    Followed by % java InnReservations
    

c) The only known "bugs" currently are the lack of error catching in almost all of the program. If the user enter invalid data the program will get mad. (If a date is formatted incorrectly or a string is put in for a number...that kind of stuff). 

FRone and FRsix were completed by Robert
FRtwo and FRfour were completed by Ethan
FRthree and FRfive were complted by Brendan
