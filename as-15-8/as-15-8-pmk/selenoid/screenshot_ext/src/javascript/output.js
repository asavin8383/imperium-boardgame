document.addEventListener("DOMContentLoaded", () => {
    chrome.extension.sendMessage({type: 'getScreenshot'}, function(response) {
        document.getElementById('screen').src = response.screenshot;
    });
});