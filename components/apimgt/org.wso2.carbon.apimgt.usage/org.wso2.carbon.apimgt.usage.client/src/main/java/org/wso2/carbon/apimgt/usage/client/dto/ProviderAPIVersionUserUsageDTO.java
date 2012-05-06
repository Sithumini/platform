/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.usage.client.dto;

public class ProviderAPIVersionUserUsageDTO {
    private String version;

    private String user;

    private String count;

    public String getCount(){
        return count;
    }

    public String getVersion(){
        return version;
    }

    public String getUser(){
        return user;
    }

    public void setCount(String count){
        this.count = count;
    }

    public void setVersion(String version){
        this.version = version;
    }

    public void setUser(String user){
        this.user = user;
    }

    public ProviderAPIVersionUserUsageDTO(String version, String user, String count){
        this.version = version;
        this.user = user;
        this.count = count;
    }
}
