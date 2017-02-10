var system = {
		token:null,
		rsaServerKey:null,
		rsaClientKey:null,
		init:function(serverPublicKey,keyLength){
			this.rsaServerKey = cryptico.publicKeyFromString(serverPublicKey) ;
			this.rsaClientKey = cryptico.generateRSAKey("pass",keyLength) ;
		},
		forwardParams:null,
		userName:null
} ;
originalParseInt = parseInt; 
parseInt = function (){ 
	if(arguments.length == 1){ 
		return originalParseInt(arguments[0],10); 
	} else { 
		return originalParseInt(arguments[0], arguments[1]); 
	}
}

function rsaEncrypt(text){
	if(text == null || text == undefined || text == ""){
		return text;
	}
	return hex2b64(system.rsaServerKey.encrypt(text)) ;
}

function rsaDecrypt(text){
	if(text == null || text == undefined || text == ""){
		return text;
	}
	return system.rsaClientKey.decrypt(text) ;
}
String.prototype.format = function(args) {
    var result = this;
    if (arguments.length > 0) {    
        if (arguments.length == 1 && typeof (args) == "object") {
            for (var key in args) {
                if(args[key]!=undefined){
                    var reg = new RegExp("({" + key + "})", "g");
                    result = result.replace(reg, args[key]);
                }
            }
        }else {
            for (var i = 0; i < arguments.length; i++) {
                if (arguments[i] != undefined) {
                    var reg = new RegExp("({[" + i + "]})", "g");
                    result = result.replace(reg, arguments[i]);
                }
            }
        }
    }
    return result;
}