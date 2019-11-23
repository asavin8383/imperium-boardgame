let screenshot;

chrome.contextMenus.create({
    title: "AS-15-8-Screenshot",
    contexts:["page", "selection", "image", "link"],
    onclick: openExt
});

function openExt(info, tab){
    chrome.tabs.create({
        url: '/popup.html',
        active: true
    });
}

chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
    if (request.type === 'takeScreenshot') {
        try {
            const displayMediaOptions = {
                video: {
                    cursor: "never"
                },
                audio: false
            };
            navigator.mediaDevices.getDisplayMedia(displayMediaOptions)
                .then(stream => {
                    setTimeout(function () {
                        let track = stream.getVideoTracks()[0];
                        let imgCapture = new ImageCapture(track);
                        imgCapture.grabFrame()
                            .then(bitmap => {
                                let canvas = document.createElement('canvas');
                                canvas.width = bitmap.width;
                                canvas.height = bitmap.height;
                                let context = canvas.getContext('2d');
                                context.drawImage(bitmap, 0, 0);
                                screenshot = canvas.toDataURL('image/png;base64,');
                                chrome.tabs.create({
                                    url: '/screenshot.html',
                                    active: true
                                });
                                sendResponse();
                            }).catch(ex => {
                                sendResponse("Error: " + ex.toString())
                            });
                    }, 3000);
                }).catch(ex => {
                    sendResponse("Error: " + ex.toString());
                });
        } catch(ex) {
            sendResponse("Error: " + ex.toString());
        };
    }
    if (request.type === 'getScreenshot') {
        sendResponse({screenshot: screenshot});
    }
    return true;
});