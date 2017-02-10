var ds_mt = {
		
}
ds_mt.getDataRule = function(configParam){
	var dataRule = configParam.required ? "required;" : "" ;
	switch(configParam.bizType){
		case "String" : break;
		case "NonNegativeInteger" : {
			dataRule += "integer;"
			if(configParam.realName == "port"){
				dataRule += "range[0~65535]" ;
			}
			break ;
		}
		case "IP" : {
			dataRule += "ipv4;"
		}
	}
	if(dataRule == ""){
		return null ;
	}
	return configParam.alias + ":" + dataRule ;
}