FROM ubergarm/openresty-nginx-jwt

RUN /usr/local/openresty/luajit/bin/luarocks install lua-resty-http

ENV JWT_SECRET qwerty
ENV GATEWAY_URI http://192.168.5.50:15880
ENV PROFILE test
ENV BASIC_AUTH YmlydC12aWV3ZXI6MXEwcDJ3OW8=
ENV CLIENTID birt-viewer
ENV CLIENT_SECRET 1q0p2w9o

RUN apk update \
 && apk add jq \
 && rm -rf /var/cache/apk/*

COPY init_eureka.sh /init_eureka.sh
COPY run.sh /run.sh
RUN chmod +x /init_eureka.sh && chmod +x /run.sh
COPY nginx.conf /nginx.conf
COPY bearer.lua /bearer.lua

ENTRYPOINT ["/run.sh"]