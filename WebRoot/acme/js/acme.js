define(

		[ "jquery", "require", "jquery-ui", "json2","history" ],
		function($, require, $ui, JSON,historyMod) {

		

			var pkg = {};

			// refs #313
			var contextPath = '/' + require.toUrl('dummy').split('/')[1];

			//Adds the function startsWith to the Strings
			if (typeof String.prototype.startsWith != 'function') {
				  String.prototype.startsWith = function (str){
				    return this.slice(0, str.length) == str;
				  };
			}
			
			Date.prototype.setISO8601 = function (timestamp) {
				 var match = timestamp.match(
				  "^([-+]?)(\\d{4,})(?:-?(\\d{2})(?:-?(\\d{2})" +
				  "(?:[Tt ](\\d{2})(?::?(\\d{2})(?::?(\\d{2})(?:\\.(\\d{1,3})(?:\\d+)?)?)?)?" +
				  "(?:[Zz]|(?:([-+])(\\d{2})(?::?(\\d{2}))?)?)?)?)?)?$");
				 if (match) {
				  for (var ints = [2, 3, 4, 5, 6, 7, 8, 10, 11], i = ints.length - 1; i >= 0; --i)
				   match[ints[i]] = (typeof match[ints[i]] != "undefined"
				    && match[ints[i]].length > 0) ? parseInt(match[ints[i]], 10) : 0;
				  if (match[1] == '-') // BC/AD
				   match[2] *= -1;
				  var ms = Date.UTC(
				   match[2], // Y
				   match[3] - 1, // M
				   match[4], // D
				   match[5], // h
				   match[6], // m
				   match[7], // s
				   match[8] // ms
				  );
				  if (typeof match[9] != "undefined" && match[9].length > 0) // offset
				   ms += (match[9] == '+' ? -1 : 1) *
				    (match[10]*3600*1000 + match[11]*60*1000); // oh om
				  if (match[2] >= 0 && match[2] <= 99) // 1-99 AD
				   ms -= 59958144000000;
				  this.setTime(ms);
				  return this;
				 }
				 else
				  return null;
			};
			//We check if this function doesn't exist because it is a standard provided by ecmascript 5. However, IE8 doesn't support it, so this adds the required backward compatibility
			if (!Date.prototype.toISOString) {
			    Date.prototype.toISOString = function() {
			        function pad(n) { return n < 10 ? '0' + n : n; };
			        return this.getUTCFullYear() + '-'
			            + pad(this.getUTCMonth() + 1) + '-'
			            + pad(this.getUTCDate()) + 'T'
			            + pad(this.getUTCHours()) + ':'
			            + pad(this.getUTCMinutes()) + ':'
			            + pad(this.getUTCSeconds()) + 'Z';
			    };
			}
			
			// Array Remove - By John Resig (MIT Licensed)
			Array.prototype.remove = function(from, to) {
			  var rest = this.slice((to || from) + 1 || this.length);
			  this.length = from < 0 ? this.length + from : from;
			  return this.push.apply(this, rest);
			};
			
			function Configurator() {
				// this is an object that has the following attributes
				// conf.appId =The id of the application where the user is
				// logged in (or null if any)
				// conf.defaulLauncher = The id of the launcher that should be
				// fired on startup (or null if any)
				var conf;

				var that = {};
				// load the configuraiton it hasn't been loaded before.
				// if force is true will reload the configuration
				that.load = function(force) {
					if (!conf || force) {
						var ajax = pkg.AJAX_FACTORY.newInstance();
						ajax.url += "/auth/loadAppConfiguration.ajax";
						ajax.success = function(response) {
							conf = response;
						};
						return $.ajax(ajax);
					}
					return conf;
				};
				
				that.GetConf = function() {
					return conf;
				};
				return that;
			}
			;

			function AuthorizationManager() {
				var SYS_OBJ = 0;
				var LEVEL_SYS = 0, LEVEL_APP = 1, LEVEL_PROJECT = 2, LEVEL_FORM = 3, LEVEL_POOL = 4;

				var that = {};
				// this is an array that stores a map with the authorization
				// granted on each level
				var auth;
				var hasAuthOnLevel = function(level, objId, authorizationName) {
					var authsGrantedOnObjSet=auth[level][objId];
					var i=0;
					if(authsGrantedOnObjSet){
						for(i=0;i<authsGrantedOnObjSet.length;i++){
							if(authsGrantedOnObjSet[i]===authorizationName){
								return true;
							}
						}
					}
					return false;
					
				};
				that.hasAuthorizationOnCurrentApp = function(authorizationName) {
					var appId = pkg.CONFIGURATOR.GetConf().appId;
					return that.hasAuthorizationOnApp(appId, authorizationName);
				};
				that.hasSystemAuth = function(authorizationName) {
					return hasAuthOnLevel(LEVEL_SYS, SYS_OBJ, authorizationName);
				};
				that.hasAuthorizationOnApp = function(appId, authorizationName) {
					return hasAuthOnLevel(LEVEL_APP, appId, authorizationName)
							|| hasAuthOnLevel(LEVEL_SYS, SYS_OBJ,
									authorizationName);
				};
				that.hasAuthorizationOnProject = function(projectId,
						authorizationName) {
					return hasAuthOnLevel(LEVEL_PROJECT, projectId, authorizationName)
					|| hasAuthOnLevel(LEVEL_SYS, SYS_OBJ,
							authorizationName);
				};
				that.hasAuthorizationOnForm = function(formId,
						authorizationName) {
					return hasAuthOnLevel(LEVEL_FORM, formId, authorizationName)
					|| hasAuthOnLevel(LEVEL_SYS, SYS_OBJ,
							authorizationName);
				};
				that.hasAuthorizationOnPool = function(poolId,
						authorizationName) {
					return hasAuthOnLevel(LEVEL_POOL, poolId, authorizationName)
					|| hasAuthOnLevel(LEVEL_SYS, SYS_OBJ,
							authorizationName);
				};
				var loadAuth = function(force) {
					if (!auth || force) {
						var ajax = pkg.AJAX_FACTORY.newInstance();
						ajax.url += "/auth/computedAuthorization.ajax";
						ajax.success = function(response) {
							auth = response.accessRights;
						};
						return $.ajax(ajax);
					}
					return auth;
				};
				// return a promise that can be used to wait for the
				// authorization to be loaded
				// The promise will be resolved once the "configuraiton"
				// (Configurator.load) and the authorization has been loaded
				that.load = function(force) {
					var dfd = $.Deferred();
					// makes sure that the configurations are loaded
					var configurationLoaded = pkg.CONFIGURATOR.load();
					// makes sures that the authorization are loaded
					var authLoaded = loadAuth(force);
					$.when(configurationLoaded, authLoaded).then(function() {
						dfd.resolve();
					});

					return dfd.promise();
				};
				that.setAuthorizations=function(computedAuthorization){
					auth=computedAuthorization.accessRights;
				};
				return that;
			};

			/**
			 * This class produces ajax object that can be used to call
			 * $.ajax(). The idea is to have a default behavior that can later
			 * be customized on each call
			 */
			function AjaxFactory() {
				var that = {};
				var defaultErrorHandler = function(jqXHR, textStatus,
						errorThrown) {
					// alert("Error processing " + this.url);
					pkg.LOG.error("Error processing " + this.url);
					pkg.LOG.error("textStatus = " + textStatus);
					pkg.LOG.error("errorThrown = " + errorThrown);

				};
				var defaultAjax = {
					type : "POST",
					url : contextPath,
					dataType : "json",
					error : defaultErrorHandler
				};

				that.setDefaultURL = function(url) {
					defaultAjax.url = url;
				};
				that.newInstance = function() {
					return $.extend({}, defaultAjax);
				};
				return that;
			};

			var pageWithErrors=false;
			var displayErrorPage=function(errorCode){
				pageWithErrors=true;
				var errorMsg='<div id="fatalErrorMsgContainer" ><p>There was an error. Please reload or contact the administrator.</p>'+
				'<p>Error code <strong>'+errorCode+'</strong></p>'+
				'<a id="reloadWorld"  >Reload</a></div>';
				$("body").html(errorMsg);
				$("#reloadWorld").click(function(){
					pkg.UTIL.reloadWorld();
				});
			};
			
			/**
			 * Manager the different launchers. Launchers can be associated to
			 * the menu or to the toolbox. There are two types of launchers:
			 * 1)actionView=true A launch of this type will load a new view and
			 * its associated toolbox. Views module and toolbox are cached, so
			 * it will only be downloaded the first time. The content provided
			 * by the urlView will always be downloaded. When the page is fully
			 * loaded (web,modules ) the method "start" will be executed. Note
			 * that the toolbox might not be ready. 2) actionJS=true A launch of
			 * this type will execute a JS function on the context of a given
			 * module
			 */
			function Launcher() {
				var contentDiv = "mf_content";
				var menuDiv = "mf_menu";
				var toolBoxDiv = "mf_toolbox";
				var navigatorDiv = "mf_navigator_content";
				
				// a hash of the form jsAMD->requireModule
				// the requireModule is the object returned by the loading of an
				// AMD
				var amdStrToModule = {};
				// this is a hash of the form
				// launchingId->launchingDef(AcmeMenuEntry)
				var laucherDefs = {};
				// a hash of the form "viewId"-> toolbox (AcmeMenuEntry)
				var viewToToolbox = {};

				var that = {};
				// a pointer to the last loaded module
				var lastViewModule = null;
				// a list of the menu options. Each element on this array is
				// potentially a tree
				var menuOptions;
				// a callback of the form function(toolbox). toolbox is the json
				// of AcmeMenuEntry
				// The root is not meaningful, it just groups all the possible
				// nodes
				var renderToolboxCallback;
				//
				var renderMenuCallback;
				var renderNavigatorCallback;
				// default page rendering just puts str as html of mf_content
				// div
				var renderPage = function(str) {
					$("#" + contentDiv).html(str);
				};
				var renderToolbox = function(toolbox) {
					if ($.isFunction(renderToolboxCallback)) {
						renderToolboxCallback(toolbox);
					}
				};
				var renderNavigator = function(navigatorList) {
					if ($.isFunction(renderNavigatorCallback)) {
						renderNavigatorCallback(navigatorList);
					}
				};
				// save the menu and the associated launching definition
				var saveMenu = function(menu) {
					var i = 0;
					menuOptions = menu;
					for (; i < menu.length; i++) {
						saveLaucherDefs(menu[i], true);
					}
				};
				// traverse recursivly the tree and builds the hash laucherDefs
				var saveLaucherDefs = function(node, isMenu) {
					var i = 0;
					var child;
					if (node.launcherId) {
						// if the node has the definition of a launcher, then we
						// need to save it
						if (!laucherDefs[node.launcherId]) {
							// The definition from the menu should prevail over
							// the toolbox.
							// This is useful to have only one path from a
							// launcher of type (actionView=true) to its root;
							// so if a launcher is invoked form a toolbox the
							// navigator will show the path from the menu, which
							// is what the user will expect
							laucherDefs[node.launcherId] = node;
						}
					}
					if (node.childrens) {
						// recursively look for launch definitions
						for (; i < node.childrens.length; i++) {
							child = node.childrens[i];
							if (isMenu) {
								// we are storing the definition of the menu,
								// therefore we will keep track of the hierarchy
								// to use this information in the navigator
								child.parent = node;
							}

							saveLaucherDefs(node.childrens[i], isMenu);
						}
					}

				};

				// The method can receive a full launcher definition
				// (AcmeMenuEntry) or a
				// launcher ID and it will return a valid launcher definition or
				// null (if it can't convert the parameter)
				var parseLauncherParameter = function(launcher) {
					if (!launcher) {
						pkg.LOG
								.error("Launcher.launch can't be invoked without parameters");
						return null;
					}
					if (typeof launcher == "number") {
						if (!laucherDefs[launcher]) {
							pkg.LOG.error("The launcher #" + launcher
									+ " is not defined");
							return;
						}
						return laucherDefs[launcher];
					} else {
						if (typeof launcher == "object") {
							if (launcher.actionView) {
								if (launcher.jsAMD && launcher.urlView) {
									return launcher;
								} else {
									pkg.LOG
											.error("A launcher of type actionView=true should define the fields jsAMD and urlView");
									return null;
								}
							} else if (launcher.actionJS) {
								if (launcher.jsActionCode) {
									return launcher;
								} else {
									pkg.LOG
											.error("A launcher of type actionView=true should define the field jsActionCode");
									return null;
								}
							} else {
								pkg.LOG
										.error("A launcher should be either of type actionJS=true or actionView=true");
								return null;
							}
						} else {
							pkg.LOG
									.error("Can't launch a module with parameter "
											+ launcher);
						}
					}
				};
				// METHODS FOR LOADING RESOURCES
				// 1) toolbox (loadToolbox)
				// 2) JS Modules (loadJSModule)
				// 3) View Page

				// loads the toolbox associated with a given view and return a
				// promise
				// when the promise is ready, the callback will receive the
				// toolbox
				var loadToolbox = function(viewId,launcherId) {
					var ajax, dfd, promiseToolboxLoaded;
					if (viewToToolbox[viewId]) {
						// if the toolbox is on the cache, just return it
						return viewToToolbox[viewId];
					} else {
						dfd = $.Deferred();
						promiseToolboxLoaded = dfd.promise();
						var ajax = pkg.AJAX_FACTORY.newInstance();
						ajax.url += '/sys/toolbox.ajax';
						ajax.data = {
							"viewId" : viewId
						};
						ajax.success = function(toolbox) {
							// store the toolbox on the cache
							if (toolbox) {
								saveLaucherDefs(toolbox);
							}
							dfd.resolve(toolbox);
						};
						$.ajax(ajax);
						return promiseToolboxLoaded;
					}
					;

				};

				// This method will first check if the JS module was already
				// loaded, if the method was loaded it will call the callback
				// immediately. Otherwise, it will load the module and call the
				// callback function when the module is ready. The callback
				// function should be of the form: function(subModule)
				var loadJSModule = function(module,launcherId) {
					var dfd, promiseModuleLoaded;
					var jsModule;
					if (!module) {
						return null;
					}
					if (!amdStrToModule[module]) {
						dfd = $.Deferred();
						promiseModuleLoaded = dfd.promise();
						jsModule = [];
						jsModule[0] =  module;
						// This module was not yet loaded, we should wait until
						// the JS are loaded
						// to fire the start
						require(jsModule, function(subModule) {
							// the JS was loaded
							amdStrToModule[module] = subModule;
							//store on the module the launcher id that was used to fire it
							subModule.launcherId=launcherId;
							// callback(subModule);
							dfd.resolve(subModule);
						});
						return promiseModuleLoaded;
					} else {
						return amdStrToModule[module];
					}
				};
				// This method will load the url of a given page (i.e.
				// menuItem.urlView).
				// The returned promise can be used listen when the page is
				// ready, the callback function will receive the full html
				var loadViewPage = function(urlView) {
					//var dfd = $.Deferred();
					//var pageLoadedPromise = dfd.promise();
					var ajax = pkg.AJAX_FACTORY.newInstance();
					ajax.url += urlView;
					ajax.type = "GET";
					ajax.dataType = "text";
					return $.ajax(ajax);
					
				};
				// Receive an AcmeMenuEntry json and will execute the
				// menuItem.jsActionCode on the context of the module
				// menuItem.jsAMD
				var launchAction = function(menuItem, data) {
					var dfd = $.Deferred();
					var promise = dfd.promise();
					var jsLoaded = loadJSModule(menuItem.jsAMD);
					$.when(jsLoaded).then(function(subModule) {

						if ($.isFunction(subModule[menuItem.jsActionCode])) {
							// check that the module has the function defined by
							// jsActionCode
							subModule[menuItem.jsActionCode](data);
						}
						dfd.resolve(subModule);
					});
					return promise;
				};

				// this method will hide or show the different section of a page
				// according to the view definition (AcmeMenuEntry)
				var adjustScreen = function(menuItem) {
					if (menuItem) {
						if (menuItem.showMenu) {
							$("#" + menuDiv).show();
						} else {
							$("#" + menuDiv).hide();
						}
						if (menuItem.showNavigator) {
							$("#" + navigatorDiv).show();
						} else {
							$("#" + navigatorDiv).hide();
						}
						if (menuItem.showToolbox) {
							$("#" + toolBoxDiv).show();
							// reduce the content area to make room for the
							// toolbox
							$("#page_width").removeClass("span12").addClass(
									"span9");
//							$("#" + contentDiv)
//									.removeClass("working-area-full");
							//$("#" + contentDiv).addClass("working-area");
						} else {
							$("#" + toolBoxDiv).hide();
							$("#page_width").removeClass("span9").addClass(
									"span12");
							//$("#" + contentDiv).removeClass("working-area");
							//$("#" + contentDiv).addClass("working-area-full");
						}
					}
				};

				var overlayProgress = null;
				
				var showProgress = function() {
					overlayProgress = OverlayDivManager.showOver();
				};
				
				var hideProgress = function() {					
					if(overlayProgress){
						overlayProgress.remove();
					}
				};
				
				// Receive an AcmeMenuEntry json, will download the associated
				// js module (i.e. menuItem.jsAMD) and the page (
				// menuItem.urlView).
				// Once the page and the JS modules were downloaded it will
				// execute the method "start" on the JS AMD (i.e.
				// menuItem.jsAMD)
				// The toolbox of the page will automatically be added based on
				// the toolbox definition of the view.
				// The page will always be downloaded, no matter if the user has
				// already visited it.
				// The JS module and the toolbox will be cached and downloaded
				// only the first time.
				// When the user is switching between pages, the old module will
				// receive a message "stop". The goal is to let the module
				// destroy data that might cause problem to the viewing of
				// another pages and to reclaim unused resources
				var launchView = function(menuItem, data) {
					var unloadPromise = null;
					if (lastViewModule) {
						var m = amdStrToModule[lastViewModule.jsAMD];
						// STEP 0) We call the preUnload function of the module
						// The module may override this return a true value
						// to avoid navigation. UNCLE BEN!, Great Power, Great
						// Responsibility!
						if (m && $.isFunction(m.preUnload)) {
						  	unloadPromise = m.preUnload();
						}
					}

					var dfd = $.Deferred();
					var promise = dfd.promise();
					$.when(unloadPromise).then(function(response){
						if(!response || response.unload){
							showProgress();
							renderNavigator(menuItem);
		
							if (lastViewModule) {
								var m = amdStrToModule[lastViewModule.jsAMD];
								// STEP 1) Stop the old page is necessary
								// If the user is switching to another page, then we
								// need to "stop" the previous page.
								if (m && $.isFunction(m.stop)) {
									// check if the module defined a "stop" method
									m.stop();
								}
							}
							// It is launching a new "view"
							// We will keep track of the last view in order to stop it
							// when the user decides to change the view (see STEP 1)
							lastViewModule = menuItem;
		
							// STEP 2) Load the new page and its module
							var pageLoadedPromise = loadViewPage(menuItem.urlView);
							var jsLoaded = loadJSModule(menuItem.jsAMD,menuItem.launcherId);
							// when the page and the js are ready, render the page and
							// call the "start" method
							var startEnd = function(mod){
								hideProgress();
								dfd.resolve(mod);
							};
							$.when(pageLoadedPromise, jsLoaded).then(
								function(htmlResponse, subModule) {
									var html=htmlResponse[0];
									// hide or show the section of the page
									// that are active for the current view
									adjustScreen(menuItem);
									renderPage(html);
									
									if (subModule && $.isFunction(subModule.start)) {
										var startPromise = subModule.start(data);
										
										if(startPromise){
											$.when(startPromise).then(function(){
												startEnd(subModule);
											});
										} else {
											startEnd(subModule);
										}
									} else {
										startEnd(subModule);
									}
								},function(jqXHR){
									hideProgress();
									/*var errorCode=jqXHR.getResponseHeader("mf_error");
									if(jqXHR.status)
									displayErrorPage(errorCode);*/
								});
							// STEP 3 Load the toolbox
							var toolboxLoaded = loadToolbox(menuItem.viewId);
							$.when(toolboxLoaded).then(renderToolbox);
						}
					});
					return promise;
				};

				// PUBLIC METHODS
				
				//return the last module if any
				that.currentModule=function(){
					if (lastViewModule) {
						return amdStrToModule[lastViewModule.jsAMD];
					}
				};
				
				// return the list of menu
				that.GetMenuOptions = function() {
					return menuOptions;
				};
				//
				that.SetRenderPageCallback = function(f) {
					renderPage = f;
				};
				that.SetRenderToolboxCallback = function(f) {
					renderToolboxCallback = f;
				};
				that.SetRenderMenuCallback = function(f) {
					renderMenuCallback = f;
				};
				that.SetRenderNavigatorCallback = function(f) {
					renderNavigatorCallback = f;
				};

				//Keep tracks of the user launched items in order to implement the compatibility with the back/forward buttons
				var navHistory=[];
				var hisPos=-1;
				//We use a random string as a prefix of the data that we want to identify within the URL
				var navHash="mfh_="+Math.random().toString(36).substring(7);
				$( window ).bind( 'popstate', function( event ) {

				    // receiving location from the window.history object
				    var loc = history.location || document.location;
				    var index=loc.pathname.indexOf(navHash);
				    if(index>0){
				    	var pos=loc.pathname.substr(index+navHash.length);
				    	pos=parseInt(pos,10);
				    	
				    	
					    if(pos>=0){
					    	hisPos=pos--;
					    	that.launchItem(navHistory[pos].menuItem,navHistory[pos].data);
					    }else{
					    	pkg.UTIL.reloadWorld();
					    }
					    
				    }else{
				    	pkg.UTIL.reloadWorld();
				    }
				    
				});
				
				
				/**
				 * This method can receive a full launcher definition (i.e.
				 * AcmeMenuEntry) or the AcmeMenuEntry.launcherId The method
				 * will return a promise that can be used to track if the module
				 * has been loaded, the first object of the callback will be the
				 * loded module. For example: var
				 * p=acme.LAUNCHER.launch(launchId);
				 * $.when(p).then(function(module){ //module is loaded });
				 * 
				 * @param launcher
				 * @param data:
				 *            if present this parameter will be passed to
				 *            'start' function
				 */

				that.launch = function(launcher, data) {
					var menuItem = parseLauncherParameter(launcher);
					//if the user is taking a new path, then we need to remove the old "forward" chain
					navHistory=navHistory.slice(0,hisPos+1);
					navHistory[navHistory.length]={'menuItem':menuItem,'data':data};
					hisPos=navHistory.length;
					history.pushState( null, null,navHash+hisPos );
					if (!menuItem) {
						// do nothing to avoid unexpected errors. The method
						// parseLauncherParameter should have log error info in
						// case !menuItem
						return null;
					}
					return that.launchItem(menuItem, data);
				};

				/**
				 * Launches a page described by the item
				 * 
				 * An item to launch a view would be an object like this item = {
				 * actionView : true, showMenu : true, showNavigator : false,
				 * urlView : '/view/url.mob', jsAMD : '/module/to/load' } The
				 * content of the url and the module are loaded. The module's
				 * start function is executed.
				 * 
				 * An item to launch an action would be item = { actionJS :
				 * true, showMenu : true, showNavigator : false, jsActionCode :
				 * 'functionToExecute', jsAMD : '/module/to/load' } In this
				 * second case the function named like jsActionCode (if any)
				 * from the loaded module is executed.
				 * 
				 * @param item
				 * @param data:
				 *            if present this parameter will be passed to
				 *            'start' function
				 * @returns
				 */
				that.launchItem = function(item, data) {
					var promise;
					if (item.actionView) {
						promise = launchView(item, data);
					} else {
						if (item.actionJS) {
							promise = launchAction(item, data);
						} else {
							pkg.LOG
									.error("Unknown item type. The item "
											+ menuItem.id
											+ " is neither of type actionView nor actionJS");
							throw "Unknown launching type";
						}
					}
					return promise;
				};

				/**
				 * Load the main and return a promise to track when the menu is
				 * ready. When the promise is ready the method GetMenuOptions
				 * will return the menu. The renderMenuCallback will be called
				 * when the menu is ready
				 * 
				 * @returns
				 */
				that.loadMenu = function() {
					var dfd = $.Deferred();
					var menuListLoaded = dfd.promise();
					var ajax = pkg.AJAX_FACTORY.newInstance();
					ajax.url += "/sys/menu.ajax";
					ajax.success = function(obj) {
						// a list of AcmeMenuEntry, each entry is a tree on
						// itself
						saveMenu(obj);
						if (renderMenuCallback
								&& $.isFunction(renderMenuCallback)) {
							renderMenuCallback(obj);
						} else {
							pkg.LOG.error("There is no menu callback registered (acme.LAUNCHER.SetRenderMenuCallback)");
						}

						dfd.resolve();
					};
					$.ajax(ajax);
					return menuListLoaded;
				};

				return that;
			}
			;

			function I18nManager() {
				var that = {};
				var languageToMap = {};
				var currentLanguage = null;

				var fill = function(language, keyMap) {
					var map = languageToMap[language] = languageToMap[language]
							|| {};
					for (key in keyMap) {
						map[key] = keyMap[key];
					}
					;
					currentLanguage = language;
				};

				that.changeLanguage = function(language) {
					currentLanguage = language;
				};

				that.getMessage = function(key, objValues) {
					var keyToMsg = languageToMap[currentLanguage];
					var i, msg = key;
					if (keyToMsg[key]) {
						// need to replace the {x} values
						msg = keyToMsg[key];
						if (objValues) {
							for (i = 0; i < objValues.length; i++) {
								var regex = new RegExp("\\{" + i + "\\}", "g");
								msg = msg.replace(regex, objValues[i]);
							}
						}

					}
					return msg;

				};
				// This method loads a set of i18n message in the given language
				// if language is null, then the default language will be used
				// The method return a promise that can be used to monitor the
				// status of the i18n loading
				that.load = function(keyList, language) {
					if (!language) {
						language = currentLanguage;
					}
					var dfd = $.Deferred();
					var i18nKeysLoaded = dfd.promise();

					var i18nRequest = {
						"language" : language,
						"keys" : keyList
					};
					var i18nRequestJSON = JSON.stringify(i18nRequest);
					var ajax = pkg.AJAX_FACTORY.newInstance();
					ajax.url += "/i18n/keys.ajax";
					ajax.contentType = 'application/json; charset=utf-8';
					ajax.data = i18nRequestJSON;
					ajax.success = function(result) {
						fill(result.language, result.values);
						dfd.resolve();
					};
					$.ajax(ajax);
					return i18nKeysLoaded;
				};

				that.currentLanguage = function() {
					return currentLanguage;
				};

				return that;
			}
			;

			function Logger() {
				var that = {};
				var console = window['console'];
				if (console && console.log) {
					// If the log console is available use it
					console.log("Using Console log message");
				} else {
					// otherwise redirect messages to the traditional alert
					console = {};
					console.log = function(msg) {
						// alert(msg);
					};
				}

				that.error = function(msg) {
					console.log("Error: " + msg);
				};
				that.info = function(msg) {
					console.log("Info: " + msg);
				};
				that.warn = function(msg) {
					console.log("Warn: " + msg);
				};
				that.debug = function(msg) {
					console.log("Debug: " + msg);
				};
				return that;
			}
			;

			function ActionManager() {

				var that = {};

				that.execute = function(params) {
					var dfd = $.Deferred();
					var promise = dfd.promise();
					var ajaxRequest = pkg.AJAX_FACTORY.newInstance();
					ajaxRequest.url += '/actions/execute.ajax';
					ajaxRequest.data = params;
					ajaxRequest.contentType = 'application/json; charset=utf-8';
					ajaxRequest.data = JSON.stringify(data);
					ajaxRequest.success = function(response) {
						if (response.success) {

						} else {

						}
					};
					ajaxRequest.error = function() {
						alert('ERROR in action request');
					};
					$.ajax(ajaxRequest);
				};

				return that;
			}
			;
			
			function Util() {
				var that = {};
				
				//Beware not to send this text straightfoward to the HTML. We shall use "text" function of jquery that takes care of escaping again
				that.decodeHTML = function(str){
					if(str){
						var div = $('<div></div>');
						var unescaped = div.html(str).text();
						// div.remove(); is a remove necessary?
						return unescaped;
					}
					return "";
				};
				
				that.clone=function(o){
			        function F(){};
			        F.prototype = o;
			        return new F();
			    };
			    
			    that.reloadWorld = function() {
			    	//window.location.reload();
					var pathname = window.location.pathname;
					// Doesn't work on firefox, only in chrome and safari
					// var origin = window.location.origin;
					
					var origin = window.location.protocol + "//" + window.location.host;		
					var pathnameParts = pathname.split("/");
					var appName = "";
					if (pathnameParts.length > 1) {
						appName = pathnameParts[1];
						window.location.href = origin + "/" + appName;
					} else {
						window.location.href = origin;
					}
			    };
			    
				return that;
			};

			pkg.AJAX_FACTORY = AjaxFactory();
			pkg.ACTION_MANAGER = ActionManager();
			pkg.LAUNCHER = Launcher();
			pkg.I18N_MANAGER = I18nManager();
			pkg.CONFIGURATOR = Configurator();
			pkg.AUTHORIZATION_MANAGER = AuthorizationManager();
			pkg.LOG = Logger();
			pkg.UTIL = Util(); 
			pkg.data = {};
			pkg.VARS = {};
			pkg.VARS.contextPath = contextPath;

			$(window).bind('resize', function () {
				var activeModule=pkg.LAUNCHER.currentModule();
				if(activeModule&&$.isFunction(activeModule.resized)){
					activeModule.resized();
				}
			});
			// Shows a progress image when executing an ajax call
			/*
			 * $('#mf_content_progress').bind('ajaxStart', function(){
			 * $(this).show(); }).bind('ajaxStop', function(){ $(this).hide();
			 * });
			 */

			// FIXME this is a async = false request! - jmpr
			// Get server variables
			(function() {
				var ajaxRequest = pkg.AJAX_FACTORY.newInstance();
				ajaxRequest.async = false;
				ajaxRequest.url += "/acme/vars.ajax";
				ajaxRequest.success = function(response) {
					if (response.success) {
						var vars = response.obj;
						for (prop in vars) {
							if (vars.hasOwnProperty(prop)) {
								pkg.VARS[prop] = vars[prop];
							}
						}
						pkg.I18N_MANAGER.changeLanguage(vars.language);
					} else {
						pkg.LOG.error('Ajax Error');
					}
				};
				$.ajax(ajaxRequest);
			})();

			var i18nPromise = pkg.I18N_MANAGER.load([
					'web.session.invalid.title', 'web.session.invalid.message',
					'web.timeout.dialog.title', 'web.timeout.dialog.message',
					'web.generic.ok', 'web.ajax.error', 'web.ajax.status',
					'web.ajax.forbidden', 'web.ajax.unreachable',
					'web.ajax.unavailable', 'web.content.invalid',
					'web.content.invalid.title','web.generic.logout' ]);
			// Invalid/Timeout Session Manger
			var SessionManger = (function() {
				var that = {};

				var lastPointer = false;

				var invalidSessionDialogShown = false;
				
				var shouldRun=false;
				
				var stopLastTimer=function(){
					//stop the timer if any
					if (lastPointer) {
						clearTimeout(lastPointer);
					}
				};
				that.stop=function(){
					shouldRun=false;
					stopLastTimer();
				};
				that.start=function(){
					shouldRun=true;
					restart();
				};
				var showTimeout = function() {
					that.invalidSessionDialog('web.timeout.dialog.title',
							'web.timeout.dialog.message');
				};
				
				var restart = function() {
					stopLastTimer();
					if(shouldRun){
						var timeout = Number(pkg.VARS.timeout);
						if (timeout) {
							lastPointer = setTimeout(showTimeout, pkg.VARS.timeout);
						}
					}
				};

				$('body').bind('ajaxSend', function() {
					restart();
				});

				that.invalidSessionDialog = function(titleKey, messageKey) {
					if (lastPointer) {
						clearTimeout(lastPointer);
					}

					if (!invalidSessionDialogShown) {
						invalidSessionDialogShown = true;
						$.when(i18nPromise).then(
								function() {
									var title = pkg.I18N_MANAGER
											.getMessage(titleKey);
									var message = pkg.I18N_MANAGER
											.getMessage(messageKey);
									var ok = pkg.I18N_MANAGER
											.getMessage('web.generic.ok');
									// var dialog =
									$('<div></div>').html(message).dialog({
										title : title,
										resizable : false,
										autoOpen : true,
										width : 400,
										height : 180,
										modal : true,
										buttons : [ {
											text : ok,
											click : function() {
												// window.location.reload();
												pkg.UTIL.reloadWorld();
											}
										} ],
										close : function() {
											// window.location.reload();
											pkg.UTIL.reloadWorld();
										}
									});
								});
					}
				};

				

				return that;
			})();
			
			var isJqueryUIAvailable = function(){
				var jqueryUICSSPresent = false;
				var ss = document.styleSheets;
				for (var i = 0, max = ss.length; i < max; i++) {
			        if (ss[i].href && ss[i].href.indexOf(contextPath + "/res/css/jquery-ui/smoothness/jquery-ui-1.8.18.custom.css") != -1){
			        	jqueryUICSSPresent = true;
			        	break;
			        }
			    }
				return jqueryUICSSPresent;
			};
			

			// refs #343
			$('body')
					.bind(
							'ajaxError',
							function(event, jqXHR, ajaxSettings, thrownError) {
								var I18N = pkg.I18N_MANAGER.getMessage;
								if (jqXHR.status === 401) {
									if(!pageWithErrors){
										//during an unexpected error the session will be invalidated and this message will appear if we don't use this hack.
										SessionManger.invalidSessionDialog(
											'web.session.invalid.title',
											'web.session.invalid.message');
									}
								} else if (jqXHR.status === 499) {
									var title = pkg.I18N_MANAGER
											.getMessage('web.content.invalid.title');
									var message = pkg.I18N_MANAGER
											.getMessage('web.content.invalid');
									var ok = pkg.I18N_MANAGER
											.getMessage('web.generic.ok');
									$('<div></div>').html(message).dialog({
										title : title,
										resizable : false,
										autoOpen : true,
										width : 400,
										height : 180,
										modal : true,
										buttons : [ {
											text : ok,
											click : function() {
												$(this).dialog('close');
											}
										} ]
									});
								} else if (jqXHR.status === 403) {
									alert('ACME error: ' + I18N('web.ajax.forbidden'));
								} else if (jqXHR.status === 0) {
									// there's no connection to the server or the request was interrupted
								    var jqueryUIAvailable = isJqueryUIAvailable();
									var message = I18N('web.ajax.unreachable');
									if(jqueryUIAvailable){
										var length = $('div#connectionErrorDialog').length;
										if(!length){
											var html = '<div style="text-align:center; margin-top:30px; font-size:18px"><a href="javascript:void(0)" class="glyphicons warning_sign">' + message + '</a> <a id="dummyLink" href="#">.</a></div>';
											var connectionDialog = $('<div></div>', {"id":'connectionErrorDialog'}).html(html).dialog({
												resizable : false,
												autoOpen : true,
												width : 300,
												height : 120,
												modal : true,
												dialogClass: 'noTitleStuff', // this is the css class, so that the dialog doesn't have a title
												open: function(){
													// this is a hack so that the warning message doesn't grab the focus
												 	$('#dummyLink').focus();
												 	$('#dummyLink').remove();
												}
											});
										}
										
										// the dialog is dismissed after 5 seconds
										setTimeout(function(){
											$(connectionDialog).dialog('close');
											$('div#connectionErrorDialog').remove();
										} , 5000);
									} else {
										// if jquery ui is not available we show a good ol' alert
										alert('ACME error: ' + message);
									}
									
								} else if (jqXHR.status === 503) {
									alert('ACME error: '+I18N('web.ajax.unavailable')+'');
								} else {
									var errorCode=jqXHR.getResponseHeader("mf_error");
									displayErrorPage(errorCode);
								}
							});

			// ------------------------------------
			//If there is an object that contains new authorization it will be automatically handle by this callback
			//This can happen after creating a new MFObject, such as a Form or Project.
			$(document).ajaxSend(function(event, jqxhr, settings) {
				jqxhr.done(function(obj){
					if(obj&&obj.computedAuthorizations){
						pkg.LOG.debug('Received new authorizations');
						pkg.AUTHORIZATION_MANAGER.setAuthorizations(obj.computedAuthorizations);
					}
				});
			});
			
			// ------------------------------------
			
			$.fn.serializeObject = function() {
				var o = {};
				var a = this.serializeArray();
				$.each(a, function() {
					if (o[this.name] !== undefined) {
						if (!o[this.name].push) {
							o[this.name] = [ o[this.name] ];
						}
						o[this.name].push(this.value || '');
					} else {
						o[this.name] = this.value || '';
					}
				});
				return o;
			};
			// --------------------------------------

			var OverlayDivManager = (function(){
				var that = {};
				
				that.showOver = function(sectionId){
					var position = {top: 0, left :0};
					var width = '100%';
					var height = '100%';
					var zIndex = 99999;
					
					var overlayDiv = $('<div></div>', {"class" : 'ui-widget-overlay'});
					var loadingImgDiv = $('<div></div>');
					if(sectionId){
						var section = $('#' + sectionId);
						if(section.is('div')){ 
							position = section.position();
							width = section.outerWidth(true);
							height = section.outerHeight(true);		
							
							overlayDiv.insertAfter(section);
							loadingImgDiv.insertAfter(section);
						} else {
							throw "Invalid ajaxLoading section #" + options.loadingSectionId;
						}
					} else {
						// if we are going to cover the whole window it means, 
						// the content of the whole window will change, so lets scroll to the top
						$(window).scrollTop(0);
						overlayDiv.appendTo('body');
						loadingImgDiv.appendTo('body');
						$('body').css("overflow", "hidden");

					}
					
					overlayDiv.css('top', position.top);
					overlayDiv.css('left', position.left);
					overlayDiv.css('width', width);
					overlayDiv.css('height', height);
					overlayDiv.css('z-index', zIndex);
					
					loadingImgDiv.css('position', 'absolute');
					loadingImgDiv.css('top', position.top);
					loadingImgDiv.css('left', position.left);
					loadingImgDiv.css('width', width);
					loadingImgDiv.css('height', height);
					loadingImgDiv.css('z-index', zIndex + 1);
					loadingImgDiv.css('background', 'url(' + contextPath + '/res/img/ajax-loader.gif) no-repeat center center');
					
					var ret = {"overlayDiv" : overlayDiv, "loadingImgDiv" : loadingImgDiv,
						"remove" : function(){
							overlayDiv.remove();
							loadingImgDiv.remove();
							if(!sectionId){
								$("body").css("overflow", "auto");
							}
						 }
					};
					
					return ret;
				};
				
				return that;
			})();
			
			$.ajaxPrefilter(function(options){
				if(options.loadingSectionId){
					var overlayProgress = null;
					if(!options.beforeSend && !options.complete){
						options.beforeSend = function(){
							overlayProgress = OverlayDivManager.showOver(options.loadingSectionId);
						};
						
						options.complete = function(){
							if(overlayProgress){
								overlayProgress.remove();
							}
						};
					}
				}
			});
			
			$.valHooks.input ={
				get: function(elem) {
					return elem.value;
				},
				set: function(elem, value) {
					elem.value = pkg.UTIL.decodeHTML(value);
					return true;
				}	
			};
			
			
		var messageManager=function(messageDivId){
			var messageDiv = $('#'+messageDivId);
			var that={};
			
			 var clear=function(){
				messageDiv.removeClass("alert-error");
				messageDiv.removeClass("alert-success");
				messageDiv.removeClass("alert-info");
				messageDiv.hide();
			};
			that.clear=clear;
			that.hide=function(){
				messageDiv.hide();
			};
			that.success=function(msg){
				clear();
				messageDiv.addClass("alert-success");
				messageDiv.html(msg);
				messageDiv.show();
			};
			that.error=function(msg){
				clear();
				messageDiv.addClass("alert-error");
				messageDiv.html(msg);
				messageDiv.show();
			};
			that.info=function(msg){
				clear();
				messageDiv.addClass("alert-info");
				messageDiv.html(msg);
				messageDiv.show();
			};
			messageDiv.addClass("alert");
			clear();
			return that;
		};
	 pkg.deactivateButtons=function(){
		var i=0;
		for(i=0;i<arguments.length;i++){
			$("#"+arguments[i]).addClass('disabled');
			$("#"+arguments[i]).attr('disabled', 'disabled');
		};
	 };
	 pkg.activateButtons=function(){
		 var i=0;
			for(i=0;i<arguments.length;i++){
				$("#"+arguments[i]).removeClass('disabled');
				$("#"+arguments[i]).removeAttr('disabled');
			};
	 };
	 
	 pkg.showErrorOnFields = function (){
		 var args = Array.prototype.slice.call(arguments);
		 var i;
		 for(i=0;i<args.length;i++){
			 $("#"+args[i]).parent().parent().addClass("error");
		 }
		 	
		};
	pkg.messageManager=messageManager;
	pkg.SessionManger=SessionManger;
	return pkg;
			
});
