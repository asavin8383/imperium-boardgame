document.getElementById('screen').addEventListener('click', takeScreenshot);

function takeScreenshot() {
    chrome.extension.sendMessage({"type": "takeScreenshot"});
    window.close();
}