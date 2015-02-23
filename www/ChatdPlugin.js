
var ChatdPlugin = function(){}

ChatdPlugin.prototype.start = function() {
    successCallback = function() {};
	errorCallback = function() {};
	
    cordova.exec(successCallback, errorCallback, "ChatdPlugin", "start", []);
};

ChatdPlugin.prototype.stop = function() {
    successCallback = function() {};
	errorCallback = function() {};
    cordova.exec(successCallback, errorCallback, "ChatdPlugin", "stop", []);
};

ChatdPlugin.prototype.connect = function(successCallback,id,password,deviceId) {
	if(!successCallback){successCallback = function() {};}
	errorCallback = function() {};
	
    cordova.exec(successCallback, errorCallback, "ChatdPlugin", "connect", [id,password,deviceId]);
};

ChatdPlugin.prototype.send = function(message) {
    successCallback = function() {};
	errorCallback = function() {};
    cordova.exec(successCallback, errorCallback, "ChatdPlugin", "send", [message]);
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
