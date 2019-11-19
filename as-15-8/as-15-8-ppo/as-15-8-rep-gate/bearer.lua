local http = require "resty.http"
local httpc = http.new()
local jwt = require "resty.jwt"

-- first try to find JWT token as url parameter e.g. ?token=BLAH
-- next try to find JWT token as Cookie e.g. token=BLAH
local   token = nil
-- try to find JWT token in Authorization header Bearer string
    local auth_header = ngx.var.http_Authorization
    if auth_header then
        _, _, token = string.find(auth_header, "Bearer%s+(.+)")
    end

    local token2 = ngx.var.cookie_COOKIE_BEARER
    if token2  == nil then
        ngx.header["Set-Cookie"] = "COOKIE_BEARER=" .. token .. "; path=/; HttpOnly"
    else
       token=token2
    end




-- finally, if still no JWT token, kick out an error and exit
if token == nil then
    ngx.status = ngx.HTTP_UNAUTHORIZED
    ngx.header.content_type = "application/json; charset=utf-8"
    ngx.say("{\"error\": \"missing JWT token or Authorization header\"}")
    ngx.exit(ngx.HTTP_UNAUTHORIZED)
end

-- validate any specific claims you need here
-- https://github.com/SkyLothar/lua-resty-jwt#jwt-validators
local validators = require "resty.jwt-validators"
local claim_spec = {
    -- validators.set_system_leeway(15), -- time in seconds
    -- exp = validators.is_not_expired(),
    -- iat = validators.is_not_before(),
    -- iss = validators.opt_matches("^http[s]?://yourdomain.auth0.com/$"),
    -- sub = validators.opt_matches("^[0-9]+$"),
    -- name = validators.equals_any_of({ "John Doe", "Mallory", "Alice", "Bob" }),
}

-- make sure to set and put "env JWT_SECRET;" in nginx.conf
local gateway_url=os.getenv("GATEWAY_URI")

local res, err = httpc:request_uri(gateway_url .. "/security/oauth/check_token",
 { method = "POST", query="token=" .. token, headers={authorization ="Basic YmlydC12aWV3ZXI6MXEwcDJ3OW8="}})

if res.status ~= 200 then
    ngx.status = ngx.HTTP_UNAUTHORIZED
    ngx.log(ngx.WARN, res.reason)
    ngx.header.content_type = "application/json; charset=utf-8"
    ngx.say("{\"error\": \"" .. res.reason .. "\",\"status\":" .. res.status .. "}")
    ngx.exit(ngx.HTTP_UNAUTHORIZED)
end

-- optionally set Authorization header Bearer token style regardless of how token received
-- if you want to forward it by setting your nginx.conf something like:
--     proxy_set_header Authorization $http_authorization;`
ngx.req.set_header("Authorization", "Bearer " .. token)
