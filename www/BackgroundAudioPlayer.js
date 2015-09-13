
var pluginServiceName = 'BackgroundAudioPlayer';
var actions = {
                play: 'action.play',
                stop: 'action.stop',
                setVolume:'action.set.volume',
                getVolume: 'action.get.volume',
                getStatus: 'action.get.status'
              };
var playerExport = {};

playerExport.play = function(successCallback, failureCallback, url){
    cordova.exec(successCallback, failureCallback, pluginServiceName, actions.play, [url]);
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
    cordova.exec(successCallback, failureCallback, pluginServiceName, actions.getStatus);
}
module.exports = playerExport;