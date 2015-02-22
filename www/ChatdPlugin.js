
var ChatdPlugin = function(){}

ChatdPlugin.prototype.startService = function() {
    successCallback = function() {};
	errorCallback = function() {};
    cordova.exec(successCallback, errorCallback, "ChatdPlugin", "startService", []);
};

ChatdPlugin.prototype.stopService = function() {
    successCallback = function() {};
	errorCallback = function() {};
    cordova.exec(successCallback, errorCallback, "ChatdPlugin", "stopService", []);
};

if(!window.plugins) {
    window.plugins = {};
}
if(!window.plugins.chatdPlugin) {
    window.plugins.chatdPlugin = new ChatdPlugin();
}

if(typeof module != 'undefined' && module.exports) {
	module.exports = ChatdPlugin;
}