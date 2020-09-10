local http = require "resty.http"
local httpc = http.new()
local jwt = require "resty.jwt"

    local token  = ngx.var.cookie_COOKIE_BEARER

if token == nil then
   token = ngx.var.arg_token
end
if token == nil then
    ngx.status = ngx.HTTP_UNAUTHORIZED
    ngx.header.content_type = "application/json; charset=utf-8"
    ngx.say("{\"error\": \"missing JWT token or Authorization header\"}")
    ngx.exit(ngx.HTTP_UNAUTHORIZED)
end


local gateway_url=os.getenv("GATEWAY_URI")
local basic_auth=os.getenv("BASIC_AUTH")
local res, err = httpc:request_uri(gateway_url .. "/security/oauth/check_token",
 { method = "POST", query="token=" .. token, headers={authorization ="Basic " .. basic_auth}})

if res.status ~= 200 then
    ngx.status = ngx.HTTP_UNAUTHORIZED
    ngx.log(ngx.WARN, res.reason, res.status)
    ngx.header.content_type = "application/json; charset=utf-8"
    ngx.say("{\"error\": \"" .. res.reason .. "\",\"status\":" .. res.status .. "}")
    ngx.exit(ngx.HTTP_UNAUTHORIZED)
end
