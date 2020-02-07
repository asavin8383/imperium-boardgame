let hintClassName = arguments[0]
let linkClassName = arguments[1];

window.urls = new Set();

const callback = function(mutationsList, observer) {
    for(let mutation of mutationsList) {
        let elem = mutation.target;
        if (typeof elem.querySelector === "function") {
            let links = elem.querySelectorAll('.'+linkClassName);
            for(let link of links) {
                window.urls.add(link.href);
            }
            if(window.urls.size > 0) {
                let hints = document.getElementsByClassName(hintClassName);
                for(let hint of hints) {
                    hint.style.display = "block";
                }
            }
        }
    }
};

const config = { attributes: false, childList: true, subtree: true };
window.observer = new MutationObserver(callback);
window.observer.observe(document.body, config);