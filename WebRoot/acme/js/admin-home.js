require(["jquery", "acme", "jquery-ui", "acme-ui", "constants", "root-functionalities", "sodep-multiselect/sodep-multiselect",
    "notify/ui.notify"], function ($, acme, $ui, acme_ui, constants, root) {

    var i18nPromise = acme.I18N_MANAGER.load(["web.admin.home.enterApp", "web.admin.home.activation",
        "web.admin.home.usage_statistics", "web.admin.home.activation.confirmation",
        "web.admin.home.activation.desconfirmation",
        "web.root.appOptions.tooltip"]);

    var I18N = acme.I18N_MANAGER.getMessage;

    var launchApp = function (appId) {
        window.location.href = acme.VARS.contextPath + "/admin/app.mob?appId=" + appId;
    };

    var activateDesactivateApp = function (item) {
        var appId = item.id;
        var isActive = item.active;
        var msg;
        var data = {
            appId: appId
        };
        var ajaxRequestForName = acme.AJAX_FACTORY.newInstance();
        ajaxRequestForName.url += "/admin/appStats.ajax";
        ajaxRequestForName.data = data;
        ajaxRequestForName.success = function (response) {
            var applicationName = response.applicationName;
            var isActive = response.applicationActive;
            if (!isActive) {
                msg = I18N("web.admin.home.activation.confirmation") + " " + applicationName + " ?";
            } else {
                msg = I18N("web.admin.home.activation.desconfirmation") + " " + applicationName + " ?";
            }
            acme_ui.confirmation(I18N('web.admin.home.activation'), msg, function () {
                var data = {
                    applicationIdentifier: appId
                };

                var ajaxRequest = acme.AJAX_FACTORY.newInstance();
                ajaxRequest.url += "/application/settings/activate.ajax";
                ajaxRequest.data = data;
                ajaxRequest.success = function (response) {
                    acme_ui.HTML_BUILDER.notifySuccess(response.title, response.message);
                    setTimeout(function () {
                        $("#applicationMultiselect").sodepMultiselect("reload");
                    }, 1000);
                    getAppsSummary();
                };
                ajaxRequest.error = function (jqXHR, textStatus, errorThrown) {
                    acme_ui.HTML_BUILDER.notifyError(I18N("web.generic.error"), I18N("web.generic.unexpectedException"));
                };
                $.ajax(ajaxRequest);
            });
        };
        ajaxRequestForName.error = function (jqXHR, textStatus, errorThrown) {
            acme_ui.HTML_BUILDER.notifyError(I18N("web.generic.error"), I18N("web.generic.unexpectedException"));
        };
        $.ajax(ajaxRequestForName);
    };

    var formatBytes = function(bytes,decimals) {
    	   if(bytes == 0) return '0 Bytes';
    	   var k = 1000;
    	   var dm = decimals + 1 || 3;
    	   var sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    	   var i = Math.floor(Math.log(bytes) / Math.log(k));
    	   return (bytes / Math.pow(k, i)).toPrecision(dm) + ' ' + sizes[i];
    }
    
    var showAppStats = function (id) {
        var data = {
            appId: id
        };
        var ajaxRequest = acme.AJAX_FACTORY.newInstance();
        ajaxRequest.url += "/admin/appStats.ajax";
        ajaxRequest.data = data;
        ajaxRequest.success = function (response) {

            $("#appStats_AppName").html(response.applicationName);
            $("#appStats_projectCount").html(response.projectCount);
            $("#appStats_formCount").html(response.formCount);
            $("#appStats_userCount").html(response.userCount);

            $("#appStats_loginsTable").html("");
            var loginInfo = response.loginInfo;
            if (loginInfo && loginInfo.length) {
                $("#appStats_loginsTable").append("<thead><tr><th>User</th><th>Time</th><th>Login Type</th></tr>");
                for (i = 0; i < loginInfo.length; i++) {
                    var login = loginInfo[i];
                    $("#appStats_loginsTable").append("<tr><td>" + login.mail + "</td><td>" + login.time + "</td><td>" + login.loginType + "</td>");
                }
            } else {
                $("#appStats_loginsTable").html("<tr><td>NO LOGIN INFO</td></tr>")
            }

            $("#appStats_documentCount").html("");
            var documentCount = response.documentCount;
            if (documentCount && documentCount.length) {
                $("#appStats_documentCount").append("<thead><tr><th>Days</th><th>Document count</th><th>Size count</th></tr></thead>")
                for (i = 0; i < documentCount.length; i++) {
                    var c = documentCount[i];
                    var sizeCount = formatBytes(c.byteCount);
                    $("#appStats_documentCount").append("<tr><td>" + c.days + "</td><td>" + c.documentCount + "</td><td>" + sizeCount + "</td></tr>");
                }
            } else {
                $("#appStats_documentCount").html("<tr><td>NO DOCUMENT UPLOAD INFO</td></tr>")
            }

            $("#appStats").show();
        };
        ajaxRequest.error = function (jqXHR, textStatus, errorThrown) {
            acme_ui.HTML_BUILDER.notifyError(I18N("web.generic.error"), I18N("web.generic.unexpectedException"));
        };
        $.ajax(ajaxRequest);
    };

    var getAppsSummary = function() {
    	var ajaxRequest = acme.AJAX_FACTORY.newInstance();
        ajaxRequest.url += "/admin/getAppsSummary.ajax";
        ajaxRequest.success = function (response) {
        	if (response.success) {
        		$("#totalApps").text(response.content.totalApps);
        		$("#activeApps").text(response.content.activeApps);
        		$("#inactiveApps").text(response.content.inactiveApps);
        	}
        };
        $.ajax(ajaxRequest);
    };
    
    function showMoreOptions (appId) {
        var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		$("#appMoreOptionsPopup").dialog("open");
        ajaxRequest.loadingSelectionId = 'appMoreOptionsPopup';
        ajaxRequest.url += '/application/admin/settings/get.ajax';
        ajaxRequest.type = 'GET';
		ajaxRequest.data={ 'appId' : appId};
		ajaxRequest.success = function(res){
			if(res.success) {
			   $("#appMoreOptionsPopup #app_id").val(res.obj.id);
               $("#appMoreOptionsPopup #has_workflow").attr('checked', res.obj.hasWorkflow);
               $("#appMoreOptionsPopup #settings_save").removeAttr('disabled');

            } else {
				acme_ui.HTML_BUILDER.notifyError(res.title,res.message);
			}
		};
		$.ajax(ajaxRequest);
    }

    function saveAppOptions(appId) {
        var hasWorkflow = $("#has_workflow").is(":checked");
        var appId = $('#app_id').val();
		var ajaxRequest = acme.AJAX_FACTORY.newInstance();
		ajaxRequest.url += '/application/admin/settings/save.ajax';
        ajaxRequest.data={ 'appId' : appId, 'hasWorkflow' : hasWorkflow};
		ajaxRequest.success = function(obj){
			if(obj.success) {
			    acme_ui.HTML_BUILDER.notifySuccess(obj.title,obj.message);
			    $("#appMoreOptionsPopup").dialog("close");
			} else {
				acme_ui.HTML_BUILDER.notifyError(obj.title,obj.message);
			}
			
		};
		$.ajax(ajaxRequest);
    }

    $(function () {

        root.init();

        $("#notificationContainer").notify();


        $('#myAccount').click(function () {
            acme.LAUNCHER.launch({
                urlView: '/settings/my-account.mob',
                jsAMD: acme.VARS.contextPath + '/acme/js/home/my-account/my-account.js',
                actionView: true
            });
        });

        $.when(i18nPromise).then(function () {
            $("#applicationMultiselect").sodepMultiselect({
                id: "applications", single: true, checkboxes: false,
                click: [
                    {
                        tooltip: I18N("web.root.appOptions.tooltip"),
                        icon: "settings",
                        func: function (item, selectArray) {
                            showMoreOptions(item.id);
                        }
                    },
                    {
                        tooltip: I18N("web.admin.home.activation"),
                        icon: "power",
                        func: function (item, selectArray) {
                            activateDesactivateApp(item);
                        }
                    },
                    {
                        tooltip: I18N("web.admin.home.enterApp"),
                        icon: "file_import",
                        func: function (item, selectArray) {
                            launchApp(item.id);
                        }
                    },
                    {
                        tooltip: I18N("web.admin.home.usage_statistics"),
                        icon: "stats",
                        func: function (item, selectArray) {
                            showAppStats(item.id);
                        }
                    }


                ],
                showInactive: true
            });
        });

        $("#toolbox_createApp").click(function () {
            root.openCreateAppPopup(
                function () {
                    $("#applicationMultiselect").sodepMultiselect("reload");
                    getAppsSummary();
                }
            );
        });

        $("#toolbox_serverInfo").click(function () {
            root.openServerInfoPopup();
        });

        $('#toolbox_pendingRegistrations').click(function () {
            acme.LAUNCHER.launch({
                urlView: '/admin/pendingRegistrations.mob',
                jsAMD: acme.VARS.contextPath + '/acme/js/admin/pending-registrations-amd.js',
                actionView: true
            });
        });

        $("#menu_unexpectedErrors").click(function () {
            acme.LAUNCHER.launch({
                urlView: '/admin/uncaughtException.mob',
                jsAMD: acme.VARS.contextPath + '/acme/js/admin/uncaughtException-amd.js',
                actionView: true
            });
        });
        $("#menu_systemParameters").click(function () {
            acme.LAUNCHER.launch({
                urlView: '/admin/systemParameters.mob',
                jsAMD: acme.VARS.contextPath + '/acme/js/admin/systemParameters-amd.js',
                actionView: true
            });
        });
        $("#menu_home").click(function () {
            acme.UTIL.reloadWorld();
        });
        $("#toolbox_reloadParameters").click(function () {
            acme_ui.confirmation("Reload parameters", "Are you sure you want to reload the system parameters ?", function () {
                var reloadPromise = root.reloadSystemParameters();
                $.when(reloadPromise).then(function (obj) {
                    if (obj.success) {
                        acme_ui.HTML_BUILDER.notifySuccess(obj.title, obj.message);
                    } else {
                        acme_ui.HTML_BUILDER.notifyError(obj.title, obj.message);
                    }
                });

            });
        });
        $("#toolbox_reloadi18n").click(function () {
            acme_ui.confirmation("Reload i18n", "Are you sure you want to reload the i18n values ?", function () {
                var reloadPromise = root.reloadi18n();
                $.when(reloadPromise).then(function (obj) {
                    if (obj.success) {
                        acme.UTIL.reloadWorld();
                    } else {
                        acme_ui.HTML_BUILDER.notifyError(obj.title, obj.message);
                    }
                });

            });
        });
        $("#toolbox_dataUsage").click(function () {
            acme.LAUNCHER.launch({
                urlView: '/admin/dataUsage.mob',
                jsAMD: acme.VARS.contextPath + '/acme/js/admin/dataUsage-amd.js',
                actionView: true
            });
        });
        $("#toolbox_failedDocuments").click(function () {
            acme.LAUNCHER.launch({
                urlView: '/admin/failedDocuments.mob',
                jsAMD: acme.VARS.contextPath + '/acme/js/admin/failedDocuments-amd.js',
                actionView: true
            });
        });
        
        $('#onlyActiveApps').click(function(){
        	$("#applicationMultiselect").sodepMultiselect("option", "requestParams", {active: $(this).is(':checked')});
        	$("#applicationMultiselect").sodepMultiselect("reload");
        });

        $('#appMoreOptionsPopup #settings_save').click(function(){
            saveAppOptions();
        });

        
        getAppsSummary();
    });
});