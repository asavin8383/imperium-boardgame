#!/usr/bin/env bash
/init_eureka.sh
/usr/local/openresty/bin/openresty  -c /nginx.conf -g "daemon off;"