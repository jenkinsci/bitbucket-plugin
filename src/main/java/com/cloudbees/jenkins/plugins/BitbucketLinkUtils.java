package com.cloudbees.jenkins.plugins;

import com.cloudbees.jenkins.plugins.bitbucket.BitbucketSCMSource;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import jenkins.plugins.git.GitSCMSource;
import jenkins.scm.api.SCMSource;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

final class BitbucketLinkUtils {

    static final String REPOSITORY_LINK_NAME = "Browse Repository";
    static final String BRANCH_LINK_NAME = "View Branch";

    Optional<BitbucketExternalLink> createRepoLink(SCMSource source) {
        return resolve(source)
                .map(remote -> new BitbucketExternalLink(REPOSITORY_LINK_NAME, remote.toRepositoryUrl()));
    }

    Optional<BitbucketExternalLink> createBranchLink(SCMSource source, String branch) {
        if (branch == null || branch.isBlank()) {
            return Optional.empty();
        }
        return resolve(source)
                .map(remote -> new BitbucketExternalLink(BRANCH_LINK_NAME, remote.toBranchUrl(branch)));
    }

    Optional<BitbucketExternalLink> createRepoLink(SCM scm) {
        return resolve(scm)
                .map(remote -> new BitbucketExternalLink(REPOSITORY_LINK_NAME, remote.toRepositoryUrl()));
    }

    Optional<BitbucketRemote> resolve(SCMSource source) {
        if (source instanceof GitSCMSource) {
            return parseRemote(((GitSCMSource) source).getRemote());
        }
        if (source instanceof BitbucketSCMSource) {
            BitbucketSCMSource bitbucketSource = (BitbucketSCMSource) source;
            return fromCoordinates(
                    bitbucketSource.getServerUrl(),
                    bitbucketSource.getRepoOwner(),
                    bitbucketSource.getRepository()
            );
        }
        return Optional.empty();
    }

    Optional<BitbucketRemote> resolve(SCM scm) {
        if (!(scm instanceof GitSCM)) {
            return Optional.empty();
        }
        List<RemoteConfig> repositories = ((GitSCM) scm).getRepositories();
        for (RemoteConfig repository : repositories) {
            for (URIish uri : repository.getURIs()) {
                Optional<BitbucketRemote> remote = parseRemote(uri.toString());
                if (remote.isPresent()) {
                    return remote;
                }
            }
        }
        return Optional.empty();
    }

    Optional<BitbucketRemote> parseRemote(@CheckForNull String remote) {
        if (remote == null || remote.isBlank()) {
            return Optional.empty();
        }
        try {
            URIish uri = new URIish(remote);
            String host = uri.getHost();
            String path = stripSlashes(uri.getPath());
            if (host == null || host.isBlank() || path == null || path.isBlank()) {
                return Optional.empty();
            }
            if (isBitbucketCloudHost(host)) {
                String[] segments = path.split("/");
                if (segments.length < 2) {
                    return Optional.empty();
                }
                return Optional.of(BitbucketRemote.cloud(httpsBaseUrl(host), segments[0], stripGitSuffix(segments[1])));
            }

            boolean allowSimpleServerPath = uri.getScheme() == null || !"http".equalsIgnoreCase(uri.getScheme())
                    && !"https".equalsIgnoreCase(uri.getScheme());
            ParsedServerPath parsedServerPath = ParsedServerPath.from(path, allowSimpleServerPath);
            if (parsedServerPath == null) {
                return Optional.empty();
            }
            return Optional.of(BitbucketRemote.server(
                    httpsBaseUrl(host, parsedServerPath.contextPath()),
                    parsedServerPath.projectKey(),
                    parsedServerPath.repositorySlug()
            ));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private Optional<BitbucketRemote> fromCoordinates(@CheckForNull String serverUrl,
                                                      @CheckForNull String ownerOrProject,
                                                      @CheckForNull String repository) {
        if (serverUrl == null || serverUrl.isBlank() || ownerOrProject == null || ownerOrProject.isBlank()
                || repository == null || repository.isBlank()) {
            return Optional.empty();
        }
        String normalizedServerUrl = trimTrailingSlash(serverUrl);
        try {
            URIish uri = new URIish(normalizedServerUrl);
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return Optional.empty();
            }
            if (isBitbucketCloudHost(host)) {
                return Optional.of(BitbucketRemote.cloud(normalizedServerUrl, ownerOrProject, stripGitSuffix(repository)));
            }
            return Optional.of(BitbucketRemote.server(normalizedServerUrl, ownerOrProject, stripGitSuffix(repository)));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static boolean isBitbucketCloudHost(String host) {
        return "bitbucket.org".equalsIgnoreCase(host);
    }

    private static String httpsBaseUrl(String host) {
        return "https://" + host;
    }

    private static String httpsBaseUrl(String host, String contextPath) {
        if (contextPath.isBlank()) {
            return httpsBaseUrl(host);
        }
        return httpsBaseUrl(host) + "/" + contextPath;
    }

    private static String stripGitSuffix(String value) {
        return value.endsWith(".git") ? value.substring(0, value.length() - 4) : value;
    }

    private static String stripSlashes(@CheckForNull String value) {
        if (value == null) {
            return null;
        }
        String stripped = value;
        while (stripped.startsWith("/")) {
            stripped = stripped.substring(1);
        }
        while (stripped.endsWith("/")) {
            stripped = stripped.substring(0, stripped.length() - 1);
        }
        return stripped;
    }

    private static String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static String encodeQuery(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String encodePath(String value) {
        return encodeQuery(value).replace("+", "%20");
    }

    static final class BitbucketRemote {
        private final Kind kind;
        private final String baseUrl;
        private final String ownerOrProject;
        private final String repositorySlug;

        private BitbucketRemote(Kind kind, String baseUrl, String ownerOrProject, String repositorySlug) {
            this.kind = kind;
            this.baseUrl = trimTrailingSlash(baseUrl);
            this.ownerOrProject = ownerOrProject;
            this.repositorySlug = repositorySlug;
        }

        static BitbucketRemote cloud(String baseUrl, String workspace, String repositorySlug) {
            return new BitbucketRemote(Kind.CLOUD, baseUrl, workspace, repositorySlug);
        }

        static BitbucketRemote server(String baseUrl, String projectKey, String repositorySlug) {
            return new BitbucketRemote(Kind.SERVER, baseUrl, projectKey, repositorySlug);
        }

        String toRepositoryUrl() {
            if (kind == Kind.CLOUD) {
                return baseUrl + "/" + ownerOrProject + "/" + repositorySlug;
            }
            return baseUrl + "/projects/" + ownerOrProject + "/repos/" + repositorySlug;
        }

        String toBranchUrl(String branch) {
            if (kind == Kind.CLOUD) {
                return toRepositoryUrl() + "/branch/" + encodePath(branch);
            }
            return toRepositoryUrl() + "/compare/commits?sourceBranch=" + encodeQuery("refs/heads/" + branch);
        }
    }

    private enum Kind {
        CLOUD,
        SERVER
    }

    private record ParsedServerPath(String contextPath, String projectKey, String repositorySlug) {
        static ParsedServerPath from(String rawPath, boolean allowSimplePath) {
            String[] segments = rawPath.split("/");
            if (segments.length >= 3) {
                for (int i = 0; i < segments.length; i++) {
                    if ("scm".equalsIgnoreCase(segments[i]) && i + 2 < segments.length) {
                        return new ParsedServerPath(
                                String.join("/", java.util.Arrays.copyOfRange(segments, 0, i)),
                                segments[i + 1],
                                stripGitSuffix(segments[i + 2])
                        );
                    }
                    if ("projects".equalsIgnoreCase(segments[i]) && i + 3 < segments.length
                            && "repos".equalsIgnoreCase(segments[i + 2])) {
                        return new ParsedServerPath(
                                String.join("/", java.util.Arrays.copyOfRange(segments, 0, i)),
                                segments[i + 1],
                                stripGitSuffix(segments[i + 3])
                        );
                    }
                }
            }
            if (allowSimplePath && segments.length == 2 && !"scm".equalsIgnoreCase(segments[0])) {
                return new ParsedServerPath("", segments[0].toUpperCase(Locale.ENGLISH), stripGitSuffix(segments[1]));
            }
            return null;
        }
    }
}
