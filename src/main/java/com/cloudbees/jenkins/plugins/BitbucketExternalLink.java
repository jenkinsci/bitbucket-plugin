package com.cloudbees.jenkins.plugins;

import hudson.model.Action;
import org.jenkins.ui.icon.IconSpec;
import edu.umd.cs.findbugs.annotations.CheckForNull;

/**
 * Sidebar link to an external Bitbucket page.
 */
public class BitbucketExternalLink implements Action, IconSpec {

    private static final String BITBUCKET_ICON_CLASS_NAME = "symbol-logo-bitbucket plugin-ionicons-api";
    private static final String FALLBACK_ICON_CLASS_NAME = "symbol-link-outline plugin-ionicons-api";

    private final String displayName;
    private final String url;

    public BitbucketExternalLink(String displayName, String url) {
        this.displayName = displayName;
        this.url = url;
    }

    @CheckForNull
    @Override
    public String getDisplayName() {
        return displayName;
    }

    @CheckForNull
    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getIconClassName() {
        return hasSymbol("logo-bitbucket") ? BITBUCKET_ICON_CLASS_NAME : FALLBACK_ICON_CLASS_NAME;
    }

    @CheckForNull
    @Override
    public String getUrlName() {
        return url;
    }

    private boolean hasSymbol(String name) {
        return BitbucketExternalLink.class.getClassLoader()
                .getResource("images/symbols/" + name + ".svg") != null;
    }
}
