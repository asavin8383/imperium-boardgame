#!/usr/bin/env bash
token=`curl --request POST   --url $GATEWAY_URI'/security/oauth/token?grant_type=client_credentials&clientid='$CLIENTID'&client_secret='$CLIENT_SECRET   --header 'authorization: Basic '${BASIC_AUTH}  | jq -r .access_token`
curl --request POST \
  --url $GATEWAY_URI/eureka/apps/birt-viewer \
  --header 'authorization: Bearer '${token} \
  --header 'content-type: application/json' \
  --data '@/config/eureka_init_'$PROFILE'.json'