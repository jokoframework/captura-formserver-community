<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Chake</title>

    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Captura Mobile Forms">
    <meta name="author" content="Captura">

    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
    <link type="text/css" href="${rc.contextPath}/res/glyphicons/css/glyphicons.css" rel="stylesheet">
    <link type="text/css" href="${rc.contextPath}/res/css/outside.css" rel="stylesheet" />

    <style>
        .alert-error {
            color: #b94a48 !important;
            background-color: #f2dede !important;
            border-color: #eed3d7 !important;
        }
        .alert-error h4 {
            color: #b94a48  !important;
        }
    </style>


    <!-- Compatibilidad con HTML5 para IE -->
    <!--[if lt IE 9]>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/html5shiv/3.7.3/html5shiv.min.js"></script>
    <![endif]-->
</head>
<body>

<div class="container">
    <div class="mf_header text-center my-4">
        <img src="${rc.contextPath}/res/img/chake_logo.png" alt="Header Image" style="width: 50px; height: 70px;">
    </div>

    <div class="recoveryBox content">
        <p class="lead text-center">${i18n("web.account.activation.title")}</p>

        <form id="activationForm">
            <label class="control-label" for="captcha">${i18n('web.account.activation.message')}</label>
            <br/>
            <div class="form-group captchaGroup">
                <div class="d-flex justify-content-center">
                    <img src="${rc.contextPath}/api/public/captcha" id="captcha_img" class="img-fluid mr-2" />
                    <a id="refreshCaptchaButton" href="javascript:void(0)" class="glyphicons refresh align-self-center"><i></i></a>
                </div>
                <input id="captcha" type="text" class="form-control mt-2" />
            </div>
        </form>

        <div id="progressBlock" style="display:none;" class="text-center">
            <img src="${rc.contextPath}/res/img/login-ajax.gif" alt="Loading..."/>
        </div>

        <div class="recoveryBottom mt-3">
            <div id="activationMessage" class="mt-3" role="alert" style="display: none;"></div>
            <button type="button" id="acceptBtn" class="btn btn-success float-right">${i18n('web.account.activation.accept')}</button>
            <div style="clear:both"></div>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.bundle.min.js"></script>
<script type="text/javascript" data-main="${rc.contextPath}/acme/js/account-activation.js" src="${rc.contextPath}/acme/js/require-jquery.js"></script>
<script type="text/javascript" src="${rc.contextPath}/acme/js/bootstrap.js"></script>

<script type="text/javascript">
    document.getElementById("activationForm").onsubmit = function(e) {
        e.preventDefault();
    };
    document.getElementById("captcha").addEventListener("keydown", function(e) {
        if (e.key === "Enter") {
            e.preventDefault();
        }
    });
</script>

</body>
</html>