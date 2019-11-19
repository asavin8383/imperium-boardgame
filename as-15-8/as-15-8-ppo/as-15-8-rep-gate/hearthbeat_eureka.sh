#!/usr/bin/env bash
token=`curl --request POST   --url $GATEWAY_URI'/security/oauth/token?grant_type=client_credentials&clientid='$CLIENTID'&client_secret='$CLIENT_SECRET   --header 'authorization: Basic '${BASIC_AUTH}  | jq -r .access_token`
curl --request PUT \
  --url $GATEWAY_URI/eureka/apps/birt-viewer/birt-viewer:192.168.5.50:15880 \
  --header 'authorization: Bearer '$token