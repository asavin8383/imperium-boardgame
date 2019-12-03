#!/usr/bin/env bash
token=`curl  -s --request POST   --url $GATEWAY_URI'/security/oauth/token?grant_type=client_credentials&clientid='$CLIENTID'&client_secret='$CLIENT_SECRET   --header 'authorization: Basic '${BASIC_AUTH}  | jq -r .access_token`
instanceId=`cat /config/eureka_init_test.json | jq -r .instance.instanceId`
echo "sending hearthbeat ..."
curl -s -I --request PUT \
  --url $GATEWAY_URI/eureka/apps/birt-viewer/$instanceId \
  --header 'authorization: Bearer '$token > /dev/null