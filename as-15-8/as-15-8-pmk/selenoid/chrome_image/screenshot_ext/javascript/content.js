document.addEventListener('as158_screenshot', function(event){
    chrome.runtime.sendMessage(
        { "type" : "DESKTOP_SCREENSHOT"},
        function(screenshot) {
            let respEvent = new CustomEvent('as158_screenshot_response',
                {
                    detail: { screenshot: screenshot}
                });
            document.dispatchEvent(respEvent);
        });
}, false);