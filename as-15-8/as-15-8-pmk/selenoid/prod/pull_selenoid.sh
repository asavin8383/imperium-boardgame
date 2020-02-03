#!/bin/bash
PULL_CMD="docker pull dcr.ec-leasing.ru/as-15-8/selenoid/vnc_chrome:78 && docker pull dcr.ec-leasing.ru/as-15-8/selenoid/vnc_chrome_hola:75-1.145";

for NODE in $(docker node ls --format '{{.Hostname}}')
do
  IP_ADDR="$(docker node inspect --format '{{.Status.Addr}}' "${NODE}")";
  if [ "$IP_ADDR" = "0.0.0.0" ]; then
    eval $PULL_CMD 1>/dev/null;
  else
    ssh "master@$IP_ADDR" "$PULL_CMD" 1>/dev/null;
    echo -e "${IP_ADDR} pull images success";
  fi
done