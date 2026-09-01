#!/bin/bash

#
# Copyright 2025-2026 IHE International.
#
#  Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

if [ -n "${HTTP_PROXY_HOST}" ]; then
    export JAVA_OPTS="$JAVA_OPTS -Dhttp.proxyHost=${HTTP_PROXY_HOST} -Dhttp.proxyPort=${HTTP_PROXY_PORT} -Dhttp.nonProxyHosts=${HTTP_NON_PROXY_HOSTS}"
fi

if [ -n "${HTTPS_PROXY_HOST}" ]; then
    export JAVA_OPTS="$JAVA_OPTS -Dhttps.proxyHost=${HTTPS_PROXY_HOST} -Dhttps.proxyPort=${HTTPS_PROXY_PORT} -Dhttps.nonProxyHosts=${HTTPS_NON_PROXY_HOSTS}"
fi


echo "                             ,                                                                             "
echo "                                ******,                                                                    "
echo "                                       ***.                                                                "
echo "                                    ....(*****.                                                            "
echo "                                           /*******,                                                       "
echo "                                          //***********                                                    "
echo "                                         //((((                                                            "
echo "                                       /((((((                                                             "
echo "                                     (((#%%%%,                                                             "
echo "                                  (#%%%%%%%%%,                                                             "
echo "                               #%%%%%%%%%%%%%,                                                             "
echo "                           %%%%%%%%%%%%%%%%%%                                                              "
echo "                      ,%%%%%%%%%%%%%%%%%%%%%*                                                              "
echo "           /((((((#%%%%%%%##(((((((((##%%%%                                                                "
echo "              %%(((((/,.....        ..(((                                                                  "
echo "            %%%%%*....             .,((                                                                    "
echo "          /%%%/...                .((                                                                      "
echo "         %%%,.                  ./.                                                                        "
echo "       #%%,.        ,#(,       ,                                                %%%   #%%                  "
echo "      %%(      .%%%%/  (%%%%                                                    %%%   %%%                  "
echo "     %%       %%%          ,       %%%%%%%% %%%   %%%%%%%%%%%#    %%%%%%%%#     %%%   %%%      %%%%%%%%    "
echo "    %.       .%%                 %%%,      %%%%          %%%(   %%%       %%#   %%%   %%%   .%%*      ,%%  "
echo "  *%         /%%      %%%%%%%%   %%*        %%%        %%%,    /%%%%%%%%%%%%%.  %%%   %%%   %%%%%%%%%%%%%% "
echo " (            %%%          %%%   %%(        %%%      %%%       *%%              %%%   %%%   %%#            "
echo ",              %%%(      #%%%    /%%#      %%%%    %%%          %%%.      %%%   %%%   %%%    %%%      ,%%  "
echo "               ,%%%%%%%%         *%%%%%%* %%%   %%%%%%%%%%%.    ,%%%%%%%#     %%%   #%%      %%%%%%%%.     "



# Run the script
/usr/local/s2i/run