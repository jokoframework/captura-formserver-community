define([ "jquery" ,"acme"], function($,acme) {

	var pkg = {};

	// Returns an object usable by a select. E.g. { "key" : "label", "key2",
	// "label2" }
	var basicSelectElementsAsOptionMap = function(aMap) {
		var options = {};
		$.each(aMap, function(key, value) {
			options[key] = value.label;
		});
		return options;
	};

	// Returns an array usable by a select. E.g. [{ "key" : "label"} , {"key2",
	// "label2" }]
	var basicSelectElementsAsOptionArray = function(aMap) {
		var options = [];
		$.each(aMap, function(key, value) {
			var newMap = {};
			newMap[key] = acme.UTIL.decodeHTML(value.label);
			options.push(newMap);
		});
		return options;
	};

	// Manages Pool items
	pkg.PoolManager = function(aPoolMap) {
		var that = {};
		var poolMap = null;

		var initialize = function() {
			poolMap = aPoolMap;
		};

		var getOptions = function() {
			return basicSelectElementsAsOptionArray(poolMap);
		};

		// Force initialization
		initialize();
		// Public methods
		that.getOptions = getOptions;
		return that;
	};

	// Maintains all process item information
	pkg.TypeManager = function(aTypeMap) {
		var that = {};
		var typeMap = null;

		// Gets the type array parameter and converts it to a map for easy
		// access
		var initialize = function() {
			typeMap = aTypeMap;
		};

		var getOptions = function() {
			return basicSelectElementsAsOptionArray(typeMap);
		};
		
		var getTypeElementsByKey = function(key) {
			return typeMap[key].elements;
		};

		// Force initialization
		initialize();
		// Public methods
		that.getOptions = getOptions;
		that.getTypeElementsByKey = getTypeElementsByKey;
		return that;
	};
	
	return pkg;

});