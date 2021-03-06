package org.zalando.stups.fullstop.teams;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Preconditions;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.RequestEntity.get;

public class RestTemplateTeamOperations implements TeamOperations {

    private final ParameterizedTypeReference<List<Account>> userTeamListType =
            new ParameterizedTypeReference<List<Account>>() {
            };

    private final ParameterizedTypeReference<List<Account>> accountType =
            new ParameterizedTypeReference<List<Account>>() {
            };

    private final ParameterizedTypeReference<Set<Team>> team =
            new ParameterizedTypeReference<Set<Team>>() {
            };

    private final RestOperations restOperations;

    private final String baseUrl;

    public RestTemplateTeamOperations(final RestOperations restOperations, final String baseUrl) {
        this.restOperations = restOperations;
        this.baseUrl = baseUrl;
    }

    @Override
    public List<Account> getAwsAccountsByUser(final String userId) {
        Preconditions.checkArgument(StringUtils.hasText(userId), "userId must not be blank");

        final ResponseEntity<List<Account>> response = restOperations.exchange(
                get(URI.create(baseUrl + "/api/accounts/aws?member=" + userId)).build(), userTeamListType);
        Preconditions.checkState(response.getStatusCode().is2xxSuccessful(), "getAwsAccountsByUser failed: %s", response);
        return response.getBody();
    }

    @Override
    public Set<String> getTeamIdsByUser(final String userId) {
        Preconditions.checkArgument(StringUtils.hasText(userId), "userId must not be blank");

        return getTeamsByUser(userId).stream().map(Team::getId).
                filter(Objects::nonNull).
                map(String::trim).
                filter(string -> !string.isEmpty()).
                collect(toSet());
    }

    private Set<Team> getTeamsByUser(final String userId) {
        final ResponseEntity<Set<Team>> response = restOperations.exchange(
                get(URI.create(baseUrl + "/api/teams?member=" + userId)).build(), team);
        Preconditions.checkState(response.getStatusCode().is2xxSuccessful(), "getTeamIdsByUser failed: %s", response);
        return response.getBody();
    }

    @Override
    public List<Account> getActiveAccounts() {
        final ResponseEntity<List<Account>> response = restOperations.exchange(
                get(URI.create(baseUrl + "/api/accounts/aws")).build(), accountType);
        Preconditions.checkState(response.getStatusCode().is2xxSuccessful(), "getAccounts failed: %s", response);
        return response.getBody().parallelStream().filter(account -> !account.isDisabled()).collect(toList());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Team {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(final String id) {
            this.id = id;
        }
    }
}
