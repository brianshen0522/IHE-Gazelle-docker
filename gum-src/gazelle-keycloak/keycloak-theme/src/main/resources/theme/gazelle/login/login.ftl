<#import "template.ftl" as layout>
<#import "passkeys.ftl" as passkeys>
<@layout.registrationLayout displayMessage=!messagesPerField.existsError('username','password') displayInfo=realm.password && realm.registrationAllowed && !registrationDisabled??; section>
    <#if section = "header">
        ${msg("loginAccountTitle")}
    <#elseif section = "form">
        <div id="kc-form">
            <div id="kc-form-wrapper">
                <#if realm.password>
                    <form id="kc-form-login" onsubmit="login.disabled = true; return true;" action="${url.loginAction}" method="post">
                        <#if !usernameHidden??>
                            <div class="${properties.kcFormGroupClass!}">
                                <label for="username" class="${properties.kcLabelClass!}"><#if !realm.loginWithEmailAllowed>${msg("username")}<#elseif !realm.registrationEmailAsUsername>${msg("email")}<#else>${msg("email")}</#if></label>

                                <input tabindex="2" id="username" class="${properties.kcInputClass!}" name="username" value="${(login.username!'')}"  type="text"
                                       autofocus autocomplete="${(enableWebAuthnConditionalUI?has_content)?then('username webauthn', 'username')}"
                                       aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                                       dir="ltr"
                                />

                                <#if messagesPerField.existsError('username','password')>
                                    <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                        ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                                </span>
                                </#if>

                            </div>
                        </#if>

                        <div class="${properties.kcFormGroupClass!}">
                            <label for="password" class="${properties.kcLabelClass!}">${msg("password")}</label>

                            <div class="${properties.kcInputGroup!}" dir="ltr">
                                <input tabindex="3" id="password" class="${properties.kcInputClass!}" name="password" type="password" autocomplete="current-password"
                                       aria-invalid="<#if messagesPerField.existsError('username','password')>true</#if>"
                                />
                                <button class="show-pwd-btn" type="button" aria-label="${msg("showPassword")}"
                                        aria-controls="password" data-password-toggle tabindex="4"
                                        data-icon-show="${properties.kcFormPasswordVisibilityIconShow!}" data-icon-hide="${properties.kcFormPasswordVisibilityIconHide!}"
                                        data-label-show="${msg('showPassword')}" data-label-hide="${msg('hidePassword')}">
                                    <i class="${properties.kcFormPasswordVisibilityIconShow!}" aria-hidden="true"></i>
                                </button>
                            </div>

                            <#if usernameHidden?? && messagesPerField.existsError('username','password')>
                                <span id="input-error" class="${properties.kcInputErrorMessageClass!}" aria-live="polite">
                                    ${kcSanitize(messagesPerField.getFirstError('username','password'))?no_esc}
                            </span>
                            </#if>

                        </div>

                        <div class="${properties.kcFormGroupClass!} ${properties.kcFormSettingClass!}">
                            <div id="kc-form-options">
                                <#if realm.rememberMe && !usernameHidden??>
                                    <div class="checkbox">
                                        <label>
                                            <#if login.rememberMe??>
                                                <input tabindex="5" id="rememberMe" name="rememberMe" type="checkbox" checked> ${msg("rememberMe")}
                                            <#else>
                                                <input tabindex="5" id="rememberMe" name="rememberMe" type="checkbox"> ${msg("rememberMe")}
                                            </#if>
                                        </label>
                                    </div>
                                </#if>
                            </div>
                            <div class="kc-form-link">
                                <#if realm.resetPasswordAllowed>
                                    <span><a tabindex="6" href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a></span>
                                </#if>
                            </div>
                        </div>

                        <div id="kc-form-buttons" class="${properties.kcFormGroupClass!}">
                            <input type="hidden" id="id-hidden-input" name="credentialId" <#if auth.selectedCredential?has_content>value="${auth.selectedCredential}"</#if>/>
                            <input tabindex="7" class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonBlockClass!} ${properties.kcButtonLargeClass!}" name="login" id="kc-login" type="submit" value="${msg("doLogIn")}"/>
                        </div>
                    </form>
                </#if>
            </div>
        </div>
        <@passkeys.conditionalUIData />
        <script type="module" src="${url.resourcesPath}/js/passwordVisibility.js"></script>
    <#elseif section = "info" >
        <#if realm.password && realm.registrationAllowed && !registrationDisabled??>
            <div id="kc-registration-container">
                <div id="kc-registration">
                    <#if properties.GZLRegistration == "true">
                        <span class="kc-registration-footer-txt">${msg("noAccount")}
                            <a tabindex="6" href="${properties.registrationUrl}">${msg("register")}</a>
                        </span>
                    <#else>
                        <!-- Registration disabled -->
                    </#if>
                </div>
            </div>
        </#if>
    <#elseif section = "socialProviders" >
        <#if realm.password && social.providers??>
            <div id="kc-social-providers" class="social-providers-ctr">
                <ul class="social-providers-ctr">
                <#assign idpLogoUrls = properties.idpLogoUrls?split(",")>
                <#assign idpContent = properties.idpContent?split(",")>
                    <#list social.providers as p>
                    <#assign logoUrl = idpLogoUrls[p?index]>
                    <#assign idpMsg = idpContent[p?index]>
                        <li>
                            <a id="social-${p.alias}" class="social-provider-button <#if social.providers?size gt 3>${properties.kcFormSocialAccountGridItem!}</#if>" type="button" href="${p.loginUrl}">
                                <#if logoUrl?has_content>
                                    <div class="social-btn-content">
                                     <#--  Display the message if available  -->
                                        <#if idpMsg?has_content>
                                            <p class="idp-content">${idpMsg}</p>
                                        </#if>
                                        <div id="idpLogo" class="logo-ctr"><img src="${logoUrl}" /></div>

                                    </div>
                                <#else>
                                    <span class="${properties.kcFormSocialAccountNameClass!} button-label">${msg("loginWith")}${p.displayName!}</span>
                                </#if>
                            </a>
                        </li>
                    </#list>
                </ul>
            </div>
            <div id="divider" class="divider-ctr">
                <hr class="divider" />
                <h4>${msg("or")}</h4>
                <hr class="divider" />
            </div>
        </#if>
    <#elseif section = "gazelleLogin" >
        <#if realm.password>
            <div id="kc-gazelle-login">
                <button id="gazelle-login" class="social-provider-button" type="button" onclick="showForm()">
                    <div id="logo1"><#include "./resources/img/gz_logo.svg"/></div>
                </button>
            </div>
            <div class="prev-ctr"><a href="" id="prevLink">${msg("previous")}</a></div>
        </#if>
    </#if>

</@layout.registrationLayout>

<#-- JS script -->
<script>
    // initial rendering
    if (document.getElementById('kc-social-providers') &&
        document.getElementById('kc-social-providers').children[0].children.length === 0) {
        document.getElementById('kc-gazelle-login').style.display = 'none';
        document.getElementById('prevLink').style.display = 'none';
        document.getElementById('divider').style.display = 'none';
        document.getElementById('kc-form').style.display = 'block';
    } else if (document.getElementById('input-error') || document.getElementsByClassName('alert-error').length !== 0) {
        document.getElementById('kc-social-providers').style.display = 'none';
        document.getElementById('divider').style.display = 'none';
        document.getElementById('kc-gazelle-login').style.display = 'none';
        document.getElementById('prevLink').style.display = 'block';
        document.getElementById('kc-form').style.display = 'block';
    } else {
        document.getElementById('kc-social-providers').style.display = 'grid';
        document.getElementById('kc-gazelle-login').style.display = 'flex';
        document.getElementById('kc-registration').style.display = 'none';
        document.getElementById('kc-form').style.display = 'none';
        document.getElementById('prevLink').style.display = 'none';
        document.getElementById('logo1-ctr').style.display = 'none';
    }

    // select Gazelle login
    function showForm() {
        document.getElementById('kc-form').style.display = 'block';
        document.getElementById('prevLink').style.display = 'block';
        document.getElementById('kc-registration').style.display = 'block';
        document.getElementById('kc-social-providers').style.display = 'none';
        document.getElementById('divider').style.display = 'none';
        document.getElementById('kc-gazelle-login').style.display = 'none';
        document.getElementById('logo1-ctr').style.display = 'flex';
    }

    // click on previous link
    document.getElementById('prevLink').addEventListener('click', function(event) {
        event.preventDefault();
        document.getElementById('kc-form').style.display = 'none';
        document.getElementById('prevLink').style.display = 'none';
        document.getElementById('kc-registration').style.display = 'none';
        document.getElementById('kc-social-providers').style.display = 'grid';
        document.getElementById('logo1-ctr').style.display = 'none';
        document.getElementById('divider').style.display = 'flex';
        document.getElementById('kc-gazelle-login').style.display = 'flex';
        document.getElementsByClassName('alert-error').length !== 0 ? document.getElementsByClassName('alert-error')[0].style.display = 'none' : '';
    });

    // adjust social providers grid
    document.addEventListener('DOMContentLoaded', function() {
        var numItems = document.querySelectorAll('#kc-social-providers .social-providers-ctr li').length;
        if (numItems > 1) {
            document.querySelector('#kc-social-providers .social-providers-ctr').style.columnGap = '1rem';
        }
    });
</script>
