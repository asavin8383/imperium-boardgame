let done = arguments[0];
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
                    done(canvas.toDataURL('image/png;base64,').replace('data:image/png;base64,', ''));
                });
            }, 1000);
        });
} catch(err) {
    console.error("Error: " + err);
}