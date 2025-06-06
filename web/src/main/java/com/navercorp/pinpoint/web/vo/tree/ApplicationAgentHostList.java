/*
 *  Copyright 2016 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.web.vo.tree;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Taejin Koo
 */
public class ApplicationAgentHostList {

    private final int startApplicationIndex;
    private final int endApplicationIndex;
    private final int totalApplications;

    private final List<ApplicationInfo> applications;

    public ApplicationAgentHostList(int startApplicationIndex, int endApplicationIndex, int totalApplications,
                                    List<ApplicationInfo> applications) {
        this.startApplicationIndex = startApplicationIndex;
        this.endApplicationIndex = endApplicationIndex;
        this.totalApplications = totalApplications;
        this.applications = Objects.requireNonNull(applications, "applications");
    }

    @JsonProperty("startIndex")
    public int getStartApplicationIndex() {
        return startApplicationIndex;
    }

    @JsonProperty("endIndex")
    public int getEndApplicationIndex() {
        return endApplicationIndex;
    }

    @JsonProperty("totalApplications")
    public int getTotalApplications() {
        return totalApplications;
    }

    public List<ApplicationInfo> getApplications() {
        return applications;
    }

    @Override
    public String toString() {
        return "ApplicationAgentHostList{" +
                "startApplicationIndex=" + startApplicationIndex +
                ", endApplicationIndex=" + endApplicationIndex +
                ", totalApplications=" + totalApplications +
                ", applications=" + applications +
                '}';
    }

    public static Builder newBuilder(int startApplicationIndex, int endApplicationIndex, int totalApplications) {
        return new Builder(startApplicationIndex, endApplicationIndex, totalApplications);
    }

    public static class Builder {

        private static final Comparator<ApplicationInfo> APPLICATION_NAME_COMPARATOR = Comparator.comparing(ApplicationInfo::applicationName);
        private static final Comparator<AgentHost> AGENTID_COMPARING = Comparator.comparing(AgentHost::agentId);

        private final int startApplicationIndex;
        private final int endApplicationIndex;
        private final int totalApplications;

        private final Map<String, List<AgentHost>> map = new HashMap<>();

        public Builder(int startApplicationIndex, int endApplicationIndex, int totalApplications) {
            this.startApplicationIndex = startApplicationIndex;
            this.endApplicationIndex = endApplicationIndex;
            this.totalApplications = totalApplications;
        }


        public void addAgentInfo(String applicationName, List<AgentInfo> agentInfoList) {
            if (applicationName == null) {
                return;
            }

            List<AgentHost> value = map.computeIfAbsent(applicationName, k -> new ArrayList<>());

            if (agentInfoList == null) {
                return;
            }

            for (AgentInfo agentInfo : agentInfoList) {
                if (agentInfo != null) {
                    value.add(newAgentHost(agentInfo));
                }
            }
        }

        private AgentHost newAgentHost(AgentInfo agentInfo) {
            String agentId = Objects.toString(agentInfo.getAgentId(), "");
            String hostName = Objects.toString(agentInfo.getHostName(), "");
            String ip = Objects.toString(agentInfo.getIp(), "");
            String serviceType = agentInfo.getServiceType().getDesc();
            return new AgentHost(agentId, hostName, ip, serviceType);
        }

        public ApplicationAgentHostList build() {
            List<ApplicationInfo> applicationInfos = buildApplicationInfo(this.map);
            ApplicationAgentHostList agents = new ApplicationAgentHostList(startApplicationIndex, endApplicationIndex, totalApplications,
                    applicationInfos);
            return agents;
        }

        private List<ApplicationInfo> buildApplicationInfo(Map<String, List<AgentHost>> map) {
            List<ApplicationInfo> applications = map.entrySet().stream()
                    .map(Builder::newApplication)
                    .sorted(APPLICATION_NAME_COMPARATOR)
                    .collect(Collectors.toList());
            return applications;
        }


        private static ApplicationInfo newApplication(Map.Entry<String, List<AgentHost>> entry) {
            String applicationName = entry.getKey();

            List<AgentHost> agentHosts = entry.getValue();
            agentHosts.sort(AGENTID_COMPARING);

            return new ApplicationInfo(applicationName, agentHosts);
        }
    }

    public record ApplicationInfo(String applicationName, List<AgentHost> agents) {
    }

    public record AgentHost(String agentId, String hostName, String ip, String serviceType) {
    }

}