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
            beginDesktop(sendResponse)
        } catch(ex) {
            sendResponse("Error: " + ex.toString());
        };
    }
    if (request.type === 'getScreenshot') {
        sendResponse({screenshot: screenshot});
    }
    return true;
});

function beginDesktop(sendResponse) {
    chrome.desktopCapture.chooseDesktopMedia(["screen", "window"], function (e) {
        if (e) {
            var t = {
                audio: !1,
                video: {
                    mandatory: {
                        chromeMediaSource: "desktop",
                        chromeMediaSourceId: e,
                        maxWidth: 2560,
                        maxHeight: 1440
                    }
                }
            };
            window.navigator.webkitGetUserMedia(t, function (r) {

                let o = document.createElement("video");
                o.setAttribute("autoplay", "true"), o.addEventListener("play", function () {
                    setTimeout(function () {
                        let e = document.createElement("canvas"), t = e.getContext("2d");
                        e.width = o.videoWidth;
                        e.height = o.videoHeight;
                        t.drawImage(o, 0, 0, e.width, e.height);
                        screenshot = e.toDataURL('image/png;base64,'), o.pause(), o.srcObject = null, r.getVideoTracks()[0].stop(), o.remove(), e.remove();
                        chrome.tabs.create({
                            url: '/screenshot.html',
                            active: true
                        });
                        sendResponse();
                    }, 300)
                }, !1), o.srcObject = r
            }, function (e) {
                alert(e)
            })
        }
    })
}