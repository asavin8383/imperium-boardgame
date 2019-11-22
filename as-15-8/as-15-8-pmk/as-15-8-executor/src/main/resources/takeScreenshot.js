let done = arguments[0];
let evt = new Event('screenshot');
evt.initEvent('as158_screenshot', true, true);
document.dispatchEvent(evt);
document.addEventListener('as158_screenshot_response', function(event){
    done(event.detail.screenshot);
});