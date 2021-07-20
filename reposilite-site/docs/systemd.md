---
id: systemd
title: Systemd
sidebar_label: Systemd
---

You can launch Reposilite as a service using [systemd](https://en.wikipedia.org/wiki/Systemd).
Here is an example configuration of `/etc/systemd/system/reposilite.service` file:

```conf
[Unit]
Description=Reposilite Service

[Service]
# Non-root user
User=reposilite-user
# The configuration file application.properties should be here:
# Change this to your workspace
WorkingDirectory=/opt/reposilite
# Path to Reposilite executable/script and its configuration.
ExecStart=java -jar reposilite-2.9.22-SNAPSHOT.jar --config=/etc/reposilite/reposilite.cdn
# Policy
SuccessExitStatus=0
TimeoutStopSec=10
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Related GitHub Issue: [#468](https://github.com/dzikoysk/reposilite/issues/468)