package com.cloudbees.jenkins.plugins;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.UnprotectedRootAction;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.GitStatus;
import hudson.scm.SCM;
import hudson.security.ACL;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
@Extension
public class BitbucketHookReceiver implements UnprotectedRootAction {


    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return "bitbucket-hook";
    }

    /**
     * Bitbucket send <a href="https://confluence.atlassian.com/display/BITBUCKET/Write+brokers+(hooks)+for+Bitbucket">payload</a>
     * as form-urlencoded <pre>payload=JSON</pre>
     * @throws IOException
     */
    public void doIndex(StaplerRequest req) throws IOException {
        String body = IOUtils.toString(req.getInputStream());
        String contentType = req.getContentType();
        if (contentType != null && contentType.startsWith("application/x-www-form-urlencoded")) {
            body = URLDecoder.decode(body);
        }
        if (body.startsWith("payload=")) body = body.substring(8);

        LOGGER.fine("Received commit hook notification : " + body);
        JSONObject payload = JSONObject.fromObject(body);
        processPayload(payload);
    }
    
/*
{
    "push": {
        "changes": [{
            "old": {
                "type": "branch",
                "target": {
                    "date": "2015-06-22T21:47:01+00:00",
                    "type": "commit",
                    "author": {
                        "raw": "author <author@somedomain.com>"
                    },
                    "message": "some message\n",
                    "links": {
                        "html": {
                            "href": "https://bitbucket.org/owner/somerepo/commits/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        },
                        "self": {
                            "href": "https://bitbucket.org/api/2.0/repositories/owner/somerepo/commit/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        }
                    },
                    "hash": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                    "parents": [{
                        "type": "commit",
                        "hash": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                        "links": {
                            "html": {
                                "href": "https://bitbucket.org/owner/somerepo/commits/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                            },
                            "self": {
                                "href": "https://bitbucket.org/api/2.0/repositories/owner/somerepo/commit/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                            }
                        }
                    }]
                },
                "name": "master",
                "links": {
                    "html": {
                        "href": "https://bitbucket.org/owner/somerepo/branch/master"
                    },
                    "commits": {
                        "href": "https://bitbucket.org/api/2.0/repositories/owner/somerepo/commits/master"
                    },
                    "self": {
                        "href": "https://bitbucket.org/api/2.0/repositories/owner/somerepo/refs/branches/master"
                    }
                }
            },
            "closed": false,
            "links": {
                "html": {
                    "href": "https://bitbucket.org/owner/somerepo/branches/compare/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx..xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                },
                "commits": {
                    "href": "https://bitbucket.org/api/2.0/repositories/owner/somerepo/commits?include=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxexclude=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                },
                "diff": {
                    "href": "https://bitbucket.org/api/2.0/repositories/owner/somerepo/diff/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx..xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                }
            },
            "forced": false,
            "new": {
                "type": "branch",
                "target": {
                    "date": "2015-06-22T22:20:08+00:00",
                    "type": "commit",
                    "author": {
                        "raw": "author <author@somedomain.com>"
                    },
                    "message": "some message\n",
                    "links": {
                        "html": {
                            "href": "https://bitbucket.org/owner/somerepo/commits/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        },
                        "self": {
                            "href": "https://bitbucket.org/api/2.0/repositories/owner/somerepo/commit/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                        }
                    },
                    "hash": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                    "parents": [{
                        "type": "commit",
                        "hash": "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
                        "links": {
                            "html": {
                                "href": "https://bitbucket.org/owner/somerepo/commits/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                            },
                            "self": {
                                "href": "https://bitbucket.org/api/2.0/repositories/owner/somerepo/commit/xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
                            }
                        }
                    }]
                },
                "name": "master",
                "links": {
                    "html": {
                        "href": "https://bitbucket.org/owner/somerepo/branch/master"
                    },
                    "commits": {
                        "href": "https://bitbucket.org/api/2.0/repositories/owner/somerepo/commits/master"
                    },
                    "self": {
                        "href": "https://bitbucket.org/api/2.0/repositories/owner/somerepo/refs/branches/master"
                    }
                }
            },
            "created": false
        }]
    },
    "actor": {
        "type": "team",
        "uuid": "{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}",
        "username": "owner",
        "display_name": "Owner",
        "links": {
            "avatar": {
                "href": "https://bitbucket.org/account/owner/avatar/32/"
            },
            "html": {
                "href": "https://bitbucket.org/owner"
            },
            "self": {
                "href": "https://bitbucket.org/api/2.0/teams/owner"
            }
        }
    },
    "repository": {
        "full_name": "owner/somerepo",
        "owner": {
            "type": "team",
            "uuid": "{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}",
            "username": "owner",
            "display_name": "Owner",
            "links": {
                "avatar": {
                    "href": "https://bitbucket.org/account/owner/avatar/32/"
                },
                "html": {
                    "href": "https://bitbucket.org/owner"
                },
                "self": {
                    "href": "https://bitbucket.org/api/2.0/teams/owner"
                }
            }
        },
        "type": "repository",
        "name": "somerepo",
        "uuid": "{xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx}",
        "links": {
            "avatar": {
                "href": "https://bitbucket.org/owner/somerepo/avatar/16/"
            },
            "html": {
                "href": "https://bitbucket.org/owner/somerepo"
            },
            "self": {
                "href": "https://bitbucket.org/api/2.0/repositories/owner/somerepo"
            }
        }
    }
}
*/
    private void processPayload(JSONObject payload) {
    	
        JSONObject repo = payload.getJSONObject("repository");
        JSONObject actor = payload.getJSONObject("actor");
        String user = actor.getString("username");
        String url = repo.getJSONObject("links").getJSONObject("html").getString("href");
        
        LOGGER.info("Received commit hook notification for "+repo);

        SecurityContext old = Jenkins.getInstance().getACL().impersonate(ACL.SYSTEM);
        try {
            URIish remote = new URIish(url);
            for (AbstractProject<?,?> job : Hudson.getInstance().getAllItems(AbstractProject.class)) {
                LOGGER.info("considering candidate job " + job.getName());
                BitBucketTrigger trigger = job.getTrigger(BitBucketTrigger.class);
                if (trigger!=null) {
                    if (match(job.getScm(), remote)) {
                    	trigger.onPost(user);
                    } else LOGGER.info("job SCM doesn't match remote repo");
                } else LOGGER.info("job hasn't BitBucketTrigger set");
            }
        } catch (URISyntaxException e) {
            LOGGER.warning("invalid repository URL " + url);
        } finally {
            SecurityContextHolder.setContext(old);
        }
    }
    
    private boolean match(SCM scm, URIish url) {
        if (scm instanceof GitSCM) {
            for (RemoteConfig remoteConfig : ((GitSCM) scm).getRepositories()) {
                for (URIish urIish : remoteConfig.getURIs()) {
                    if (GitStatus.looselyMatches(urIish, url))
                        return true;
                }
            }
        }
        return false;
    }

    private static final Logger LOGGER = Logger.getLogger(BitbucketHookReceiver.class.getName());

}
