
var pluginServiceName = 'BackgroundAudioPlayerPlugin';
var actions = {
                play: 'action.play',
                stop: 'action.stop',
                setVolume:'action.set.volume',
                getVolume: 'action.get.volume',
                getStatus: 'action.get.status',
                getCurrentRadio: 'action.get.current.radio',
                scheduleClose:'action.schedule.close',
                cancelScheduledClose:'action.cancel.scheduled.close',
                getTimeToScheduledClose: 'action.get.time.to.scheduled.close'
              };


var playerExport = {};

playerExport.play = function(successCallback, failureCallback, url, radioId){
    cordova.exec(successCallback, failureCallback, pluginServiceName, actions.play, [url, radioId]);
}

playerExport.stop = function(successCallback, failureCallback){
    cordova.exec(successCallback, failureCallback, pluginServiceName, actions.stop, []);
}

playerExport.setVolume = function(successCallback, failureCallback, volume){
    cordova.exec(successCallback, failureCallback, pluginServiceName, actions.setVolume, [volume]);
}

playerExport.getVolume = function(successCallback, failureCallback){
    cordova.exec(successCallback, failureCallback, pluginServiceName, actions.getVolume, []);
}
playerExport.getStatus = function(successCallback, failureCallback){
    cordova.exec(successCallback, failureCallback, pluginServiceName, actions.getStatus, []);
}

playerExport.getCurrentRadio = function(successCallback, failureCallback){
    cordova.exec(successCallback, failureCallback, pluginServiceName, actions.getCurrentRadio, []);
}

playerExport.scheduleClose = function(successCallback, failureCallback, timeToCloseInMinutes){
    cordova.exec(successCallback, failureCallback, pluginServiceName, actions.scheduleClose, [timeToCloseInMinutes]);
}
playerExport.cancelScheduledClose = function(successCallback, failureCallback){
    cordova.exec(successCallback, failureCallback, pluginServiceName, actions.cancelScheduledClose, []);
}
playerExport.getTimeToScheduledClose = function(successCallback, failureCallback){
    cordova.exec(successCallback, failureCallback, pluginServiceName, actions.getTimeToScheduledClose, []);
}
module.exports = playerExport;