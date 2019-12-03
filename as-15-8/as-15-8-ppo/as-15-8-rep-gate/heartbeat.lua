


--local http = require "resty.http"
--local httpc = http.new()
--local jwt = require "resty.jwt"

--local stdin = "hello"
--local timeout = 1000  -- ms
--local max_size = 4096  -- byte

os.execute('/init_eureka.sh')
local delay = 20

local handler
handler = function (premature)
--    os.execute('/heartbeat_eureka.sh')
    ngx.log(ngx.INFO, "Eureka heartbeat sent")
end
ngx.timer.every(delay, handler)
