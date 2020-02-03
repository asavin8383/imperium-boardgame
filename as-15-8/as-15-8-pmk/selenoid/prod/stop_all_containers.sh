#!/bin/bash
PULL_CMD="docker container stop \$(docker container ls -aq) && docker container prune";

for NODE in $(docker node ls --format '{{.Hostname}}')
do
  IP_ADDR="$(docker node inspect --format '{{.Status.Addr}}' "${NODE}")";
  if [ "$IP_ADDR" = "0.0.0.0" ]; then
    eval $PULL_CMD 1>/dev/null;
  else
    ssh "master@$IP_ADDR" "$PULL_CMD" 1>/dev/null;
    echo -e "${IP_ADDR} containers clear success";
  fi
done