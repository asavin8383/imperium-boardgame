function checkStatuses400(e) {
    if(String(e.status)[0] !== '4') return false;
    return true;
}

function checkStatus403(e, callback) {
    if(String(e.status)[0] === '4' && e.status === 403){
        callback({message: 'Недостаточно прав доступа'});
        return true;
    }else{
        return false;
    }
}

export default {
    checkStatuses400,
    checkStatus403,
}