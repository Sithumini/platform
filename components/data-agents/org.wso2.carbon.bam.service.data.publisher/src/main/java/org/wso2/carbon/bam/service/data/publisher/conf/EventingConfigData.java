/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.service.data.publisher.conf;


public class EventingConfigData {

    private boolean isServiceStatsEnable;
    private boolean isMsgDumpingEnable;
    private String url;
    private String userName;
    private String password;
    private boolean isHttpTransportEnable;
    private boolean isSocketTransportEnable;
    private int port;

    private String streamName;
    private String version;
    private String nickName;
    private String description;

    private Property[] properties;

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Property[] getProperties() {
        return properties;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    public boolean isHttpTransportEnable() {
        return isHttpTransportEnable;
    }

    public void setHttpTransportEnable(boolean httpTransportEnable) {
        isHttpTransportEnable = httpTransportEnable;
    }

    public boolean isSocketTransportEnable() {
        return isSocketTransportEnable;
    }

    public void setSocketTransportEnable(boolean socketTransportEnable) {
        isSocketTransportEnable = socketTransportEnable;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isServiceStatsEnable() {
        return isServiceStatsEnable;
    }

    public void setServiceStatsEnable(boolean serviceStatsEnable) {
        isServiceStatsEnable = serviceStatsEnable;
    }

    public boolean isMsgDumpingEnable() {
        return isMsgDumpingEnable;
    }

    public void setMsgDumpingEnable(boolean msgDumpingEnable) {
        isMsgDumpingEnable = msgDumpingEnable;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        this.url = url;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
