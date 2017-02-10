/*!
 * FastJson Plugin v1.0
 * https://github.com/sdragoncai/fastjson.js
 * Copyright 2015
 * 
 */
;
var FastJson = {
	isArray: function(a) {
		return typeof(a) == "object" && Object.prototype.toString.call(a).toLowerCase() == "[object array]"
	},
	isObject: function(a) {
		return typeof(a) == "object" && Object.prototype.toString.call(a).toLowerCase() == "[object object]"
	},
	format: function(v) {
		if (v == null) {
			return null
		}
		if ((typeof v) == "string") {
			v = eval("(" + v + ")")
		}
		return this._format(v, v, null, null, null)
	},
	_randomId: function() {
		return "randomId_" + parseInt(Math.random() * 1000000000)
	},
	_getJsonValue: function(a, b) {
		var f = this._randomId();
		var d = "";
		d += "function " + f + "($){";
		d += "return " + b + ";";
		d += "}";
		d += "";
		var e = document.createElement("script");
		e.id = f;
		e.text = d;
		document.body.appendChild(e);
		var c = window[f](a);
		e.parentNode.removeChild(e);
		return c
	},
	_format: function(g, i, b, j, a, e) {
		if (!b) {
			b = ""
		}
		if (this.isObject(i)) {
			if (i["$ref"]) {
				var c = i["$ref"];
				if (c.indexOf("$") == 0) {
					j[a] = this._getJsonValue(g, c)
				} else {
					if (c.indexOf("@") == 0) {
						j[a] = j
					} else {
						if (c.indexOf("..") == 0) {
							j[a] = e
						}
					}
				}
				return
			}
			for (var h in i) {
				var f = b;
				if (f != "") {
					f = f + "."
				}
				var c = i[h];
				f = f + h;
				this._format(g, c, f, i, h, j)
			}
		} else {
			if (this.isArray(i)) {
				for (var h in i) {
					var f = b;
					var c = i[h];
					f = f + "[" + h + "]";
					this._format(g, c, f, i, h, j)
				}
			} else {}
		}
		return g
	}
};