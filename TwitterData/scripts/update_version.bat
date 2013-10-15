REM #-------------------------------------------------------------------------------
REM # DARPA XDATA licenses this file to You under the Apache License, Version 2.0
REM # (the "License"); you may not use this file except in compliance with 
REM # the License.  You may obtain a copy of the License at 
REM # 
REM #     http://www.apache.org/licenses/LICENSE-2.0
REM # 
REM # Unless required by applicable law or agreed to in writing, software 
REM # distributed under the License is distributed on an "AS IS" BASIS, 
REM # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM # See the License for the specific language governing permissions and  
REM # limitations under the License.
REM # 
REM # Copyright 2013 Raytheon BBN Technologies Corp.  All Rights Reserved. 
REM #-------------------------------------------------------------------------------

date /T >> date.tmp
time /T >> time.tmp
set /p dateVar= < date.tmp
set /p timeVar= < time.tmp

echo package com.bbn.xdata.twitter;                >  com\bbn\xdata\twitter\Version.java
echo public class Version {                        >> com\bbn\xdata\twitter\Version.java
echo   public static String buildDate = "%date%";  >> com\bbn\xdata\twitter\Version.java
echo   public static String buildTime = "%time%";  >> com\bbn\xdata\twitter\Version.java
echo }                                             >> com\bbn\xdata\twitter\Version.java
