---
id: authorization
title: Authorization
sidebar_label: Authorization
---

To simplify management process and reduce complex permission system between users and available projects,
Reposilite uses path based token system.

## Access token
The access token consists of four elements:

* Path - the path covered by the token
* Alias - the short form associated with token
* Permissions - the permissions associated with token
* Token - generated secret token used to access associated path

### Generate token
Tokens are generated using the `keygen` command in Reposilite CLI:

```log
$ keygen <path> <alias> [<permissions>]
```

As an example, we can generate access token for `root` and standard `user`:

```bash
$ keygen / root m
19:55:20.692 INFO | Generated new access token for root (/) with 'm' permissions
19:55:20.692 INFO | AW7-kaXSSXTRVL_Ip9v7ruIiqe56gh96o1XdSrqZCyTX2vUsrZU3roVOfF-YYF-y
19:55:20.723 INFO | Stored tokens: 1

$ keygen com.example.project user w
19:55:20.692 INFO | Generated new access token for user (*/com/example/project) with 'w' permissions
19:55:20.692 INFO | AW7-kaXSSXTRVL_Ip9v7ruIiqe56gh96o1XdSrqZCyTX2vUsrZU3roVOfF-YYF-y
19:55:20.723 INFO | Stored tokens: 2
```

## Properties

### Permission
Currently supported permissions:

* `r` - allows to read repository content *(default)*
* `w` - allows to write *(deploy)* artifacts using this token
* `m` - marks token as manager's *(admin)* token, grants full access to any path in the repository and allows you to access remote CLI through the dashboard

### Path

The path must follow the given pattern: `/{repository}/{gav}` or `x.y.z` gav pattern:
* `{repository}` - required name of repository used to distinguish repositories
* `{gav}` - optional GAV (`group-artifact-version`) path

Some examples:

| Path | Matches | Status |
| :--  | :--     | :--:   |
| /releases | /releases/* | Ok |
| /releases/abc | /releases/abc* | Ok |
| /snapshots | /snapshots/* | Ok |
| / | /releases/* | Ok (if `rewrite-paths` enabled) |
| */ | /* | Ok |

As an example, we can imagine we have several projects located in our repository. 
In most cases, the administrator want to have permission to whole repository, so the credentials for us should look like this:

```properties
path: */
alias: admin
permissions: m
```

Access to requested paths is resolved by comparing the access token path with the beginning of current uri. Our `admin` user associated with `*/` has access to all paths, because all of requests starts with this path separator:

| URI | Status |
| :-- | :----: |
| / | Ok |
| /releases | Ok |
| /releases/groupId/artifactId | Ok |
| /snapshots | Ok |

We also might add some of co-workers to their projects. 
For instance, we will add *Daenerys Targaryen* user as `khaleesi` to `com.hbo.got` project:

```properties
path: /releases/com/hbo/got
alias: khaleesi
```

The access table for the following configuration:

| URI | Status |
| :-- | :----: |
| / | Unauthorized |
| /releases/com/hbo/got | Ok |
| /releases/com/hbo/got/sub-project | Ok |

We recommend to use user-specific names for individual access tokens.
In case of a larger teams, 
it might be a good idea to use project name as an alias and distribute shared access token between them:

```properties
# for a crew
alias: got
# or mixed solution to increase traffic control
alias: got_khaleesi
```

Finally, we can also grant access to multiple repositories using `*` wildcard operator.
As you can see, we have to provide the repository name in access token path. 
In a various situations, we want to maintain `releases` and `snapshots` repositories for the same project.
Instead of generating separate access token, we can just replace repository name with wildcard operator:

```properties
path: */com/hbo/got
alias: khaleesi
```

## Other commands

### List tokens
To display list of all generated tokens, just use `tokens` command in Reposilite CLI:

```bash
$ tokens
23:48:57.880 INFO | Tokens (2)
23:48:57.880 INFO | /releases/auth/test as authtest
23:48:57.880 INFO | / as admin
```

### Revoke tokens
You can revoke token using the `revoke <alias>` command in Reposilite CLI.