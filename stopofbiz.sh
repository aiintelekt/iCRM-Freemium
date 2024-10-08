#!/bin/sh
#####################################################################
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#####################################################################
####
# ofbiz.admin.key and ofbiz.admin.port must match that which OFBIZ was started with
####

# location of java executable
if [ -f "$JAVA_HOME/bin/java" ]; then
  JAVA=$JAVA_HOME/bin/java
else
  JAVA=java
fi                                                                                                                                                                      

# shutdown settings
ADMIN_PORT=10538
ADMIN_KEY=icrm_b2b_sit

RES=0
FIRST=1
echo "Stopping ofbiz"
while [ $RES -eq 0 ]; do
  if [ $FIRST -eq 1 ]; then
    FIRST=0
    $JAVA -Dofbiz.admin.port=$ADMIN_PORT -Dofbiz.admin.key=$ADMIN_KEY -jar ofbiz.jar -shutdown
  else
    sleep 2
    $JAVA -Dofbiz.admin.port=$ADMIN_PORT -Dofbiz.admin.key=$ADMIN_KEY -jar ofbiz.jar -shutdown > /dev/null 2> /dev/null
  fi
  RES=$?
done;
